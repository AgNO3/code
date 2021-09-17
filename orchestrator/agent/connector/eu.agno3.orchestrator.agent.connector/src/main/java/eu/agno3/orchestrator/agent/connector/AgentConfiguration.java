/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.08.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.connector;


import java.util.UUID;


/**
 * @author mbechler
 *
 */
public interface AgentConfiguration {

    /**
     * @return the configured agent id
     */
    UUID getAgentId ();

}
