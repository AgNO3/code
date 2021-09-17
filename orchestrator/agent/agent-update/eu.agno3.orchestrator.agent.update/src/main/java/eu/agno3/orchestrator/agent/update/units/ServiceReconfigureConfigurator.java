/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.03.2016 by mbechler
 */
package eu.agno3.orchestrator.agent.update.units;


import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReference;
import eu.agno3.orchestrator.config.model.realm.StructuralObjectReferenceImpl;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class ServiceReconfigureConfigurator extends AbstractConfigurator<StatusOnlyResult, ReconfigureService, ServiceReconfigureConfigurator> {

    /**
     * @param unit
     */
    protected ServiceReconfigureConfigurator ( ReconfigureService unit ) {
        super(unit);
    }


    /**
     * @param sos
     * @return this configurator
     */
    public ServiceReconfigureConfigurator service ( ServiceStructuralObject sos ) {
        return service(StructuralObjectReferenceImpl.fromObject(sos));
    }


    /**
     * @param reference
     * @return this configurator
     */
    public ServiceReconfigureConfigurator service ( StructuralObjectReference reference ) {
        this.getExecutionUnit().setService(reference);
        return this.self();
    }


    /**
     * 
     * @return this configurator
     */
    public ServiceReconfigureConfigurator ignoreError () {
        this.getExecutionUnit().setIgnoreError(true);
        return this.self();
    }


    /**
     * 
     * @return this configurator
     */
    public ServiceReconfigureConfigurator allowRestart () {
        this.getExecutionUnit().setAllowRestart(true);
        return this.self();
    }

}
