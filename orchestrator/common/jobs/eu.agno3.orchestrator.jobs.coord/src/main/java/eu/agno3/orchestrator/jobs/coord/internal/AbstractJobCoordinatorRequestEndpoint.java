/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord.internal;


import java.util.Optional;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.jobs.JobCoordinator;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.JobTarget;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.exceptions.JobUnknownException;
import eu.agno3.orchestrator.jobs.msg.JobCoordinatorRequestMessage;
import eu.agno3.orchestrator.jobs.msg.JobInfoReplyMessage;
import eu.agno3.orchestrator.jobs.msg.JobMessageTarget;
import eu.agno3.orchestrator.jobs.msg.JobMessageTargetDestinationResolver;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.listener.CustomDestination;
import eu.agno3.runtime.messaging.listener.MessageProcessingException;
import eu.agno3.runtime.messaging.listener.RequestEndpoint;
import eu.agno3.runtime.messaging.xml.DefaultXmlErrorResponseMessage;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public abstract class AbstractJobCoordinatorRequestEndpoint <T extends JobCoordinatorRequestMessage<@NonNull ? extends MessageSource>>
        implements RequestEndpoint<T, JobInfoReplyMessage, DefaultXmlErrorResponseMessage>, CustomDestination {

    private static final Logger log = Logger.getLogger(AbstractJobCoordinatorRequestEndpoint.class);
    private Optional<@NonNull MessageSource> messageSource = Optional.empty();
    private JobCoordinator coordinator;


    protected synchronized void setCoordinator ( JobCoordinator coord ) {
        this.coordinator = coord;
    }


    protected synchronized void unsetCoordinator ( JobCoordinator coord ) {
        if ( this.coordinator == coord ) {
            this.coordinator = null;
        }
    }


    /**
     * @return the coordinator
     */
    protected synchronized JobCoordinator getCoordinator () {
        return this.coordinator;
    }


    protected synchronized void setMessageSource ( @NonNull MessageSource source ) {
        this.messageSource = Optional.of(source);
    }


    protected synchronized void unsetMessageSource ( MessageSource source ) {
        if ( this.messageSource.equals(source) ) {
            this.messageSource = Optional.empty();
        }
    }


    /**
     * @return the messageSource
     */
    public synchronized @NonNull MessageSource getMessageSource () {
        return this.messageSource.get();
    }


    protected abstract JobInfo handle ( @NonNull T msg, JobInfo job ) throws JobQueueException;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#onReceive(eu.agno3.runtime.messaging.msg.RequestMessage)
     */
    @Override
    public JobInfoReplyMessage onReceive ( @NonNull T msg ) throws MessageProcessingException {
        JobInfoReplyMessage reply = new JobInfoReplyMessage(this.getMessageSource(), msg);
        return onRecieveInternal(msg, reply, getJobInfo(msg));
    }


    /**
     * @param msg
     * @return
     */
    private JobInfo getJobInfo ( T msg ) {
        JobInfo knownJobInfo = null;
        try {
            knownJobInfo = this.getCoordinator().getJobInfo(msg.getJobId());
        }
        catch ( JobQueueException e ) {
            log.debug("Failed to lookup job info", e); //$NON-NLS-1$
        }
        return knownJobInfo;
    }


    /**
     * @param msg
     * @param reply
     * @param knownJobInfo
     * @return
     * @throws MessageProcessingException
     */
    private JobInfoReplyMessage onRecieveInternal ( @NonNull T msg, JobInfoReplyMessage reply, JobInfo knownJobInfo )
            throws MessageProcessingException {
        if ( this.requireJobInfo() && knownJobInfo == null ) {
            log.warn(String.format("Failed to get job info for %s in %s", msg.getJobId(), this.getClass().getSimpleName())); //$NON-NLS-1$
            if ( msg.hasResponse() ) {
                throw new MessageProcessingException(new DefaultXmlErrorResponseMessage(new JobUnknownException("Failed to find job " //$NON-NLS-1$
                        + msg.getJobId()),
                    this.messageSource.get(), msg));
            }
            return null;
        }

        return doHandle(msg, reply, knownJobInfo);
    }


    /**
     * @param msg
     * @param reply
     * @param knownJobInfo
     * @return
     * @throws MessageProcessingException
     */
    private JobInfoReplyMessage doHandle ( @NonNull T msg, JobInfoReplyMessage reply, JobInfo knownJobInfo ) throws MessageProcessingException {
        try {
            reply.setJobInfo(handle(msg, knownJobInfo));
            return reply;
        }
        catch ( JobQueueException e ) {
            log.warn("Failed to handle job:", e); //$NON-NLS-1$
            if ( msg.hasResponse() ) {
                throw new MessageProcessingException(new DefaultXmlErrorResponseMessage(e, this.messageSource.get(), msg));
            }
            return null;
        }
    }


    /**
     * @return
     */
    protected boolean requireJobInfo () {
        return true;
    }


    @Override
    public Destination createCustomDestination ( Session s ) throws JMSException {
        JobTarget[] listeningTargets = JobMessageTarget.listeningTargets(this.getMessageSource());
        return JobMessageTargetDestinationResolver.destinationForJobTargets(s, this.getMessageType(), listeningTargets);
    }


    @Override
    public String createCustomDestinationId () {
        JobTarget[] listeningTargets = JobMessageTarget.listeningTargets(this.getMessageSource());
        return JobMessageTargetDestinationResolver.destinationIdForJobTargets(this.getMessageType(), listeningTargets);
    }

}
