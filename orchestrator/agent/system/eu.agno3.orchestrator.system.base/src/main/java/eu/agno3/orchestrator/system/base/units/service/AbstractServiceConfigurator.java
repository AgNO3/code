/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.service;


import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.base.service.ServiceResult;


/**
 * @author mbechler
 * @param <TExecutionUnit>
 * @param <TConfigurator>
 * 
 */
public abstract class AbstractServiceConfigurator <TExecutionUnit extends AbstractServiceExecutionUnit<TExecutionUnit, TConfigurator>, TConfigurator extends AbstractServiceConfigurator<TExecutionUnit, TConfigurator>>
        extends AbstractConfigurator<ServiceResult, TExecutionUnit, TConfigurator> {

    /**
     * @param unit
     */
    protected AbstractServiceConfigurator ( TExecutionUnit unit ) {
        super(unit);
    }


    /**
     * @param name
     * @return this configurator
     */
    public TConfigurator service ( String name ) {
        this.getExecutionUnit().setServiceName(name);
        return this.self();
    }


    /**
     * 
     * @param name
     * @return this configurator
     */
    public TConfigurator instance ( String name ) {
        this.getExecutionUnit().setInstance(name);
        return this.self();
    }
}
