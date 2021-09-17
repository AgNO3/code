/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.impl;


import java.util.HashMap;
import java.util.Map;

import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.cfg.Param;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidParameterException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;


/**
 * @author mbechler
 * @param <TResult>
 * @param <TExecutionUnit>
 * 
 */
@SuppressWarnings ( "serial" )
public abstract class AbstractParamConfigurableExecutionUnit <TResult extends Result, TExecutionUnit extends AbstractParamConfigurableExecutionUnit<TResult, TExecutionUnit>>
        extends AbstractExecutionUnit<TResult, TExecutionUnit, DefaultConfigurator<TResult, TExecutionUnit>> {

    private Map<Param, Object> params = new HashMap<>();


    protected Param[] getRequiredParams () {
        return new Param[] {};
    }


    protected void setParam ( Param param, Object value ) throws InvalidUnitConfigurationException {
        if ( !param.getDefinition().getParamType().isAssignableFrom(value.getClass()) ) {
            throw new InvalidParameterException(String.format("The parameter type %s does not match the defined parameter type %s", //$NON-NLS-1$
                value.getClass().getName(),
                param.getDefinition().getParamType()));
        }

        this.params.put(param, param.getDefinition().getParamType().cast(value));
    }


    /**
     * 
     * @param param
     * @param clazz
     * @return the parameter
     * @throws InvalidUnitConfigurationException
     */
    @SuppressWarnings ( "unchecked" )
    public <T> T getParam ( Param param, Class<T> clazz ) throws InvalidUnitConfigurationException {

        if ( !param.getDefinition().getParamType().isAssignableFrom(clazz) ) {
            throw new InvalidParameterException(String.format("The requested parameter type %s does not match the defined parameter type %s", //$NON-NLS-1$
                clazz.getName(),
                param.getDefinition().getParamType()));
        }

        return (T) param.getDefinition().processValue(this.params.get(param));
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutionUnit#createConfigurator()
     */
    @SuppressWarnings ( {
        "rawtypes", "unchecked"
    } )
    @Override
    public DefaultConfigurator<TResult, TExecutionUnit> createConfigurator () {
        return new DefaultConfigurator(this);
    }

}
