/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.component.msg;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.server.component.ComponentIllegalConnStateException;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.ErrorResponseMessage;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.msg.ResponseStatus;
import eu.agno3.runtime.messaging.msg.impl.EmptyMessage;


/**
 * @author mbechler
 * 
 */
public class ComponentConnStateFailureMessage extends EmptyMessage<@NonNull ServerMessageSource>
        implements ErrorResponseMessage<@NonNull ServerMessageSource> {

    /**
     * 
     */
    public ComponentConnStateFailureMessage () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public ComponentConnStateFailureMessage ( @NonNull ServerMessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public ComponentConnStateFailureMessage ( @NonNull ServerMessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public ComponentConnStateFailureMessage ( @NonNull ServerMessageSource origin ) {
        super(origin);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.ResponseMessage#getStatus()
     */
    @Override
    public ResponseStatus getStatus () {
        return ResponseStatus.ERROR;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.ErrorResponseMessage#getThrowable()
     */
    @Override
    public Throwable getThrowable () {
        return new ComponentIllegalConnStateException("Server returned failure on PING"); //$NON-NLS-1$
    }

}
