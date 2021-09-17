/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord.internal.exec;


import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobGroup;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.coord.Executor;
import eu.agno3.orchestrator.jobs.coord.JobForExecutionProvider;
import eu.agno3.orchestrator.jobs.coord.JobRunnableFactoryInternal;
import eu.agno3.orchestrator.jobs.coord.JobStateTracker;
import eu.agno3.orchestrator.jobs.coord.OutputHandlerFactory;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.exceptions.JobRunnableException;
import eu.agno3.orchestrator.jobs.exec.JobOutputHandler;
import eu.agno3.orchestrator.jobs.exec.JobResumptionHandler;
import eu.agno3.orchestrator.jobs.exec.JobRunnable;


/**
 * @author mbechler
 * 
 */
public class ExecutorImpl implements Executor {

    private static final Logger log = Logger.getLogger(ExecutorImpl.class);

    private static final long SHUTDOWN_TIMEOUT = 30;

    private JobForExecutionProvider coordinator;
    private JobStateTracker jst;
    private JobRunnableFactoryInternal jobRunnableFactory;

    private Map<Job, Future<?>> runnables = new ConcurrentHashMap<>();

    private ExecutorService executor;

    private long shutdownTimeout;

    private boolean enabled = true;

    private OutputHandlerFactory outHandlerFactory;


    /**
     * @param coordinator
     * @param jst
     * @param runnableFactory
     * @param outputHandlerFactory
     * @param numThreads
     * @param shutdownTimeout
     */
    public ExecutorImpl ( JobForExecutionProvider coordinator, JobStateTracker jst, JobRunnableFactoryInternal runnableFactory,
            OutputHandlerFactory outputHandlerFactory, int numThreads, long shutdownTimeout ) {
        if ( coordinator == null || jst == null || runnableFactory == null || outputHandlerFactory == null ) {
            throw new IllegalArgumentException();
        }
        this.coordinator = coordinator;
        this.jst = jst;
        this.jobRunnableFactory = runnableFactory;
        this.executor = Executors.newFixedThreadPool(numThreads, new ThreadFactoryImplementation());
        this.shutdownTimeout = shutdownTimeout;
        this.outHandlerFactory = outputHandlerFactory;
    }


