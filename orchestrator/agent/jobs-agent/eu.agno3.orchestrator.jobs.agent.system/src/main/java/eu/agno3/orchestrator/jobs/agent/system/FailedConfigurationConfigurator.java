/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.12.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class FailedConfigurationConfigurator extends AbstractConfigurator<StatusOnlyResult, FailedConfiguration, FailedConfigurationConfigurator> {

    /**
     * @param unit
     */
    protected FailedConfigurationConfigurator ( FailedConfiguration unit ) {
        super(unit);
    }


    /**
     * @param service
     * @return this configurator
     */
    public FailedConfigurationConfigurator service ( @NonNull ServiceStructuralObject service ) {
        this.getExecutionUnit().setService(service);
        return this.self();
    }


    /**
     * 
     * @param config
     * @return this configurator
     */
    public FailedConfigurationConfigurator config ( @NonNull ConfigurationInstance config ) {
        this.getExecutionUnit().setConfig(config);
        return this.self();
    }

}
