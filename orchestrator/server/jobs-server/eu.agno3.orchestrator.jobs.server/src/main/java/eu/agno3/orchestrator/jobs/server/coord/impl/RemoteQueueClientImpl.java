/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.server.coord.impl;


import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.server.AgentConnectorWatcher;
import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.JobTarget;
import eu.agno3.orchestrator.jobs.msg.JobCancelRequestMessage;
import eu.agno3.orchestrator.jobs.msg.JobInfoReplyMessage;
import eu.agno3.orchestrator.jobs.msg.JobInfoRequestMessage;
import eu.agno3.orchestrator.jobs.msg.JobQueueRequestMessage;
import eu.agno3.orchestrator.jobs.targets.AgentTarget;
import eu.agno3.orchestrator.jobs.targets.AnyServerTarget;
import eu.agno3.orchestrator.jobs.targets.ServerTarget;
import eu.agno3.orchestrator.server.component.ComponentState;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.client.MessagingClient;


/**
 * @author mbechler
 * 
 */
@Component ( service = RemoteQueueClient.class )
public class RemoteQueueClientImpl implements RemoteQueueClient {

    private static final Logger log = Logger.getLogger(RemoteQueueClientImpl.class);

    private MessagingClient<MessageSource> msgClient;
    private AgentConnectorWatcher agentWatcher;


    @Reference
    protected synchronized void setMessagingClient ( MessagingClient<MessageSource> mc ) {
        this.msgClient = mc;
    }


    protected synchronized void unsetMessagingClient ( MessagingClient<MessageSource> mc ) {
        if ( this.msgClient == mc ) {
            this.msgClient = null;
        }
    }


    @Reference
    protected synchronized void setAgentWatcher ( AgentConnectorWatcher acw ) {
        this.agentWatcher = acw;
    }


    protected synchronized void unsetAgentWatcher ( AgentConnectorWatcher acw ) {
        if ( this.agentWatcher == acw ) {
            this.agentWatcher = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.server.coord.impl.RemoteQueueClient#tryQueueJob(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public boolean tryQueueJob ( Job j ) {
        JobTarget target = j.getTarget();

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Queuing on remote %s: %s", target, j.getClass().getName())); //$NON-NLS-1$
        }
        JobQueueRequestMessage req = new JobQueueRequestMessage(target, this.msgClient.getMessageSource());
        req.setJobId(j.getJobId());
        req.setJob(j);
        try {
            this.msgClient.sendMessage(req);
            return this.targetAvailable(target);
        }
        catch (
            MessagingException |
            InterruptedException e ) {
            log.debug("Failed to queue job:", e); //$NON-NLS-1$
            return false;
        }
    }


    /**
     * @param target
     * @return
     */
    protected boolean targetAvailable ( JobTarget target ) {

        if ( target instanceof AnyServerTarget ) {
            return true;
        }
        else if ( target instanceof AgentTarget ) {
            ComponentState agentState = this.agentWatcher.getComponentConnectorState( ( (AgentTarget) target ).getAgentId());
            return agentState == ComponentState.CONNECTED;
        }
        else if ( target instanceof ServerTarget ) {
            // TODO: implement when multiple servers shall be supported
            return true;
        }

        throw new IllegalArgumentException("Unknown job target " + target); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.jobs.server.coord.impl.RemoteQueueClient#tryCancelJob(eu.agno3.orchestrator.jobs.Job)
     */
    @Override
    public boolean tryCancelJob ( Job j ) {
        JobTarget target = j.getTarget();

        JobCancelRequestMessage req = new JobCancelRequestMessage(target, this.msgClient.getMessageSource());
        req.setJobId(j.getJobId());

        try {
            this.msgClient.sendMessage(req);
            return this.targetAvailable(target);
        }
        catch (
            MessagingException |
            InterruptedException e ) {
            log.debug("Failed to cancel job:", e); //$NON-NLS-1$
            return false;
        }
    }


    @Override
    public JobInfo tryGetJobInfo ( Job j ) {
        JobTarget target = j.getTarget();
        JobInfoRequestMessage jobInfoReq = new JobInfoRequestMessage(target, this.msgClient.getMessageSource());
        jobInfoReq.setJobId(j.getJobId());
        try {
            JobInfoReplyMessage reply = this.msgClient.sendMessage(jobInfoReq);

            if ( reply == null ) {
                return null;
            }

            return reply.getJobInfo();
        }
        catch (
            MessagingException |
            InterruptedException e ) {
            log.debug("Failed to get job info", e); //$NON-NLS-1$
            return null;
        }
    }
}
