/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.04.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord.internal;


import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobCoordinator;
import eu.agno3.orchestrator.jobs.JobGroup;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.coord.Executor;
import eu.agno3.orchestrator.jobs.coord.ExecutorFactory;
import eu.agno3.orchestrator.jobs.coord.InternalQueue;
import eu.agno3.orchestrator.jobs.coord.JobForExecutionProvider;
import eu.agno3.orchestrator.jobs.coord.JobStateTracker;
import eu.agno3.orchestrator.jobs.coord.QueueFactory;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.exceptions.JobUnknownException;
import eu.agno3.orchestrator.jobs.msg.JobKeepAliveEvent;
import eu.agno3.orchestrator.jobs.msg.JobStateUpdatedEvent;
import eu.agno3.orchestrator.jobs.state.JobStateListener;
import eu.agno3.orchestrator.jobs.state.JobStateObservable;


/**
 * @author mbechler
 * 
 */
public class JobCoordinatorImpl implements JobCoordinator, JobStateListener, JobForExecutionProvider, JobStateObservable {

    private static final Logger log = Logger.getLogger(JobCoordinatorImpl.class);
    private static final long STARTUP_TIMEOUT = 30000;
    private Set<JobGroup> knownJobGroups = new HashSet<>();
    private Map<JobGroup, InternalQueue> groupQueues = new HashMap<>();
    private QueueFactory queueFactory;
    private JobStateTracker jobStateTracker;
    private Executor executor;
    private ExecutorFactory executorFactory;
    private boolean initialized;

    private long initStarted = System.currentTimeMillis();

    private Queue<Job> initializationQueue;


    /**
     * 
     */
    public JobCoordinatorImpl () {}


    /**
     * @param stateTracker
     * 
     */
    public JobCoordinatorImpl ( JobStateTracker stateTracker ) {
        setJobStateTracker(stateTracker);
    }


    /**
     * @param queueFactory
     * @param execFactory
     * @param stateTracker
     */
    public JobCoordinatorImpl ( QueueFactory queueFactory, ExecutorFactory execFactory, JobStateTracker stateTracker ) {
        this(stateTracker);
        this.queueFactory = queueFactory;
        this.executorFactory = execFactory;
    }


    protected final synchronized void setJobStateTracker ( JobStateTracker jst ) {
        this.jobStateTracker = jst;
        this.jobStateTracker.addListener(this);
    }


    protected final synchronized void unsetJobStateTracker ( JobStateTracker jst ) {
        if ( this.jobStateTracker == jst ) {
            jst.removeListener(this);
            this.jobStateTracker = null;
        }
    }


    protected synchronized void addJobGroup ( JobGroup g ) {
        this.knownJobGroups.add(g);
    }


    protected synchronized void removeJobGroup ( JobGroup g ) {
        this.knownJobGroups.remove(g);
    }


    /**
     * @param queueFactory
     *            the queueFactory to set
     */
    protected synchronized void setQueueFactory ( QueueFactory queueFactory ) {
        this.queueFactory = queueFactory;
    }


    protected synchronized void unsetQueueFactory ( QueueFactory qf ) {
        if ( this.queueFactory == qf ) {
            this.queueFactory = null;
        }
    }


    /**
     * 
     */
    public void shutdown () {
        this.executor.shutdown();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.JobCoordinator#needsMaintenance()
     */
    @Override
    public boolean needsMaintenance () {

        if ( !this.initialized ) {
            return true;
        }

        for ( InternalQueue q : this.groupQueues.values() ) {
            if ( !q.isEmpty() ) {
                return true;
            }
        }
        return false;
    }


    /**
     * @param executor
     *            the executor to set
     */
    protected synchronized void setExecutorFactory ( ExecutorFactory ef ) {
        this.executorFactory = ef;
        this.executor = null;
    }


    protected synchronized void unsetExecutorFactory ( ExecutorFactory ef ) {
        if ( this.executorFactory == ef ) {
            this.executorFactory = null;
            this.executor = null;
        }
    }


    /**
     * @return the jobStateTracker
     */
    public synchronized JobStateTracker getJobStateTracker () {
        return this.jobStateTracker;
    }


    protected synchronized void init () {
        if ( this.initialized ) {
            return;
        }

        this.jobStateTracker.clearFinishedJobs();

        log.debug("Initializing job coordinator"); //$NON-NLS-1$
        this.initializationQueue = new ConcurrentLinkedDeque<>(this.jobStateTracker.getLoadableJobs());

        while ( !this.initializationQueue.isEmpty() ) {
            Job j = this.initializationQueue.peek();

            if ( this.queueFactory.isLocal(j.getTarget()) && !this.executorFactory.canRun(j) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Queued for execution but not yet runnable " + j); //$NON-NLS-1$
                }

                if ( System.currentTimeMillis() - this.initStarted > STARTUP_TIMEOUT ) {
                    log.warn("Timeout waiting for job, dropping"); //$NON-NLS-1$
                    this.initializationQueue.poll();
                    continue;
                }

                return;
            }

            j = this.initializationQueue.poll();
            try {
                InternalQueue r = this.getOrCreateGroupQueue(j.getJobGroup());

                JobInfo js = null;
                try {
                    js = this.jobStateTracker.getJobState(j);
                }
                catch ( JobQueueException e ) {
                    log.debug("Job not found, good", e); //$NON-NLS-1$
                }

                if ( js == null || EnumSet.of(JobState.NEW, JobState.QUEUED, JobState.SUSPENDED).contains(js.getState()) ) {
                    r.queueJob(j);
                }
                else {
                    r.loadJob(j, js);
                }

            }
            catch ( JobQueueException e ) {
                log.warn("Failed to queue loadable job", e); //$NON-NLS-1$
            }

        }

        this.initialized = this.initializationQueue == null || this.initializationQueue.isEmpty();
    }


