/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.09.2015 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.service.units;


import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class ClearConfigFilesConfigurator extends AbstractConfigurator<StatusOnlyResult, ClearConfigFiles, ClearConfigFilesConfigurator> {

    /**
     * @param unit
     */
    protected ClearConfigFilesConfigurator ( ClearConfigFiles unit ) {
        super(unit);
    }


    /**
     * 
     * @param service
     * @return this configurator
     */
    public ClearConfigFilesConfigurator service ( ServiceStructuralObject service ) {
        getExecutionUnit().setService(service);
        return this.self();
    }


    /**
     * 
     * @param root
     * @return this configurator
     */
    public ClearConfigFilesConfigurator root ( String root ) {
        getExecutionUnit().setRoot(root);
        return this.self();
    }
}
