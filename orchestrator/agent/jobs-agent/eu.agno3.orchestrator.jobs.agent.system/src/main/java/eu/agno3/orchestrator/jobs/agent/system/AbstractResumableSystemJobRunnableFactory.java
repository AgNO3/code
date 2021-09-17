/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system;


import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.exceptions.JobRunnableException;
import eu.agno3.orchestrator.jobs.exec.JobResumptionHandler;
import eu.agno3.orchestrator.jobs.exec.JobRunnable;
import eu.agno3.orchestrator.jobs.exec.ResumableJobRunnableFactory;
import eu.agno3.orchestrator.system.base.execution.JobBuilder;
import eu.agno3.orchestrator.system.base.execution.JobSuspendHandler;
import eu.agno3.orchestrator.system.base.execution.Runner;
import eu.agno3.orchestrator.system.base.execution.SuspendData;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;
import eu.agno3.orchestrator.system.base.execution.exception.InvalidUnitConfigurationException;
import eu.agno3.orchestrator.system.base.execution.impl.context.JobSuspendData;
import eu.agno3.runtime.util.classloading.CompositeClassLoader;
import eu.agno3.runtime.util.serialization.UnsafeObjectInputStream;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public abstract class AbstractResumableSystemJobRunnableFactory <T extends Job> extends AbstractSystemJobRunnableFactory<T>
        implements ResumableJobRunnableFactory<T> {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.agent.system.AbstractSystemJobRunnableFactory#getRunnableForJob(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public JobRunnable getRunnableForJob ( T j ) throws JobRunnableException {
        return this.getRunnableForJob(j, null);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.exec.ResumableJobRunnableFactory#getRunnableForJob(eu.agno3.orchestrator.jobs.Job,
     *      eu.agno3.orchestrator.jobs.exec.JobResumptionHandler)
     */
    @Override
    public JobRunnable getRunnableForJob ( T j, JobResumptionHandler h ) throws JobRunnableException {

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
                new ExtraServiceExecutionConfigWrapper(this.getConfig(), b.getServices()),
                h != null ? new SystemJobSuspendHandler<>(j, h) : null);
        }
        catch (
            JobBuilderException |
            InvalidUnitConfigurationException e ) {
            throw new JobRunnableException("Job initialization failed", e); //$NON-NLS-1$
        }
    }


    @Override
    public JobRunnable getJobResumption ( T job, JobResumptionHandler h ) throws JobRunnableException {
        if ( job == null ) {
            throw new JobRunnableException("Job is null"); //$NON-NLS-1$
        }
        Runner r = makeRunner();
        @SuppressWarnings ( "null" )
        @NonNull
        JobBuilder b = r.makeJobBuilder();
        SuspendData suspended;
        try ( InputStream state = h.getSuspendData(job) ) {
            initServices(b);
            if ( state == null ) {
                throw new JobRunnableException("No resumption data available for " + job); //$NON-NLS-1$
            }
            Set<ClassLoader> classloaders = new HashSet<>(
                Arrays.asList(this.getClass().getClassLoader(), JobSuspendHandler.class.getClassLoader(), job.getClass().getClassLoader()));

            classloaders.addAll(getExtraUnitClassLoaders(job));
            CompositeClassLoader cl = new CompositeClassLoader(classloaders);
            try ( UnsafeObjectInputStream ois = new UnsafeObjectInputStream(state, cl) ) {
                suspended = (SuspendData) ois.readObject();
            }

            h.resumed(job);
        }
        catch (
            IOException |
            ClassNotFoundException |
            JobQueueException |
            InvalidUnitConfigurationException e ) {
            throw new JobRunnableException("Failed to read suspended data " + job, e); //$NON-NLS-1$
        }

        return new SystemJobRunnableAdapter(
            r,
            suspended,
            new ExtraServiceExecutionConfigWrapper(this.getConfig(), b.getServices()),
            new SystemJobSuspendHandler<Job>(job, h));
    }


    /**
     * @return
     */
    protected Collection<ClassLoader> getExtraUnitClassLoaders ( @NonNull T job ) {
        return Collections.EMPTY_SET;
    }

    /**
     * @author mbechler
     *
     * @param <T>
     */
    public static class SystemJobSuspendHandler <T extends Job> implements JobSuspendHandler {

        private @NonNull T j;
        private JobResumptionHandler h;


        /**
         * @param j
         * @param h
         */
        public SystemJobSuspendHandler ( @NonNull T j, JobResumptionHandler h ) {
            this.j = j;
            this.h = h;
        }


        /**
         * {@inheritDoc}
         * 
         * @throws ExecutionException
         *
         * @see eu.agno3.orchestrator.system.base.execution.JobSuspendHandler#suspended(eu.agno3.orchestrator.system.base.execution.impl.context.JobSuspendData)
         */
        @Override
        public void suspended ( JobSuspendData suspend ) throws ExecutionException {
            try {
                this.h.writeSuspendData(this.j, suspend);
            }
            catch ( JobQueueException e ) {
                throw new ExecutionException("Failed to save job state", e); //$NON-NLS-1$
            }
        }

    }
}
