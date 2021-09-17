/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.msg;


import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.EventMessage;
import eu.agno3.runtime.messaging.msg.Message;


/**
 * @author mbechler
 * 
 */
@Component ( service = EventMessage.class )
public class JobStateUpdatedEvent extends JobEvent<@NonNull MessageSource> {

    private JobInfo jobInfo;


    /**
     * 
     */
    public JobStateUpdatedEvent () {
        super();
    }


    /**
     * @param jobId
     * @param origin
     * @param ttl
     */
    public JobStateUpdatedEvent ( UUID jobId, @NonNull MessageSource origin, int ttl ) {
        super(jobId, origin, ttl);
    }


    /**
     * @param jobId
     * @param origin
     * @param replyTo
     */
    public JobStateUpdatedEvent ( UUID jobId, @NonNull MessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(jobId, origin, replyTo);
    }


    /**
     * @param jobId
     * @param origin
     */
    public JobStateUpdatedEvent ( UUID jobId, @NonNull MessageSource origin ) {
        super(jobId, origin);
    }


    /**
     * @return the jobInfo
     */
    public JobInfo getJobInfo () {
        return this.jobInfo;
    }


    /**
     * @param jobInfo
     *            the jobInfo to set
     */
    public void setJobInfo ( JobInfo jobInfo ) {
        this.jobInfo = jobInfo;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return "Job state update: " + this.jobInfo; //$NON-NLS-1$
    }

}
