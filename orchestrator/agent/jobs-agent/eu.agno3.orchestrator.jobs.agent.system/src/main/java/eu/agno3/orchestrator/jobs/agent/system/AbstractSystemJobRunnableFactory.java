/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.agent.service.ServiceManager;
import eu.agno3.orchestrator.jobs.exceptions.JobRunnableException;
import eu.agno3.orchestrator.jobs.exec.JobRunnable;
import eu.agno3.orchestrator.jobs.exec.JobRunnableFactory;
import eu.agno3.orchestrator.system.base.execution.ExecutionConfig;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.JobSuspendHandler;
import eu.agno3.orchestrator.system.base.execution.Runner;
import eu.agno3.orchestrator.system.base.execution.RunnerFactory;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public abstract class AbstractSystemJobRunnableFactory <T extends Job> implements JobRunnableFactory<T> {

    private RunnerFactory rf;
    private ExecutionConfig config;
    private ServiceManager serviceManager;


    /**
     * @param factory
     */
    protected void setRunnerFactory ( RunnerFactory factory ) {
        this.rf = factory;
    }


    protected void unsetRunnerFactory ( RunnerFactory factory ) {
        if ( this.rf == factory ) {
            this.rf = null;
        }
    }


    protected void setExecutionConfig ( ExecutionConfig cfg ) {
        this.config = cfg;
    }


    protected void unsetExecutionConfig ( ExecutionConfig cfg ) {
        if ( this.config == cfg ) {
            this.config = null;
        }
    }


    @Reference
    protected synchronized void setServiceManager ( ServiceManager sm ) {
        this.serviceManager = sm;
    }


    protected synchronized void unsetServiceManager ( ServiceManager sm ) {
        if ( this.serviceManager == sm ) {
            this.serviceManager = null;
        }
    }


    /**
     * @return the config
     */
    public ExecutionConfig getConfig () {
        return this.config;
    }


    /**
     * @return the serviceManager
     */
    public ServiceManager getServiceManager () {
        return this.serviceManager;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws JobRunnableException
     * 
     * @see eu.agno3.orchestrator.jobs.exec.JobRunnableFactory#getRunnableForJob(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public JobRunnable getRunnableForJob ( T j ) throws JobRunnableException {
        if ( j == null ) {
            throw new JobRunnableException("Job is null"); //$NON-NLS-1$
        }
        Runner r = makeRunner();
        @SuppressWarnings ( "null" )
        @NonNull
        JobBuilder b = r.makeJobBuilder();

        try {
            initServices(b);
            this.buildJob(b, j);
            return new SystemJobRunnableAdapter(
                r,
                b.getJob(),
                new ExtraServiceExecutionConfigWrapper(this.config, b.getServices()),
                this.getSuspendHandler(j));
        }
        catch (
            JobBuilderException |
            InvalidUnitConfigurationException e ) {
            throw new JobRunnableException("Job initialization failed", e); //$NON-NLS-1$
        }
    }


    /**
     * @param b
     * @throws InvalidUnitConfigurationException
     */
    protected void initServices ( @NonNull JobBuilder b ) throws InvalidUnitConfigurationException {

    }


    /**
     * @return
     */
    protected Runner makeRunner () {
        return this.rf.createRunner();
    }


    /**
     * @param j
     * @return
     */
    protected JobSuspendHandler getSuspendHandler ( @NonNull T j ) {
        return null;
    }


    /**
     * @param b
     * @param j
     * @throws JobBuilderException
     */
    protected abstract void buildJob ( @NonNull JobBuilder b, @NonNull T j ) throws JobBuilderException;

}
