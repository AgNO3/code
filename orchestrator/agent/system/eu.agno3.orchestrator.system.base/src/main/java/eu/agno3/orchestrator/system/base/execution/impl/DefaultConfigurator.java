/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.impl;


import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.cfg.Param;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;


/**
 * @author mbechler
 * @param <TResult>
 * @param <TExecutionUnit>
 * 
 */
public class DefaultConfigurator <TResult extends Result, TExecutionUnit extends AbstractParamConfigurableExecutionUnit<TResult, TExecutionUnit>>
        extends AbstractConfigurator<TResult, TExecutionUnit, DefaultConfigurator<TResult, TExecutionUnit>> {

    /**
     * @param unit
     */
    public DefaultConfigurator ( TExecutionUnit unit ) {
        super(unit);
    }


    /**
     * @param param
     * @param value
     * @return this configurator
     * @throws InvalidUnitConfigurationException
     */
    public DefaultConfigurator<TResult, TExecutionUnit> with ( Param param, Object value ) throws InvalidUnitConfigurationException {
        param.getDefinition().validateValue(value);
        this.getExecutionUnit().setParam(param, param.getDefinition().getParamType().cast(value));
        return this;
    }

}
