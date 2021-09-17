/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.01.2016 by mbechler
 */
package eu.agno3.orchestrator.agent.monitor.server.internal;


import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.jws.WebService;
import javax.persistence.EntityManager;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.config.AgentConfig;
import eu.agno3.orchestrator.agent.monitor.server.MonitoringServiceInternal;
import eu.agno3.orchestrator.agent.monitor.server.data.ServiceStateCacheEntry;
import eu.agno3.orchestrator.agent.server.AgentLifecycleListener;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.base.server.tree.TreeUtil;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.server.service.AgentServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.InstanceServerService;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.gui.connector.GuiNotificationEvent;
import eu.agno3.orchestrator.jobs.JobImpl;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.orchestrator.system.monitor.ServiceRuntimeStatus;
import eu.agno3.orchestrator.system.monitor.jobs.DisableServiceJob;
import eu.agno3.orchestrator.system.monitor.jobs.EnableServiceJob;
import eu.agno3.orchestrator.system.monitor.jobs.RestartServiceJob;
import eu.agno3.orchestrator.system.monitor.service.MonitoringService;
import eu.agno3.orchestrator.system.monitor.service.MonitoringServiceDescriptor;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.client.MessagingClient;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.transaction.TransactionContext;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    MonitoringServiceInternal.class, MonitoringService.class, SOAPWebService.class
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.system.monitor.service.MonitoringService",
    targetNamespace = MonitoringServiceDescriptor.NAMESPACE,
    serviceName = MonitoringServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/agent/monitor" )
public class MonitoringServiceImpl implements MonitoringService, MonitoringServiceInternal, AgentLifecycleListener {

    private static final Logger log = Logger.getLogger(MonitoringServiceImpl.class);

    private MessagingClient<ServerMessageSource> msgClient;
    private DefaultServerServiceContext sctx;
    private InstanceServerService instanceService;
    private AgentServerService agentService;

    private Map<UUID, ServiceRuntimeStatus> stateCache = Collections.synchronizedMap(new LRUMap<>(10));


    @Reference
    protected synchronized void setContext ( DefaultServerServiceContext ctx ) {
        this.sctx = ctx;
    }


    protected synchronized void unsetContext ( DefaultServerServiceContext ctx ) {
        if ( this.sctx == ctx ) {
            this.sctx = null;
        }
    }


    @Reference
    protected synchronized void setMsgClient ( MessagingClient<ServerMessageSource> mc ) {
        this.msgClient = mc;
    }


