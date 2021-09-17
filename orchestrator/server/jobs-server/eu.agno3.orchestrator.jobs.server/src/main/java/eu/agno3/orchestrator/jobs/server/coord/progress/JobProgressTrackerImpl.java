/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.06.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.server.coord.progress;


import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.gui.connector.GuiNotificationEvent;
import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobCoordinator;
import eu.agno3.orchestrator.jobs.JobProgressInfo;
import eu.agno3.orchestrator.jobs.exceptions.JobUnknownException;
import eu.agno3.orchestrator.jobs.msg.JobKeepAliveEvent;
import eu.agno3.orchestrator.jobs.msg.JobProgressEvent;
import eu.agno3.orchestrator.jobs.msg.JobStateUpdatedEvent;
import eu.agno3.orchestrator.jobs.server.JobProgressTracker;
import eu.agno3.orchestrator.jobs.server.coord.db.impl.DatabaseJobStateTrackerImpl;
import eu.agno3.orchestrator.jobs.state.JobStateListener;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.client.MessagingClient;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    JobProgressTracker.class, JobStateListener.class
} )
public class JobProgressTrackerImpl implements JobProgressTracker, JobStateListener {

    private static final Logger log = Logger.getLogger(JobProgressTrackerImpl.class);

    private DatabaseJobStateTrackerImpl dbTracker;
    private JobCoordinator coordinator;
    private MessagingClient<ServerMessageSource> msgClient;


    @Reference
    protected synchronized void setMessagingClient ( MessagingClient<ServerMessageSource> mc ) {
        this.msgClient = mc;
    }


    protected synchronized void unsetMessagingClient ( MessagingClient<ServerMessageSource> mc ) {
        if ( this.msgClient == mc ) {
            this.msgClient = null;
        }
    }


    @Reference
    protected synchronized void setDatabaseJobTracker ( DatabaseJobStateTrackerImpl dbt ) {
        this.dbTracker = dbt;
        this.dbTracker.addListener(this);
    }


    protected synchronized void unsetDatabaseJobTracker ( DatabaseJobStateTrackerImpl dbt ) {
        if ( this.dbTracker == dbt ) {
            this.dbTracker.removeListener(this);
            this.dbTracker = null;
        }
    }


    @Reference
    protected synchronized void setCoordinator ( JobCoordinator coord ) {
        this.coordinator = coord;
    }


    protected synchronized void unsetCoordinator ( JobCoordinator coord ) {
        if ( this.coordinator == coord ) {
            this.coordinator = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.server.JobProgressTracker#getProgressInfo(java.util.UUID)
     */
    @Override
    public JobProgressInfo getProgressInfo ( UUID jobId ) {
        return this.dbTracker.getProgressInfo(jobId);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.server.JobProgressTracker#handleEvent(eu.agno3.orchestrator.jobs.msg.JobProgressEvent)
     */
    @Override
    public void handleEvent ( JobProgressEvent ev ) {
        try {
            this.dbTracker.handleEvent(ev);
            this.coordinator.notifyExternalKeepAlive(ev.getJobId());
        }
        catch ( JobUnknownException e ) {
            log.warn("Keepalive failed:", e); //$NON-NLS-1$
        }

        try {
            log.trace("Notifying GUI of progress"); //$NON-NLS-1$
            notifyGuiProgress(ev);
        }
        catch (
            MessagingException |
            InterruptedException e ) {
            log.debug("Failed to notify GUIs of progress", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.state.JobStateListener#jobUpdated(eu.agno3.orchestrator.jobs.msg.JobStateUpdatedEvent)
     */
    @Override
    public void jobUpdated ( JobStateUpdatedEvent ev ) {
        try {
            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Notifying GUI of state change for %s: %s", ev.getJobId(), ev.getJobInfo().getState())); //$NON-NLS-1$
            }
            notifyGuiState(ev);
        }
        catch (
            MessagingException |
            InterruptedException e ) {
            log.debug("Failed to notify GUIs of state change", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.state.JobStateListener#jobKeepalive(eu.agno3.orchestrator.jobs.Job,
     *      eu.agno3.orchestrator.jobs.msg.JobKeepAliveEvent)
     */
    @Override
    public void jobKeepalive ( Job job, JobKeepAliveEvent ev ) {
        // unused
    }


    /**
     * @param ev
     * @throws MessagingException
     * @throws InterruptedException
     */
    private void notifyGuiProgress ( JobProgressEvent ev ) throws MessagingException, InterruptedException {
        String path = "/jobs/progress/" + ev.getJobId(); //$NON-NLS-1$
        String stateMessage = ev.getProgressInfo().getStateMessage() != null ? ev.getProgressInfo().getStateMessage() : StringUtils.EMPTY;
        String payload = ev.getProgressInfo().getProgress() + "/" + stateMessage; //$NON-NLS-1$
        this.msgClient.publishEvent(new GuiNotificationEvent(this.msgClient.getMessageSource(), path, payload));
    }


    /**
     * @param ev
     * @throws MessagingException
     * @throws InterruptedException
     */
    private void notifyGuiState ( JobStateUpdatedEvent ev ) throws MessagingException, InterruptedException {
        String path = "/jobs/state/" + ev.getJobId(); //$NON-NLS-1$
        String payload = ev.getJobInfo().getState().toString();
        this.msgClient.publishEvent(new GuiNotificationEvent(this.msgClient.getMessageSource(), path, payload));
    }

}
