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

import eu.agno3.orchestrator.agent.msg.AgentPingMessage;
import eu.agno3.orchestrator.server.component.ComponentState;
import eu.agno3.orchestrator.server.component.msg.ComponentConnStateFailureMessage;
import eu.agno3.orchestrator.server.component.msg.ComponentPongMessage;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.client.MessagingClient;
import eu.agno3.runtime.messaging.listener.CustomDestination;
import eu.agno3.runtime.messaging.listener.MessageProcessingException;
import eu.agno3.runtime.messaging.listener.RequestEndpoint;


/**
 * @author mbechler
 * 
 */
@Component ( service = RequestEndpoint.class, property = "msgType=eu.agno3.orchestrator.agent.msg.AgentPingMessage" )
public class AgentPingListener
        implements RequestEndpoint<AgentPingMessage, ComponentPongMessage, ComponentConnStateFailureMessage>, CustomDestination {

    private static final Logger log = Logger.getLogger(AgentPingListener.class);
    private AgentConnectorWatcherImpl agentConnectorWatcher;
    private MessagingClient<ServerMessageSource> messageClient;


    @Reference
    protected synchronized void setAgentConnectorWatcher ( AgentConnectorWatcherImpl watcher ) {
        this.agentConnectorWatcher = watcher;
    }


    protected synchronized void unsetAgentConnectorWatcher ( AgentConnectorWatcherImpl watcher ) {
        if ( this.agentConnectorWatcher == watcher ) {
            this.agentConnectorWatcher = null;
        }
    }


    @Reference
    protected synchronized void setMessageClient ( MessagingClient<ServerMessageSource> mc ) {
        this.messageClient = mc;
    }


    protected synchronized void unsetMessageClient ( MessagingClient<ServerMessageSource> mc ) {
        if ( this.messageClient == mc ) {
            this.messageClient = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#onReceive(eu.agno3.runtime.messaging.msg.RequestMessage)
     */
    @Override
    public ComponentPongMessage onReceive ( @NonNull AgentPingMessage msg ) throws MessageProcessingException {
        UUID agentId = msg.getOrigin().getAgentId();
        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Agent %s ping", agentId.toString())); //$NON-NLS-1$
        }
        this.agentConnectorWatcher.pinging(msg.getOrigin().getAgentId());

        if ( this.agentConnectorWatcher.getComponentConnectorState(agentId) == ComponentState.FAILURE ) {
            throw new MessageProcessingException(new ComponentConnStateFailureMessage(this.messageClient.getMessageSource(), msg));
        }

        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Respond to agent %s ping", agentId.toString())); //$NON-NLS-1$
        }
        return new ComponentPongMessage(this.messageClient.getMessageSource(), msg);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.listener.CustomDestination#createCustomDestination(javax.jms.Session)
     */
    @Override
    public Destination createCustomDestination ( Session s ) throws JMSException {
        return s.createQueue("agents-ping"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.CustomDestination#createCustomDestinationId()
     */
    @Override
    public String createCustomDestinationId () {
        return "queue://agents-ping"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#getMessageType()
     */
    @Override
    public Class<AgentPingMessage> getMessageType () {
        return AgentPingMessage.class;
    }

}
