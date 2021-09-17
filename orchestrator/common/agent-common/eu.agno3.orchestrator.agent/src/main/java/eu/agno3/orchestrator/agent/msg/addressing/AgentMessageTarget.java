/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.03.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.msg.addressing;


import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageTarget;


/**
 * @author mbechler
 * 
 */
public class AgentMessageTarget implements MessageTarget {

    @NonNull
    private final UUID agentId;


    /**
     * @param targetId
     */
    public AgentMessageTarget ( @NonNull UUID targetId ) {
        this.agentId = targetId;
    }


    /**
     * @return the agentId
     */
    @NonNull
    public UUID getAgentId () {
        return this.agentId;
    }
}
