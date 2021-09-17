/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.listener;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.RequestMessage;
import eu.agno3.runtime.messaging.msg.impl.AckMessage;
import eu.agno3.runtime.messaging.msg.impl.DefaultErrorResponseMessage;


/**
 * @author mbechler
 * @param <TMsg>
 * 
 */
public interface AckingDefaultRequestEndpoint <TMsg extends RequestMessage<@NonNull ? extends MessageSource, AckMessage, DefaultErrorResponseMessage>>
        extends RequestEndpoint<TMsg, AckMessage, DefaultErrorResponseMessage> {

}
