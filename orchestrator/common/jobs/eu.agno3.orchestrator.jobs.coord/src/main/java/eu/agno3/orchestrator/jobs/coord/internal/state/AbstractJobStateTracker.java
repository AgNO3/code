/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.11.2015 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord.internal.state;


import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobGroup;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.JobInfoImpl;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.coord.JobStateMachine;
import eu.agno3.orchestrator.jobs.coord.JobStateTracker;
import eu.agno3.orchestrator.jobs.exceptions.JobIllegalStateException;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.exceptions.JobUnknownException;
import eu.agno3.orchestrator.jobs.msg.JobStateUpdatedEvent;
import eu.agno3.orchestrator.jobs.state.JobStateListener;
import eu.agno3.orchestrator.jobs.state.LocalJobStateListener;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public abstract class AbstractJobStateTracker implements JobStateTracker {

    /**
     * 
     */
    private static final String UNKNOWN_JOB = "Unknown job "; //$NON-NLS-1$


    protected abstract boolean removeJob ( Job j );


    protected abstract Set<Job> listJobs ();


    protected abstract void trackJobState ( Job job, JobInfoImpl newInfo );


    protected abstract JobInfoImpl addNewJob ( Job j, JobState s ) throws JobQueueException;


    protected abstract JobInfoImpl getJobStateOrNull ( Job j );


    @Override
    public abstract Job getJobData ( UUID jobId );


    @Override
    public abstract Collection<Job> getLoadableJobs ();

    protected static final Logger log = Logger.getLogger(AbstractJobStateTracker.class);
    private final JobStateMachine jobSM = new JobStateMachine();
    private final JobStateMachine noEventJobSM = new JobStateMachine();


    /**
     * @param userPrincipal
     * @param j
     * @param s
     * @return
     */
    protected static JobInfoImpl makeNewJobInfo ( UUID jobId, String type, UserPrincipal owner, JobState s ) {
        JobInfoImpl info = new JobInfoImpl();
        info.setJobId(jobId);
        info.setOwner(owner);
        info.setType(type);
        info.setQueuedTime(DateTime.now());
        info.setState(s);
        return info;
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindListener ( LocalJobStateListener listener ) {
        this.jobSM.addListener(listener);
    }


    protected synchronized void unbindListener ( LocalJobStateListener listener ) {
        this.jobSM.removeListener(listener);
    }


    /**
     * @param l
     */
    @Override
    public void addListener ( JobStateListener l ) {
        this.jobSM.addListener(l);
    }


    @Override
    public void removeListener ( JobStateListener l ) {
        this.jobSM.removeListener(l);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.JobStateTracker#getJobState(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public JobInfoImpl getJobState ( Job j ) throws JobUnknownException {
        JobInfoImpl info = getJobStateOrNull(j);

        if ( info == null ) {
            throw new JobUnknownException("Job is unknown"); //$NON-NLS-1$
        }

        return info;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws JobUnknownException
     * 
     * @see eu.agno3.orchestrator.jobs.coord.JobStateTracker#getJobState(java.util.UUID)
     */
    @Override
    public JobInfo getJobState ( UUID jobId ) throws JobUnknownException {
        if ( jobId == null ) {
            return null;
        }

        Job j = getJobData(jobId);

        if ( j == null ) {
            return null;
        }
        return getJobState(j);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws JobUnknownException
     * 
     * @see eu.agno3.orchestrator.jobs.coord.JobStateTracker#getJobStates(java.util.Collection)
     */
    @Override
    public List<JobInfo> getJobStates ( Collection<Job> jobs ) throws JobUnknownException {
        List<JobInfo> res = new LinkedList<>();

        for ( Job j : jobs ) {
            res.add(getJobState(j));
        }

        return res;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws JobQueueException
     * 
     * @see eu.agno3.orchestrator.jobs.coord.JobStateTracker#updateJobState(eu.agno3.orchestrator.jobs.Job,
     *      eu.agno3.orchestrator.jobs.JobState)
     */
    @Override
    public JobInfo updateJobState ( Job j, JobState s ) throws JobQueueException {
        return updateJobState(j, s, false);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.JobStateTracker#updateJobStateExternal(eu.agno3.orchestrator.jobs.Job,
     *      eu.agno3.orchestrator.jobs.JobState)
     */
    @Override
    public JobInfo updateJobStateExternal ( Job j, JobState s ) throws JobQueueException {
        return updateJobState(j, s, true);
    }


    /**
     * @param j
     * @param s
     * @param suppressEvents
     * @return
     * @throws JobQueueException
     * @throws JobUnknownException
     * @throws JobIllegalStateException
     */
    private JobInfo updateJobState ( Job j, JobState s, boolean suppressEvents )
            throws JobQueueException, JobUnknownException, JobIllegalStateException {
        JobInfoImpl info = getJobStateOrNull(j);

        if ( info == null ) {
            if ( s == JobState.NEW ) {
                info = addNewJob(j, s);
            }
            else {
                throw new JobUnknownException("Job is not new but unknown " + j); //$NON-NLS-1$
            }
        }
        else if ( info.getState() == s ) {
            return info;
        }

        return this.updateStateInternal(s, !suppressEvents ? this.jobSM : this.noEventJobSM, j, info);
    }


    protected JobInfo updateStateInternal ( JobState state, JobStateMachine sm, Job job, JobInfoImpl info ) throws JobIllegalStateException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Updating job state to %s: %s", state, job)); //$NON-NLS-1$
        }

        JobInfoImpl newInfo = new JobInfoImpl(sm.applyState(info, state));
        newInfo.setLastKeepAliveTime(DateTime.now());
        trackJobState(job, newInfo);
        return newInfo;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.JobStateTracker#handleEvent(eu.agno3.orchestrator.jobs.msg.JobStateUpdatedEvent)
     */
    @Override
    public synchronized void handleEvent ( JobStateUpdatedEvent ev ) {
        UUID jobId = ev.getJobInfo().getJobId();
        Job job = this.getJobData(jobId);

        if ( job == null ) {
            log.warn(UNKNOWN_JOB + jobId);
            return;
        }

        JobInfoImpl info;
        try {
            info = getJobState(job);
        }
        catch ( JobUnknownException e1 ) {
            log.warn(UNKNOWN_JOB + job, e1);
            return;
        }

        if ( info.getState().equals(ev.getJobInfo().getState()) ) {
            return;
        }

        try {
            updateStateInternal(ev.getJobInfo().getState(), this.noEventJobSM, job, info);
        }
        catch ( Exception e ) {
            log.debug("Failed to update job state from event:", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     */
    public AbstractJobStateTracker () {
        super();
    }


    /**
     * {@inheritDoc}
     * 
     * @throws JobQueueException
     * 
     * @see eu.agno3.orchestrator.jobs.coord.JobStateTracker#getAllJobInfo(eu.agno3.orchestrator.jobs.JobGroup)
     */
    @Override
    public Collection<JobInfo> getAllJobInfo ( JobGroup g ) throws JobQueueException {
        Set<Job> inGroup = new HashSet<>();

        for ( Job j : listJobs() ) {
            if ( g.equals(j.getJobGroup()) ) {
                inGroup.add(j);
            }
        }

        try {
            return this.getJobStates(inGroup);
        }
        catch ( JobUnknownException e ) {
            throw new JobQueueException("Job was removed while enumerating jobs:", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.JobStateTracker#clearFinishedJobs()
     */
    @Override
    public int clearFinishedJobs () {
        List<JobInfo> cleared = new ArrayList<>();

        for ( Job j : listJobs() ) {
            JobInfo i;
            try {
                i = getJobState(j);
                if ( EnumSet.of(JobState.CANCELLED, JobState.FINISHED, JobState.FAILED, JobState.TIMEOUT).contains(i.getState()) ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug("Removing already finished job " + j.getJobId()); //$NON-NLS-1$
                    }
                    if ( removeJob(j) ) {
                        cleared.add(i);
                    }
                }
            }
            catch ( JobUnknownException e ) {
                log.warn("Job state unknown for job while removing", e); //$NON-NLS-1$
                removeJob(j);
            }

        }

        return cleared.size();
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.coord.JobStateTracker#doKeepAlive(java.util.UUID)
     */
    @Override
    public void doKeepAlive ( UUID jobId ) {
        try {
            JobInfoImpl info = (JobInfoImpl) this.getJobState(jobId);
            if ( info != null ) {
                info.setLastKeepAliveTime(DateTime.now());
            }
        }
        catch ( JobUnknownException e ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Tried to keep alive non-existant job " + jobId, e); //$NON-NLS-1$
            }
        }
    }

}