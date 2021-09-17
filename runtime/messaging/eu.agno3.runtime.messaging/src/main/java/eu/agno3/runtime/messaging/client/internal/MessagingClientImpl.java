/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.client.internal;


import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.UUID;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ConnectionFailedException;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.runtime.messaging.CallErrorException;
import eu.agno3.runtime.messaging.MessageTimeoutException;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.SharedConnectionFactory;
import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.client.MessagingClient;
import eu.agno3.runtime.messaging.marshalling.MarshallerManager;
import eu.agno3.runtime.messaging.marshalling.MarshallingException;
import eu.agno3.runtime.messaging.marshalling.MessageMarshaller;
import eu.agno3.runtime.messaging.marshalling.MessageUnmarshaller;
import eu.agno3.runtime.messaging.marshalling.UnmarshallerManager;
import eu.agno3.runtime.messaging.msg.ErrorResponseMessage;
import eu.agno3.runtime.messaging.msg.EventMessage;
import eu.agno3.runtime.messaging.msg.MessageProperties;
import eu.agno3.runtime.messaging.msg.RequestMessage;
import eu.agno3.runtime.messaging.msg.ResponseMessage;
import eu.agno3.runtime.messaging.msg.ResponseStatus;
import eu.agno3.runtime.messaging.routing.DestinationResolverManager;
import eu.agno3.runtime.messaging.routing.EventRouter;
import eu.agno3.runtime.messaging.routing.EventRouterManager;
import eu.agno3.runtime.messaging.routing.MessageDestinationResolver;
import eu.agno3.runtime.messaging.routing.MessageRoutingException;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public class MessagingClientImpl <T extends MessageSource> implements MessagingClient<T> {

    private static final Logger log = Logger.getLogger(MessagingClientImpl.class);

    private EventRouterManager eventRouterManager;
    private DestinationResolverManager destinationResolverManager;
    private MarshallerManager marshallerManager;
    private UnmarshallerManager unmarshallerManager;

    private volatile Session session;
    private volatile TemporaryQueue replyQueue;

    @NonNull
    private final T messageSource;
    private final Object sessionLock = new Object();

    private ConnectionFactory cf;
    private Connection conn;

    private MessageProducer replyProducer;


    /**
     * @param source
     * @param cf
     * @param eventRouterManager
     * @param destinationResolverManager
     * @param marshallerManager
     * @param unmarshallerManager
     */
    public MessagingClientImpl ( @NonNull T source, ConnectionFactory cf, EventRouterManager eventRouterManager,
            DestinationResolverManager destinationResolverManager, MarshallerManager marshallerManager, UnmarshallerManager unmarshallerManager ) {
        super();
        this.messageSource = source;
        this.cf = cf;
        this.eventRouterManager = eventRouterManager;
        this.destinationResolverManager = destinationResolverManager;
        this.marshallerManager = marshallerManager;
        this.unmarshallerManager = unmarshallerManager;
    }


    @Override
    public void open () throws MessagingException {
        synchronized ( this.sessionLock ) {
            this.session = createLocalSession(true, Session.SESSION_TRANSACTED, false);
        }
    }


    protected TemporaryQueue getReplyQueue ( Session s ) throws JMSException {
        if ( this.replyQueue == null ) {
            try {
                this.replyQueue = this.session.createTemporaryQueue();
            }
            catch ( JMSException e ) {
                throw e;
            }
        }
        if ( s == this.session ) {
            return this.replyQueue;
        }
        return s.createTemporaryQueue();
    }


    /**
     * @throws MessagingException
     * @throws JMSException
     * 
     */
    protected void reopenSession () throws MessagingException, JMSException {
        synchronized ( this.sessionLock ) {
            log.info("Reopening session after failure"); //$NON-NLS-1$
            Session newSession = this.createLocalSession(true, Session.SESSION_TRANSACTED, false);
            TemporaryQueue oldReplyQueue = this.replyQueue;
            Session oldSession = this.session;
            MessageProducer oldProducer = this.replyProducer;
            this.replyQueue = newSession.createTemporaryQueue();
            this.session = newSession;
            this.replyProducer = null;
            closeSession(oldSession, oldReplyQueue, oldProducer);
        }
    }


    /**
     * @return the connection
     * @throws JMSException
     */
    public synchronized Connection getConnection () throws JMSException {
        if ( this.conn == null ) {
            this.conn = this.cf.createConnection();
            if ( ! ( this.cf instanceof SharedConnectionFactory ) ) {
                this.conn.start();
            }
        }
        return this.conn;
    }


    /**
     * @return
     */
    protected synchronized boolean isConnectionOpen () {
        if ( this.conn == null ) {
            return false;
        }

        Connection c = this.conn;
        if ( Proxy.isProxyClass(c.getClass()) ) {
            InvocationHandler inv = Proxy.getInvocationHandler(c);
            if ( "com.atomikos.jms.AtomikosJmsConnectionProxy".equals(inv.getClass().getName()) ) { //$NON-NLS-1$
                try {
                    Field f = inv.getClass().getDeclaredField("delegate"); //$NON-NLS-1$
                    f.setAccessible(true);
                    c = (Connection) f.get(inv);
                }
                catch ( Exception e ) {
                    log.warn("Failed to get delegate connection", e); //$NON-NLS-1$
                }
            }
        }

        if ( c instanceof ActiveMQConnection ) {
            if ( ( (ActiveMQConnection) c ).isClosing() || ( (ActiveMQConnection) c ).isClosed() ) {
                return false;
            }
        }
        else {
            log.info(c.getClass().getName());
        }

        return true;
    }


    @Override
    public void close () throws MessagingException {
        synchronized ( this.sessionLock ) {

            try {
                this.eventRouterManager.close();
            }
            catch ( JMSException e ) {
                log.warn("Failed to close event routers", e); //$NON-NLS-1$
            }

            closeSession(this.session, this.replyQueue, this.replyProducer);

            try {
                if ( this.conn != null ) {
                    if ( ! ( this.cf instanceof SharedConnectionFactory ) ) {
                        this.conn.stop();
                    }
                    this.conn = null;
                }
            }
            catch ( JMSException e ) {
                log.debug("Failed to close connection"); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param oldProducer
     * @throws MessagingException
     */
    void closeSession ( Session s, TemporaryQueue rq, MessageProducer oldProducer ) throws MessagingException {
        try {

            try {
                if ( oldProducer != null ) {
                    oldProducer.close();
                }
            }
            catch ( JMSException e ) {
                log.warn("Failed to close producer:", e); //$NON-NLS-1$
            }

            try {

                if ( s != null ) {
                    s.close();
                }
            }
            catch ( JMSException e ) {
                log.warn("Failed to close session:", e); //$NON-NLS-1$
                throw new MessagingException(e);
            }
            finally {
                if ( rq != null ) {
                    rq.delete();
                }
            }
        }
        catch ( JMSException e ) {
            log.debug("Failed to remove temporary queue:", e); //$NON-NLS-1$
        }
    }


    /**
     * @return the session
     * @throws MessagingException
     */
    @Override
    public Session getSession () throws MessagingException {
        return this.session;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.client.MessagingClient#getSession(boolean, int)
     */
    @Override
    public Session getSession ( boolean transacted, int ackMode ) throws MessagingException {
        return this.session;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws InterruptedException
     * 
     * @see eu.agno3.runtime.messaging.client.MessagingClient#publishEvent(eu.agno3.runtime.messaging.msg.EventMessage)
     */
    @Override
    public void publishEvent ( @NonNull EventMessage<@NonNull ?> evt ) throws MessagingException, InterruptedException {
        publishEvent(this.session, evt);
    }


    /**
     * @param evt
     * @param sess
     * @throws MessagingException
     * @throws MarshallingException
     * @throws InterruptedException
     */
    @Override
    public void publishEvent ( Session sess, @NonNull EventMessage<@NonNull ? extends MessageSource> evt )
            throws MessagingException, MarshallingException, InterruptedException {
        Collection<EventScope> scopes = evt.getScopes();

        if ( Thread.interrupted() ) {
            // make sure we don't call into JMS when interrupted (this kills the connection)
            throw new InterruptedException();
        }

        @SuppressWarnings ( "unchecked" )
        Class<@NonNull EventMessage<@NonNull ? extends MessageSource>> clz = (Class<@NonNull EventMessage<@NonNull ? extends MessageSource>>) evt
                .getClass();
        MessageMarshaller<@NonNull EventMessage<@NonNull ? extends MessageSource>> marshaller = this.marshallerManager.getMarshaller(clz);

        try {
            Message m = marshaller.marshall(sess, evt);
            m.setJMSDeliveryMode(evt.getDeliveryMode());
            m.setJMSPriority(evt.getDeliveryPriority());
            m.setJMSExpiration(evt.getDeliveryTTL() != 0 ? ( System.currentTimeMillis() + evt.getDeliveryTTL() ) : 0);
            m.setIntProperty("ttl", evt.getTTL()); //$NON-NLS-1$

            for ( EventScope scope : scopes ) {
                EventRouter router = this.eventRouterManager.getRouterFor(scope);
                synchronized ( this.sessionLock ) {
                    router.routeMessage(sess, scope, m);
                }
            }
            log.trace("Published event " + evt.getClass().getName()); //$NON-NLS-1$
        }
        catch (
            JMSException |
            MessageRoutingException e ) {
            throw new MessagingException("Failed to publish event:", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws InterruptedException
     *
     * @see eu.agno3.runtime.messaging.client.internal.MessagingClientImpl#sendMessage(eu.agno3.runtime.messaging.msg.RequestMessage)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public <@NonNull TMsg extends RequestMessage<@NonNull ? extends MessageSource, TResponse, TError>, TResponse extends ResponseMessage<@NonNull ? extends MessageSource>, TError extends ErrorResponseMessage<@NonNull ? extends MessageSource>> @Nullable TResponse sendMessage (
            TMsg msg ) throws MessagingException, InterruptedException {

        if ( Thread.interrupted() ) {
            // make sure we don't call into JMS when interrupted (this kills the connection)
            throw new InterruptedException();
        }

        MessageContext ctx = this.createMessageContext();

        Class<TMsg> clz = (Class<TMsg>) msg.getClass();
        MessageMarshaller<TMsg> requestMarshaller = this.marshallerManager.getMarshaller(clz);

        MessageDestinationResolver targetResolver = this.destinationResolverManager.getResolverFor(
            (RequestMessage<@NonNull ? extends MessageSource, ResponseMessage<@NonNull ? extends MessageSource>, ErrorResponseMessage<@NonNull ? extends MessageSource>>) msg);

        String correlationId = genCorrelationID(msg.getClass().getName());

        try {
            sendRequestMessage(msg, requestMarshaller, targetResolver, correlationId, ctx);
        }
        catch ( JMSException e ) {
            throw new MessagingException("Failed to send request message:", e); //$NON-NLS-1$
        }

        if ( msg.hasResponse() ) {
            if ( Thread.interrupted() ) {
                // make sure we don't call into JMS when interrupted (this kills the connection)
                throw new InterruptedException();
            }
            Session rs = createLocalSession(true, Session.DUPS_OK_ACKNOWLEDGE, false);
            try {
                return readResponseMessage(rs, msg, correlationId, ctx);
            }
            finally {
                try {
                    rs.close();
                }
                catch ( JMSException e ) {
                    log.debug("Faile to close session", e); //$NON-NLS-1$
                }
            }
        }
        handleNoResponse(ctx);
        return null;
    }


    /**
     * @param ctx
     * @throws MessagingException
     */
    protected void handleNoResponse ( MessageContext ctx ) throws MessagingException {

    }


    /**
     * @return
     */
    protected MessageContext createMessageContext () {
        return new BaseMessageContext();
    }


    protected synchronized Session createLocalSession ( boolean transacted, int ackMode, boolean retry ) throws MessagingException {
        try {
            return getConnection().createSession(transacted, ackMode);
        }
        catch ( JMSException e ) {
            if ( e.getCause() instanceof ConnectionFailedException || e.getCause() instanceof IOException ) {
                this.conn = null;
                if ( !retry ) {
                    return createLocalSession(transacted, ackMode, true);
                }
            }
            throw new MessagingException("Failed to create local session", e); //$NON-NLS-1$
        }
    }


    /**
     * @param msg
     * @param correlationId
     * @return
     * @throws InterruptedException
     * @throws Exception
     * @throws Throwable
     */
    protected <@Nullable TResponse extends ResponseMessage<@NonNull ? extends MessageSource>, TMsg extends RequestMessage<@NonNull ? extends MessageSource, TResponse, TError>, @NonNull TError extends ErrorResponseMessage<@NonNull ? extends MessageSource>> TResponse readResponseMessage (
            Session s, TMsg msg, String correlationId, MessageContext ctx ) throws MessagingException, InterruptedException {
        MessageUnmarshaller<TResponse> responseUnmarshaller = this.unmarshallerManager.getUnmarshaller(msg.getResponseType());
        MessageUnmarshaller<TError> errorUnmarshaller = this.unmarshallerManager.getUnmarshaller(msg.getErrorResponseType());

        String selector = String.format("JMSCorrelationID = '%s'", correlationId); //$NON-NLS-1$

        try {
            TemporaryQueue rq = ctx.getReplyQueue();
            MessageConsumer replyConsumer = s.createConsumer(rq, selector);
            try {
                if ( log.isTraceEnabled() ) {
                    log.trace("Waiting for reply message on " + rq); //$NON-NLS-1$
                }
                long start = System.currentTimeMillis();
                Message m = replyConsumer.receive(msg.getReplyTimeout());
                if ( log.isTraceEnabled() ) {
                    log.trace(String.format(
                        "Reply took %.2f s", //$NON-NLS-1$
                        ( System.currentTimeMillis() - start ) / 1000.0));
                }

                if ( m == null ) {
                    throw new MessageTimeoutException(
                        String.format("Did not recieve reply within configured timeout (%d ms)", msg.getReplyTimeout())); //$NON-NLS-1$
                }

                if ( m.getStringProperty(MessageProperties.STATUS) == null ) {
                    throw new MessagingException("Response did not provide status"); //$NON-NLS-1$
                }

                return processResponse(msg, responseUnmarshaller, errorUnmarshaller, m, ctx);
            }
            finally {
                replyConsumer.close();
            }
        }
        catch (
            CallErrorException |
            MessageTimeoutException e ) {
            throw e;
        }
        catch ( JMSException e ) {
            throw new MessagingException("Failed to process call result:", e); //$NON-NLS-1$
        }
    }


    /**
     * @param msg
     * @param responseUnmarshaller
     * @param errorUnmarshaller
     * @param m
     * @return
     * @throws JMSException
     * @throws MessagingException
     * @throws CallErrorException
     */
    protected <TMsg extends RequestMessage<@NonNull ? extends MessageSource, TResponse, TError>, @Nullable TResponse extends ResponseMessage<@NonNull ? extends MessageSource>, @NonNull TError extends ErrorResponseMessage<@NonNull ? extends MessageSource>> TResponse processResponse (
            @NonNull TMsg msg, @NonNull MessageUnmarshaller<TResponse> responseUnmarshaller, @NonNull MessageUnmarshaller<TError> errorUnmarshaller,
            Message m, MessageContext ctx ) throws JMSException, MessagingException {
        ResponseStatus status = ResponseStatus.ERROR;

        try {
            status = ResponseStatus.valueOf(m.getStringProperty(MessageProperties.STATUS));
        }
        catch ( IllegalArgumentException e ) {
            throw new MessagingException("Illegal response status:", e); //$NON-NLS-1$
        }

        switch ( status ) {
        case ERROR:
            // always throws exception
            handleErrorResponse(msg, errorUnmarshaller, m, ctx);
            return null;
        case SUCCESS:

            return handleSuccessResponse(msg, responseUnmarshaller, m, ctx);
        default:
            throw new MessagingException("Unimplemented response state " + status); //$NON-NLS-1$
        }
    }


    /**
     * @param msg
     * @param responseUnmarshaller
     * @param m
     * @return
     * @throws JMSException
     * @throws MessagingException
     */
    @SuppressWarnings ( "unchecked" )
    protected <@Nullable TResponse extends ResponseMessage<@NonNull ? extends MessageSource>, TMsg extends RequestMessage<@NonNull ? extends MessageSource, @Nullable TResponse, @NonNull TError>, TError extends ErrorResponseMessage<@NonNull ? extends MessageSource>> @Nullable TResponse handleSuccessResponse (
            @NonNull TMsg msg, MessageUnmarshaller<@Nullable TResponse> responseUnmarshaller, Message m, MessageContext ctx )
                    throws JMSException, MessagingException {
        if ( !msg.getResponseType().getName().equals(m.getStringProperty(eu.agno3.runtime.messaging.msg.MessageProperties.TYPE)) ) {
            throw new MessagingException("Unexpected response type " //$NON-NLS-1$
                    + m.getStringProperty(eu.agno3.runtime.messaging.msg.MessageProperties.TYPE));
        }

        return responseUnmarshaller
                .unmarshall(m, msg.getResponseType().getClassLoader(), (eu.agno3.runtime.messaging.msg.Message<@NonNull MessageSource>) msg);
    }


    /**
     * @param msg
     * @param errorUnmarshaller
     * @param m
     * @throws JMSException
     * @throws MessagingException
     * @throws CallErrorException
     */
    @SuppressWarnings ( "unchecked" )
    protected <TResponse extends ResponseMessage<@NonNull ? extends MessageSource>, TMsg extends RequestMessage<@NonNull ? extends MessageSource, TResponse, TError>, TError extends ErrorResponseMessage<@NonNull ? extends MessageSource>> void handleErrorResponse (
            TMsg msg, MessageUnmarshaller<TError> errorUnmarshaller, Message m, MessageContext ctx ) throws JMSException, MessagingException {
        if ( !msg.getErrorResponseType().getName().equals(m.getStringProperty(eu.agno3.runtime.messaging.msg.MessageProperties.TYPE)) ) {
            throw new MessagingException("Unexpected error response type " //$NON-NLS-1$
                    + m.getStringProperty(eu.agno3.runtime.messaging.msg.MessageProperties.TYPE));
        }

        TError errorMessage = errorUnmarshaller
                .unmarshall(m, msg.getErrorResponseType().getClassLoader(), (eu.agno3.runtime.messaging.msg.Message<@NonNull MessageSource>) msg);

        throw new CallErrorException(errorMessage, msg);
    }


    /**
     * @param msg
     * @param requestMarshaller
     * @param targetResolver
     * @param correlationId
     * @throws JMSException
     * @throws MessagingException
     * @throws InterruptedException
     */
    @SuppressWarnings ( "unchecked" )
    protected <TMsg extends RequestMessage<@NonNull ? extends MessageSource, TResponse, TError>, TResponse extends ResponseMessage<@NonNull ? extends MessageSource>, TError extends ErrorResponseMessage<@NonNull ? extends MessageSource>> void sendRequestMessage (
            TMsg msg, MessageMarshaller<TMsg> requestMarshaller, MessageDestinationResolver targetResolver, String correlationId, MessageContext ctx )
                    throws JMSException, MessagingException, InterruptedException {

        try {
            this.session.getAcknowledgeMode();
        }
        catch ( IllegalStateException e ) {
            log.warn("Session is invalid", e); //$NON-NLS-1$
            this.reopenSession();
        }

        Message m = requestMarshaller.marshall(this.session, msg);
        m.setJMSCorrelationID(correlationId);
        m.setJMSDeliveryMode(msg.getDeliveryMode());
        m.setJMSPriority(msg.getDeliveryPriority());
        long exp = System.currentTimeMillis() + ( msg.getDeliveryTTL() != 0 ? msg.getDeliveryTTL() : msg.getReplyTimeout() );
        m.setJMSExpiration(exp);
        if ( msg.hasResponse() ) {
            ctx.setReplyQueue(getReplyQueue(this.session));
            m.setJMSReplyTo(ctx.getReplyQueue());
        }
        Destination d = targetResolver.createDestination(
            this.session,
            (RequestMessage<@NonNull ? extends MessageSource, ResponseMessage<@NonNull ? extends MessageSource>, ErrorResponseMessage<@NonNull ? extends MessageSource>>) msg);

        if ( log.isTraceEnabled() ) {
            log.trace("Sending message to destination " + d); //$NON-NLS-1$
        }

        synchronized ( this.sessionLock ) {
            MessageProducer producer = this.session.createProducer(d);
            try {
                producer.send(d, m, msg.getDeliveryMode(), msg.getDeliveryPriority(), exp);
            }
            finally {
                producer.close();
            }
        }
    }


    /**
     * @return a unique correlation ID for this request
     */
    private static String genCorrelationID ( String id ) {
        return id + "-" + UUID.randomUUID(); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.client.MessagingClient#getMessageSource()
     */
    @Override
    public @NonNull T getMessageSource () {
        return this.messageSource;
    }
}
