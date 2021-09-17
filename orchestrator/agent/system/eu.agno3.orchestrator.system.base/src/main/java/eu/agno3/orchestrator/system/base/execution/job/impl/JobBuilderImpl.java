/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.job.impl;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.execution.Configurator;
import eu.agno3.orchestrator.system.base.execution.ExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.GroupJobBuilder;
import eu.agno3.orchestrator.system.base.execution.Job;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.JobIterator;
import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.Runner;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;
import eu.agno3.orchestrator.system.base.execution.impl.JobIteratorImpl;


/**
 * @author mbechler
 * 
 */
public class JobBuilderImpl implements JobBuilderInternal {

    private static final String DEFAULT_JOB_NAME = "JOB"; //$NON-NLS-1$

    private final Runner runner;
    private JobIterator executionUnits = new JobIteratorImpl();
    private String name = DEFAULT_JOB_NAME;
    private Set<String> flags = new HashSet<>();

    private Map<Class<? extends SystemService>, SystemService> services = new HashMap<>();

    private FailureJobBuilder failureJobBuilder;


    /**
     * 
     * @param runner
     */
    public JobBuilderImpl ( Runner runner ) {
        this.runner = runner;
    }


    @Override
    public <TResult extends Result, T extends ExecutionUnit<TResult, T, TConfigurator>, TConfigurator extends Configurator<TResult, T, TConfigurator>> TConfigurator add (
            Class<T> unitClass ) throws UnitInitializationFailedException {
        T unit = createUnit(unitClass);
        return this.add(unit);
    }


    @Override
    public <TResult extends Result, T extends ExecutionUnit<TResult, T, TConfigurator>, TConfigurator extends Configurator<TResult, T, TConfigurator>> T createUnit (
            Class<T> unitClass ) throws UnitInitializationFailedException {
        return this.runner.getExecutionUnitFactory().createExecutionUnit(unitClass);
    }


    @Override
    public <TResult extends Result, T extends ExecutionUnit<TResult, T, TConfigurator>, TConfigurator extends Configurator<TResult, T, TConfigurator>> TConfigurator add (
            T unit ) throws UnitInitializationFailedException {
        this.executionUnits.add(unit);
        return makeConfigurator(unit);
    }


    /**
     * @param unit
     * @return
     * @throws UnitInitializationFailedException
     */
    static <TResult extends Result, T extends ExecutionUnit<TResult, T, TConfigurator>, TConfigurator extends Configurator<TResult, T, TConfigurator>> TConfigurator makeConfigurator (
            T unit ) throws UnitInitializationFailedException {
        TConfigurator configurator = unit.createConfigurator();
        if ( configurator == null ) {
            throw new UnitInitializationFailedException(String.format(
                "Unit %s did return NULL for configurator", //$NON-NLS-1$
                unit.getClass().getName()));
        }
        return configurator;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.JobBuilder#onFail()
     */
    @Override
    public JobBuilder onFail () {
        if ( this.failureJobBuilder == null ) {
            this.failureJobBuilder = new FailureJobBuilder(this, this.executionUnits);
        }
        return this.failureJobBuilder;
    }


    @Override
    public @NonNull GroupJobBuilder beginGroup () {
        GroupJobBuilderImpl b = new GroupJobBuilderImpl(this);
        this.executionUnits.add(b.getExecutionGroup());
        return b;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.JobBuilder#named(java.lang.String)
     */
    @Override
    public void named ( String n ) {
        this.name = n;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.JobBuilder#withFlag(java.lang.String)
     */
    @Override
    public void withFlag ( String flag ) {
        this.flags.add(flag);
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.JobBuilder#getJob()
     */
    @Override
    public Job getJob () {
        return new JobImpl(this.executionUnits, this.name, this.flags.toArray(new String[] {}));
    }


    /**
     * {@inheritDoc}
     * 
     * @throws InvalidUnitConfigurationException
     *
     * @see eu.agno3.orchestrator.system.base.execution.JobBuilder#withService(java.lang.Class,
     *      eu.agno3.orchestrator.system.base.SystemService)
     */
    @Override
    public <T extends SystemService> void withService ( Class<T> serviceClass, T service ) throws InvalidUnitConfigurationException {
        if ( this.services.put(serviceClass, service) != null ) {
            throw new InvalidUnitConfigurationException("Multiple services registered for type " + serviceClass.getName()); //$NON-NLS-1$
        }
    }


    /**
     * @return the services
     */
    @Override
    public Map<Class<? extends SystemService>, SystemService> getServices () {
        return this.services;
    }

}
