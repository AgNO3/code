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

import eu.agno3.orchestrator.agent.events.AgentConnectingEvent;
import eu.agno3.runtime.messaging.listener.CustomDestination;
import eu.agno3.runtime.messaging.listener.EventListener;


/**
 * @author mbechler
 * 
 */
@Component ( service = EventListener.class, property = "eventType=eu.agno3.orchestrator.agent.events.AgentConnectingEvent" )
public class AgentConnectingListener implements EventListener<AgentConnectingEvent>, CustomDestination {

    private static final Logger log = Logger.getLogger(AgentConnectingListener.class);

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
    public void onEvent ( @NonNull AgentConnectingEvent event ) {
        UUID agentId = event.getOrigin().getAgentId();
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Agent %s connecting", agentId)); //$NON-NLS-1$
        }
        this.agentConnectorWatcher.connecting(event.getOrigin().getAgentId());
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.listener.EventListener#getEventType()
     */
    @Override
    public @NonNull Class<AgentConnectingEvent> getEventType () {
        return AgentConnectingEvent.class;
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
