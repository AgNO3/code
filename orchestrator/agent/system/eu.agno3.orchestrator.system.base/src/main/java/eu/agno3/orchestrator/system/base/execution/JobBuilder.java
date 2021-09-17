/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;


/**
 * @author mbechler
 * 
 */
public interface JobBuilder {

    /**
     * 
     * @param unitClass
     * @return a configurator for the added unit
     * @throws UnitInitializationFailedException
     */
    <TResult extends Result, T extends ExecutionUnit<TResult, T, TConfigurator>, TConfigurator extends Configurator<TResult, T, TConfigurator>> TConfigurator add (
            Class<T> unitClass ) throws UnitInitializationFailedException;


    /**
     * 
     * @param unit
     * @return a configurator for the added unit
     * @throws UnitInitializationFailedException
     */
    <TResult extends Result, T extends ExecutionUnit<TResult, T, TConfigurator>, TConfigurator extends Configurator<TResult, T, TConfigurator>> TConfigurator add (
            T unit ) throws UnitInitializationFailedException;


    /**
     * 
     * @return a job builder for the failure branch
     */
    JobBuilder onFail ();


    /**
     * @return a group job builder
     */
    @NonNull
    GroupJobBuilder beginGroup ();


    /**
     * @return the built job
     */
    Job getJob ();


    /**
     * @param name
     */
    void named ( String name );


    /**
     * @param flag
     */
    void withFlag ( String flag );


    /**
     * @param serviceClass
     * @param service
     * @throws InvalidUnitConfigurationException
     */
    <T extends SystemService> void withService ( Class<T> serviceClass, T service ) throws InvalidUnitConfigurationException;


    /**
     * @return the local services
     */
    Map<Class<? extends SystemService>, SystemService> getServices ();

}
