/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2014 by mbechler
 */
package eu.agno3.orchestrator.gui.connector.remote;


import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.gui.component.auth.GuiComponentPrincipal;
import eu.agno3.orchestrator.gui.config.GuiConfig;
import eu.agno3.orchestrator.gui.config.GuiConfigRequest;
import eu.agno3.orchestrator.gui.connector.GuiConnector;
import eu.agno3.orchestrator.gui.connector.GuiConnectorConfiguration;
import eu.agno3.orchestrator.gui.events.GuiConnectedEvent;
import eu.agno3.orchestrator.gui.events.GuiConnectingEvent;
import eu.agno3.orchestrator.gui.events.GuiDisconnectingEvent;
import eu.agno3.orchestrator.gui.msg.GuiPingRequest;
import eu.agno3.orchestrator.gui.msg.addressing.GuiMessageSource;
import eu.agno3.orchestrator.server.component.msg.ComponentPingRequest;
import eu.agno3.orchestrator.server.connector.ServerConnector;
import eu.agno3.orchestrator.server.connector.impl.AbstractServerConnector;
import eu.agno3.runtime.messaging.client.MessagingClientFactory;
import eu.agno3.runtime.messaging.msg.EventMessage;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    RemoteGuiConnector.class, GuiConnector.class, ServerConnector.class
}, immediate = true, configurationPid = GuiConnectorConfiguration.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class RemoteGuiConnector extends AbstractServerConnector<@NonNull GuiMessageSource, @NonNull GuiConfig> implements GuiConnector {

    @Override
    @Activate
    protected synchronized void activate ( ComponentContext context ) {
        super.activate(context);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.connector.impl.AbstractServerConnector#deactivate(org.osgi.service.component.ComponentContext)
     */
    @Override
    @Deactivate
    protected synchronized void deactivate ( ComponentContext context ) {
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
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.connector.impl.AbstractServerConnector#getMessageSource()
     */
    @Override
    public @NonNull GuiMessageSource getMessageSource () {
        return new GuiMessageSource(this.getComponentId());
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.connector.impl.AbstractServerConnector#makeConfigRequest()
     */
    @Override
    protected @NonNull GuiConfigRequest makeConfigRequest () {
        return new GuiConfigRequest(this.getMessageSource());
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.connector.impl.AbstractServerConnector#makePingEvent()
     */
    @Override
    protected @NonNull ComponentPingRequest<@NonNull GuiMessageSource> makePingEvent () {
        return new GuiPingRequest(this.getMessageSource());
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.connector.impl.AbstractServerConnector#makeDisconnectingEvent()
     */
    @Override
    protected @NonNull EventMessage<@NonNull GuiMessageSource> makeDisconnectingEvent ( @NonNull GuiMessageSource ms ) {
        return new GuiDisconnectingEvent(ms);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.connector.impl.AbstractServerConnector#createConnectingEvent()
     */
    @Override
    protected @NonNull EventMessage<@NonNull GuiMessageSource> createConnectingEvent () {
        return new GuiConnectingEvent(this.getMessageSource());
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.connector.impl.AbstractServerConnector#createConnectedEvent()
     */
    @Override
    protected @NonNull EventMessage<@NonNull GuiMessageSource> createConnectedEvent () {
        return new GuiConnectedEvent(this.getMessageSource());
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.connector.impl.AbstractServerConnector#getConfigClass()
     */
    @SuppressWarnings ( "null" )
    @Override
    protected @NonNull Class<@NonNull GuiConfig> getConfigClass () {
        return GuiConfig.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.connector.impl.AbstractServerConnector#buildUserName(java.util.UUID)
     */
    @Override
    protected String buildUserName ( UUID id ) {
        return GuiComponentPrincipal.GUI_USER_PREFIX.concat(id.toString());
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.connector.impl.AbstractServerConnector#getSystemEventTopic()
     */
    @Override
    protected String getSystemEventTopic () {
        return "system-guis"; //$NON-NLS-1$
    }

}
