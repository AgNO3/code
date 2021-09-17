/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.msg.impl;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.msg.ResponseMessage;
import eu.agno3.runtime.messaging.msg.ResponseStatus;


/**
 * @author mbechler
 * 
 */
public class AckMessage extends EmptyMessage<@NonNull MessageSource> implements ResponseMessage<@NonNull MessageSource> {

    /**
     * 
     */
    public AckMessage () {}


    /**
     * @param origin
     * @param replyTo
     */
    public AckMessage ( @NonNull MessageSource origin, Message<@NonNull MessageSource> replyTo ) {
        super(origin, replyTo);
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
