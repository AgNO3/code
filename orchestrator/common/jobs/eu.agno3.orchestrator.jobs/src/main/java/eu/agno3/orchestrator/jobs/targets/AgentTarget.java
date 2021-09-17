/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.04.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.targets;


import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.jobs.JobTarget;


/**
 * @author mbechler
 * 
 */
public class AgentTarget implements JobTarget {

    protected static final String AGENT_PREFIX = "agent:"; //$NON-NLS-1$

    private static final long serialVersionUID = 1258803872582087025L;
    @NonNull
    private UUID agentId;


    /**
     * @param agentId
     */
    public AgentTarget ( @NonNull UUID agentId ) {
        this.agentId = agentId;
    }


    /**
     * @return the agentId
     */
    @NonNull
    public UUID getAgentId () {
        return this.agentId;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return AGENT_PREFIX + this.agentId;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof AgentTarget ) {
            return this.agentId.equals( ( (AgentTarget) obj ).agentId);
        }
        return super.equals(obj);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return this.agentId.hashCode();
    }
}
