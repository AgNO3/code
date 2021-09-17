/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord.internal;


import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobCoordinator;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.msg.JobKeepAliveEvent;
import eu.agno3.orchestrator.jobs.state.JobStateListener;
import eu.agno3.runtime.messaging.listener.EventListener;


/**
 * @author mbechler
 * 
 */
@Component ( service = EventListener.class, property = "eventType=eu.agno3.orchestrator.jobs.msg.JobKeepAliveEvent" )
public class JobKeepAliveEventListener implements EventListener<JobKeepAliveEvent> {

    private static final Logger log = Logger.getLogger(JobKeepAliveEventListener.class);
    private JobCoordinator coordinator;
    private Set<JobStateListener> listeners = new HashSet<>();


    @Reference
    protected synchronized void setCoordinator ( JobCoordinator coord ) {
        this.coordinator = coord;
    }


    protected synchronized void unsetCoordinator ( JobCoordinator coord ) {
        if ( this.coordinator == coord ) {
            this.coordinator = null;
        }
    }


    @Reference ( cardinality = ReferenceCardinality.AT_LEAST_ONE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindListener ( JobStateListener l ) {
        this.listeners.add(l);
    }


    protected synchronized void unbindListener ( JobStateListener l ) {
        this.listeners.remove(l);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.listener.EventListener#getEventType()
     */
    @Override
    public @NonNull Class<JobKeepAliveEvent> getEventType () {
        return JobKeepAliveEvent.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.listener.EventListener#onEvent(eu.agno3.runtime.messaging.msg.EventMessage)
     */
    @Override
    public void onEvent ( @NonNull JobKeepAliveEvent ev ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Recieved event " + ev); //$NON-NLS-1$
        }

        try {
            Job job = this.coordinator.getJobData(ev.getJobId());

            if ( job == null ) {
                log.warn("Failed to retrieve job information for " + ev.getJobId()); //$NON-NLS-1$
                return;
            }

            this.notifyAllStatus(job, ev);
        }
        catch ( JobQueueException e ) {
            log.warn("Failed to keepalive job:", e); //$NON-NLS-1$
        }

    }


    private synchronized void notifyAllStatus ( Job job, JobKeepAliveEvent ev ) {
        for ( JobStateListener l : this.listeners ) {
            l.jobKeepalive(job, ev);
        }
    }

}
