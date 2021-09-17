/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.server.coord.progress;


import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.jobs.msg.JobProgressEvent;
import eu.agno3.orchestrator.jobs.server.JobProgressTracker;
import eu.agno3.runtime.messaging.listener.EventListener;


/**
 * @author mbechler
 * 
 */
@Component ( service = EventListener.class, property = "eventType=eu.agno3.orchestrator.jobs.msg.JobProgressEvent" )
public class JobProgressEventListener implements EventListener<JobProgressEvent> {

    private static final Logger log = Logger.getLogger(JobProgressEventListener.class);

    private JobProgressTracker progressTracker;


    @Reference
    protected synchronized void setJobProgressTracker ( JobProgressTracker pt ) {
        this.progressTracker = pt;
    }


    protected synchronized void unsetJobProgressTracker ( JobProgressTracker pt ) {
        if ( this.progressTracker == pt ) {
            this.progressTracker = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.listener.EventListener#getEventType()
     */
    @Override
    public @NonNull Class<JobProgressEvent> getEventType () {
        return JobProgressEvent.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.listener.EventListener#onEvent(eu.agno3.runtime.messaging.msg.EventMessage)
     */
    @Override
    public void onEvent ( @NonNull JobProgressEvent ev ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Got progress event for job %s: %.2f", ev.getJobId(), ev.getProgressInfo().getProgress())); //$NON-NLS-1$
        }
        this.progressTracker.handleEvent(ev);
    }

}
