/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.component.msg;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.msg.ResponseMessage;
import eu.agno3.runtime.messaging.msg.ResponseStatus;
import eu.agno3.runtime.messaging.msg.impl.EmptyMessage;


/**
 * @author mbechler
 * 
 */
public class ComponentPongMessage extends EmptyMessage<@NonNull ServerMessageSource> implements ResponseMessage<@NonNull ServerMessageSource> {

    /**
     * 
     */
    public ComponentPongMessage () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public ComponentPongMessage ( @NonNull ServerMessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public ComponentPongMessage ( @NonNull ServerMessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public ComponentPongMessage ( @NonNull ServerMessageSource origin ) {
        super(origin);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.ResponseMessage#getStatus()
     */
    @Override
    public ResponseStatus getStatus () {
        return ResponseStatus.SUCCESS;
    }

}
