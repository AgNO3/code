/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.10.2016 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.listener;


import java.util.Objects;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.config.AgentConfig;
import eu.agno3.orchestrator.agent.server.AgentLifecycleListener;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.msg.AgentServiceEntry;
import eu.agno3.orchestrator.config.model.msg.AgentServicesRequest;
import eu.agno3.orchestrator.config.model.msg.AgentServicesResponse;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.server.service.ConfigApplyServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ServiceServerService;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.client.MessagingClient;
import eu.agno3.runtime.transaction.TransactionContext;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    ServiceRefresher.class, AgentLifecycleListener.class
} )
public class ServiceRefresher implements AgentLifecycleListener {

    private static final Logger log = Logger.getLogger(ServiceRefresher.class);

    private MessagingClient<ServerMessageSource> msgClient;
    private ServiceServerService serviceService;
    private DefaultServerServiceContext sctx;
    private ConfigApplyServerService configApplyService;


    @Reference
    protected synchronized void setMessagingClient ( MessagingClient<ServerMessageSource> mc ) {
        this.msgClient = mc;
    }


    protected synchronized void unsetMessagingClient ( MessagingClient<ServerMessageSource> mc ) {
        if ( this.msgClient == mc ) {
            this.msgClient = null;
        }
    }


    @Reference
    protected synchronized void setServiceService ( ServiceServerService ss ) {
        this.serviceService = ss;
    }


    protected synchronized void unsetServiceService ( ServiceServerService ss ) {
        if ( this.serviceService == ss ) {
            this.serviceService = null;
        }
    }


    @Reference
    protected synchronized void setContext ( DefaultServerServiceContext sc ) {
        this.sctx = sc;
    }


    protected synchronized void unsetContext ( DefaultServerServiceContext sc ) {
        if ( this.sctx == sc ) {
            this.sctx = null;
        }
    }


    @Reference
    protected synchronized void setConfigApplyService ( ConfigApplyServerService ss ) {
        this.configApplyService = ss;
    }


    protected synchronized void unsetConfigApplyService ( ConfigApplyServerService ss ) {
        if ( this.configApplyService == ss ) {
            this.configApplyService = null;
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

        try {
            UUID agentId = c.getId();
            if ( agentId == null ) {
                return;
            }
            if ( log.isDebugEnabled() ) {
                log.debug("Requesting services from agent" + c.getId()); //$NON-NLS-1$
            }

            @Nullable
            AgentServicesResponse response = this.msgClient.sendMessage(new AgentServicesRequest(agentId, this.msgClient.getMessageSource()));
            if ( response != null ) {
                try ( TransactionContext tc = this.sctx.getTransactionService().ensureTransacted() ) {
                    @NonNull
                    EntityManager em = this.sctx.createConfigEM();
                    for ( AgentServiceEntry agentServiceEntry : response.getServices() ) {
                        Long appRev = agentServiceEntry.getAppliedRevision();
                        Long lastRev = agentServiceEntry.getFailsafeRevision();
                        ServiceStructuralObject s = agentServiceEntry.getService();
                        if ( log.isDebugEnabled() ) {
                            log.debug(String.format(
                                "Found service %s applied: %d failsafe: %d", //$NON-NLS-1$
                                s,
                                appRev != null ? appRev : -1,
                                lastRev != null ? lastRev : -1));
                        }

                        if ( s == null ) {
                            continue;
                        }

                        try {
                            @NonNull
                            ServiceStructuralObject persistent = this.serviceService.fetch(em, s);
                            Long appliedRevision = persistent.getAppliedRevision();
                            if ( !Objects.equals(appliedRevision, appRev) ) {
                                if ( log.isDebugEnabled() ) {
                                    log.debug(String.format(
                                        "Mismatching in revision numbers agent: %d server: %d", //$NON-NLS-1$
                                        appRev != null ? appRev : -1,
                                        appliedRevision != null ? appliedRevision : -1));
                                }

                                this.configApplyService.setAppliedRevision(em, persistent, true, appRev);
                                em.flush();
                                tc.commit();
                            }
                            else if ( appliedRevision != null ) {
                                log.debug("Applied revision is " + appliedRevision); //$NON-NLS-1$
                            }
                            else {
                                log.debug("Not configured"); //$NON-NLS-1$
                            }
                        }
                        catch ( ModelObjectNotFoundException e ) {
                            log.debug("Service not known", e); //$NON-NLS-1$
                            continue;
                        }
                    }
                }
            }

        }
        catch (
            MessagingException |
            InterruptedException |
            ModelServiceException e ) {
            log.warn("Failed to request services from agent", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.component.ComponentLifecycleListener#disconnecting(eu.agno3.orchestrator.server.component.ComponentConfig)
     */
    @Override
    public void disconnecting ( AgentConfig c ) {}


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.component.ComponentLifecycleListener#failed(eu.agno3.orchestrator.server.component.ComponentConfig)
     */
    @Override
    public void failed ( AgentConfig c ) {}

}
