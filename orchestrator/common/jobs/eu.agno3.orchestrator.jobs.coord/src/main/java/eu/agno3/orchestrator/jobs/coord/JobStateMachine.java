/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord;


import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.JobInfoImpl;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.exceptions.JobIllegalStateException;
import eu.agno3.orchestrator.jobs.msg.JobStateUpdatedEvent;
import eu.agno3.orchestrator.jobs.state.JobStateListener;
import eu.agno3.orchestrator.jobs.state.JobStateListenerVerbose;


/**
 * @author mbechler
 * 
 */
public class JobStateMachine implements JobStateListenerVerbose {

    private static final Logger log = Logger.getLogger(JobStateMachine.class);

    private Set<JobStateListener> listeners = Collections.newSetFromMap(new ConcurrentHashMap<>());


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.state.JobStateListenerVerbose#queued(eu.agno3.orchestrator.jobs.JobInfo)
     */
    @Override
    public JobInfo queued ( JobInfo j ) throws JobIllegalStateException {
        if ( j.getState() == JobState.SUSPENDED ) {
            return j;
        }

        if ( !EnumSet.of(JobState.NEW, JobState.STALLED, JobState.RUNNABLE).contains(j.getState()) ) {
            throw new JobIllegalStateException("Job not in state NEW or STALLED: " + j.getState()); //$NON-NLS-1$
        }

        JobInfoImpl info = makeJobInfo(j, JobState.QUEUED);
        info.setQueuedTime(DateTime.now());
        this.publishEvent(this.makeEvent(info));
        return info;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.state.JobStateListenerVerbose#runnable(eu.agno3.orchestrator.jobs.JobInfo)
     */
    @Override
    public JobInfo runnable ( JobInfo j ) throws JobIllegalStateException {

        if ( j.getState() == JobState.SUSPENDED ) {
            return j;
        }

        if ( !EnumSet.of(JobState.QUEUED, JobState.STALLED, JobState.UNKNOWN).contains(j.getState()) ) {
            throw new JobIllegalStateException("Job not in state QUEUED or STALLED: " + j.getState()); //$NON-NLS-1$
        }
        JobInfoImpl info = makeJobInfo(j, JobState.RUNNABLE);
        this.publishEvent(this.makeEvent(info));
        return info;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.state.JobStateListenerVerbose#stalled(eu.agno3.orchestrator.jobs.JobInfo)
     */
    @Override
    public JobInfo stalled ( JobInfo j ) throws JobIllegalStateException {
        if ( JobState.QUEUED != j.getState() ) {
            throw new JobIllegalStateException("Job not in state QUEUED: " + j.getState()); //$NON-NLS-1$
        }
        JobInfoImpl info = makeJobInfo(j, JobState.STALLED);
        this.publishEvent(this.makeEvent(info));
        return info;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.state.JobStateListenerVerbose#timeout(eu.agno3.orchestrator.jobs.JobInfo)
     */
    @Override
    public JobInfo timeout ( JobInfo j ) throws JobIllegalStateException {
        if ( !EnumSet.of(JobState.QUEUED, JobState.STALLED, JobState.RUNNABLE, JobState.TIMEOUT, JobState.RUNNING).contains(j.getState()) ) {
            throw new JobIllegalStateException("Job not in state QUEUED, STALLED, RUNNING or RUNNABLE: " + j.getState()); //$NON-NLS-1$
        }
        JobInfoImpl info = makeJobInfo(j, JobState.TIMEOUT);
        this.publishEvent(this.makeEvent(info));
        return info;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.state.JobStateListenerVerbose#cancel(eu.agno3.orchestrator.jobs.JobInfo)
     */
    @Override
    public JobInfo cancel ( JobInfo j ) throws JobIllegalStateException {

        if ( EnumSet.of(JobState.FINISHED, JobState.CANCELLED, JobState.FAILED).contains(j.getState()) ) {
            return j;
        }

        JobInfoImpl info = makeJobInfo(j, JobState.CANCELLED);
        info.setFinishedTime(DateTime.now());
        this.publishEvent(this.makeEvent(info));
        return info;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.state.JobStateListenerVerbose#running(eu.agno3.orchestrator.jobs.JobInfo)
     */
    @Override
    public JobInfo running ( JobInfo j ) throws JobIllegalStateException {
        if ( j.getState() == JobState.SUSPENDED ) {
            JobInfoImpl info = makeJobInfo(j, JobState.RESUMED);
            this.publishEvent(this.makeEvent(info));
            return info;
        }

        if ( !EnumSet.of(JobState.QUEUED, JobState.RUNNABLE, JobState.UNKNOWN, JobState.STALLED, JobState.RESUMED).contains(j.getState()) ) {
            throw new JobIllegalStateException("Job not in state QUEUED, RUNNABLE, STALLED, RESUMED or UNKNOWN: " + j.getState()); //$NON-NLS-1$
        }
        JobInfoImpl info = makeJobInfo(j, JobState.RUNNING);
        info.setStartedTime(DateTime.now());
        this.publishEvent(this.makeEvent(info));
        return info;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.state.JobStateListenerVerbose#finished(eu.agno3.orchestrator.jobs.JobInfo)
     */
    @Override
    public JobInfo finished ( JobInfo j ) throws JobIllegalStateException {
        if ( !EnumSet
                .of(JobState.RUNNING, JobState.UNKNOWN, JobState.STALLED, JobState.TIMEOUT, JobState.RESUMED, JobState.CANCELLED, JobState.QUEUED)
                .contains(j.getState()) ) {
            throw new JobIllegalStateException("Job not in state RUNNING or UNKNOWN: " + j.getState()); //$NON-NLS-1$
        }
        JobInfoImpl info = makeJobInfo(j, JobState.FINISHED);
        info.setFinishedTime(DateTime.now());
        this.publishEvent(this.makeEvent(info));
        return info;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.state.JobStateListenerVerbose#failed(eu.agno3.orchestrator.jobs.JobInfo)
     */
    @Override
    public JobInfo failed ( JobInfo j ) throws JobIllegalStateException {
        JobInfoImpl info = makeJobInfo(j, JobState.FAILED);
        info.setFinishedTime(DateTime.now());
        this.publishEvent(this.makeEvent(info));
        return info;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.state.JobStateListenerVerbose#updateTimeout(eu.agno3.orchestrator.jobs.JobInfo)
     */
    @Override
    public JobInfo updateTimeout ( JobInfo j ) throws JobIllegalStateException {

        if ( JobState.FAILED == j.getState() ) {
            return j;
        }

        if ( !EnumSet
                .of(JobState.QUEUED, JobState.STALLED, JobState.RUNNABLE, JobState.SUSPENDED, JobState.RUNNING, JobState.RESUMED, JobState.UNKNOWN)
                .contains(j.getState()) ) {
            throw new JobIllegalStateException("Job not in state QUEUED, STALLED, RUNNABLE, RUNNING or UNKNOWN: " + j.getState()); //$NON-NLS-1$
        }
        JobInfoImpl info = makeJobInfo(j, JobState.UNKNOWN);
        this.publishEvent(this.makeEvent(info));
        return info;
    }


    /**
     * @param j
     * @return
     * @throws JobIllegalStateException
     */
    private JobInfo resumed ( JobInfo j ) throws JobIllegalStateException {
        if ( !EnumSet.of(JobState.QUEUED, JobState.STALLED, JobState.RUNNABLE, JobState.RUNNING, JobState.SUSPENDED, JobState.UNKNOWN)
                .contains(j.getState()) ) {
            throw new JobIllegalStateException("Job not in state QUEUED, STALLED, RUNNABLE, RUNNING, SUSPENDED or UNKNOWN: " + j.getState()); //$NON-NLS-1$
        }
        JobInfoImpl info = makeJobInfo(j, JobState.RESUMED);
        this.publishEvent(this.makeEvent(info));
        return info;
    }


    /**
     * @param j
     * @return
     * @throws JobIllegalStateException
     */
    private JobInfo suspended ( JobInfo j ) throws JobIllegalStateException {
        if ( !EnumSet.of(JobState.QUEUED, JobState.STALLED, JobState.RUNNABLE, JobState.SUSPENDED, JobState.RUNNING, JobState.UNKNOWN)
                .contains(j.getState()) ) {
            throw new JobIllegalStateException("Job not in state QUEUED, STALLED, RUNNABLE, RUNNING or UNKNOWN: " + j.getState()); //$NON-NLS-1$
        }
        JobInfoImpl info = makeJobInfo(j, JobState.SUSPENDED);
        this.publishEvent(this.makeEvent(info));
        return info;
    }


    /**
     * @param j
     * @param s
     * @return the updated job info
     * @throws JobIllegalStateException
     */
    public JobInfo applyState ( JobInfo j, JobState s ) throws JobIllegalStateException {

        if ( log.isTraceEnabled() ) {
            log.trace(String.format("applyState %s: %s", j.getJobId(), s)); //$NON-NLS-1$
        }

        switch ( s ) {
        case NEW:
            return j;
        case CANCELLED:
            return this.cancel(j);
        case FAILED:
            return this.failed(j);
        case FINISHED:
            return this.finished(j);
        case QUEUED:
            return this.queued(j);
        case RUNNABLE:
            return this.runnable(j);
        case RUNNING:
            return this.running(j);
        case STALLED:
            return this.stalled(j);
        case TIMEOUT:
            return this.timeout(j);
        case UNKNOWN:
            return this.updateTimeout(j);
        case SUSPENDED:
            return this.suspended(j);
        case RESUMED:
            return this.resumed(j);
        default:
            throw new JobIllegalStateException("Unacceptable state " + s); //$NON-NLS-1$
        }
    }


    /**
     * @param makeEvent
     */
    private void publishEvent ( JobStateUpdatedEvent ev ) {
        if ( log.isTraceEnabled() ) {
            log.trace(
                String.format("Notifiying %d listeners of job %s new state %s", this.listeners.size(), ev.getJobId(), ev.getJobInfo().getState())); //$NON-NLS-1$
        }

        for ( JobStateListener l : this.listeners ) {
            l.jobUpdated(ev);
        }
    }


    /**
     * @param l
     */
    public void addListener ( JobStateListener l ) {
        this.listeners.add(l);
    }


    /**
     * @param l
     */
    public void removeListener ( JobStateListener l ) {
        this.listeners.remove(l);
    }


    /**
     * @param info
     * @return
     */
    protected JobStateUpdatedEvent makeEvent ( JobInfoImpl info ) {
        JobStateUpdatedEvent ev = new JobStateUpdatedEvent();
        ev.setJobId(info.getJobId());
        ev.setJobInfo(info);
        return ev;
    }


    /**
     * @param j
     * @param queued
     * @return
     */
    private static JobInfoImpl makeJobInfo ( JobInfo j, JobState state ) {
        JobInfoImpl info = new JobInfoImpl(j);
        info.setState(state);
        return info;
    }

}
