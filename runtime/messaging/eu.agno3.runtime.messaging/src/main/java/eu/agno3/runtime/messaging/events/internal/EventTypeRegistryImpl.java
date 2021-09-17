/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.09.2013 by mbechler
 */
package eu.agno3.runtime.messaging.events.internal;


import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.events.EventTypeRegistry;
import eu.agno3.runtime.messaging.msg.EventMessage;


/**
 * @author mbechler
 * 
 */
@Component ( service = EventTypeRegistry.class )
public class EventTypeRegistryImpl implements EventTypeRegistry {

    private static final Logger log = Logger.getLogger(EventTypeRegistry.class);

    private Map<String, Class<@NonNull ? extends EventMessage<@NonNull ? extends MessageSource>>> eventMap = new HashMap<>();


    @SuppressWarnings ( "unchecked" )
    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindEventType ( EventMessage<@NonNull ? extends MessageSource> msg ) {
        if ( this.eventMap.containsKey(msg.getClass().getName()) ) {
            log.warn(String.format("Event type %s already registered", msg.getClass().getName())); //$NON-NLS-1$
        }

        this.eventMap.put(msg.getClass().getName(), (Class<@NonNull ? extends EventMessage<@NonNull ? extends MessageSource>>) msg.getClass());
    }


    protected synchronized void unbindEventType ( EventMessage<@NonNull ? extends MessageSource> msg ) {
        this.eventMap.remove(msg.getClass().getName());
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.events.EventTypeRegistry#getEventType(java.lang.String)
     */

    @SuppressWarnings ( "null" )
    @Override
    public synchronized @NonNull Class<@NonNull ? extends EventMessage<@NonNull ? extends MessageSource>> getEventType ( String eventType )
            throws MessagingException {

        Class<? extends EventMessage<@NonNull ? extends MessageSource>> evType = this.eventMap.get(eventType);

        if ( evType == null ) {
            throw new MessagingException("Unknown event type " + eventType); //$NON-NLS-1$
        }

        return evType;
    }

}
