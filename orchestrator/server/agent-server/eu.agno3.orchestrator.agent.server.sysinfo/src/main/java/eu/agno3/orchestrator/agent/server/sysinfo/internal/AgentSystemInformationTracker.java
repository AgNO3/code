/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.03.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.server.sysinfo.internal;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.config.AgentConfig;
import eu.agno3.orchestrator.agent.server.AgentConnectorWatcher;
import eu.agno3.orchestrator.agent.server.AgentLifecycleListener;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.server.service.InstanceServerService;
import eu.agno3.orchestrator.gui.connector.GuiNotificationEvent;
import eu.agno3.orchestrator.server.component.ComponentState;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.orchestrator.system.info.msg.AgentSystemInformation;
import eu.agno3.orchestrator.system.info.msg.RefreshRequest;
import eu.agno3.orchestrator.system.info.msg.SystemInformationUpdatedEvent;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.client.MessagingClient;
import eu.agno3.runtime.messaging.listener.EventListener;
import eu.agno3.runtime.transaction.TransactionService;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    AgentLifecycleListener.class, AgentSystemInformationTracker.class, EventListener.class
}, immediate = true, property = "eventType=eu.agno3.orchestrator.system.info.msg.SystemInformationUpdatedEvent" )
public class AgentSystemInformationTracker implements AgentLifecycleListener, EventListener<SystemInformationUpdatedEvent> {

    private static final Logger log = Logger.getLogger(AgentSystemInformationTracker.class);

    private Map<UUID, AgentSystemInformation> cached = new HashMap<>();

    private MessagingClient<ServerMessageSource> messagingClient;
    private AgentConnectorWatcher agents;
    private InstanceServerService instanceService;
    private TransactionService transactionService;


    @Reference
    protected synchronized void setMessagingClient ( MessagingClient<ServerMessageSource> client ) {
        this.messagingClient = client;
    }


    protected synchronized void unsetMessagingClient ( MessagingClient<ServerMessageSource> client ) {
        if ( this.messagingClient == client ) {
            this.messagingClient = null;
        }
    }


    @Reference
    protected synchronized void setAgentConnectorWatcher ( AgentConnectorWatcher watcher ) {
        this.agents = watcher;
    }


    protected synchronized void unsetAgentConnectorWatcher ( AgentConnectorWatcher watcher ) {
        if ( this.agents == watcher ) {
            this.agents = null;
        }
    }


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
    protected synchronized void setTransactionManager ( TransactionService ts ) {
        this.transactionService = ts;
    }


    protected synchronized void unsetTransactionManager ( TransactionService ts ) {
        if ( this.transactionService == ts ) {
            this.transactionService = null;
        }
    }


    @Activate
    protected void activate ( ComponentContext ctx ) {}


    @Deactivate
    protected void deactivate ( ComponentContext ctx ) {
        this.cached.clear();
    }


    /**
     * @param agentUUID
     * @return the system information or null if unavailable
     */
    public AgentSystemInformation getInformation ( UUID agentUUID ) {
        return this.cached.get(agentUUID);
    }


    /**
     * 
     */
    public void refreshAll () {
        for ( UUID agentId : this.agents.getActiveComponentIds() ) {
            if ( this.agents.getComponentConnectorState(agentId) != ComponentState.CONNECTED ) {
                continue;
            }

            try {
                this.refreshAgent(agentId);
            }
            catch ( AgentOfflineException e ) {
                log.debug("Looks like agent is offline " + agentId, e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param agent
     * @throws AgentOfflineException
     */
    public void refreshAgent ( @NonNull UUID agent ) throws AgentOfflineException {
        RefreshRequest req = new RefreshRequest(agent, this.messagingClient.getMessageSource());

        if ( log.isDebugEnabled() ) {
            log.debug("Refreshing system information of agent " + agent); //$NON-NLS-1$
        }

        try {
            this.messagingClient.sendMessage(req);
        }
        catch (
            MessagingException |
            IllegalStateException |
            SecurityException |
            InterruptedException e ) {
            log.warn("Failed to refresh system information of agent " + agent, e); //$NON-NLS-1$
        }
    }


    /**
     * @param agentId
     * @param sysInfo
     */
    private void haveSystemInformation ( @NonNull UUID agentId, AgentSystemInformation sysInfo ) {

        if ( log.isDebugEnabled() ) {
            log.debug("Have system information for " + agentId); //$NON-NLS-1$
        }

        if ( sysInfo == null ) {
            return;
        }

        this.cached.put(agentId, sysInfo);

        try {
            @NonNull
            InstanceStructuralObject instanceForAgent = this.instanceService.getInstanceForAgent(agentId);
            String path = String.format("/instance/%s/sysinfo_update", instanceForAgent.getId()); //$NON-NLS-1$
            this.messagingClient.publishEvent(new GuiNotificationEvent(this.messagingClient.getMessageSource(), path, null));
        }
        catch (
            ModelObjectNotFoundException |
            MessagingException |
            InterruptedException |
            ModelServiceException e ) {
            log.debug("Failed to publish system info update event"); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.EventListener#getEventType()
     */
    @Override
    public @NonNull Class<SystemInformationUpdatedEvent> getEventType () {
        return SystemInformationUpdatedEvent.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.EventListener#onEvent(eu.agno3.runtime.messaging.msg.EventMessage)
     */
    @Override
    public void onEvent ( @NonNull SystemInformationUpdatedEvent event ) {
        haveSystemInformation(event.getOrigin().getAgentId(), event.getSystemInfo());
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.agent.server.AgentLifecycleListener#connected(eu.agno3.orchestrator.agent.config.AgentConfig)
     */
    @Override
    public void connected ( AgentConfig c ) {
        try {
            UUID id = c.getId();
            if ( id == null ) {
                log.debug("No known ID"); //$NON-NLS-1$
                return;
            }
            refreshAgent(id);
        }
        catch ( Exception e ) {
            log.error("Failed to refresh system information after connect", e); //$NON-NLS-1$
        }

    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.agent.server.AgentLifecycleListener#disconnecting(eu.agno3.orchestrator.agent.config.AgentConfig)
     */
    @Override
    public void disconnecting ( AgentConfig c ) {
        this.cached.remove(c.getId());
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.agent.server.AgentLifecycleListener#failed(eu.agno3.orchestrator.agent.config.AgentConfig)
     */
    @Override
    public void failed ( AgentConfig c ) {
        this.cached.remove(c.getId());
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.agent.server.AgentLifecycleListener#connecting(eu.agno3.orchestrator.agent.config.AgentConfig)
     */
    @Override
    public void connecting ( AgentConfig c ) {
        // ignore
    }
}
