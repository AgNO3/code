/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.09.2015 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.service.units;


import java.util.Set;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class ReloadServiceConfigurator extends AbstractConfigurator<StatusOnlyResult, ReloadServiceConfig, ReloadServiceConfigurator> {

    /**
     * @param unit
     */
    protected ReloadServiceConfigurator ( ReloadServiceConfig unit ) {
        super(unit);
    }


    /**
     * 
     * @param pids
     * @return this configurator
     */
    public ReloadServiceConfigurator modified ( Set<String> pids ) {
        getExecutionUnit().setModifiedPids(pids);
        return this.self();
    }


    /**
     * @param force
     * @return this configurator
     */
    public ReloadServiceConfigurator forceRestart ( boolean force ) {
        getExecutionUnit().setForceRestart(force);
        return this.self();
    }


    /**
     * 
     * @return this configurator
     */
    public ReloadServiceConfigurator forceRestart () {
        return forceRestart(true);
    }


    /**
     * 
     * @param service
     * @return this configurator
     */
    public ReloadServiceConfigurator service ( ServiceStructuralObject service ) {
        getExecutionUnit().setService(service);
        return this.self();
    }

}
