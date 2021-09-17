/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.state;


import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.msg.JobStateUpdatedEvent;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractJobStateListener implements JobStateListener, JobStateListenerVerbose {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.state.JobStateListener#jobUpdated(eu.agno3.orchestrator.jobs.msg.JobStateUpdatedEvent)
     */
    @Override
    public void jobUpdated ( JobStateUpdatedEvent ev ) {
        JobInfo jobInfo = ev.getJobInfo();
        switch ( jobInfo.getState() ) {
        case CANCELLED:
            cancel(jobInfo);
            break;
        case FAILED:
            failed(jobInfo);
            break;
        case FINISHED:
            finished(jobInfo);
            break;
        case QUEUED:
            queued(jobInfo);
            break;
        case RUNNABLE:
            runnable(jobInfo);
            break;
        case RUNNING:
            running(jobInfo);
            break;
        case STALLED:
            stalled(jobInfo);
            break;
        case TIMEOUT:
            timeout(jobInfo);
            break;
        case UNKNOWN:
            updateTimeout(jobInfo);
            break;
        default:
            break;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.state.JobStateListenerVerbose#cancel(eu.agno3.orchestrator.jobs.JobInfo)
     */
    @Override
    public JobInfo cancel ( JobInfo j ) {
        return j;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.state.JobStateListenerVerbose#failed(eu.agno3.orchestrator.jobs.JobInfo)
     */
    @Override
    public JobInfo failed ( JobInfo j ) {
        return j;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.state.JobStateListenerVerbose#finished(eu.agno3.orchestrator.jobs.JobInfo)
     */
    @Override
    public JobInfo finished ( JobInfo j ) {
        return j;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.state.JobStateListenerVerbose#queued(eu.agno3.orchestrator.jobs.JobInfo)
     */
    @Override
    public JobInfo queued ( JobInfo j ) {
        return j;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.state.JobStateListenerVerbose#runnable(eu.agno3.orchestrator.jobs.JobInfo)
     */
    @Override
    public JobInfo runnable ( JobInfo j ) {
        return j;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.state.JobStateListenerVerbose#running(eu.agno3.orchestrator.jobs.JobInfo)
     */
    @Override
    public JobInfo running ( JobInfo j ) {
        return j;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.state.JobStateListenerVerbose#stalled(eu.agno3.orchestrator.jobs.JobInfo)
     */
    @Override
    public JobInfo stalled ( JobInfo j ) {
        return j;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.state.JobStateListenerVerbose#timeout(eu.agno3.orchestrator.jobs.JobInfo)
     */
    @Override
    public JobInfo timeout ( JobInfo j ) {
        return j;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.state.JobStateListenerVerbose#updateTimeout(eu.agno3.orchestrator.jobs.JobInfo)
     */
    @Override
    public JobInfo updateTimeout ( JobInfo j ) {
        return j;
    }

}
