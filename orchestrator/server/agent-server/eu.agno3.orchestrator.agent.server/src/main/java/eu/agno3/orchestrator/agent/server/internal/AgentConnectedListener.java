/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.server.internal;


import java.util.UUID;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.events.AgentConnectedEvent;
import eu.agno3.runtime.messaging.listener.CustomDestination;
import eu.agno3.runtime.messaging.listener.EventListener;


/**
 * @author mbechler
 * 
 */
@Component ( service = EventListener.class, property = "eventType=eu.agno3.orchestrator.agent.events.AgentConnectedEvent" )
public class AgentConnectedListener implements EventListener<AgentConnectedEvent>, CustomDestination {

    private static final Logger log = Logger.getLogger(AgentConnectedListener.class);
    private AgentConnectorWatcherImpl agentConnectorWatcher;


    @Reference
    protected synchronized void setAgentConnectorWatcher ( AgentConnectorWatcherImpl watcher ) {
        this.agentConnectorWatcher = watcher;
    }


    protected synchronized void unsetAgentConnectorWatcher ( AgentConnectorWatcherImpl watcher ) {
        if ( this.agentConnectorWatcher == watcher ) {
            this.agentConnectorWatcher = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.listener.EventListener#onEvent(eu.agno3.runtime.messaging.msg.EventMessage)
     */
    @Override
    public void onEvent ( @NonNull AgentConnectedEvent event ) {
        UUID agentId = event.getOrigin().getAgentId();
        log.debug(String.format("Agent %s connected", agentId)); //$NON-NLS-1$
        this.agentConnectorWatcher.connected(agentId);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.listener.EventListener#getEventType()
     */
    @Override
    public @NonNull Class<AgentConnectedEvent> getEventType () {
        return AgentConnectedEvent.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.listener.CustomDestination#createCustomDestination(javax.jms.Session)
     */
    @Override
    public Destination createCustomDestination ( Session s ) throws JMSException {
        return s.createTopic("system-agents"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.CustomDestination#createCustomDestinationId()
     */
    @Override
    public String createCustomDestinationId () {
        return "topic://system-agents"; //$NON-NLS-1$
    }

}
