/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.connector.internal;


import java.net.InetAddress;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.component.auth.AgentComponentPrincipal;
import eu.agno3.orchestrator.agent.config.AgentConfig;
import eu.agno3.orchestrator.agent.config.AgentConfigRequest;
import eu.agno3.orchestrator.agent.connector.AgentServerConnector;
import eu.agno3.orchestrator.agent.events.AgentConnectedEvent;
import eu.agno3.orchestrator.agent.events.AgentConnectingEvent;
import eu.agno3.orchestrator.agent.events.AgentDisconnectingEvent;
import eu.agno3.orchestrator.agent.msg.AgentPingMessage;
import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.orchestrator.server.connector.ServerConnector;
import eu.agno3.orchestrator.server.connector.ServerConnectorConfiguration;
import eu.agno3.orchestrator.server.connector.impl.AbstractServerConnector;
import eu.agno3.orchestrator.system.img.util.SystemImageUtil;
import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.messaging.client.MessagingClientFactory;
import eu.agno3.runtime.messaging.msg.EventMessage;
import eu.agno3.runtime.util.net.LocalHostUtil;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    AgentServerConnector.class, ServerConnector.class
}, immediate = true )
public class AgentServerConnectorImpl extends AbstractServerConnector<@NonNull AgentMessageSource, AgentConfig> implements AgentServerConnector {

    private static final Logger log = Logger.getLogger(AgentServerConnectorImpl.class);


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.connector.impl.AbstractServerConnector#activate(org.osgi.service.component.ComponentContext)
     */
    @Activate
    @Override
    protected synchronized void activate ( ComponentContext context ) {
        log.debug("Initialize server connection"); //$NON-NLS-1$
        super.activate(context);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.connector.impl.AbstractServerConnector#deactivate(org.osgi.service.component.ComponentContext)
     */
    @Deactivate
    @Override
    protected synchronized void deactivate ( ComponentContext context ) {
        log.debug("Tear down server connection"); //$NON-NLS-1$
        super.deactivate(context);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.connector.impl.AbstractServerConnector#setMessagingClientFactory(eu.agno3.orchestrator.messaging.client.MessagingClientFactory)
     */
    @Reference
    @Override
    protected synchronized void setMessagingClientFactory ( MessagingClientFactory mcf ) {
        super.setMessagingClientFactory(mcf);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.connector.impl.AbstractServerConnector#unsetMessagingClientFactory(eu.agno3.orchestrator.messaging.client.MessagingClientFactory)
     */
    @Override
    protected synchronized void unsetMessagingClientFactory ( MessagingClientFactory mcf ) {
        super.unsetMessagingClientFactory(mcf);
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.connector.ServerConnector#getMessageSource()
     */
    @Override
    public @NonNull AgentMessageSource getMessageSource () {
        return new AgentMessageSource(this.getComponentId());
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.connector.impl.AbstractServerConnector#setTLSContext(eu.agno3.runtime.crypto.tls.TLSContext)
     */
    @Override
    @Reference ( target = "(subsystem=agent/client)" )
    protected synchronized void setTLSContext ( TLSContext tc ) {
        super.setTLSContext(tc);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.connector.impl.AbstractServerConnector#unsetTLSContext(eu.agno3.runtime.crypto.tls.TLSContext)
     */
    @Override
    protected synchronized void unsetTLSContext ( TLSContext tc ) {
        super.unsetTLSContext(tc);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.connector.impl.AbstractServerConnector#setServerConnectorConfig(eu.agno3.orchestrator.server.connector.ServerConnectorConfiguration)
     */
    @Override
    @Reference ( updated = "setServerConnectorConfig" )
    protected synchronized void setServerConnectorConfig ( ServerConnectorConfiguration scc ) {
        super.setServerConnectorConfig(scc);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.connector.impl.AbstractServerConnector#updatedServerConnectorConfig(eu.agno3.orchestrator.server.connector.ServerConnectorConfiguration)
     */
    @Override
    protected synchronized void updatedServerConnectorConfig ( ServerConnectorConfiguration scc ) {
        super.updatedServerConnectorConfig(scc);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.connector.impl.AbstractServerConnector#unsetServerConnectorConfig(eu.agno3.orchestrator.server.connector.ServerConnectorConfiguration)
     */
    @Override
    protected synchronized void unsetServerConnectorConfig ( ServerConnectorConfiguration scc ) {
        super.unsetServerConnectorConfig(scc);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.connector.impl.AbstractServerConnector#getSystemEventTopic()
     */
    @Override
    protected String getSystemEventTopic () {
        return "system-agents"; //$NON-NLS-1$
    }


    @Override
    protected String buildUserName ( UUID id ) {
        return AgentComponentPrincipal.AGENT_USER_PREFIX.concat(id.toString());
    }


    @Override
    protected @NonNull Class<AgentConfig> getConfigClass () {
        return AgentConfig.class;
    }


    @Override
    protected @NonNull AgentConfigRequest makeConfigRequest () {
        AgentConfigRequest req = new AgentConfigRequest(this.getMessageSource());
        req.setImageType(SystemImageUtil.getLocalImageType());
        String applianceBuild = SystemImageUtil.getApplianceBuild();
        Long buildVersion = null;
        if ( !StringUtils.isBlank(applianceBuild) ) {
            try {
                buildVersion = Long.parseLong(applianceBuild);
            }
            catch ( IllegalArgumentException e ) {
                log.warn("Invalid version", e); //$NON-NLS-1$
            }
        }
        req.setBuildVersion(buildVersion);

        String hostName = LocalHostUtil.guessPrimaryHostName();
        InetAddress guessPrimaryAddress = LocalHostUtil.guessPrimaryAddress();
        if ( guessPrimaryAddress == null ) {
            return req;
        }

        String primaryAddr = guessPrimaryAddress.getHostAddress();

        if ( primaryAddr != null ) {
            req.setHostAddress(primaryAddr);
        }

        if ( hostName != null && !hostName.equals(primaryAddr) ) {
            req.setHostName(hostName);
        }

        return req;
    }


    @Override
    protected @NonNull EventMessage<@NonNull AgentMessageSource> makeDisconnectingEvent ( @NonNull AgentMessageSource ms ) {
        return new AgentDisconnectingEvent(ms);
    }


    /**
     * @return
     */
    @Override
    protected @NonNull AgentPingMessage makePingEvent () {
        return new AgentPingMessage(new Date(), this.getMessageSource());
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.connector.impl.AbstractServerConnector#createConnectingEvent()
     */
    @Override
    protected @NonNull EventMessage<@NonNull AgentMessageSource> createConnectingEvent () {
        return new AgentConnectingEvent(this.getMessageSource());
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.connector.impl.AbstractServerConnector#createConnectedEvent()
     */
    @Override
    protected @NonNull EventMessage<@NonNull AgentMessageSource> createConnectedEvent () {
        return new AgentConnectedEvent(this.getMessageSource());
    }

}
