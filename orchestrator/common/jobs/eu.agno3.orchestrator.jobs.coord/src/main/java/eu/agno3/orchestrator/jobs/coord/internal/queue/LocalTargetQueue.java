/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.04.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord.internal.queue;


import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.JobTarget;
import eu.agno3.orchestrator.jobs.coord.InternalQueue;
import eu.agno3.orchestrator.jobs.coord.JobStateTracker;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.msg.JobKeepAliveEvent;


/**
 * @author mbechler
 * 
 */
public class LocalTargetQueue extends AbstractTargetQueue implements InternalQueue {

    /**
     * @param groupQueue
     * @param target
     * @param jst
     */
    public LocalTargetQueue ( InternalQueue groupQueue, JobTarget target, JobStateTracker jst ) {
        super(groupQueue, target, jst);
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
        this.getStateTracker().updateJobState(j, JobState.RUNNABLE);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.internal.queue.AbstractTargetQueue#loadJob(eu.agno3.orchestrator.jobs.Job,
     *      eu.agno3.orchestrator.jobs.JobInfo)
     */
    @Override
    public void loadJob ( Job j, JobInfo js ) {
        if ( js != null && js.getState() == JobState.RUNNING ) {
            // this probably was running while a shutdown occured
        }
        super.loadJob(j, js);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.state.JobStateListener#jobKeepalive(eu.agno3.orchestrator.jobs.Job,
     *      eu.agno3.orchestrator.jobs.msg.JobKeepAliveEvent)
     */
    @Override
    public void jobKeepalive ( Job job, JobKeepAliveEvent ev ) {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("LocalTargetQueue[%s,%s]", this.target, super.toString()); //$NON-NLS-1$
    }
}
