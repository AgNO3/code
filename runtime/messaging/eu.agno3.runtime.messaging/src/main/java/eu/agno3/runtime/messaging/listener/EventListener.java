/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.listener;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.EventMessage;


/**
 * @author mbechler
 * @param <TEvent>
 * 
 */
public interface EventListener <TEvent extends EventMessage<@NonNull ? extends MessageSource>> extends BaseListener {

    /**
     * Called when an event arrives
     * 
     * @param event
     */
    void onEvent ( @NonNull TEvent event );


    /**
     * @return the event type for this listener
     */
    @NonNull
    Class<TEvent> getEventType ();
}
