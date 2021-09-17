/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2016 by mbechler
 */
package eu.agno3.orchestrator.agent.system.info;


import eu.agno3.orchestrator.system.base.SystemService;


/**
 * @author mbechler
 *
 */
public interface SystemInformationRefresher extends SystemService {

    /**
     * @param rescanPartitions
     * 
     */
    void triggerRefresh ( boolean rescanPartitions );

}