    /**
     * 
     * @param coordinator
     * @param jst
     * @param runnableFactory
     * @param outputHandlerFactory
     * @param numThreads
     */
    public ExecutorImpl ( JobForExecutionProvider coordinator, JobStateTracker jst, JobRunnableFactoryInternal runnableFactory,
            OutputHandlerFactory outputHandlerFactory, int numThreads ) {
        this(coordinator, jst, runnableFactory, outputHandlerFactory, numThreads, SHUTDOWN_TIMEOUT);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.Executor#shutdown()
     */
    @Override
    public synchronized void shutdown () {
        this.executor.shutdown();

        try {
            if ( this.executor.awaitTermination(this.shutdownTimeout, TimeUnit.SECONDS) ) {
                return;
            }
        }
        catch ( InterruptedException e ) {
            log.warn("Executor shutdown was interrupted", e); //$NON-NLS-1$
            return;
        }

        for ( Job j : this.runnables.keySet() ) {
            try {
                this.cancel(j);
            }
            catch ( JobQueueException e ) {
                log.warn("Failed to cancel job " + j, e); //$NON-NLS-1$
            }
        }

        this.cleanFinishedJobs();
        this.executor.shutdownNow();
        this.runnables.clear();
    }


    /**
     * {@inheritDoc}
     * 
     * 
     * @see eu.agno3.orchestrator.jobs.coord.Executor#run()
     */
    @Override
    public synchronized void run () {
        if ( !this.enabled || this.executor.isShutdown() ) {
            return;
        }

        if ( log.isTraceEnabled() ) {
            log.trace("Currently running " + this.runnables.keySet()); //$NON-NLS-1$
        }

        cleanFinishedJobs();

        for ( JobGroup g : this.coordinator.getKnownGroups() ) {
            Job j = this.coordinator.getJobForExecution(g);

            if ( j == null || this.runnables.containsKey(j) ) {
                if ( log.isTraceEnabled() ) {
                    log.trace("No job found in group " + g); //$NON-NLS-1$
                }
                continue;
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Found job for execution " + j); //$NON-NLS-1$
            }

            try {
                JobInfo jobState = this.jst.getJobState(j);
                Runnable runnable = this.makeJobRunnable(j, jobState != null ? jobState.getState() : JobState.RUNNABLE);
                this.runnables.put(j, this.executor.submit(runnable));
            }
            catch ( Exception e ) {
                log.warn("Failed to create runnable for job " + j, e); //$NON-NLS-1$
                try {
                    this.jst.updateJobState(j, JobState.FAILED);
                }
                catch ( JobQueueException e2 ) {
                    log.warn("Failed to set job state to FAILED:", e2); //$NON-NLS-1$
                }
            }
        }
    }


    private void cleanFinishedJobs () {
        for ( Entry<Job, Future<?>> e : this.runnables.entrySet() ) {
            if ( e.getValue().isDone() ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Removing job " + e.getKey()); //$NON-NLS-1$
                }
                this.runnables.remove(e.getKey());
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.Executor#waitFor(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public void waitFor ( Job j ) throws InterruptedException {
        Future<?> f = this.runnables.get(j);
        if ( f == null ) {
            return;
        }

        try {
            f.get();
        }
        catch ( ExecutionException e ) {
            log.warn("Job failed: ", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.Executor#cancel(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public void cancel ( Job j ) throws JobQueueException {
        if ( log.isDebugEnabled() ) {
            log.debug("Cancelling job " + j); //$NON-NLS-1$
        }

        Future<?> f = this.runnables.get(j);
        if ( f == null ) {
            this.jst.updateJobState(j, JobState.CANCELLED);
            return;
        }

        f.cancel(true);
        this.jst.updateJobState(j, JobState.CANCELLED);
    }


    /**
     * @param j
     * @param jobState
     * @return
     * @throws JobRunnableException
     */
    private Runnable makeJobRunnable ( Job j, JobState jobState ) throws JobRunnableException {
        JobOutputHandler outHandler = this.outHandlerFactory.forJob(j);
        if ( outHandler == null ) {
            throw new JobRunnableException("No output handler"); //$NON-NLS-1$
        }
        try {
            return new JobWrapper(this.jst, j, this.getJobRunnable(j, jobState), outHandler);
        }
        catch ( JobRunnableException e ) {
            try {
                outHandler.start();
                dumpCreationFailure(e, outHandler);
            }
            finally {
                outHandler.end();
            }
            throw e;
        }
    }


    /**
     * @param e
     * @param outHandler
     */
    private void dumpCreationFailure ( Throwable e, JobOutputHandler outHandler ) {
        outHandler.logLineError(e.getMessage(), e); // $NON-NLS-1$
        if ( e.getCause() != null ) {
            dumpCreationFailure(e.getCause(), outHandler);
        }
    }


    @Override
    public boolean canRun ( Job j ) {
        if ( j instanceof JobRunnable ) {
            return true;
        }
        if ( this.jobRunnableFactory != null ) {
            return this.jobRunnableFactory.hasRunnable(j);
        }
        return false;
    }


    /**
     * @param j
     * @param jobState
     * @return
     * @throws JobRunnableException
     */
    protected JobRunnable getJobRunnable ( Job j, JobState jobState ) throws JobRunnableException {

        if ( j instanceof JobRunnable ) {
            return (JobRunnable) j;
        }

        if ( this.jobRunnableFactory != null ) {
            JobResumptionHandler h = this.jst instanceof JobResumptionHandler ? (JobResumptionHandler) this.jst : null;
            JobRunnable r = this.jobRunnableFactory.getRunnableForJob(j, jobState, h);
            if ( r == null ) {
                log.error("Runnable factory did not return a job, running NOP"); //$NON-NLS-1$
                return makeNOPJob(j.getClass().toString());
            }
            return r;
        }

        log.error("No runnable factory set on executor, running NOP"); //$NON-NLS-1$
        return makeNOPJob(j.getClass().toString());
    }


    /**
     * @return
     */
    private static JobRunnable makeNOPJob ( String type ) {
        return new JobRunnable() {

            @Override
            public JobState run ( @NonNull JobOutputHandler h ) throws JobRunnableException {
                throw new JobRunnableException("No job handler found for " + type); //$NON-NLS-1$
            }
        };
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.Executor#disableExecution()
     */
    @Override
    public void disableExecution () {
        this.enabled = false;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.Executor#enableExecution()
     */
    @Override
    public void enableExecution () {
        this.enabled = true;
    }

    /**
     * @author mbechler
     * 
     */
    private static final class ThreadFactoryImplementation implements ThreadFactory {

        /**
         * 
         */
        public ThreadFactoryImplementation () {}


        @Override
        public Thread newThread ( Runnable r ) {
            return new Thread(r, r.getClass().getName());
        }
    }

}
