/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;


/**
 * @author mbechler
 * 
 */
public interface ExecutionUnitFactory {

    /**
     * @param clazz
     * @return an execution unit of the specified class
     * @throws UnitInitializationFailedException
     */
    <TResult extends Result, TConfigurator extends Configurator<TResult, T, TConfigurator>, T extends ExecutionUnit<TResult, T, TConfigurator>> T createExecutionUnit (
            Class<T> clazz ) throws UnitInitializationFailedException;
}
