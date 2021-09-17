/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.09.2013 by mbechler
 */
package eu.agno3.runtime.messaging.client.internal;


import java.io.InterruptedIOException;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.atomikos.datasource.ResourceException;

import eu.agno3.runtime.messaging.CallErrorException;
import eu.agno3.runtime.messaging.InvalidSessionException;
import eu.agno3.runtime.messaging.MessageTimeoutException;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.marshalling.MarshallerManager;
import eu.agno3.runtime.messaging.marshalling.MessageMarshaller;
import eu.agno3.runtime.messaging.marshalling.MessageUnmarshaller;
import eu.agno3.runtime.messaging.marshalling.UnmarshallerManager;
import eu.agno3.runtime.messaging.msg.ErrorResponseMessage;
import eu.agno3.runtime.messaging.msg.EventMessage;
import eu.agno3.runtime.messaging.msg.RequestMessage;
import eu.agno3.runtime.messaging.msg.ResponseMessage;
import eu.agno3.runtime.messaging.routing.DestinationResolverManager;
import eu.agno3.runtime.messaging.routing.EventRouterManager;
import eu.agno3.runtime.messaging.routing.MessageDestinationResolver;


/**
 * 
 * 
 * A word of warning when using this with coordiated transactions.
 * Sending a message requires commiting the current transaction, therefor
 * - anything on the pending transaction will be commited if the send succeeds
 * - anything on the pending transaction will be rolled back if the send fails
 * 
 * If there was a valid transaction, this will always return a new one (the one contains reading the response),
 * replacing the previously active one.
 * 
 * @author mbechler
 * @param <T>
 * 
 */
public class TransactedMessagingClientImpl <T extends MessageSource> extends MessagingClientImpl<T> {

    private static final Logger log = Logger.getLogger(TransactedMessagingClientImpl.class);

    private TransactionManager tm;

    private Object transactedSessionLock = new Object();


