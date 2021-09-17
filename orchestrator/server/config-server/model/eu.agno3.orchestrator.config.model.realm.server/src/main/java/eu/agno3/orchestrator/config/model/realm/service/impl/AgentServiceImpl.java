/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.impl;


import java.util.UUID;

import javax.persistence.EntityManager;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageTarget;
import eu.agno3.orchestrator.agent.server.AgentConnectorWatcher;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.exceptions.faults.AgentCommunicationErrorFault;
import eu.agno3.orchestrator.config.model.base.exceptions.faults.AgentDetachedFault;
import eu.agno3.orchestrator.config.model.base.exceptions.faults.AgentOfflineFault;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.server.service.AgentServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.jobs.JobCoordinator;
import eu.agno3.orchestrator.jobs.JobImpl;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.targets.AgentTarget;
import eu.agno3.orchestrator.server.component.ComponentState;


/**
 * @author mbechler
 *
 */
@Component ( service = AgentServerService.class )
public class AgentServiceImpl implements AgentServerService {

    private ObjectAccessControl authz;
    private DefaultServerServiceContext sctx;
    private PersistenceUtil persistenceUtil;
    private JobCoordinator jobCoord;
    private AgentConnectorWatcher agentConnectorWatcher;


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
    protected synchronized void setObjectAccessControl ( ObjectAccessControl oac ) {
        this.authz = oac;
    }


    protected synchronized void unsetObjectAccessControl ( ObjectAccessControl oac ) {
        if ( this.authz == oac ) {
            this.authz = null;
        }
    }


    @Reference
    protected synchronized void setAgentConnectorWatcher ( AgentConnectorWatcher agc ) {
        this.agentConnectorWatcher = agc;
    }


    protected synchronized void unsetAgentConnectorWatcher ( AgentConnectorWatcher agc ) {
        if ( this.agentConnectorWatcher == agc ) {
            this.agentConnectorWatcher = null;
        }
    }


    @Reference
    protected synchronized void setJobCoordinator ( JobCoordinator jc ) {
        this.jobCoord = jc;
    }


    protected synchronized void unsetJobCoordinator ( JobCoordinator jc ) {
        if ( this.jobCoord == jc ) {
            this.jobCoord = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.AgentServerService#getAgentID(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject)
     */
    @Override
    public @NonNull UUID getAgentID ( @Nullable InstanceStructuralObject instance )
            throws ModelObjectNotFoundException, ModelServiceException, AgentDetachedException {
        EntityManager em = this.sctx.getConfigEMF().createEntityManager();
        InstanceStructuralObject persistent = this.persistenceUtil.fetch(em, InstanceStructuralObjectImpl.class, instance);

        return getAgentIdInternal(persistent);
    }


    /**
     * @param persistent
     * @return
     * @throws AgentDetachedException
     */
    private @NonNull static UUID getAgentIdInternal ( InstanceStructuralObject persistent ) throws AgentDetachedException {
        UUID agentId = persistent.getAgentId();
        if ( agentId == null ) {
            throw new AgentDetachedException("Instance is detached", new AgentDetachedFault((String) null, persistent.getDisplayName())); //$NON-NLS-1$
        }
        return agentId;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.AgentServerService#getMessageTarget(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject)
     */
    @Override
    public @NonNull AgentMessageTarget getMessageTarget ( @Nullable InstanceStructuralObject instance )
            throws ModelObjectNotFoundException, AgentDetachedException, ModelServiceException {
        return new AgentMessageTarget(getAgentID(instance));
    }


    /**
     * {@inheritDoc}
     * 
     * @throws AgentOfflineException
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.AgentServerService#ensureAgentOnline(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject)
     */
    @Override
    public @NonNull AgentMessageTarget ensureAgentOnline ( @Nullable InstanceStructuralObject instance )
            throws ModelObjectNotFoundException, ModelServiceException, AgentDetachedException, AgentOfflineException {
        EntityManager em = this.sctx.getConfigEMF().createEntityManager();
        InstanceStructuralObject persistent = this.persistenceUtil.fetch(em, InstanceStructuralObjectImpl.class, instance);
        @NonNull
        UUID agentID = getAgentIdInternal(persistent);
        if ( this.agentConnectorWatcher.getComponentConnectorState(agentID) != ComponentState.CONNECTED ) {
            throw new AgentOfflineException(new AgentOfflineFault(agentID, persistent.getDisplayName()));
        }

        return new AgentMessageTarget(agentID);
    }


    /**
     * {@inheritDoc}
     * 
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.AgentServerService#isAgentOnline(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject)
     */
    @Override
    public boolean isAgentOnline ( @Nullable InstanceStructuralObject instance ) throws ModelObjectNotFoundException, ModelServiceException {
        EntityManager em = this.sctx.getConfigEMF().createEntityManager();
        InstanceStructuralObject persistent = this.persistenceUtil.fetch(em, InstanceStructuralObjectImpl.class, instance);
        UUID agentID = persistent.getAgentId();
        if ( agentID == null ) {
            return false;
        }
        return this.agentConnectorWatcher.getComponentConnectorState(agentID) == ComponentState.CONNECTED;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws AgentDetachedException
     *
     * @see eu.agno3.orchestrator.config.model.realm.server.service.AgentServerService#handleCommFault(java.lang.Throwable,
     *      eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject)
     */
    @Override
    public @NonNull AgentCommunicationErrorFault handleCommFault ( @Nullable Throwable cause, @Nullable InstanceStructuralObject instance )
            throws ModelObjectNotFoundException, ModelServiceException, AgentDetachedException {
        EntityManager em = this.sctx.getConfigEMF().createEntityManager();
        InstanceStructuralObject persistent = this.persistenceUtil.fetch(em, InstanceStructuralObjectImpl.class, instance);
        @NonNull
        UUID agentID = getAgentIdInternal(persistent);
        return new AgentCommunicationErrorFault(agentID, persistent.getDisplayName());
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws AgentDetachedException
     * @throws ModelObjectNotFoundException
     * @throws AgentCommunicationErrorException
     *
     */
    @Override
    public @NonNull JobInfo submitJob ( @Nullable InstanceStructuralObject instance, @NonNull JobImpl j )
            throws ModelObjectNotFoundException, AgentDetachedException, ModelServiceException, AgentCommunicationErrorException {
        EntityManager em = this.sctx.getConfigEMF().createEntityManager();
        InstanceStructuralObject persistent = this.persistenceUtil.fetch(em, InstanceStructuralObjectImpl.class, instance);
        @NonNull
        UUID agentID = getAgentIdInternal(persistent);
        AgentTarget target = new AgentTarget(agentID);
        j.setTarget(target);
        try {
            JobInfo info = this.jobCoord.queueJob(j);

            if ( info == null ) {
                throw new ModelServiceException("Job queuing failed"); //$NON-NLS-1$
            }
            return info;
        }
        catch ( JobQueueException e ) {
            throw new AgentCommunicationErrorException("Failed to queue config job", new AgentCommunicationErrorFault( //$NON-NLS-1$
                agentID,
                persistent.getDisplayName()), e);
        }
    }
}
