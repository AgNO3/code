/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord.internal;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.jobs.JobCoordinator;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.JobInfoImpl;
import eu.agno3.orchestrator.jobs.JobState;
import eu.agno3.orchestrator.jobs.msg.JobInfoRequestMessage;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.listener.RequestEndpoint;


/**
 * @author mbechler
 * 
 */
@Component ( service = RequestEndpoint.class, property = "msgType=eu.agno3.orchestrator.jobs.msg.JobInfoRequestMessage" )
public class JobInfoRequestEndpoint extends AbstractJobCoordinatorRequestEndpoint<JobInfoRequestMessage> {

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
    protected JobInfo handle ( @NonNull JobInfoRequestMessage msg, JobInfo info ) {
        if ( info == null ) {
            JobInfoImpl ji = new JobInfoImpl();
            ji.setJobId(msg.getJobId());
            ji.setState(JobState.UNKNOWN);
            return ji;
        }
        return info;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.coord.internal.AbstractJobCoordinatorRequestEndpoint#requireJobInfo()
     */
    @Override
    protected boolean requireJobInfo () {
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#getMessageType()
     */
    @Override
    public Class<JobInfoRequestMessage> getMessageType () {
        return JobInfoRequestMessage.class;
    }

}
