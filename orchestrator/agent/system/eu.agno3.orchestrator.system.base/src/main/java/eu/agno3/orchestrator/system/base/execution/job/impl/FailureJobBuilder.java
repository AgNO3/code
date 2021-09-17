/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.11.2015 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.job.impl;


import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.execution.Configurator;
import eu.agno3.orchestrator.system.base.execution.ExecutionUnit;
import eu.agno3.orchestrator.system.base.execution.GroupJobBuilder;
import eu.agno3.orchestrator.system.base.execution.Job;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.JobIterator;
import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.exception.UnitInitializationFailedException;


/**
 * @author mbechler
 *
 */
public class FailureJobBuilder implements JobBuilderInternal {

    private JobBuilderInternal parent;
    private JobIterator it;


    /**
     * @param parent
     * @param it
     */
    public FailureJobBuilder ( JobBuilderInternal parent, JobIterator it ) {
        this.parent = parent;
        this.it = it;
    }


    @Override
    public <TResult extends Result, T extends ExecutionUnit<TResult, T, TConfigurator>, TConfigurator extends Configurator<TResult, T, TConfigurator>> TConfigurator add (
            Class<T> unitClass ) throws UnitInitializationFailedException {
        return this.add(this.parent.createUnit(unitClass));
    }


    @Override
    public <TResult extends Result, T extends ExecutionUnit<TResult, T, TConfigurator>, TConfigurator extends Configurator<TResult, T, TConfigurator>> TConfigurator add (
            T unit ) throws UnitInitializationFailedException {
        this.it.addFailureUnit(unit);
        return JobBuilderImpl.makeConfigurator(unit);
    }


    @Override
    public <TResult extends Result, T extends ExecutionUnit<TResult, T, TConfigurator>, TConfigurator extends Configurator<TResult, T, TConfigurator>> T createUnit (
            Class<T> unitClass ) throws UnitInitializationFailedException {
        return this.parent.createUnit(unitClass);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.JobBuilder#onFail()
     */
    @Override
    public JobBuilder onFail () {
        throw new UnsupportedOperationException("Cannot nest failure handlers"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.JobBuilder#beginGroup()
     */
    @Override
    public @NonNull GroupJobBuilder beginGroup () {
        GroupJobBuilderImpl groupJobBuilder = new GroupJobBuilderImpl(this.parent);
        this.it.add(groupJobBuilder.getExecutionGroup());
        return groupJobBuilder;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.JobBuilder#getJob()
     */
    @Override
    public Job getJob () {
        return this.parent.getJob();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.JobBuilder#named(java.lang.String)
     */
    @Override
    public void named ( String name ) {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.JobBuilder#withFlag(java.lang.String)
     */
    @Override
    public void withFlag ( String flag ) {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.JobBuilder#withService(java.lang.Class,
     *      eu.agno3.orchestrator.system.base.SystemService)
     */
    @Override
    public <T extends SystemService> void withService ( Class<T> serviceClass, T service ) throws InvalidUnitConfigurationException {
        this.parent.withService(serviceClass, service);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.execution.JobBuilder#getServices()
     */
    @Override
    public Map<Class<? extends SystemService>, SystemService> getServices () {
        return this.parent.getServices();
    }

}
