/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.progress;


import eu.agno3.orchestrator.system.base.execution.ExecutorEvent;
import eu.agno3.orchestrator.system.base.execution.ExecutorEventListener;


/**
 * @author mbechler
 * 
 */
public class LoggingJobProgressListener implements ExecutorEventListener {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.ExecutorEventListener#onEvent(eu.agno3.orchestrator.system.base.execution.ExecutorEvent)
     */
    @Override
    public void onEvent ( ExecutorEvent ev ) {
        if ( ev instanceof JobProgressStatusEvent ) {
            JobProgressStatusEvent sev = (JobProgressStatusEvent) ev;
            ev.getContext().getJobOutput().info(String.format("at %5.1f%%", sev.getProgressInfo().getProgress() * 100)); //$NON-NLS-1$
        }

    }

}