    protected synchronized void unsetMsgClient ( MessagingClient<ServerMessageSource> mc ) {
        if ( this.msgClient == mc ) {
            this.msgClient = null;
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
    protected synchronized void setAgentService ( AgentServerService as ) {
        this.agentService = as;
    }


    protected synchronized void unsetAgentService ( AgentServerService as ) {
        if ( this.agentService == as ) {
            this.agentService = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.monitor.service.MonitoringService#getServiceStatus(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject)
     */
    @Override
    @RequirePermissions ( "structure:view:serviceState" )
    public String getServiceStatus ( ServiceStructuralObject service ) throws ModelServiceException, ModelObjectNotFoundException {
        if ( service == null ) {
            throw new ModelObjectNotFoundException(ServiceStructuralObject.class, null);
        }
        ServiceRuntimeStatus cached = this.stateCache.get(service.getId());
        if ( cached != null ) {
            return cached.name();
        }

        EntityManager em = this.sctx.getOrchestratorEMF().createEntityManager();
        ServiceStateCacheEntry find = em.find(ServiceStateCacheEntry.class, service.getId());
        if ( find == null ) {
            return ServiceRuntimeStatus.UNKNOWN.name();
        }
        return find.getRuntimeState().name();
    }


    @Override
    @RequirePermissions ( "service:enable" )
    public @NonNull JobInfo enableService ( ServiceStructuralObject service )
            throws ModelServiceException, ModelObjectNotFoundException, AgentDetachedException, AgentCommunicationErrorException {
        return submitUserJob(service, new EnableServiceJob(service));
    }


    @Override
    @RequirePermissions ( "service:disable" )
    public @NonNull JobInfo disableService ( ServiceStructuralObject service )
            throws ModelServiceException, ModelObjectNotFoundException, AgentDetachedException, AgentCommunicationErrorException {
        return submitUserJob(service, new DisableServiceJob(service));
    }


    @Override
    @RequirePermissions ( "service:restart" )
    public @NonNull JobInfo restartService ( ServiceStructuralObject service )
            throws ModelServiceException, ModelObjectNotFoundException, AgentDetachedException, AgentCommunicationErrorException {
        return submitUserJob(service, new RestartServiceJob(service));
    }


    @NonNull
    JobInfo submitUserJob ( ServiceStructuralObject service, JobImpl j )
            throws ModelObjectNotFoundException, ModelServiceException, AgentDetachedException, AgentCommunicationErrorException {
        j.setOwner(SecurityUtils.getSubject().getPrincipals().oneByType(UserPrincipal.class));
        return this.agentService.submitJob(getInstance(service), j);
    }


    @SuppressWarnings ( "null" )
    InstanceStructuralObject getInstance ( ServiceStructuralObject service ) throws ModelServiceException, ModelObjectNotFoundException {
        if ( service == null ) {
            throw new ModelObjectNotFoundException(ServiceStructuralObject.class, null);
        }

        EntityManager em = this.sctx.createConfigEM();
        @NonNull
        ServiceStructuralObjectImpl persistent = PersistenceUtil.fetch(em, ServiceStructuralObjectImpl.class, service.getId());
        Optional<? extends @NonNull AbstractStructuralObjectImpl> parent = TreeUtil.getParent(em, AbstractStructuralObjectImpl.class, persistent);
        if ( !parent.isPresent() || ! ( parent.get() instanceof InstanceStructuralObject ) ) {
            throw new ModelObjectNotFoundException(InstanceStructuralObject.class, null);
        }

        InstanceStructuralObject instance = (InstanceStructuralObject) parent.get();
        return instance;
    }


    /**
     * @param service
     * @param payload
     */
    private void publishGUIEvent ( ServiceStructuralObject service, String payload ) {
        String path = "/service/" + service.getId() + "/runtime_status"; //$NON-NLS-1$ //$NON-NLS-2$
        try {
            this.msgClient.publishEvent(new GuiNotificationEvent(this.msgClient.getMessageSource(), path, payload)); // $NON-NLS-1$
        }
        catch (
            MessagingException |
            InterruptedException e ) {
            log.warn("Failed to publish service status event", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.monitor.server.MonitoringServiceInternal#haveServiceState(eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject,
     *      eu.agno3.orchestrator.system.monitor.ServiceRuntimeStatus)
     */
    @Override
    public void haveServiceState ( @NonNull ServiceStructuralObject service, ServiceRuntimeStatus newStatus ) {
        this.stateCache.put(service.getId(), newStatus);
        try ( TransactionContext tx = this.sctx.getTransactionService().ensureTransacted() ) {
            EntityManager em = this.sctx.getOrchestratorEMF().createEntityManager();
            ServiceStateCacheEntry found = em.find(ServiceStateCacheEntry.class, service.getId());
            if ( found == null ) {
                found = new ServiceStateCacheEntry();
                found.setServiceId(service.getId());
            }
            if ( found.getRuntimeState() != newStatus ) {
                found.setRuntimeState(newStatus);
                publishGUIEvent(service, newStatus.name());
            }
            em.persist(found);
            em.flush();
            tx.commit();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.component.ComponentLifecycleListener#connecting(eu.agno3.orchestrator.server.component.ComponentConfig)
     */
    @Override
    public void connecting ( AgentConfig c ) {

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.component.ComponentLifecycleListener#connected(eu.agno3.orchestrator.server.component.ComponentConfig)
     */
    @Override
    public void connected ( AgentConfig c ) {}


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.component.ComponentLifecycleListener#disconnecting(eu.agno3.orchestrator.server.component.ComponentConfig)
     */
    @Override
    public void disconnecting ( AgentConfig c ) {
        setAllState(c, ServiceRuntimeStatus.UNKNOWN);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.component.ComponentLifecycleListener#failed(eu.agno3.orchestrator.server.component.ComponentConfig)
     */
    @Override
    public void failed ( AgentConfig c ) {
        setAllState(c, ServiceRuntimeStatus.UNKNOWN);
    }


    /**
     * @param c
     * @param state
     */
    private void setAllState ( AgentConfig c, ServiceRuntimeStatus state ) {
        try {
            UUID id = c.getId();
            if ( id == null ) {
                return;
            }
            @NonNull
            InstanceStructuralObject instance = this.instanceService.getInstanceForAgent(id);
            for ( ServiceStructuralObject service : this.instanceService.getServices(instance) ) {
                if ( service == null ) {
                    continue;
                }
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Setting service %s state to %s", service, state)); //$NON-NLS-1$
                }
                haveServiceState(service, state);
            }
        }
        catch (
            ModelObjectNotFoundException |
            ModelServiceException e ) {
            log.debug("Could not get instance", e); //$NON-NLS-1$
        }
    }

}
