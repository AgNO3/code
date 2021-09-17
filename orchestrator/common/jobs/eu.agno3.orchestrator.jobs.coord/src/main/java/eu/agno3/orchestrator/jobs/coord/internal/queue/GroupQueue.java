/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.04.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord.internal.queue;


import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobGroup;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.JobTarget;
import eu.agno3.orchestrator.jobs.coord.InternalQueue;
import eu.agno3.orchestrator.jobs.coord.JobStateTracker;
import eu.agno3.orchestrator.jobs.coord.QueueFactory;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.exceptions.JobUnknownException;
import eu.agno3.orchestrator.jobs.msg.JobKeepAliveEvent;
import eu.agno3.orchestrator.jobs.msg.JobStateUpdatedEvent;


/**
 * 
 * 
 * Handles global serialization and global conflicts.
 * 
 * Lock order: preQueued < queued < delegates
 * 
 * @author mbechler
 * 
 */
public class GroupQueue implements InternalQueue {

    private static final Logger log = Logger.getLogger(GroupQueue.class);

    private JobGroup group;

    private final Map<JobTarget, InternalQueue> delegates = new HashMap<>();
    private final Deque<Job> preQueued = new LinkedList<>();
    private final Deque<Job> queued = new LinkedList<>();

    private QueueFactory queueFactory;

    private JobStateTracker stateTracker;


    /**
     * @param g
     * @param qf
     * @param jst
     */
    public GroupQueue ( JobGroup g, QueueFactory qf, JobStateTracker jst ) {
        this.group = g;
        this.queueFactory = qf;
        this.stateTracker = jst;
    }


