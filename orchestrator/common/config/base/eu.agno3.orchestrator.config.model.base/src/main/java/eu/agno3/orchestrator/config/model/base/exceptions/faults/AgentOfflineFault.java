/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.exceptions.faults;

import java.util.UUID;


/**
 * @author mbechler
 *
 */
public class AgentOfflineFault extends AgentFault {

    /**
     * 
     */
    private static final long serialVersionUID = 7056821492601950894L;


    /**
     * 
     */
    public AgentOfflineFault () {
        super();
    }


    /**
     * @param agentId
     * @param instanceName
     */
    public AgentOfflineFault ( UUID agentId, String instanceName ) {
        super(agentId, instanceName);
    }


    /**
     * @param agentId
     * @param instanceName
     */
    public AgentOfflineFault ( String agentId, String instanceName ) {
        super(agentId, instanceName);
    }

}
