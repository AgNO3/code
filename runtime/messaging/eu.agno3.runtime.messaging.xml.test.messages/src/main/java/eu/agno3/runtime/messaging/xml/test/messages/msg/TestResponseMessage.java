/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.09.2013 by mbechler
 */
package eu.agno3.runtime.messaging.xml.test.messages.msg;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.msg.ResponseMessage;
import eu.agno3.runtime.messaging.msg.ResponseStatus;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 * 
 */
public class TestResponseMessage extends XmlMarshallableMessage<@NonNull MessageSource> implements ResponseMessage<@NonNull MessageSource> {

    /**
     * 
     */
    public TestResponseMessage () {

    }


    /**
     * @param origin
     * @param replyTo
     */
    public TestResponseMessage ( @NonNull MessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
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
