/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.09.2013 by mbechler
 */
package eu.agno3.runtime.messaging.transaction.internal;


import java.io.EOFException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.apache.activemq.ActiveMQMessageConsumer;
import org.apache.activemq.MessageAvailableListener;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import com.atomikos.jms.AtomikosJMSException;

import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.listener.MessageListener;
import eu.agno3.runtime.messaging.msg.MessageProperties;
import eu.agno3.runtime.messaging.transaction.TransactedMessageListenerContainer;


/**
 * @author mbechler
 * 
 */
public class TransactedMessageListenerContainerImpl
        implements TransactedMessageListenerContainer, UncaughtExceptionHandler, MessageAvailableListener {

    private static final Logger log = Logger.getLogger(TransactedMessageListenerContainerImpl.class);

    private static final String ATOMIKOS_PROXY_CLASS = "com.atomikos.jms.AtomikosJmsMessageConsumerProxy"; //$NON-NLS-1$
    private static final String ATOMIKOS_DELEGEATE_METHOD = "getDelegate"; //$NON-NLS-1$

    private Session session;
    private MessageConsumer consumer;
    private MessageListener listener;

    private TransactionManager tm;

    private boolean started = false;

    private final Object connectionLock = new Object();

    private RedeliveryPolicy redeliveryPolicy;

    private boolean exclusiveSession;


    /**
     * @param listener
     * @param tm
     * @param pol
     */
    public TransactedMessageListenerContainerImpl ( MessageListener listener, TransactionManager tm, RedeliveryPolicy pol ) {
        super();
        this.listener = listener;
        this.tm = tm;
        this.redeliveryPolicy = pol;
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    /**
     * @return the started
     */
    public boolean isStarted () {
        return this.started;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
     */
    @Override
    public void uncaughtException ( Thread thread, Throwable t ) {
        log.error("Uncaught exception in TransactedMessageListener thread:", t); //$NON-NLS-1$
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.MessageListenerContainer#start(javax.jms.Session)
     */
    @Override
    public void start ( Session s ) throws JMSException, MessagingException {
        start(s, false);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.MessageListenerContainer#start(javax.jms.Session, boolean)
     */
    @Override
    public void start ( Session sess, boolean exclusive ) throws JMSException, MessagingException {
        synchronized ( this.connectionLock ) {
            this.exclusiveSession = exclusive;
            try {
                Destination destination = this.listener.getDestination(this.listener, sess);
                this.consumer = sess.createConsumer(destination);

                if ( log.isDebugEnabled() ) {
                    log.debug("Listening on " + destination); //$NON-NLS-1$
                }

                ActiveMQMessageConsumer c = unwrapConsumer(this.consumer);
                if ( this.redeliveryPolicy != null ) {
                    c.setRedeliveryPolicy(this.redeliveryPolicy);
                }
                else {
                    this.redeliveryPolicy = c.getRedeliveryPolicy();
                }

                c.setAvailableListener(this);
            }
            catch ( Exception e ) {
                stop();
                throw e;
            }

            this.session = sess;
            this.started = true;
        }
    }


    /**
     * @param c
     * @return
     * @throws MessagingException
     */
    private static ActiveMQMessageConsumer unwrapConsumer ( MessageConsumer c ) throws MessagingException {
        MessageConsumer mc = c;
        if ( ATOMIKOS_PROXY_CLASS.equals(c.getClass().getName()) ) {

            try {
                Method method = c.getClass().getDeclaredMethod(ATOMIKOS_DELEGEATE_METHOD);
                method.setAccessible(true);
                mc = (MessageConsumer) method.invoke(c);
            }
            catch (
                NoSuchMethodException |
                SecurityException |
                IllegalAccessException |
                IllegalArgumentException |
                InvocationTargetException e ) {
                throw new MessagingException("Failed to get atomikos delegate", e); //$NON-NLS-1$
            }

        }

        if ( mc instanceof ActiveMQMessageConsumer ) {
            return (ActiveMQMessageConsumer) mc;
        }

        throw new MessagingException("Invalid message consumer type"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.transaction.TransactedMessageListenerContainer#stop()
     */
    @Override
    public void stop () throws JMSException {
        synchronized ( this.connectionLock ) {
            log.debug("Stopping listener"); //$NON-NLS-1$
            this.started = false;
            try {
                if ( this.consumer != null ) {
                    this.consumer.close();
                }
                this.listener = null;
            }
            finally {
                if ( this.exclusiveSession && this.session != null ) {
                    this.session.close();
                }
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.activemq.MessageAvailableListener#onMessageAvailable(javax.jms.MessageConsumer)
     */
    @Override
    public void onMessageAvailable ( MessageConsumer c ) {
        boolean done = false;
        boolean redelivery = false;
        int maxRedeliveries = this.redeliveryPolicy.getMaximumRedeliveries();
        long redeliveryTimeout = this.redeliveryPolicy.getInitialRedeliveryDelay();
        Object oldTTL = MDC.get(MessageProperties.TTL);
        MDC.remove(MessageProperties.TTL);

        Session sess = getSession(c);
        // we need the transactional consumer here
        MessageConsumer cons = this.consumer;
        try {
            while ( this.started && !done ) {
                boolean commit = false;
                boolean exit = false;
                MDC.put(MessageProperties.TTL, 1);

                synchronized ( this.connectionLock ) {
                    if ( !this.started ) {
                        return;
                    }
                    try {
                        this.tm.begin();

                        if ( log.isTraceEnabled() ) {
                            log.trace(String.format(
                                "Transaction is %s (thread: %d, listener: %s)", //$NON-NLS-1$
                                this.tm.getTransaction(),
                                Thread.currentThread().getId(),
                                this.listener.getClass().getName()));
                        }

                        if ( !redelivery ) {
                            maxRedeliveries = this.redeliveryPolicy.getMaximumRedeliveries();
                            redeliveryTimeout = this.redeliveryPolicy.getInitialRedeliveryDelay();
                            Message m = doReadNoWait(cons);
                            if ( m == null ) {
                                log.debug("No message"); //$NON-NLS-1$
                                done = true;
                                commit = true;
                                return;
                            }

                            Object ttlProp = m.getObjectProperty(MessageProperties.TTL);
                            if ( ttlProp instanceof Integer ) {
                                MDC.remove(MessageProperties.TTL);
                                MDC.put(MessageProperties.TTL, (int) ttlProp - 1);
                            }
                            commit = doDeliver(m, sess);
                        }
                        else {
                            log.warn(String.format("Waiting %d ms for redelivery (remain attempts %d)", redeliveryTimeout, maxRedeliveries)); //$NON-NLS-1$
                            redelivery = false;
                            maxRedeliveries--;
                            commit = doRead(redeliveryTimeout, sess, cons);
                        }

                    }
                    catch ( JMSException e ) {
                        if ( TransactionUtil.isInterrupted(e) ) {
                            log.debug("Interrupted transaction", e); //$NON-NLS-1$
                            return;
                        }
                        getLog().warn("JMS Exception in message listener", e); //$NON-NLS-1$
                    }
                    catch ( Exception e ) {
                        getLog().warn("Exception in message listener:", e); //$NON-NLS-1$
                    }
                    finally {
                        try {
                            TransactionUtil.endTransaction(this.tm, commit);
                            if ( !commit && maxRedeliveries > 0 ) {
                                if ( redelivery ) {
                                    redeliveryTimeout = this.redeliveryPolicy.getNextRedeliveryDelay(redeliveryTimeout);
                                }
                                redelivery = true;
                            }
                        }
                        catch ( SystemException t ) {
                            getLog().error("Unrecoverable error while ending transaction, exiting listener:", t); //$NON-NLS-1$
                            exit = true;
                        }
                    }
                }

                if ( exit ) {
                    return;
                }
            }

            log.debug("Finished onMessageAvailable"); //$NON-NLS-1$
        }
        finally {
            if ( oldTTL != null ) {
                MDC.remove(MessageProperties.TTL);
                MDC.put(MessageProperties.TTL, oldTTL);
            }
        }
    }


    private Session getSession ( MessageConsumer c ) {
        return this.session;
    }


    /**
     * @param recvTimeout
     * @return whether a message was delivered
     * @throws JMSException
     * @throws AtomikosJMSException
     */
    boolean doRead ( long recvTimeout, Session sess, MessageConsumer cons ) throws JMSException, AtomikosJMSException {
        if ( !this.started ) {
            return true;
        }

        Message msg = null;
        try {
            msg = cons.receive(recvTimeout);
        }
        catch ( AtomikosJMSException e ) {
            if ( e.getCause() instanceof IllegalStateException
                    || ( e.getCause() instanceof JMSException && e.getCause().getCause() instanceof EOFException ) ) {
                log.debug("Connection closed (EOF)", e); //$NON-NLS-1$
                return false;
            }

            throw e;
        }

        return doDeliver(msg, sess);
    }


    private Message doReadNoWait ( MessageConsumer cons ) throws JMSException {
        if ( !this.started ) {
            return null;
        }

        Message msg = null;
        try {
            msg = cons.receiveNoWait();
        }
        catch ( AtomikosJMSException e ) {
            if ( e.getCause() instanceof IllegalStateException
                    || ( e.getCause() instanceof JMSException && e.getCause().getCause() instanceof EOFException ) ) {
                log.debug("Connection closed (EOF)", e); //$NON-NLS-1$
                return null;
            }

            throw e;
        }

        return msg;
    }


    private boolean doDeliver ( Message msg, Session sess ) {
        try {
            if ( msg != null ) {
                log.trace("Delivering message to listener " + msg); //$NON-NLS-1$
                MessageListener l = this.listener;
                if ( l != null ) {
                    l.onMessage(msg, sess);
                    return true;
                }

                log.debug("Listener already closed"); //$NON-NLS-1$
                return false;
            }
        }
        catch ( Exception e ) {
            log.warn("Exception in message listener, rollback transaction", e); //$NON-NLS-1$
            return false;
        }

        return true;
    }

}
