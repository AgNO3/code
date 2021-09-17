/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.10.2014 by mbechler
 */
package eu.agno3.orchestrator.bootstrap.msg;


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
public class BootstrapResponseMessage extends EmptyMessage<@NonNull ServerMessageSource> implements ResponseMessage<@NonNull ServerMessageSource> {

    /**
     * 
     */
    public BootstrapResponseMessage () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public BootstrapResponseMessage ( @NonNull ServerMessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public BootstrapResponseMessage ( @NonNull ServerMessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public BootstrapResponseMessage ( @NonNull ServerMessageSource origin ) {
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
