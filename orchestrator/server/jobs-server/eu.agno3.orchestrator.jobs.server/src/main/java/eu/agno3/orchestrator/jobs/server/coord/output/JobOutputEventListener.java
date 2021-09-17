/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.server.coord.output;


import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.jobs.msg.JobOutputEvent;
import eu.agno3.orchestrator.jobs.server.JobOutputTracker;
import eu.agno3.runtime.messaging.listener.EventListener;


/**
 * @author mbechler
 * 
 */
@Component ( service = EventListener.class, property = "eventType=eu.agno3.orchestrator.jobs.msg.JobOutputEvent" )
public class JobOutputEventListener implements EventListener<JobOutputEvent> {

    private static final Logger log = Logger.getLogger(JobOutputEventListener.class);

    private JobOutputTracker outputTracker;


    @Reference
    protected synchronized void setJobProgressTracker ( JobOutputTracker pt ) {
        this.outputTracker = pt;
    }


    protected synchronized void unsetJobProgressTracker ( JobOutputTracker pt ) {
        if ( this.outputTracker == pt ) {
            this.outputTracker = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.listener.EventListener#getEventType()
     */
    @Override
    public @NonNull Class<JobOutputEvent> getEventType () {
        return JobOutputEvent.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.listener.EventListener#onEvent(eu.agno3.runtime.messaging.msg.EventMessage)
     */
    @Override
    public void onEvent ( @NonNull JobOutputEvent ev ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "Got output event for job %s: %s (eof: %s)", //$NON-NLS-1$
                ev.getJobId(),
                ev.getText() != null ? ev.getText().trim() : StringUtils.EMPTY,
                ev.isEof()));
        }
        this.outputTracker.handleOutputEvent(ev);
    }

}
