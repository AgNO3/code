/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.server.internal.router;


import java.util.UUID;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.config.AgentConfig;
import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageTarget;
import eu.agno3.orchestrator.agent.server.AgentConfigurationProvider;
import eu.agno3.orchestrator.server.component.ComponentConfigurationException;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.MessageTarget;
import eu.agno3.runtime.messaging.msg.ErrorResponseMessage;
import eu.agno3.runtime.messaging.msg.RequestMessage;
import eu.agno3.runtime.messaging.msg.ResponseMessage;
import eu.agno3.runtime.messaging.routing.MessageDestinationResolver;


/**
 * @author mbechler
 * 
 */
@Component ( service = MessageDestinationResolver.class, property = {
    "targetClass=eu.agno3.orchestrator.agent.msg.addressing.AgentMessageTarget"
}, immediate = true )
public class AgentTargetDestinationResolver implements MessageDestinationResolver {

    private AgentConfigurationProvider agentConfigProvider;


    @Reference
    protected synchronized void setAgentConfigProvider ( AgentConfigurationProvider provider ) {
        this.agentConfigProvider = provider;
    }


    protected synchronized void unsetAgentConfigProvider ( AgentConfigurationProvider provider ) {
        if ( this.agentConfigProvider == provider ) {
            this.agentConfigProvider = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * 
     * @see eu.agno3.runtime.messaging.routing.MessageDestinationResolver#createDestination(javax.jms.Session,
     *      eu.agno3.runtime.messaging.msg.RequestMessage)
     */
    @Override
    public Destination createDestination ( Session s,
            RequestMessage<@NonNull ? extends MessageSource, ResponseMessage<@NonNull ? extends MessageSource>, ErrorResponseMessage<@NonNull ? extends MessageSource>> msg )
                    throws MessagingException {

        MessageTarget t = msg.getTarget();

        if ( ! ( t instanceof AgentMessageTarget ) ) {
            throw new MessagingException("Illegal message type, target not AgentMessageTarget"); //$NON-NLS-1$
        }

        String targetPrefix = resolveTargetQueuePrefix(t);
        String queueName = targetPrefix.concat(msg.getClass().getName());

        try {
            return s.createQueue(queueName);
        }
        catch ( JMSException e ) {
            throw new MessagingException("Failed to connect to destination queue:", e); //$NON-NLS-1$
        }
    }


    private String resolveTargetQueuePrefix ( MessageTarget t ) throws MessagingException {
        AgentMessageTarget at = (AgentMessageTarget) t;
        UUID agentId = at.getAgentId();

        try {
            AgentConfig config = this.agentConfigProvider.getConfiguration(agentId);
            return config.getRequestQueuePrefix();
        }
        catch ( ComponentConfigurationException e1 ) {
            throw new MessagingException("Failed to fetch agent configuration", e1); //$NON-NLS-1$
        }
    }

}
