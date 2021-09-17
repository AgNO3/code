/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: May 10, 2017 by mbechler
 */
package eu.agno3.orchestrator.system.agent;

import eu.agno3.orchestrator.system.base.SystemService;

/**
 * @author mbechler
 *
 */
public interface AgentConnectionService extends SystemService {

    /**
     * @return whether the connection was established
     */
    boolean tryConnect ();


    /**
     * 
     */
    void disconnect ();

}
