/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.impl;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.AgentInfo;
import eu.agno3.orchestrator.agent.server.AgentConnectorWatcher;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectNotFoundFault;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.base.server.tree.TreeUtil;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.server.service.AgentServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.InstanceServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.config.model.realm.service.InstanceService;
import eu.agno3.orchestrator.config.model.realm.service.InstanceServiceDescriptor;
import eu.agno3.orchestrator.server.component.ComponentState;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    InstanceService.class, InstanceServerService.class, SOAPWebService.class,
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.config.model.realm.service.InstanceService",
    targetNamespace = InstanceServiceDescriptor.NAMESPACE,
    serviceName = InstanceServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/realm/instance" )
public class InstanceServiceImpl implements InstanceService, InstanceServerService, SOAPWebService {

    /**
     * 
     */
    private static final String AGENT_ID_ATTR = "agentId"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(InstanceServiceImpl.class);
    private DefaultServerServiceContext sctx;
    private PersistenceUtil persistenceUtil;
    private AgentConnectorWatcher agentConnectorWatcher;
    private ObjectAccessControl authz;


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
    protected synchronized void setPersistenceUtil ( PersistenceUtil pu ) {
        this.persistenceUtil = pu;
    }


    protected synchronized void unsetPersistenceUtil ( PersistenceUtil pu ) {
        if ( this.persistenceUtil == pu ) {
            this.persistenceUtil = null;
        }
    }


    @Reference
    protected synchronized void setAgentConnectorWatcher ( AgentConnectorWatcher acw ) {
        this.agentConnectorWatcher = acw;
    }


    protected synchronized void unsetAgentConnectorWatcher ( AgentConnectorWatcher acw ) {
        if ( this.agentConnectorWatcher == acw ) {
            this.agentConnectorWatcher = null;
        }
    }


    @Reference
    protected synchronized void setObjectAccessControl ( ObjectAccessControl oac ) {
        this.authz = oac;
    }


    protected synchronized void unsetObjectAccessControl ( ObjectAccessControl oac ) {
        if ( this.authz == oac ) {
            this.authz = null;
        }
    }

