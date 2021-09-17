/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.job.impl;


import eu.agno3.orchestrator.system.base.execution.Configurator;
import eu.agno3.orchestrator.system.base.execution.ExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;


/**
 * @author mbechler
 *
 */
public interface JobBuilderInternal extends JobBuilder {

    /**
     * @param unitClass
     * @return the created unit
     * @throws UnitInitializationFailedException
     */
    <TResult extends Result, T extends ExecutionUnit<TResult, T, TConfigurator>, TConfigurator extends Configurator<TResult, T, TConfigurator>> T createUnit (
            Class<T> unitClass ) throws UnitInitializationFailedException;

}
