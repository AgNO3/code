/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord.internal;


import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.jobs.JobCoordinator;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.msg.JobCancelRequestMessage;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.listener.RequestEndpoint;


/**
 * @author mbechler
 * 
 */
@Component ( service = RequestEndpoint.class, property = "msgType=eu.agno3.orchestrator.jobs.msg.JobCancelRequestMessage" )
public class JobCancelRequestEndpoint extends AbstractJobCoordinatorRequestEndpoint<JobCancelRequestMessage> {

    /**
     * 
     */
    private static final String CANCEL_NONEXISTANT = "Server tried to cancel a job that does not exist "; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(JobCancelRequestEndpoint.class);


    @Override
    @Reference
    protected synchronized void setCoordinator ( JobCoordinator coord ) {
        super.setCoordinator(coord);
    }


    @Override
    protected synchronized void unsetCoordinator ( JobCoordinator coord ) {
        super.unsetCoordinator(coord);
    }


    @Override
    @Reference
    protected synchronized void setMessageSource ( @NonNull MessageSource source ) {
        super.setMessageSource(source);
    }


    @Override
    protected synchronized void unsetMessageSource ( MessageSource source ) {
        super.unsetMessageSource(source);
    }


    @Override
    protected JobInfo handle ( @NonNull JobCancelRequestMessage msg, JobInfo info ) throws JobQueueException {
        if ( info == null ) {
            log.warn(CANCEL_NONEXISTANT + msg.getJobId());
            return null;
        }
        return this.getCoordinator().cancelJob(info.getJobId());
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#getMessageType()
     */
    @Override
    public @NonNull Class<JobCancelRequestMessage> getMessageType () {
        return JobCancelRequestMessage.class;
    }

}
