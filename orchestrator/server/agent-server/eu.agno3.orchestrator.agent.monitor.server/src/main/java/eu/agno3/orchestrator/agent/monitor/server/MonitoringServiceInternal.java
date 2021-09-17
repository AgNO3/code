/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.01.2016 by mbechler
 */
package eu.agno3.orchestrator.agent.monitor.server;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.system.monitor.ServiceRuntimeStatus;
import eu.agno3.orchestrator.system.monitor.service.MonitoringService;


/**
 * @author mbechler
 *
 */
public interface MonitoringServiceInternal extends MonitoringService {

    /**
     * @param service
     * @param newStatus
     */
    void haveServiceState ( @NonNull ServiceStructuralObject service, ServiceRuntimeStatus newStatus );

}
