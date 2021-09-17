/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.03.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.connector;


import java.util.UUID;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.config.AgentConfig;
import eu.agno3.orchestrator.agent.msg.AbstractAgentRequestMessage;
import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.listener.CustomDestination;
import eu.agno3.runtime.messaging.listener.RequestEndpoint;
import eu.agno3.runtime.messaging.msg.ErrorResponseMessage;
import eu.agno3.runtime.messaging.msg.ResponseMessage;


/**
 * @author mbechler
 * @param <TRequest>
 * @param <TResponse>
 * @param <TError>
 * 
 */
public abstract class AbstractAgentRequestEndpoint <TRequest extends AbstractAgentRequestMessage<@NonNull ? extends MessageSource, TResponse, TError>, TResponse extends ResponseMessage<@NonNull ? extends MessageSource>, TError extends ErrorResponseMessage<@NonNull ? extends MessageSource>>
        implements RequestEndpoint<TRequest, TResponse, TError>, CustomDestination {

    private AgentConfig agentConfig;


    @Reference
    protected synchronized void setAgentConfig ( AgentConfig config ) {
        this.agentConfig = config;
    }


    protected synchronized void unsetAgentConfig ( AgentConfig config ) {
        if ( this.agentConfig == config ) {
            this.agentConfig = null;
        }
    }


    /**
     * @return the agentConfig
     */
    protected synchronized AgentConfig getAgentConfig () {
        return this.agentConfig;
    }


    protected @NonNull AgentMessageSource getMessageSource () throws MessagingException {
        UUID id = this.getAgentConfig().getId();

        if ( id == null ) {
            throw new MessagingException("Agent id is null"); //$NON-NLS-1$
        }

        return new AgentMessageSource(id);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.listener.CustomDestination#createCustomDestination(javax.jms.Session)
     */
    @Override
    public Destination createCustomDestination ( Session s ) throws JMSException {
        return s.createQueue(getQueueName());
    }


    private String getQueueName () {
        return this.getAgentConfig().getRequestQueuePrefix().concat(this.getMessageType().getName());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.CustomDestination#createCustomDestinationId()
     */
    @Override
    public String createCustomDestinationId () {
        return "queue://" + getQueueName(); //$NON-NLS-1$
    }

}
