/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.msg.addressing;


import java.util.Optional;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSourceRegistration;


/**
 * @author mbechler
 * 
 */
@Component ( service = MessageSourceRegistration.class, property = "type=agent" )
public class AgentMessageSource implements MessageSource, MessageSourceRegistration {

    private static final String AGENT_PREFIX = "agent:"; //$NON-NLS-1$
    /**
     * 
     */
    private static final long serialVersionUID = -2761325778982002679L;

    private Optional<@NonNull UUID> agentId;


    /**
     * Internal
     * 
     */
    public AgentMessageSource () {

    }


    /**
     * 
     * @param agentId
     */
    public AgentMessageSource ( @NonNull UUID agentId ) {
        this.agentId = Optional.of(agentId);
    }


    /**
     * @return the agentId
     */
    @NonNull
    public UUID getAgentId () {
        return this.agentId.get();
    }


    /**
     * @param agentId
     *            the agentId to set
     */
    protected void setAgentId ( @NonNull UUID agentId ) {
        this.agentId = Optional.of(agentId);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.addressing.MessageSource#encode()
     */
    @Override
    public String encode () {
        return String.format("%s%s", AGENT_PREFIX, this.getAgentId()); //$NON-NLS-1$
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.addressing.MessageSource#parse(java.lang.String)
     */
    @Override
    public void parse ( String encoded ) {
        if ( !encoded.startsWith(AGENT_PREFIX) ) {
            throw new IllegalArgumentException("Invalid agent message source"); //$NON-NLS-1$
        }

        UUID fromString = UUID.fromString(encoded.substring(AGENT_PREFIX.length()));
        if ( fromString == null ) {
            throw new IllegalArgumentException();
        }
        this.agentId = Optional.of(fromString);
    }
}
