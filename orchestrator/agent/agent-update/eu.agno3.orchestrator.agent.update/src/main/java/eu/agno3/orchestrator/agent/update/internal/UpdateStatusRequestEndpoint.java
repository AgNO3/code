/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.12.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.internal;


import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.config.AgentConfig;
import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.orchestrator.agent.update.UpdateInstallation;
import eu.agno3.orchestrator.agent.update.UpdateTracker;
import eu.agno3.orchestrator.system.update.msg.AgentUpdateStatusRequest;
import eu.agno3.orchestrator.system.update.msg.AgentUpdateStatusResponse;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.listener.MessageProcessingException;
import eu.agno3.runtime.messaging.listener.RequestEndpoint;
import eu.agno3.runtime.messaging.xml.DefaultXmlErrorResponseMessage;


/**
 * @author mbechler
 *
 */
@Component ( service = RequestEndpoint.class, property = {
    "msgType=eu.agno3.orchestrator.system.update.msg.AgentUpdateStatusRequest"
} )
public class UpdateStatusRequestEndpoint
        implements RequestEndpoint<AgentUpdateStatusRequest, AgentUpdateStatusResponse, DefaultXmlErrorResponseMessage> {

    private AgentConfig agentConfig;
    private UpdateTracker updateTracker;


    @Reference
    protected synchronized void setAgentConfig ( AgentConfig config ) {
        this.agentConfig = config;
    }


    protected synchronized void unsetAgentConfig ( AgentConfig config ) {
        if ( this.agentConfig == config ) {
            this.agentConfig = null;
        }
    }


    @Reference
    protected synchronized void setUpdateTracker ( UpdateTracker ut ) {
        this.updateTracker = ut;
    }


    protected synchronized void unsetUpdateTracker ( UpdateTracker ut ) {
        if ( this.updateTracker == ut ) {
            this.updateTracker = null;
        }
    }


    /**
     * @return the agentConfig
     */
    protected synchronized AgentConfig getAgentConfig () {
        return this.agentConfig;
    }


    protected @NonNull AgentMessageSource getMessageSource () throws MessagingException {
        UUID id = this.getAgentConfig().getId();

        if ( id == null ) {
            throw new MessagingException("Agent id is null"); //$NON-NLS-1$
        }

        return new AgentMessageSource(id);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#onReceive(eu.agno3.runtime.messaging.msg.RequestMessage)
     */
    @Override
    public AgentUpdateStatusResponse onReceive ( @NonNull AgentUpdateStatusRequest msg ) throws MessageProcessingException, MessagingException {
        AgentUpdateStatusResponse response = new AgentUpdateStatusResponse(getMessageSource(), msg);

        UpdateInstallation current = this.updateTracker.getCurrent();
        UpdateInstallation revert = this.updateTracker.getRevert();

        response.setCurrentSequence(current.getSequence());
        response.setCurrentStream(current.getStream());
        response.setCurrentInstallDate(current.getInstallDate());
        response.setRebootIndicated(this.updateTracker.isRebootIndicated());

        if ( revert != null ) {
            response.setRevertSequence(revert.getSequence());
            response.setRevertStream(revert.getStream());
            response.setRevertTimestamp(revert.getInstallDate());
        }

        return response;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#getMessageType()
     */
    @Override
    public Class<AgentUpdateStatusRequest> getMessageType () {
        return AgentUpdateStatusRequest.class;
    }

}
