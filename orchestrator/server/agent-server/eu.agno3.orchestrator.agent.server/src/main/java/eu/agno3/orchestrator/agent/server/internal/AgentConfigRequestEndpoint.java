/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.server.internal;


import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.config.AgentConfig;
import eu.agno3.orchestrator.agent.config.AgentConfigRequest;
import eu.agno3.orchestrator.agent.config.AgentConfigResponse;
import eu.agno3.orchestrator.agent.server.AgentConfigurationProvider;
import eu.agno3.orchestrator.server.component.ComponentConfigurationException;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.listener.MessageProcessingException;
import eu.agno3.runtime.messaging.listener.RequestEndpoint;
import eu.agno3.runtime.messaging.xml.DefaultXmlErrorResponseMessage;


/**
 * @author mbechler
 * 
 */
@Component ( service = RequestEndpoint.class, property = "msgType=eu.agno3.orchestrator.agent.config.AgentConfigRequest" )
public class AgentConfigRequestEndpoint implements RequestEndpoint<AgentConfigRequest, AgentConfigResponse, DefaultXmlErrorResponseMessage> {

    private static final Logger log = Logger.getLogger(AgentConfigRequestEndpoint.class);

    private AgentConfigurationProvider configProvider;
    private AgentConnectorWatcherImpl connectorWatcher;
    private Optional<@NonNull ServerMessageSource> messageSource = Optional.empty();


    @Reference
    protected synchronized void setAgentConfigProvider ( AgentConfigurationProvider provider ) {
        this.configProvider = provider;
    }


    protected synchronized void unsetAgentConfigProvider ( AgentConfigurationProvider provider ) {
        if ( this.configProvider == provider ) {
            this.configProvider = null;
        }
    }


    @Reference
    protected synchronized void setAgentConnectorWatcher ( AgentConnectorWatcherImpl acw ) {
        this.connectorWatcher = acw;
    }


    protected synchronized void unsetAgentConnectorWatcher ( AgentConnectorWatcherImpl acw ) {
        if ( this.connectorWatcher == acw ) {
            this.connectorWatcher = null;
        }
    }


    @Reference
    protected synchronized void setMessageSource ( @NonNull MessageSource ms ) {
        this.messageSource = Optional.of((ServerMessageSource) ms);
    }


    protected synchronized void unsetMessageSource ( MessageSource ms ) {
        if ( this.messageSource.equals(ms) ) {
            this.messageSource = Optional.empty();
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#onReceive(eu.agno3.runtime.messaging.msg.RequestMessage)
     */
    @Override
    public AgentConfigResponse onReceive ( @NonNull AgentConfigRequest msg ) throws MessageProcessingException {
        if ( log.isDebugEnabled() ) {
            log.debug("Recieved agent configuration request from " + msg.getOrigin().getAgentId()); //$NON-NLS-1$
        }
        try {
            AgentConfig agentConfiguration = this.configProvider.getConfiguration(msg.getOrigin().getAgentId());
            if ( agentConfiguration == null ) {
                throw new ComponentConfigurationException("No config available"); //$NON-NLS-1$
            }
            this.connectorWatcher.haveConfig(msg.getOrigin().getAgentId(), msg);
            return new AgentConfigResponse(agentConfiguration, this.messageSource.get(), msg);
        }
        catch ( ComponentConfigurationException e ) {
            log.warn("Failed to retrieve agent configuration:", e); //$NON-NLS-1$
            throw new MessageProcessingException(new DefaultXmlErrorResponseMessage(e, this.messageSource.get(), msg));
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#getMessageType()
     */
    @Override
    public Class<AgentConfigRequest> getMessageType () {
        return AgentConfigRequest.class;
    }

}
