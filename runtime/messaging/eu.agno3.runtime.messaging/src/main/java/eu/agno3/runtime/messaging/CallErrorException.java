/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.ErrorResponseMessage;
import eu.agno3.runtime.messaging.msg.RequestMessage;
import eu.agno3.runtime.messaging.msg.ResponseMessage;


/**
 * @author mbechler
 * 
 */
public class CallErrorException extends MessagingException {

    /**
     * 
     */
    private static final long serialVersionUID = -5926500545795544696L;
    private final ErrorResponseMessage<@NonNull ? extends MessageSource> result;

    private final RequestMessage<@NonNull MessageSource, ResponseMessage<@NonNull MessageSource>, ErrorResponseMessage<@NonNull MessageSource>> request;


    /**
     * @param msg
     * @param req
     */
    @SuppressWarnings ( "unchecked" )
    public <TError extends ErrorResponseMessage<@NonNull ? extends MessageSource>> CallErrorException ( TError msg,
            RequestMessage<@NonNull ? extends MessageSource, ? extends ResponseMessage<@NonNull ? extends MessageSource>, TError> req ) {
        super();
        this.result = msg;
        this.request = (RequestMessage<@NonNull MessageSource, ResponseMessage<@NonNull MessageSource>, ErrorResponseMessage<@NonNull MessageSource>>) req;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Throwable#getCause()
     */
    @Override
    public synchronized Throwable getCause () {
        return this.result.getThrowable();
    }


    /**
     * @param clazz
     * @return the actual error response
     */
    @SuppressWarnings ( "unchecked" )
    public <TErrorResult extends ErrorResponseMessage<@NonNull MessageSource>> TErrorResult getResult ( Class<TErrorResult> clazz ) {

        if ( !clazz.isAssignableFrom(this.result.getClass()) ) {
            throw new IllegalArgumentException("The given result type does not match the actual"); //$NON-NLS-1$
        }

        return (TErrorResult) this.result;
    }


    /**
     * @param clazz
     * @return the request causing this error response
     */
    @SuppressWarnings ( "unchecked" )
    public <TRequest extends RequestMessage<@NonNull MessageSource, ResponseMessage<@NonNull MessageSource>, ErrorResponseMessage<@NonNull MessageSource>>> TRequest getRequest (
            Class<TRequest> clazz ) {
        if ( !clazz.isAssignableFrom(this.request.getClass()) ) {
            throw new IllegalArgumentException("The given request type does not match the actual"); //$NON-NLS-1$
        }

        return (TRequest) this.request;
    }
}
