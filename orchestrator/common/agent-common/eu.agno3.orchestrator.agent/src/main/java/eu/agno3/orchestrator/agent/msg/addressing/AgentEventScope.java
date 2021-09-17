/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.msg.addressing;


import java.util.Objects;
import java.util.UUID;

import eu.agno3.runtime.messaging.addressing.EventScope;


/**
 * @author mbechler
 * 
 */
public class AgentEventScope extends AgentsEventScope {

    private UUID agentId;


    /**
     * @param agentId
     */
    public AgentEventScope ( UUID agentId ) {
        this.agentId = agentId;
    }


    /**
     * @return the agentId
     */
    public UUID getAgentId () {
        return this.agentId;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.addressing.EventScope#getParent()
     */
    @Override
    public EventScope getParent () {
        return new AgentsEventScope();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.agent.msg.addressing.AgentsEventScope#getEventTopic()
     */
    @Override
    public String getEventTopic () {
        return "events-agent-" + this.agentId; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.addressing.scopes.GlobalEventScope#hashCode()
     */
    @Override
    public int hashCode () {
        return super.hashCode() + ( this.agentId != null ? 3 * this.agentId.hashCode() : 0 );
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.addressing.scopes.GlobalEventScope#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        return super.equals(obj) && Objects.equals(this.agentId, ( (AgentEventScope) obj ).agentId);
    }
}
