/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.config;


import eu.agno3.orchestrator.server.component.ComponentConfig;


/**
 * @author mbechler
 * 
 */
public interface AgentConfig extends ComponentConfig {

    /**
     * 
     * @return this agent's request queue prefix
     */
    String getRequestQueuePrefix ();

}
