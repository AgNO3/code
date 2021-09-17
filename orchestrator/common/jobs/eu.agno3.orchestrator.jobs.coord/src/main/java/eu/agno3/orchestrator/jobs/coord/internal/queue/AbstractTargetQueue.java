/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord.internal.queue;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.JobTarget;
import eu.agno3.orchestrator.jobs.coord.InternalQueue;
import eu.agno3.orchestrator.jobs.coord.JobStateTracker;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.msg.JobStateUpdatedEvent;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractTargetQueue implements InternalQueue {

    private static final Logger log = Logger.getLogger(AbstractTargetQueue.class);
    protected final InternalQueue parent;
    protected final JobTarget target;
    private Deque<Job> queued = new LinkedList<>();
    private Set<Job> active = new HashSet<>();
    protected JobStateTracker stateTracker;


    /**
     * @param groupQueue
     * @param target
     * @param jst
     */
    public AbstractTargetQueue ( InternalQueue groupQueue, JobTarget target, JobStateTracker jst ) {
        this.parent = groupQueue;
        this.target = target;
        this.stateTracker = jst;
    }


    /**
     * @return the parent
     */
    public InternalQueue getParent () {
        return this.parent;
    }


    /**
     * @return the target
     */
    public JobTarget getTarget () {
        return this.target;
    }


    /**
     * @return the stateTracker
     */
    public JobStateTracker getStateTracker () {
        return this.stateTracker;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.InternalQueue#isEmpty()
     */
    @Override
    public boolean isEmpty () {
        return this.queued.isEmpty() && this.active.isEmpty();
    }


    @Override
    public void queueJob ( Job j ) throws JobQueueException {
        checkForQueuing(j);
        this.queued.add(j);
        this.stateTracker.updateJobState(j, JobState.QUEUED);
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
        this.queued.remove(j);
        this.active.remove(j);
    }


    protected void checkForQueuing ( Job j ) throws JobQueueException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Queueing job %s on target %s", j, this.target)); //$NON-NLS-1$
        }

        if ( j.getTarget() == null || !j.getTarget().equals(this.getTarget()) ) {
            throw new IllegalArgumentException("Target of queued job does not match this queues target"); //$NON-NLS-1$
        }

        if ( this.queued.contains(j) || this.active.contains(j) ) {
            throw new JobQueueException("Job is already queued"); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.InternalQueue#loadJob(eu.agno3.orchestrator.jobs.Job,
     *      eu.agno3.orchestrator.jobs.JobInfo)
     */
    @Override
    public void loadJob ( Job j, JobInfo js ) {
        log.warn(String.format("Job loading not supported by %s, setting to FAILED %s from %s", this.getClass().getName(), j, js.getState())); //$NON-NLS-1$
        this.endExecution(j, false);
        try {
            this.stateTracker.updateJobState(j, JobState.FAILED);
        }
        catch ( JobQueueException e ) {
            log.warn("Failed to set job to failed", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.InternalQueue#getQueueForJob(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public InternalQueue getQueueForJob ( Job j ) {
        return this;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.InternalQueue#getQueuedJobs()
     */
    @Override
    public List<Job> getQueuedJobs () {
        synchronized ( this.queued ) {
            return new ArrayList<>(this.queued);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.InternalQueue#getJobForExecution()
     */
    @Override
    public Job getJobForExecution () {
        Job j = this.queued.peek();
        if ( log.isDebugEnabled() && j == null ) {
            log.debug(String.format("No job found from active=%s queued=%s", this.active, this.queued)); //$NON-NLS-1$
        }
        return j;
    }


    /**
     * 
     * @param job
     */
    protected void startExecution ( Job job ) {
        synchronized ( this.queued ) {
            synchronized ( this.active ) {
                if ( this.active.contains(job) ) {
                    // already active
                    return;
                }
                if ( !job.equals(this.queued.peek()) ) {
                    throw new IllegalStateException("Tried to execute job while not first in queue " + this.queued); //$NON-NLS-1$
                }
                this.active.add(this.queued.poll());
            }
        }
    }


    /**
     * 
     * @param job
     * @param ok
     */
    protected void endExecution ( Job job, boolean ok ) {
        this.active.remove(job);
        if ( !ok ) {
            this.queued.remove(job);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.InternalQueue#getActiveJobs()
     */
    @Override
    public Collection<Job> getActiveJobs () {
        return Collections.unmodifiableSet(this.active);
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

        if ( job == null || !this.target.equals(job.getTarget()) ) {
            return;
        }

        jobUpdatedInternal(jobInfo, job);
    }


    /**
     * @param jobInfo
     * @param job
     */
    protected void jobUpdatedInternal ( JobInfo jobInfo, Job job ) {
        if ( jobInfo.getState() == JobState.TIMEOUT ) {
            jobUpdateTimeout(job);
        }
        else if ( EnumSet.of(JobState.FINISHED, JobState.FAILED).contains(jobInfo.getState()) ) {
            jobUpdateEnd(jobInfo, job);
        }
        else if ( jobInfo.getState() == JobState.RUNNING || jobInfo.getState() == JobState.RESUMED ) {
            jobUpdateStart(job);
        }
        else if ( jobInfo.getState() == JobState.CANCELLED ) {
            jobUpdateCancelled(jobInfo, job, false);
        }
    }


    /**
     * @param job
     */
    protected void jobUpdateTimeout ( Job job ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Timeout for " + job); //$NON-NLS-1$
        }
        this.queued.remove(job);
    }


    /**
     * @param job
     */
    protected void jobUpdateStart ( Job job ) {
        this.startExecution(job);
    }


    /**
     * @param jobInfo
     * @param job
     */
    protected void jobUpdateEnd ( JobInfo jobInfo, Job job ) {
        if ( log.isDebugEnabled() && jobInfo.getState() == JobState.FINISHED && !this.active.contains(job) ) {
            log.debug("Finished a job that was not marked active " + jobInfo.getState()); //$NON-NLS-1$
        }
        this.endExecution(job, jobInfo.getState() == JobState.FINISHED);
    }


    /**
     * @param jobInfo
     * @param job
     */
    protected void jobUpdateCancelled ( JobInfo jobInfo, Job job ) {
        jobUpdateCancelled(jobInfo, job, true);
    }


    /**
     * @param jobInfo
     * @param job
     * @param checkRemoved
     */
    protected final void jobUpdateCancelled ( JobInfo jobInfo, Job job, boolean checkRemoved ) {
        boolean activeRemoved = this.active.remove(job);
        boolean queuedRemoved = this.queued.remove(job);
        JobState jobState = jobInfo != null ? jobInfo.getState() : null;

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Removed job from active=%s queued=%s state=%s", activeRemoved, queuedRemoved, jobState)); //$NON-NLS-1$
        }

        if ( checkRemoved && !activeRemoved && !queuedRemoved ) {

            throw new IllegalStateException("Cancelled a job that is not queued or active " + jobState); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.InternalQueue#doMaintenance()
     */
    @Override
    public void doMaintenance () {
        List<Job> qd = new LinkedList<>(this.queued);
        List<Job> remove = new LinkedList<>();
        for ( Job j : qd ) {
            if ( j.getDeadline() != null && j.getDeadline().isBeforeNow() ) {
                try {
                    this.stateTracker.updateJobState(j, JobState.TIMEOUT);
                }
                catch ( JobQueueException e ) {
                    log.warn("Failed to set state to TIMEOUT:", e); //$NON-NLS-1$
                }
                finally {
                    if ( log.isDebugEnabled() ) {
                        log.debug("Removing job " + j); //$NON-NLS-1$
                    }
                    remove.add(j);
                }
            }
        }

        this.queued.removeAll(remove);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format(
            "target=%s,queued=%d,active=%d", //$NON-NLS-1$
            this.target,
            this.queued.size(),
            this.active.size());
    }
}