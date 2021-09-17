/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.09.2013 by mbechler
 */
package eu.agno3.runtime.messaging.transaction.internal;


import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.activemq.ConnectionFailedException;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.listener.BaseListener;
import eu.agno3.runtime.messaging.listener.DestinationStrategy;
import eu.agno3.runtime.messaging.listener.MessageListener;
import eu.agno3.runtime.messaging.listener.MessageListenerContainer;
import eu.agno3.runtime.messaging.listener.MessageListenerFactory;
import eu.agno3.runtime.messaging.listener.RedeliveryPolicyProvider;
import eu.agno3.runtime.messaging.transaction.TransactedMessageListenerContainer;
import eu.agno3.runtime.transaction.TransactionService;


/**
 * @author mbechler
 * 
 */
@Component ( service = MessageListenerFactory.class )
public class TransactedMessageListenerFactoryImpl implements MessageListenerFactory {

    private static final Logger log = Logger.getLogger(TransactedMessageListenerFactoryImpl.class);

    private TransactionService transactionService;

    private Set<WeakReference<TransactedMessageListenerContainer>> handedOut = new HashSet<>();

    private ConnectionFactory connectionFactory;
    private Connection conn;


    @Activate
    protected synchronized void activate ( ComponentContext context ) {
        try {
            this.conn = this.connectionFactory.createConnection();
            this.conn.start();
        }
        catch ( JMSException e ) {
            log.error("Failed to create connection", e); //$NON-NLS-1$
            this.conn = null;
            return;
        }
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext context ) {
        for ( WeakReference<TransactedMessageListenerContainer> ref : this.handedOut ) {
            TransactedMessageListenerContainer container = ref.get();
            if ( container != null ) {
                try {
                    container.stop();
                }
                catch ( JMSException e ) {
                    log.warn("Failed to stop message listener container:", e); //$NON-NLS-1$
                }
            }
        }
        this.handedOut.clear();
        try {
            if ( this.conn != null ) {
                this.conn.stop();
            }
        }
        catch ( ConnectionFailedException e ) {
            log.debug("Failed to stop connection, conn refused", e); //$NON-NLS-1$
        }
        catch ( JMSException e ) {
            log.error("Failed to stop connection", e); //$NON-NLS-1$
        }
    }


    @Reference
    protected synchronized void setTransactionService ( TransactionService ts ) {
        this.transactionService = ts;
    }


    protected synchronized void unsetTransactionService ( TransactionService ts ) {
        if ( this.transactionService == ts ) {
            this.transactionService = null;
        }
    }


    @Reference
    protected synchronized void bindConnectionFactory ( ConnectionFactory cf ) {
        this.connectionFactory = cf;
    }


    protected synchronized void unbindConnectionFactory ( ConnectionFactory cf ) {
        if ( this.connectionFactory == cf ) {
            this.connectionFactory = null;
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.MessageListenerFactory#createMessageListener(eu.agno3.runtime.messaging.listener.MessageListener)
     */
    @Override
    public synchronized TransactedMessageListenerContainer createMessageListener ( MessageListener listener ) {

        Set<WeakReference<TransactedMessageListenerContainer>> toRemove = new HashSet<>();
        for ( WeakReference<TransactedMessageListenerContainer> ref : this.handedOut ) {
            if ( ref.get() == null ) {
                toRemove.add(ref);
            }
        }
        this.handedOut.removeAll(toRemove);
        TransactedMessageListenerContainerImpl tml = new TransactedMessageListenerContainerImpl(
            listener,
            this.transactionService.getTransactionManager(),
            getRedeliveryPolicy(listener));

        this.handedOut.add(new WeakReference<TransactedMessageListenerContainer>(tml));
        return tml;
    }


    /**
     * @param listener
     * @return
     */
    protected RedeliveryPolicy getRedeliveryPolicy ( MessageListener listener ) {

        if ( listener instanceof RedeliveryPolicyProvider ) {
            RedeliveryPolicy pol = ( (RedeliveryPolicyProvider) listener ).getRedeliveryPolicy();
            if ( pol != null ) {
                return pol;
            }
        }

        RedeliveryPolicy pol = new RedeliveryPolicy();
        pol.setUseExponentialBackOff(true);
        pol.setInitialRedeliveryDelay(500);
        pol.setMaximumRedeliveries(2);
        pol.setBackOffMultiplier(4.0);
        return pol;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.MessageListenerFactory#createDestination(eu.agno3.runtime.messaging.listener.DestinationStrategy,
     *      eu.agno3.runtime.messaging.listener.BaseListener)
     */
    @Override
    public synchronized Destination createDestination ( DestinationStrategy dest, BaseListener listener ) throws JMSException {
        if ( this.conn == null ) {
            throw new JMSException("Connection failed"); //$NON-NLS-1$
        }
        Session s = this.conn.createSession(true, Session.SESSION_TRANSACTED);
        try {
            return dest.getDestination(listener, s);
        }
        finally {
            s.close();
        }

    }


    /**
     * {@inheritDoc}
     * 
     * @throws MessagingException
     * @throws JMSException
     *
     * @see eu.agno3.runtime.messaging.listener.MessageListenerFactory#startListener(eu.agno3.runtime.messaging.listener.MessageListenerContainer)
     */
    @Override
    public synchronized void startListener ( MessageListenerContainer messageListener ) throws MessagingException, JMSException {
        if ( ! ( messageListener instanceof TransactedMessageListenerContainer ) ) {
            throw new MessagingException("Invalid listener"); //$NON-NLS-1$
        }
        TransactedMessageListenerContainer tml = (TransactedMessageListenerContainer) messageListener;
        if ( this.conn == null ) {
            throw new JMSException("Connection failed"); //$NON-NLS-1$
        }
        Session s = this.conn.createSession(true, Session.SESSION_TRANSACTED);
        tml.start(s, true);
    }


    @Override
    public void startListener ( MessageListenerContainer messageListener, Session session ) throws MessagingException, JMSException {
        if ( ! ( messageListener instanceof TransactedMessageListenerContainer ) ) {
            throw new MessagingException("Invalid listener"); //$NON-NLS-1$
        }
        TransactedMessageListenerContainer tml = (TransactedMessageListenerContainer) messageListener;
        tml.start(session, false);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws JMSException
     *
     * @see eu.agno3.runtime.messaging.listener.MessageListenerFactory#remove(eu.agno3.runtime.messaging.listener.MessageListenerContainer)
     */
    @Override
    public void remove ( MessageListenerContainer service ) throws JMSException {
        service.stop();
        Set<WeakReference<TransactedMessageListenerContainer>> toRemove = new HashSet<>();
        for ( WeakReference<TransactedMessageListenerContainer> ref : this.handedOut ) {
            if ( ref.get() == null ) {
                toRemove.add(ref);
            }
            else if ( ref.get() == service ) {
                toRemove.add(ref);
            }
        }
        this.handedOut.removeAll(toRemove);
    }

}
