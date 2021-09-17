/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.09.2013 by mbechler
 */
package eu.agno3.runtime.messaging.events;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.EventMessage;


/**
 * @author mbechler
 * 
 */
public interface EventTypeRegistry {

    /**
     * @param eventType
     * @return the event type class
     * @throws MessagingException
     */
    Class<@NonNull ? extends EventMessage<@NonNull ? extends MessageSource>> getEventType ( String eventType ) throws MessagingException;
}
