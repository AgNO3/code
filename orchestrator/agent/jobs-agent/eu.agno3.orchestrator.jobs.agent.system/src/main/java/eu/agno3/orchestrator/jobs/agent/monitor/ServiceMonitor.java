/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.01.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.monitor;


import eu.agno3.orchestrator.config.model.realm.StructuralObjectReference;
import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.monitor.ServiceRuntimeStatus;


/**
 * @author mbechler
 *
 */
public interface ServiceMonitor extends SystemService {

    /**
     * @param service
     * @param timeout
     * @return the runtime status after waiting timeout secs if any transient state occurs
     * @throws InterruptedException
     */
    ServiceRuntimeStatus checkServiceActive ( StructuralObjectReference service, long timeout ) throws InterruptedException;

}
