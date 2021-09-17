/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.msg;


import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.jobs.JobTarget;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.MessageTarget;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.msg.RequestMessage;
import eu.agno3.runtime.messaging.xml.DefaultXmlErrorResponseMessage;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * 
 * 
 * These messages are intended for internal coordination only, e.g.
 * they are sent directly to the delegate coordinator by the main coordinator.
 * 
 * @author mbechler
 * @param <T>
 * 
 */
public class JobCoordinatorRequestMessage <@NonNull T extends MessageSource> extends XmlMarshallableMessage<T>
        implements RequestMessage<T, JobInfoReplyMessage, DefaultXmlErrorResponseMessage> {

    private static final long TIMEOUT = 500;
    private UUID jobId;
    private JobTarget jobTarget;


    /**
     * 
     */
    public JobCoordinatorRequestMessage () {
        super();
    }


    /**
     * @param target
     * @param origin
     * @param ttl
     */
    public JobCoordinatorRequestMessage ( JobTarget target, T origin, int ttl ) {
        super(origin, ttl);
        this.jobTarget = target;
    }


    /**
     * @param target
     * @param origin
     * @param replyTo
     */
    public JobCoordinatorRequestMessage ( JobTarget target, T origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
        this.jobTarget = target;
    }


    /**
     * @param target
     * @param origin
     */
    public JobCoordinatorRequestMessage ( JobTarget target, T origin ) {
        super(origin);
        this.jobTarget = target;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getErrorResponseType()
     */
    @Override
    public Class<DefaultXmlErrorResponseMessage> getErrorResponseType () {
        return DefaultXmlErrorResponseMessage.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getReplyTimeout()
     */
    @Override
    public long getReplyTimeout () {
        return TIMEOUT;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getResponseType()
     */
    @Override
    public Class<JobInfoReplyMessage> getResponseType () {
        return JobInfoReplyMessage.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getTarget()
     */
    @Override
    public MessageTarget getTarget () {
        return new JobMessageTarget(this.jobTarget);
    }


    /**
     * @return the jobTarget
     */
    public JobTarget getJobTarget () {
        return this.jobTarget;
    }


    /**
     * @param jobTarget
     *            the jobTarget to set
     */
    public void setJobTarget ( JobTarget jobTarget ) {
        this.jobTarget = jobTarget;
    }


    /**
     * @return the job
     */
    public UUID getJobId () {
        return this.jobId;
    }


    /**
     * @param job
     *            the job to set
     */
    public void setJobId ( UUID job ) {
        this.jobId = job;
    }

}