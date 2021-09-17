/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.12.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.impl;


import java.util.UUID;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.config.AgentConfig;
import eu.agno3.orchestrator.agent.server.AgentLifecycleListener;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.server.service.InstanceServerService;
import eu.agno3.orchestrator.gui.connector.GuiNotificationEvent;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.client.MessagingClient;


/**
 * @author mbechler
 *
 */
@Component ( service = AgentLifecycleListener.class )
public class AgentGUINotifier implements AgentLifecycleListener {

    private static final Logger log = Logger.getLogger(AgentGUINotifier.class);

    private InstanceServerService instanceService;
    private MessagingClient<ServerMessageSource> msgClient;


    @Reference
    protected synchronized void setInstanceService ( InstanceServerService iss ) {
        this.instanceService = iss;
    }


    protected synchronized void unsetInstanceService ( InstanceServerService iss ) {
        if ( this.instanceService == iss ) {
            this.instanceService = null;
        }
    }


    @Reference
    protected synchronized void setMessageClient ( MessagingClient<ServerMessageSource> mc ) {
        this.msgClient = mc;
    }


    protected synchronized void unsetMessageClient ( MessagingClient<ServerMessageSource> mc ) {
        if ( this.msgClient == mc ) {
            this.msgClient = null;
        }
    }


    /**
     * @param inst
     * @param payload
     */
    private void publishGUIEvent ( InstanceStructuralObject inst, String payload ) {
        String path = "/instance/" + inst.getId() + "/agent_status"; //$NON-NLS-1$ //$NON-NLS-2$
        try {
            if ( log.isDebugEnabled() ) {
                log.debug("Publish to " + path); //$NON-NLS-1$
            }
            this.msgClient.publishEvent(new GuiNotificationEvent(this.msgClient.getMessageSource(), path, payload)); // $NON-NLS-1$
        }
        catch (
            MessagingException |
            InterruptedException e ) {
            log.warn("Failed to publish agent status event", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.component.ComponentLifecycleListener#connecting(eu.agno3.orchestrator.server.component.ComponentConfig)
     */
    @Override
    public void connecting ( AgentConfig c ) {}


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.component.ComponentLifecycleListener#connected(eu.agno3.orchestrator.server.component.ComponentConfig)
     */
    @Override
    public void connected ( AgentConfig c ) {
        log.debug("Connected"); //$NON-NLS-1$
        if ( c == null ) {
            return;
        }
        UUID id = c.getId();
        if ( id == null ) {
            return;
        }

        try {
            publishGUIEvent(getInstance(id), "CONNECTED"); //$NON-NLS-1$
        }
        catch ( ModelObjectNotFoundException e ) {
            log.debug("Agent detached", e); //$NON-NLS-1$
        }
        catch ( ModelServiceException e ) {
            log.warn("Failed to get instance for agent", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.component.ComponentLifecycleListener#disconnecting(eu.agno3.orchestrator.server.component.ComponentConfig)
     */
    @Override
    public void disconnecting ( AgentConfig c ) {
        if ( c == null ) {
            return;
        }
        UUID id = c.getId();
        if ( id == null ) {
            return;
        }

        try {
            publishGUIEvent(getInstance(id), "DISCONNECT"); //$NON-NLS-1$
        }
        catch ( ModelObjectNotFoundException e ) {
            log.debug("Agent detached", e); //$NON-NLS-1$
        }
        catch ( ModelServiceException e ) {
            log.warn("Failed to get instance for agent", e); //$NON-NLS-1$
        }
    }


    /**
     * @param id
     * @return
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    private @NonNull InstanceStructuralObject getInstance ( @NonNull UUID id ) throws ModelObjectNotFoundException, ModelServiceException {
        return this.instanceService.getInstanceForAgent(id);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.component.ComponentLifecycleListener#failed(eu.agno3.orchestrator.server.component.ComponentConfig)
     */
    @Override
    public void failed ( AgentConfig c ) {
        if ( c == null ) {
            return;
        }
        UUID id = c.getId();
        if ( id == null ) {
            return;
        }

        try {
            publishGUIEvent(getInstance(id), "FAILED"); //$NON-NLS-1$
        }
        catch ( ModelObjectNotFoundException e ) {
            log.debug("Agent detached", e); //$NON-NLS-1$
        }
        catch ( ModelServiceException e ) {
            log.warn("Failed to get instance for agent", e); //$NON-NLS-1$
        }
    }

}