    /**
     * @param messageSource
     * @param cf
     * @param tm
     * @param eventRouterManager
     * @param destinationResolverManager
     * @param marshallerManager
     * @param unmarshallerManager
     */
    public TransactedMessagingClientImpl ( @NonNull T messageSource, ConnectionFactory cf, TransactionManager tm,
            EventRouterManager eventRouterManager, DestinationResolverManager destinationResolverManager, MarshallerManager marshallerManager,
            UnmarshallerManager unmarshallerManager ) {
        super(messageSource, cf, eventRouterManager, destinationResolverManager, marshallerManager, unmarshallerManager);
        this.tm = tm;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws MessagingException
     *
     * @see eu.agno3.runtime.messaging.client.internal.MessagingClientImpl#getSession()
     */
    @Override
    public Session getSession () throws MessagingException {
        return createLocalSession(true, Session.SESSION_TRANSACTED, false);
    }


    /**
     * @param transacted
     * @return a session
     * @throws MessagingException
     */
    @Override
    public Session getSession ( boolean transacted, int ackMode ) throws MessagingException {
        return createLocalSession(transacted, ackMode, false);
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws InterruptedException
     * 
     * 
     * @see eu.agno3.runtime.messaging.client.MessagingClient#publishEvent(eu.agno3.runtime.messaging.msg.EventMessage)
     */
    @Override
    public void publishEvent ( @NonNull EventMessage<@NonNull ? extends MessageSource> evt ) throws MessagingException, InterruptedException {
        // transacted session reuse does cause issues (even when synchronized)
        publishEvent(this.createLocalSession(true, Session.SESSION_TRANSACTED, false), evt);
    }


    /**
     * @param s
     * @param evt
     * @throws MessagingException
     * @throws InterruptedException
     */
    @Override
    public void publishEvent ( Session s, @NonNull EventMessage<@NonNull ? extends MessageSource> evt )
            throws MessagingException, InterruptedException {
        try {
            boolean startedTransaction;
            try {
                startedTransaction = s.getAcknowledgeMode() == Session.SESSION_TRANSACTED && beginTransactionIfNotActive();
            }
            catch ( Exception e ) {
                throw new InvalidSessionException("Session is invalid", e); //$NON-NLS-1$
            }
            super.publishEvent(s, evt);
            if ( startedTransaction ) {
                this.commit();
                if ( log.isDebugEnabled() ) {
                    log.debug("Comitted event " + evt.getClass().getName()); //$NON-NLS-1$
                }
            }
        }
        catch ( ResourceException e ) {
            tryRollback();
            if ( e.getCause() instanceof XAException && e.getCause() instanceof JMSException
                    && e.getCause().getCause() instanceof InterruptedIOException ) {
                log.debug("Interrupted origin", e); //$NON-NLS-1$
                throw new InterruptedException();
            }
            throw new MessagingException("Failed to publish event", e); //$NON-NLS-1$
        }
        catch ( MessagingException e ) {
            this.tryRollback();
            throw new InvalidSessionException("Failed to publish event", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.client.internal.MessagingClientImpl#createMessageContext()
     */
    @Override
    protected MessageContext createMessageContext () {
        return new TransactedMessageContext();
    }


    private boolean beginTransactionIfNotActive () throws MessagingException {
        try {
            Transaction transaction = this.tm.getTransaction();
            if ( transaction == null || transaction.getStatus() != Status.STATUS_ACTIVE ) {
                this.beginTransaction();
                return true;
            }
        }
        catch ( SystemException e ) {
            log.warn("Failed to determine transaction status", e); //$NON-NLS-1$
        }
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws InterruptedException
     * 
     * @see eu.agno3.runtime.messaging.client.internal.MessagingClientImpl#sendRequestMessage(eu.agno3.runtime.messaging.msg.RequestMessage,
     *      eu.agno3.runtime.messaging.marshalling.MessageMarshaller,
     *      eu.agno3.runtime.messaging.routing.MessageDestinationResolver, java.lang.String)
     */
    @Override
    protected <TMsg extends RequestMessage<@NonNull ? extends MessageSource, TResponse, TError>, TResponse extends ResponseMessage<@NonNull ? extends MessageSource>, TError extends ErrorResponseMessage<@NonNull ? extends MessageSource>> void sendRequestMessage (
            TMsg msg, MessageMarshaller<TMsg> requestMarshaller, MessageDestinationResolver targetResolver, String correlationId, MessageContext ctx )
                    throws JMSException, MessagingException, InterruptedException {

        synchronized ( this.transactedSessionLock ) {
            // if there is an active transaction, use it
            ( (TransactedMessageContext) ctx ).setAutomaticTransaction(this.beginTransactionIfNotActive());

            try {
                this.tm.setTransactionTimeout((int) ( msg.getReplyTimeout() / 1000 ) + 1);
                super.sendRequestMessage(msg, requestMarshaller, targetResolver, correlationId, ctx);
                this.commit();
            }
            catch ( ResourceException e ) {
                tryRollback();
                if ( e.getCause() instanceof XAException && e.getCause() instanceof JMSException
                        && e.getCause().getCause() instanceof InterruptedIOException ) {
                    log.debug("Interrupted origin", e); //$NON-NLS-1$
                    throw new InterruptedException();
                }
                throw new MessagingException("Failed to publish event:", e); //$NON-NLS-1$
            }
            catch ( Exception e ) {
                log.warn("Message sending failed:", e); //$NON-NLS-1$
                tryRollback();
                throw new MessagingException("Message send failed:", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @throws MessagingException
     */
    protected void beginTransaction () throws MessagingException {
        try {
            log.trace("Starting transaction"); //$NON-NLS-1$
            this.tm.begin();
        }
        catch (
            SystemException |
            NotSupportedException e ) {
            throw new MessagingException("Failed to start transaction:", e); //$NON-NLS-1$
        }
    }


    /**
     * @throws HeuristicMixedException
     * @throws HeuristicRollbackException
     * @throws RollbackException
     * @throws SystemException
     * @throws MessagingException
     */
    protected void commit () throws MessagingException {
        try {
            log.trace("Commiting transaction"); //$NON-NLS-1$
            if ( !isConnectionOpen() || Thread.interrupted() ) {
                this.tm.rollback();
            }
            else {
                this.tm.commit();
            }
        }
        catch (
            HeuristicMixedException |
            HeuristicRollbackException |
            RollbackException e ) {
            throw new MessagingException("Transaction rollback:", e); //$NON-NLS-1$
        }
        catch ( SystemException e ) {
            throw new InvalidSessionException();
        }
    }


    /**
     * 
     */
    protected final void tryRollback () {
        try {
            if ( this.tm.getStatus() != Status.STATUS_NO_TRANSACTION ) {
                log.trace("Rolling back transaction"); //$NON-NLS-1$
                this.tm.rollback();
            }
        }
        catch (
            SystemException |
            IllegalStateException e ) {
            log.error("Failed to rollback transaction", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws InterruptedException
     *
     * @see eu.agno3.runtime.messaging.client.internal.TransactedMessagingClientImpl#readResponseMessage(javax.jms.Session,
     *      eu.agno3.runtime.messaging.msg.RequestMessage, java.lang.String,
     *      eu.agno3.runtime.messaging.client.internal.MessageContext)
     */
    @Override
    protected <TResponse extends ResponseMessage<@NonNull ? extends MessageSource>, TMsg extends RequestMessage<@NonNull ? extends MessageSource, @Nullable TResponse, TError>, TError extends ErrorResponseMessage<@NonNull ? extends MessageSource>> @Nullable TResponse readResponseMessage (
            Session s, TMsg msg, String correlationId, MessageContext ctx ) throws MessagingException, InterruptedException {
        this.beginTransaction();
        try {
            return super.readResponseMessage(s, msg, correlationId, ctx);
        }
        catch ( ResourceException e ) {
            tryRollback();
            if ( e.getCause() instanceof XAException && e.getCause() instanceof JMSException
                    && e.getCause().getCause() instanceof InterruptedIOException ) {
                log.debug("Interrupted origin", e); //$NON-NLS-1$
                throw new InterruptedException();
            }
            throw new MessagingException("Failed to publish event:", e); //$NON-NLS-1$
        }
        catch ( MessageTimeoutException e ) {
            tryRollback();
            throw e;
        }
        catch ( CallErrorException e ) {
            throw e;
        }
        catch ( MessagingException e ) {
            tryRollback();
            throw new MessagingException("Failed to process call result:", e); //$NON-NLS-1$
        }
        finally {
            if ( ! ( (TransactedMessageContext) ctx ).isAutomaticTransaction() ) {
                // make sure we leave with an active transaction is we had one
                // this new transaction will ultimately be handled by some outer transaction context
                // but if we don't leave one callers catching any of the exceptions will have the reopen one
                // on the other hand this means that all pending transaction operations will be rolled back
                beginTransactionIfNotActive();
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws MessagingException
     *
     * @see eu.agno3.runtime.messaging.client.internal.MessagingClientImpl#handleNoResponse(eu.agno3.runtime.messaging.client.internal.MessageContext)
     */
    @Override
    protected void handleNoResponse ( MessageContext ctx ) throws MessagingException {
        super.handleNoResponse(ctx);

        if ( ! ( (TransactedMessageContext) ctx ).isAutomaticTransaction() ) {
            // we commited a transaction and have to start a new one
            beginTransactionIfNotActive();
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.client.internal.MessagingClientImpl#handleErrorResponse(eu.agno3.runtime.messaging.msg.RequestMessage,
     *      eu.agno3.runtime.messaging.marshalling.MessageUnmarshaller, javax.jms.Message)
     */
    @Override
    protected <TResponse extends ResponseMessage<@NonNull ? extends MessageSource>, TMsg extends RequestMessage<@NonNull ? extends MessageSource, TResponse, TError>, TError extends ErrorResponseMessage<@NonNull ? extends MessageSource>> void handleErrorResponse (
            TMsg msg, MessageUnmarshaller<TError> errorUnmarshaller, Message m, MessageContext ctx ) throws JMSException, MessagingException {
        try {
            super.handleErrorResponse(msg, errorUnmarshaller, m, ctx);
        }
        catch ( CallErrorException e ) {
            if ( ( (TransactedMessageContext) ctx ).isAutomaticTransaction() ) {
                this.commit();
            }
            throw e;
        }
        catch ( Exception e ) {
            throw new MessagingException("Failed parsing error response:", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.client.internal.TransactedMessagingClientImpl#handleSuccessResponse(eu.agno3.runtime.messaging.msg.RequestMessage,
     *      eu.agno3.runtime.messaging.marshalling.MessageUnmarshaller, javax.jms.Message,
     *      eu.agno3.runtime.messaging.client.internal.MessageContext)
     */
    @Override
    protected <TResponse extends ResponseMessage<@NonNull ? extends MessageSource>, TMsg extends RequestMessage<@NonNull ? extends MessageSource, @Nullable TResponse, @NonNull TError>, TError extends ErrorResponseMessage<@NonNull ? extends MessageSource>> @Nullable TResponse handleSuccessResponse (
            @NonNull TMsg msg, MessageUnmarshaller<@Nullable TResponse> responseUnmarshaller, Message m, MessageContext ctx )
                    throws JMSException, MessagingException {
        try {
            @Nullable
            TResponse res = super.handleSuccessResponse(msg, responseUnmarshaller, m, ctx);
            if ( ( (TransactedMessageContext) ctx ).isAutomaticTransaction() ) {
                this.commit();
            }
            return res;
        }
        catch ( Exception e ) {
            throw new MessagingException("Failed parsing success response:", e); //$NON-NLS-1$
        }
    }
}