    /**
     * @return the group
     */
    public JobGroup getGroup () {
        return this.group;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.InternalQueue#isEmpty()
     */
    @Override
    public boolean isEmpty () {
        if ( !this.preQueued.isEmpty() || !this.queued.isEmpty() ) {
            return false;
        }

        synchronized ( this.delegates ) {
            for ( InternalQueue internalQueue : this.delegates.values() ) {
                if ( !internalQueue.isEmpty() ) {
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public void queueJob ( Job j ) throws JobQueueException {
        if ( !j.getJobGroup().equals(this.getGroup()) ) {
            throw new IllegalArgumentException(String.format(
                "Job group %s does not match the queue's %s", //$NON-NLS-1$
                j.getJobGroup(),
                this.getGroup()));
        }

        if ( this.getQueuedJobs().contains(j) || this.getActiveJobs().contains(j) ) {
            log.debug("Job is already queued " + j); //$NON-NLS-1$
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Adding job to pre-queue: " + j); //$NON-NLS-1$
        }

        this.preQueued.add(j);
        this.stateTracker.updateJobState(j, JobState.QUEUED);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.InternalQueue#loadJob(eu.agno3.orchestrator.jobs.Job,
     *      eu.agno3.orchestrator.jobs.JobInfo)
     */
    @Override
    public void loadJob ( Job j, JobInfo js ) {
        this.getQueueForJob(j).loadJob(j, js);
    }


    /**
     * 
     * 
     * Retrieve a job from the pre-queue that do not conflict with currently
     * queued or active jobs and submit to the approriate delegate coordinator
     * 
     * @return the newly queued job
     */
    public Job processEligibleJobs () {
        synchronized ( this.preQueued ) {
            Job peek = this.preQueued.peek();

            if ( peek == null ) {
                return null;
            }

            synchronized ( this.delegates ) {
                Collection<InternalQueue> checkDelegates = getDelegatesForConflictCheck(peek);

                if ( log.isDebugEnabled() ) {
                    log.debug("Checking conflicts in queues " + checkDelegates); //$NON-NLS-1$
                }

                for ( InternalQueue delegate : checkDelegates ) {
                    if ( checkForConflict(peek, delegate) ) {
                        log.debug("Conflicting job found"); //$NON-NLS-1$
                        return null;
                    }
                }

            }

            return queueEligibleJob();
        }
    }


    private boolean checkForConflict ( Job peek, InternalQueue delegate ) {
        if ( checkQueuedForConflict(peek, delegate) ) {
            return true;
        }

        if ( checkActiveForConflict(peek, delegate) ) {
            return true;
        }

        return false;
    }


    /**
     * @param peek
     * @param delegate
     * @return
     */
    private boolean checkActiveForConflict ( Job peek, InternalQueue delegate ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Active jobs " + delegate.getActiveJobs()); //$NON-NLS-1$
        }
        Set<Job> toCancel = new HashSet<>();
        for ( Job q : delegate.getActiveJobs() ) {
            if ( this.group.conflicts(peek, q) ) {

                if ( !checkAlive(peek) ) {
                    toCancel.add(peek);
                    continue;
                }

                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Active conflicts %s <-> %s", peek, q)); //$NON-NLS-1$
                }
                return true;
            }
        }
        cancelAll(delegate, toCancel);
        return false;
    }


    /**
     * @param delegate
     * @param toCancel
     */
    private static void cancelAll ( InternalQueue delegate, Set<Job> toCancel ) {
        for ( Job cancel : toCancel ) {
            try {
                delegate.cancelJob(cancel);
            }
            catch ( JobQueueException e ) {
                log.warn("Failed to cancel stale job", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param peek
     * @param delegate
     */
    private boolean checkQueuedForConflict ( Job peek, InternalQueue delegate ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Queued jobs " + delegate.getActiveJobs()); //$NON-NLS-1$
        }

        Set<Job> toCancel = new HashSet<>();
        for ( Job q : delegate.getQueuedJobs() ) {
            if ( this.group.conflicts(peek, q) ) {

                if ( !checkAlive(peek) ) {
                    toCancel.add(peek);
                    continue;
                }

                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Queued conflicts %s <-> %s", peek, q)); //$NON-NLS-1$
                }
                return true;
            }
        }
        cancelAll(delegate, toCancel);
        return false;
    }


    /**
     * @param peek
     */
    private boolean checkAlive ( Job peek ) {
        try {
            JobInfo jobState = this.stateTracker.getJobState(peek);
            if ( jobState == null
                    || !EnumSet.of(JobState.QUEUED, JobState.RUNNABLE, JobState.RUNNING, JobState.SUSPENDED).contains(jobState.getState()) ) {
                return false;
            }
        }
        catch ( JobUnknownException e ) {
            log.debug("Job not found", e); //$NON-NLS-1$
            return false;
        }

        return true;
    }


    private Job queueEligibleJob () {
        synchronized ( this.preQueued ) {
            synchronized ( this.queued ) {
                Job j = this.preQueued.poll();
                if ( log.isDebugEnabled() ) {
                    log.debug("Queuing job " + j); //$NON-NLS-1$
                }
                try {
                    InternalQueue targetQueue = getOrCreateDelegateQueue(j);
                    targetQueue.queueJob(j);
                    this.queued.add(j);
                    return j;
                }
                catch ( Exception e ) {
                    log.warn("Failed to queue job:", e); //$NON-NLS-1$
                    this.queued.remove(j);
                    this.preQueued.remove(j);
                    return null;
                }
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws JobQueueException
     * 
     * @see eu.agno3.orchestrator.jobs.coord.InternalQueue#cancelJob(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public void cancelJob ( Job j ) throws JobQueueException {
        this.getOrCreateDelegateQueue(j).cancelJob(j);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.InternalQueue#doMaintenance()
     */
    @Override
    public void doMaintenance () {
        this.processEligibleJobs();

        synchronized ( this.delegates ) {
            for ( InternalQueue delegate : this.delegates.values() ) {
                delegate.doMaintenance();
            }
        }
    }


    private Collection<InternalQueue> getDelegatesForConflictCheck ( Job peek ) {
        Collection<InternalQueue> checkDelegates;
        synchronized ( this.delegates ) {
            if ( this.group.isCheckGlobalConflicts() ) {
                checkDelegates = this.delegates.values();
            }
            else {
                checkDelegates = Arrays.asList(this.getOrCreateDelegateQueue(peek));
            }
        }
        return checkDelegates;
    }


    private InternalQueue getOrCreateDelegateQueue ( Job j ) {
        JobTarget target = j.getTarget();
        if ( target == null ) {
            throw new IllegalArgumentException("target may not be null"); //$NON-NLS-1$
        }
        synchronized ( this.delegates ) {
            if ( !this.delegates.containsKey(target) ) {
                this.delegates.put(target, this.queueFactory.createTargetQueue(this, target, this.stateTracker));
            }
            return this.delegates.get(target);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.InternalQueue#getQueueForJob(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public InternalQueue getQueueForJob ( Job j ) {
        return this.getOrCreateDelegateQueue(j);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.InternalQueue#getQueuedJobs()
     */
    @Override
    public List<Job> getQueuedJobs () {
        List<Job> jobs = new LinkedList<>();

        synchronized ( this.preQueued ) {
            jobs.addAll(this.queued);
            jobs.addAll(this.preQueued);
        }

        return jobs;
    }


    @Override
    public Job getJobForExecution () {
        synchronized ( this.queued ) {
            Job q = this.queued.peek();

            if ( q == null ) {
                q = processEligibleJobs();
            }

            if ( q == null ) {
                log.trace("Queue is empty " + this.group.getId()); //$NON-NLS-1$
                return null;
            }

            InternalQueue delegateQueue = getOrCreateDelegateQueue(q);
            Job d = delegateQueue.getJobForExecution();

            if ( d != null && !q.equals(d) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Got: " + d); //$NON-NLS-1$
                    log.debug("Expected: " + q); //$NON-NLS-1$
                }

                log.warn("Job returned by delegate does not match expected job, cancelling unexpected"); //$NON-NLS-1$

                try {
                    delegateQueue.cancelJob(d);
                }
                catch ( JobQueueException e ) {
                    log.warn("Failed to cancel unexpected job", e); //$NON-NLS-1$
                }
            }

            if ( d == null ) {
                // a remote target wont return any jobs for execution
                // still, have to wait until finished
                log.trace("Delegate returned null " + delegateQueue); //$NON-NLS-1$
                return null;
            }

            return q;
        }

    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.InternalQueue#getActiveJobs()
     */
    @Override
    public Collection<Job> getActiveJobs () {
        Set<Job> active = new HashSet<>();
        synchronized ( this.delegates ) {
            for ( InternalQueue delegate : this.delegates.values() ) {
                active.addAll(delegate.getActiveJobs());
            }
        }
        return active;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.state.JobStateListener#jobUpdated(eu.agno3.orchestrator.jobs.msg.JobStateUpdatedEvent)
     */
    @Override
    public void jobUpdated ( JobStateUpdatedEvent ev ) {
        JobInfo jobInfo = ev.getJobInfo();
        Job job = this.stateTracker.getJobData(jobInfo.getJobId());
        if ( job == null || !job.getJobGroup().equals(this.group) ) {
            return;
        }

        InternalQueue delegate = this.getOrCreateDelegateQueue(job);
        if ( jobInfo.getState() == JobState.RUNNING ) {
            try {
                jobUpdatedRunning(job);
            }
            catch ( JobQueueException e ) {
                log.warn("Failed to set job to RUNNING", e); //$NON-NLS-1$
            }
        }
        else if ( EnumSet.of(JobState.CANCELLED, JobState.FAILED, JobState.SUSPENDED, JobState.FINISHED).contains(jobInfo.getState()) ) {
            synchronized ( this.preQueued ) {

                if ( this.preQueued.remove(job) ) {
                    return;
                }

                synchronized ( this.queued ) {
                    this.queued.remove(job);
                }
            }
        }

        delegate.jobUpdated(ev);
    }


    /**
     * @param job
     * @throws JobQueueException
     */
    protected void jobUpdatedRunning ( Job job ) throws JobQueueException {
        synchronized ( this.queued ) {
            this.processEligibleJobs();
            Job peek = this.queued.peek();
            if ( !job.equals(peek) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Job queue currently is " + this.queued); //$NON-NLS-1$
                }

                if ( peek == null ) {
                    log.debug("Recieved update for gone job " + job); //$NON-NLS-1$
                    return;
                }

                throw new JobQueueException(String.format("Tried to execute job %s while not first in queue (%s)", job, peek)); //$NON-NLS-1$
            }
            this.queued.poll();
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
        if ( !j.getJobGroup().equals(this.group) ) {
            return;
        }
        InternalQueue delegate = this.getOrCreateDelegateQueue(j);
        delegate.jobKeepalive(j, ev);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format(
            "GroupQueue[%s,preQueued=%d,queued=%d,delegates=%s]", //$NON-NLS-1$
            this.group,
            this.preQueued.size(),
            this.queued.size(),
            this.delegates);
    }
}
