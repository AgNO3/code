/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.system;


import eu.agno3.orchestrator.jobs.JobProgressInfo;
import eu.agno3.orchestrator.jobs.JobProgressInfoImpl;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.exec.JobOutputHandler;
import eu.agno3.orchestrator.system.base.execution.ExecutorEvent;
import eu.agno3.orchestrator.system.base.execution.ExecutorEventListener;
import eu.agno3.orchestrator.system.base.execution.events.ResumeEvent;
import eu.agno3.orchestrator.system.base.execution.events.SuspendEvent;
import eu.agno3.orchestrator.system.base.execution.progress.JobProgressStatusEvent;


/**
 * @author mbechler
 * 
 */
public class ExecutorEventListenerOutputAdapter implements ExecutorEventListener {

    private JobOutputHandler outHandler;
    private JobProgressInfo lastProgressInfo;


    /**
     * @param outHandler
     */
    public ExecutorEventListenerOutputAdapter ( JobOutputHandler outHandler ) {
        this.outHandler = outHandler;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutorEventListener#onEvent(eu.agno3.orchestrator.system.base.execution.ExecutorEvent)
     */
    @Override
    public void onEvent ( ExecutorEvent ev ) {
        if ( ev instanceof JobProgressStatusEvent ) {
            JobProgressInfo progressInfo = ( (JobProgressStatusEvent) ev ).getProgressInfo();
            this.lastProgressInfo = progressInfo;
            this.outHandler.setProgress(progressInfo);
        }
        else if ( ev instanceof SuspendEvent ) {
            if ( this.lastProgressInfo != null ) {
                JobProgressInfoImpl pi = new JobProgressInfoImpl(this.lastProgressInfo);
                pi.setState(JobState.SUSPENDED);
                this.outHandler.setProgress(pi);
            }
        }
        else if ( ev instanceof ResumeEvent ) {
            if ( this.lastProgressInfo != null ) {
                JobProgressInfoImpl pi = new JobProgressInfoImpl(this.lastProgressInfo);
                pi.setState(JobState.RESUMED);
                this.outHandler.setProgress(pi);
            }
        }
    }
}
