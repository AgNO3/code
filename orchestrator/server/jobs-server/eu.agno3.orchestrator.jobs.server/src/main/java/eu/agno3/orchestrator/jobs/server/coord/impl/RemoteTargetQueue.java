/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.server.coord.impl;


import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.JobTarget;
import eu.agno3.orchestrator.jobs.coord.InternalQueue;
import eu.agno3.orchestrator.jobs.coord.JobStateTracker;
import eu.agno3.orchestrator.jobs.coord.internal.queue.AbstractTargetQueue;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.msg.JobKeepAliveEvent;
import eu.agno3.orchestrator.jobs.msg.JobStateUpdatedEvent;


/**
 * @author mbechler
 * 
 */
public class RemoteTargetQueue extends AbstractTargetQueue {

    private static final Logger log = Logger.getLogger(RemoteTargetQueue.class);

    /**
     * 
     */
    private static final int KEEPALIVE_TIMEOUT = 10;
    private Map<UUID, DateTime> keepAlives = new HashMap<>();
    private RemoteQueueClient remoteQueueClient;


    /**
     * @param groupQueue
     * @param target
     * @param jst
     * @param remoteClient
     */
    public RemoteTargetQueue ( InternalQueue groupQueue, JobTarget target, JobStateTracker jst, RemoteQueueClient remoteClient ) {
        super(groupQueue, target, jst);
        this.remoteQueueClient = remoteClient;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.internal.queue.AbstractTargetQueue#getJobForExecution()
     */
    @Override
    public Job getJobForExecution () {
        return null;
    }


    private void keepAlive ( UUID uuid ) {
        this.keepAlives.put(uuid, DateTime.now());
    }


    /**
     * {@inheritDoc}
     * 
     * @throws JobQueueException
     * 
     * @see eu.agno3.orchestrator.jobs.coord.internal.queue.AbstractTargetQueue#queueJob(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public void queueJob ( Job j ) throws JobQueueException {
        super.queueJob(j);
        this.keepAlive(j.getJobId());
        this.submitToRemote(j);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.internal.queue.AbstractTargetQueue#loadJob(eu.agno3.orchestrator.jobs.Job,
     *      eu.agno3.orchestrator.jobs.JobInfo)
     */
    @Override
    public void loadJob ( Job j, JobInfo js ) {
        JobInfo ji = this.remoteQueueClient.tryGetJobInfo(j);
        if ( ji != null && ji.getState() != JobState.UNKNOWN ) {
            log.debug("Job already exists on target " + ji.getState()); //$NON-NLS-1$
            this.jobUpdatedInternal(ji, j);
            try {
                this.stateTracker.updateJobState(j, ji.getState());
            }
            catch ( JobQueueException e ) {
                log.warn("Failed to set job state from remote", e); //$NON-NLS-1$
            }
            return;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws JobQueueException
     * 
     * @see eu.agno3.orchestrator.jobs.coord.internal.queue.AbstractTargetQueue#cancelJob(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public void cancelJob ( Job j ) throws JobQueueException {
        if ( !this.remoteQueueClient.tryCancelJob(j) ) {
            this.getStateTracker().updateJobState(j, JobState.UNKNOWN);
            return;
        }
        super.cancelJob(j);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.internal.queue.AbstractTargetQueue#endExecution(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    protected void endExecution ( Job job, boolean ok ) {
        try {
            super.endExecution(job, ok);
        }
        catch ( IllegalStateException e ) {
            log.warn("Remote job removed that was not known", e); //$NON-NLS-1$
        }
    }


    /**
     * @param j
     * @throws JobQueueException
     */
    protected void submitToRemote ( Job j ) throws JobQueueException {
        if ( log.isDebugEnabled() ) {
            log.debug("Submitting JOB to remote queue " + j); //$NON-NLS-1$
        }

        if ( !this.remoteQueueClient.tryQueueJob(j) ) {
            this.getStateTracker().updateJobState(j, JobState.STALLED);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.internal.queue.AbstractTargetQueue#jobUpdated(eu.agno3.orchestrator.jobs.msg.JobStateUpdatedEvent)
     */
    @Override
    public void jobUpdated ( JobStateUpdatedEvent ev ) {
        if ( !EnumSet.of(JobState.NEW, JobState.TIMEOUT).contains(ev.getJobInfo().getState()) ) {
            this.keepAlive(ev.getJobId());

            if ( !EnumSet.of(JobState.QUEUED, JobState.RUNNABLE).contains(ev.getJobInfo().getState()) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Remote job event " + ev); //$NON-NLS-1$
                }
                this.getStateTracker().handleEvent(ev);
            }
        }

        try {
            super.jobUpdated(ev);
        }
        catch ( IllegalStateException e ) {
            log.error("Illegal job state " + ev, e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.internal.queue.AbstractTargetQueue#jobUpdateCancelled(eu.agno3.orchestrator.jobs.JobInfo,
     *      eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    protected void jobUpdateCancelled ( JobInfo jobInfo, Job job ) {
        super.jobUpdateCancelled(jobInfo, job, false);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.internal.queue.AbstractTargetQueue#doMaintenance()
     */
    @Override
    public void doMaintenance () {
        super.doMaintenance();

        // check for keep alives
        DateTime timeout = DateTime.now().minusSeconds(KEEPALIVE_TIMEOUT);
        Set<UUID> removeKeepalive = new LinkedHashSet<>();
        for ( Entry<UUID, DateTime> e : this.keepAlives.entrySet() ) {
            Job j = this.stateTracker.getJobData(e.getKey());

            if ( j == null || !this.getQueuedJobs().contains(j) ) {
                if ( this.getActiveJobs().contains(j) ) {
                    DateTime lastKeepalive = e.getValue();
                    if ( lastKeepalive.isBefore(timeout) ) {
                        activeCheckJob(j);
                    }
                    return;
                }
                removeKeepalive.add(e.getKey());
                continue;
            }

            DateTime lastKeepalive = e.getValue();

            if ( lastKeepalive.isBefore(timeout) ) {
                activeCheckJob(j);
                doKeepalive(j);
            }
        }

        for ( UUID j : removeKeepalive ) {
            this.keepAlives.remove(j);
        }
    }


    /**
     * @param j
     */
    private void activeCheckJob ( Job j ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Active checking job " + j.getJobId()); //$NON-NLS-1$
        }
        try {
            JobInfo ji = this.remoteQueueClient.tryGetJobInfo(j);
            if ( ji != null ) {
                if ( ji.getState() == JobState.UNKNOWN ) {
                    log.debug("Job is unknown"); //$NON-NLS-1$
                    this.jobUpdateCancelled(ji, j, false);
                }
                else {
                    JobInfo js = this.stateTracker.getJobState(j);
                    if ( log.isDebugEnabled() ) {
                        log.debug("Job state is " + ji.getState()); //$NON-NLS-1$
                    }
                    if ( js.getState() != ji.getState() ) {
                        log.debug("Setting state to " + ji.getState()); //$NON-NLS-1$
                        this.stateTracker.updateJobStateExternal(j, ji.getState());
                    }
                }
            }
            else {
                this.stateTracker.updateJobStateExternal(j, JobState.UNKNOWN);
            }
        }
        catch ( JobQueueException e1 ) {
            log.warn("Failed to update state for " + j.getJobId(), e1); //$NON-NLS-1$
        }
    }


    private void doKeepalive ( Job j ) {
        this.keepAlive(j.getJobId());

        try {
            JobInfo jobInfo = this.getStateTracker().getJobState(j);
            if ( EnumSet.of(JobState.RUNNABLE, JobState.RUNNING, JobState.UNKNOWN).contains(jobInfo.getState()) ) {
                this.getStateTracker().updateJobState(j, JobState.UNKNOWN);
            }
            else if ( jobInfo.getState() == JobState.SUSPENDED ) {
                // ignore
            }
            else if ( jobInfo.getState() != JobState.FAILED ) {
                this.getStateTracker().updateJobState(j, JobState.STALLED);
            }
        }
        catch ( JobQueueException ex ) {
            log.warn("Failed to update job state:", ex); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.state.JobStateListener#jobKeepalive(eu.agno3.orchestrator.jobs.Job,
     *      eu.agno3.orchestrator.jobs.msg.JobKeepAliveEvent)
     */
    @Override
    public void jobKeepalive ( Job job, JobKeepAliveEvent ev ) {
        this.keepAlive(job.getJobId());
    }
}
