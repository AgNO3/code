/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.impl;


import eu.agno3.orchestrator.system.base.execution.Configurator;
import eu.agno3.orchestrator.system.base.execution.ExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.ExecutionUnitFactory;
import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;


/**
 * @author mbechler
 * 
 */
public class DefaultExecutionUnitFactory implements ExecutionUnitFactory {

    @Override
    public <TResult extends Result, TConfigurator extends Configurator<TResult, T, TConfigurator>, T extends ExecutionUnit<TResult, T, TConfigurator>> T createExecutionUnit (
            Class<T> clazz ) throws UnitInitializationFailedException {
        try {
            return clazz.newInstance();
        }
        catch (
            IllegalAccessException |
            InstantiationException e ) {
            throw new UnitInitializationFailedException("Failed to create instance of unit " + clazz.getName(), e); //$NON-NLS-1$
        }
    }
}
