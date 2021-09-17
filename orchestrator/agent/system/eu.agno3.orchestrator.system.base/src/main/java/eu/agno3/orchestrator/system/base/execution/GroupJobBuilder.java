/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;


/**
 * @author mbechler
 *
 */
public interface GroupJobBuilder extends JobBuilder {

    /**
     * 
     * @param unitClass
     * @return a configurator for the added unit
     * @throws UnitInitializationFailedException
     */
    <TResult extends Result, T extends ExecutionUnit<TResult, T, TConfigurator>, TConfigurator extends Configurator<TResult, T, TConfigurator>> TConfigurator before (
            Class<T> unitClass ) throws UnitInitializationFailedException;


    /**
     * 
     * @param unit
     * @return a configurator for the added unit
     * @throws UnitInitializationFailedException
     */
    <TResult extends Result, T extends ExecutionUnit<TResult, T, TConfigurator>, TConfigurator extends Configurator<TResult, T, TConfigurator>> TConfigurator before (
            T unit ) throws UnitInitializationFailedException;


    /**
     * 
     * @param unitClass
     * @return a configurator for the added unit
     * @throws UnitInitializationFailedException
     */
    <TResult extends Result, T extends ExecutionUnit<TResult, T, TConfigurator>, TConfigurator extends Configurator<TResult, T, TConfigurator>> TConfigurator after (
            Class<T> unitClass ) throws UnitInitializationFailedException;


    /**
     * 
     * @param unit
     * @return a configurator for the added unit
     * @throws UnitInitializationFailedException
     */
    <TResult extends Result, T extends ExecutionUnit<TResult, T, TConfigurator>, TConfigurator extends Configurator<TResult, T, TConfigurator>> TConfigurator after (
            T unit ) throws UnitInitializationFailedException;
}
