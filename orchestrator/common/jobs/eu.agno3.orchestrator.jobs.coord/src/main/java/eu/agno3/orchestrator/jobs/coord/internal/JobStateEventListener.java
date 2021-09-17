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

import eu.agno3.orchestrator.jobs.JobCoordinator;
import eu.agno3.orchestrator.jobs.msg.JobStateUpdatedEvent;
import eu.agno3.orchestrator.jobs.state.JobStateListener;
import eu.agno3.runtime.messaging.listener.EventListener;


/**
 * @author mbechler
 * 
 */
@Component ( service = EventListener.class, property = "eventType=eu.agno3.orchestrator.jobs.msg.JobStateUpdatedEvent" )
public class JobStateEventListener implements EventListener<JobStateUpdatedEvent> {

    private static final Logger log = Logger.getLogger(JobStateEventListener.class);
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
    public @NonNull Class<JobStateUpdatedEvent> getEventType () {
        return JobStateUpdatedEvent.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.listener.EventListener#onEvent(eu.agno3.runtime.messaging.msg.EventMessage)
     */
    @Override
    public void onEvent ( @NonNull JobStateUpdatedEvent ev ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Recieved event " + ev); //$NON-NLS-1$
        }

        notifyAllStatus(ev);
    }


    private synchronized void notifyAllStatus ( JobStateUpdatedEvent ev ) {
        for ( JobStateListener l : this.listeners ) {
            l.jobUpdated(ev);
        }
    }

}
