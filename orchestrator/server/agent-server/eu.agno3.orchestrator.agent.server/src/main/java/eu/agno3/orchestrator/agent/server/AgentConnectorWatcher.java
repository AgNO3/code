/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.server;


import java.util.Map;
import java.util.UUID;

import eu.agno3.orchestrator.agent.AgentInfo;
import eu.agno3.orchestrator.server.component.ComponentConnectorWatcher;


/**
 * @author mbechler
 * 
 */
public interface AgentConnectorWatcher extends ComponentConnectorWatcher {

    /**
     * @return status information for the active components
     */
    Map<UUID, AgentInfo> getActiveComponents ();


    /**
     * @param agentId
     * @return agent info
     */
    AgentInfo getAgentInfo ( UUID agentId );

}