    private AgentServerService agentService;


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
     * @see eu.agno3.orchestrator.config.model.realm.server.service.InstanceServerService#getInstanceForAgent(java.util.UUID)
     */
    @Override
    @RequirePermissions ( "structure:view:byAgentId:INSTANCE" )
    public @NonNull InstanceStructuralObject getInstanceForAgent ( @NonNull UUID agentId )
            throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.getConfigEMF().createEntityManager();
        if ( em == null ) {
            throw new ModelServiceException();
        }
        return getInstanceForAgent(em, agentId);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.InstanceServerService#getInstanceForAgent(javax.persistence.EntityManager,
     *      java.util.UUID)
     */
    @Override
    public @NonNull InstanceStructuralObjectImpl getInstanceForAgent ( @NonNull EntityManager em, @NonNull UUID agentId )
            throws ModelObjectNotFoundException, ModelServiceException {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();

            CriteriaQuery<InstanceStructuralObjectImpl> cq = cb.createQuery(InstanceStructuralObjectImpl.class);
            Root<InstanceStructuralObjectImpl> tbl = cq.from(InstanceStructuralObjectImpl.class);

            cq.where(cb.equal(tbl.get(tbl.getModel().getSingularAttribute(AGENT_ID_ATTR, UUID.class)), agentId));

            InstanceStructuralObjectImpl instance = em.createQuery(cq).getSingleResult();
            this.authz.checkAccess(instance, "structure:view:byAgentId:INSTANCE"); //$NON-NLS-1$

            if ( instance == null ) {
                throw new ModelObjectNotFoundException(new ModelObjectNotFoundFault(InstanceStructuralObject.class, agentId));
            }

            return instance;
        }
        catch ( NoResultException e ) {
            throw new ModelObjectNotFoundException(new ModelObjectNotFoundFault(InstanceStructuralObject.class, agentId), e);
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch agent", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.server.service.InstanceServerService#getAgentId(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject)
     */
    @Override
    @RequirePermissions ( "structure:view:agentId:INSTANCE" )
    public @NonNull UUID getAgentId ( @NonNull InstanceStructuralObject host ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.getConfigEMF().createEntityManager();
        try {
            InstanceStructuralObjectImpl persistent = this.persistenceUtil.fetch(em, InstanceStructuralObjectImpl.class, host);
            this.authz.checkAccess(persistent, "structure:view:agentId:INSTANCE"); //$NON-NLS-1$
            UUID agentId = persistent.getAgentId();

            if ( agentId == null ) {
                throw new ModelServiceException("Agent id is null"); //$NON-NLS-1$
            }

            return agentId;
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to get agent id", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.InstanceService#getServices(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject)
     */
    @Override
    @RequirePermissions ( "structure:view:services:SERVICE" )
    public Set<ServiceStructuralObject> getServices ( InstanceStructuralObject host ) throws ModelServiceException, ModelObjectNotFoundException {
        EntityManager em = this.sctx.getConfigEMF().createEntityManager();

        try {
            InstanceStructuralObjectImpl persistent = this.persistenceUtil.fetch(em, InstanceStructuralObjectImpl.class, host);

            Set<ServiceStructuralObject> res = new HashSet<>();
            for ( AbstractStructuralObjectImpl child : TreeUtil.getDirectChildren(em, AbstractStructuralObjectImpl.class, persistent) ) {
                if ( ! ( child instanceof ServiceStructuralObject ) ) {
                    continue;
                }

                if ( !this.authz.hasAccess(child, "structure:view:services:SERVICE") ) { //$NON-NLS-1$
                    continue;
                }

                res.add((ServiceStructuralObject) child);
            }
            return res;
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch services", e); //$NON-NLS-1$
        }
    }


    @Override
    @RequirePermissions ( "structure:view:imageTypes" )
    public Set<String> getAvailableImageTypes () {
        return this.sctx.getImageTypeRegistry().getImageTypes();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.service.InstanceService#getDetachedAgents()
     */
    @Override
    @RequirePermissions ( "agents:list:detached" )
    public Set<AgentInfo> getDetachedAgents () {
        Map<UUID, AgentInfo> knownAgents = new HashMap<>(this.agentConnectorWatcher.getActiveComponents());

        if ( knownAgents.isEmpty() ) {
            return Collections.EMPTY_SET;
        }

        EntityManager em = this.sctx.getConfigEMF().createEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<UUID> cq = cb.createQuery(UUID.class);
        Root<InstanceStructuralObjectImpl> tbl = cq.from(InstanceStructuralObjectImpl.class);

        Path<UUID> agentId = tbl.get(tbl.getModel().getSingularAttribute(AGENT_ID_ATTR, UUID.class));
        cq.where(agentId.in(knownAgents.keySet()));
        cq.select(agentId);

        for ( UUID assigned : em.createQuery(cq).getResultList() ) {
            knownAgents.remove(assigned);
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Found %d detached agents", knownAgents.size())); //$NON-NLS-1$
        }

        return new HashSet<>(knownAgents.values());
    }


    /**
     * 
     * @param host
     * @return the agent state
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    @Override
    @RequirePermissions ( "structure:view:agentState:INSTANCE" )
    public String getAgentState ( InstanceStructuralObject host ) throws ModelServiceException, ModelObjectNotFoundException {
        EntityManager em = this.sctx.getConfigEMF().createEntityManager();
        try {
            InstanceStructuralObjectImpl persistent = this.persistenceUtil.fetch(em, InstanceStructuralObjectImpl.class, host);
            this.authz.checkAccess(persistent, "structure:view:agentState:INSTANCE"); //$NON-NLS-1$

            UUID agentId = persistent.getAgentId();
            if ( agentId == null ) {
                return ComponentState.UNKNOWN.name();
            }

            return this.agentConnectorWatcher.getComponentConnectorState(agentId).name();
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch instance", e); //$NON-NLS-1$
        }
    }


    @Override
    @RequirePermissions ( "structure:view:agentState:INSTANCE" )
    public AgentInfo getAgentInfo ( InstanceStructuralObject host ) throws ModelServiceException, ModelObjectNotFoundException {
        EntityManager em = this.sctx.getConfigEMF().createEntityManager();
        try {
            InstanceStructuralObjectImpl persistent = this.persistenceUtil.fetch(em, InstanceStructuralObjectImpl.class, host);
            this.authz.checkAccess(persistent, "structure:view:agentState:INSTANCE"); //$NON-NLS-1$

            UUID agentId = persistent.getAgentId();
            if ( agentId == null ) {
                return null;
            }

            return this.agentConnectorWatcher.getAgentInfo(agentId);
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch instance", e); //$NON-NLS-1$
        }

    }

}
