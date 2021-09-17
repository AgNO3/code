/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.connector.internal;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.agent.config.AgentConfig;
import eu.agno3.orchestrator.agent.events.AgentConfigUpdatedEvent;
import eu.agno3.orchestrator.server.connector.impl.AbstractConfigUpdatedListener;
import eu.agno3.runtime.messaging.listener.EventListener;


/**
 * @author mbechler
 * 
 */
@Component ( service = EventListener.class, property = "eventType=eu.agno3.orchestrator.agent.events.AgentConfigUpdatedEvent" )
public class AgentConfigUpdatedListener extends AbstractConfigUpdatedListener<AgentConfigUpdatedEvent, AgentConfig> {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.listener.EventListener#getEventType()
     */
    @Override
    public @NonNull Class<AgentConfigUpdatedEvent> getEventType () {
        return AgentConfigUpdatedEvent.class;
    }

}