    /**
     * Do queue and executor maintenance
     * 
     * @throws JobQueueException
     */
    @Override
    public void run () throws JobQueueException {

        if ( !this.initialized ) {
            init();
        }

        if ( !this.initialized ) {
            return;
        }

        if ( log.isTraceEnabled() ) {
            log.trace("Active queues " + this.groupQueues.values()); //$NON-NLS-1$
        }
        for ( InternalQueue q : this.groupQueues.values() ) {
            q.doMaintenance();
        }
        getExecutor().run();
    }


    private Executor getExecutor () {
        if ( this.executor == null ) {
            this.executor = this.executorFactory.makeExecutor(this, this.jobStateTracker);
        }
        return this.executor;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.state.JobStateListener#jobUpdated(eu.agno3.orchestrator.jobs.msg.JobStateUpdatedEvent)
     */
    @Override
    public void jobUpdated ( JobStateUpdatedEvent ev ) {
        Job j = this.jobStateTracker.getJobData(ev.getJobInfo().getJobId());

        if ( j != null ) {
            this.getOrCreateGroupQueue(j.getJobGroup()).jobUpdated(ev);
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.state.JobStateListener#jobKeepalive(eu.agno3.orchestrator.jobs.Job,
     *      eu.agno3.orchestrator.jobs.msg.JobKeepAliveEvent)
     */
    @Override
    public void jobKeepalive ( Job j, JobKeepAliveEvent ev ) {
        this.getOrCreateGroupQueue(j.getJobGroup()).jobKeepalive(j, ev);
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws JobQueueException
     * 
     * @see eu.agno3.orchestrator.jobs.JobCoordinator#queueJob(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public JobInfo queueJob ( Job j ) throws JobQueueException {
        Objects.requireNonNull(j);
        Objects.requireNonNull(j.getJobGroup());
        Objects.requireNonNull(j.getTarget());
        this.jobStateTracker.updateJobState(j, JobState.NEW);
        if ( this.initialized || this.initializationQueue == null ) {
            getQueueForJob(j).queueJob(j);
        }
        else {
            this.initializationQueue.add(j);
        }
        return this.jobStateTracker.getJobState(j);
    }


    @Override
    public JobInfo cancelJob ( UUID jobId ) throws JobQueueException {
        Job j = this.jobStateTracker.getJobData(jobId);

        if ( j == null ) {
            throw new JobUnknownException("Job data not available"); //$NON-NLS-1$
        }

        this.getOrCreateGroupQueue(j.getJobGroup()).cancelJob(j);
        getExecutor().cancel(j);
        return this.jobStateTracker.getJobState(j);

    }


    @Override
    public JobInfo cancelJob ( Job j ) throws JobQueueException {
        this.getOrCreateGroupQueue(j.getJobGroup()).cancelJob(j);
        getExecutor().cancel(j);
        return this.jobStateTracker.getJobState(j);

    }


    /**
     * {@inheritDoc}
     * 
     * @throws JobUnknownException
     * 
     * @see eu.agno3.orchestrator.jobs.JobCoordinator#getJobInfo(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public JobInfo getJobInfo ( Job job ) throws JobUnknownException {
        return this.jobStateTracker.getJobState(job);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws JobUnknownException
     * 
     * @see eu.agno3.orchestrator.jobs.JobCoordinator#getJobInfo(java.util.UUID)
     */
    @Override
    public JobInfo getJobInfo ( UUID jobId ) throws JobUnknownException {
        return this.jobStateTracker.getJobState(jobId);
    }


    @Override
    public Job getJobData ( UUID jobId ) throws JobUnknownException {
        Job j = this.jobStateTracker.getJobData(jobId);

        if ( j == null ) {
            throw new JobUnknownException("Job data unavailable"); //$NON-NLS-1$
        }

        return j;
    }


    private InternalQueue getQueueForJob ( Job j ) {
        JobGroup g = j.getJobGroup();
        return this.getOrCreateGroupQueue(g);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.JobForExecutionProvider#getJobForExecution(eu.agno3.orchestrator.jobs.JobGroup)
     */
    @Override
    public Job getJobForExecution ( JobGroup g ) {
        return this.getOrCreateGroupQueue(g).getJobForExecution();
    }


    /**
     * @param g
     * @return
     */
    private InternalQueue getOrCreateGroupQueue ( JobGroup g ) {
        if ( g == null ) {
            throw new IllegalArgumentException("Group may not be null"); //$NON-NLS-1$
        }
        synchronized ( this.groupQueues ) {
            if ( !this.groupQueues.containsKey(g) ) {
                this.groupQueues.put(g, this.queueFactory.createForGroup(g, this.jobStateTracker));
            }
        }
        return this.groupQueues.get(g);
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws JobQueueException
     * 
     * @see eu.agno3.orchestrator.jobs.JobCoordinator#getAllJobs(eu.agno3.orchestrator.jobs.JobGroup)
     */
    @Override
    public Collection<JobInfo> getAllJobs ( JobGroup g ) throws JobQueueException {
        return this.jobStateTracker.getAllJobInfo(g);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws JobQueueException
     * 
     * @see eu.agno3.orchestrator.jobs.JobCoordinator#getQueuedJobs(eu.agno3.orchestrator.jobs.JobGroup)
     */
    @Override
    public Collection<JobInfo> getQueuedJobs ( JobGroup g ) throws JobQueueException {
        List<Job> jobs = this.getOrCreateGroupQueue(g).getQueuedJobs();
        return this.getJobStateInfo(jobs);
    }


    private Collection<JobInfo> getJobStateInfo ( Collection<Job> jobs ) throws JobQueueException {
        try {
            return this.jobStateTracker.getJobStates(jobs);
        }
        catch ( JobQueueException e ) {
            throw new JobQueueException("Job was removed while enumerating jobs (is queued but no info found)", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws JobQueueException
     * 
     * @see eu.agno3.orchestrator.jobs.JobCoordinator#getActiveJobs(eu.agno3.orchestrator.jobs.JobGroup)
     */
    @Override
    public Collection<JobInfo> getActiveJobs ( JobGroup g ) throws JobQueueException {
        Collection<Job> jobs = this.getOrCreateGroupQueue(g).getActiveJobs();
        return this.getJobStateInfo(jobs);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.JobCoordinator#getKnownGroups()
     */
    @Override
    public Set<JobGroup> getKnownGroups () {
        Set<JobGroup> groups = new HashSet<>(this.knownJobGroups);
        groups.addAll(this.groupQueues.keySet());
        return groups;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.JobCoordinator#clearFinishedJobs()
     */
    @Override
    public int clearFinishedJobs () {
        return this.jobStateTracker.clearFinishedJobs();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.JobCoordinator#disableLocalExecution()
     */
    @Override
    public void disableLocalExecution () {
        getExecutor().disableExecution();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.JobCoordinator#enableLocalExecution()
     */
    @Override
    public void enableLocalExecution () {
        getExecutor().enableExecution();
    }


    /**
     * {@inheritDoc}
     * 
     * @throws JobUnknownException
     * 
     * @see eu.agno3.orchestrator.jobs.JobCoordinator#notifyExternalKeepAlive(java.util.UUID)
     */
    @Override
    public void notifyExternalKeepAlive ( UUID jobId ) throws JobUnknownException {
        this.getJobStateTracker().doKeepAlive(jobId);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.state.JobStateObservable#registerStateListener(eu.agno3.orchestrator.jobs.state.JobStateListener)
     */
    @Override
    public void registerStateListener ( JobStateListener l ) {
        this.jobStateTracker.addListener(l);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.state.JobStateObservable#unregisterStateListener(eu.agno3.orchestrator.jobs.state.JobStateListener)
     */
    @Override
    public void unregisterStateListener ( JobStateListener l ) {
        this.jobStateTracker.removeListener(l);
    }

}
