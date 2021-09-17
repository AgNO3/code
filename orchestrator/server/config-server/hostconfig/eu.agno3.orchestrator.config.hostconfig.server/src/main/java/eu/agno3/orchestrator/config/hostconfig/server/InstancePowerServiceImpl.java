/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.server;


import java.util.Collection;
import java.util.UUID;

import javax.jws.WebService;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.hostconfig.jobs.RebootJob;
import eu.agno3.orchestrator.config.hostconfig.jobs.ShutdownJob;
import eu.agno3.orchestrator.config.hostconfig.service.InstancePowerService;
import eu.agno3.orchestrator.config.hostconfig.service.InstancePowerServiceDescriptor;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.jobs.JobCoordinator;
import eu.agno3.orchestrator.jobs.JobImpl;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.jobs.exceptions.JobQueueException;
import eu.agno3.orchestrator.jobs.targets.AgentTarget;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    InstancePowerService.class, SOAPWebService.class,
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.config.hostconfig.service.InstancePowerService",
    targetNamespace = InstancePowerServiceDescriptor.NAMESPACE,
    serviceName = InstancePowerServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/realm/hostconfig/power" )
public class InstancePowerServiceImpl implements InstancePowerService {

    private static final String POWER_SHUTDOWN = "power:shutdown"; //$NON-NLS-1$
    private static final String POWER_REBOOT = "power:reboot"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(InstancePowerServiceImpl.class);

    private JobCoordinator coord;
    private ObjectAccessControl authz;
    private DefaultServerServiceContext sctx;
    private PersistenceUtil persistenceUtil;


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
    protected synchronized void setJobCoordinator ( JobCoordinator jobCoord ) {
        this.coord = jobCoord;
    }


    protected synchronized void unsetJobCoordinator ( JobCoordinator jobCoord ) {
        if ( this.coord == jobCoord ) {
            this.coord = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelObjectNotFoundException
     *
     * @see eu.agno3.orchestrator.config.hostconfig.service.InstancePowerService#reboot(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject)
     */
    @Override
    @RequirePermissions ( POWER_REBOOT )
    public JobInfo reboot ( InstanceStructuralObject instance ) throws ModelServiceException, ModelObjectNotFoundException {

        InstanceStructuralObject persistent = getPersistentInstance(instance);
        this.authz.checkAccess(persistent, POWER_REBOOT);

        if ( log.isDebugEnabled() ) {
            log.debug("Rebooting instance " + persistent); //$NON-NLS-1$
        }

        try {
            RebootJob rebootJob = new RebootJob();
            setupAgentJob(persistent, rebootJob);
            return this.coord.queueJob(rebootJob);
        }
        catch ( JobQueueException e ) {
            throw new ModelServiceException("Failed to queue reboot job", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     *
     * @see eu.agno3.orchestrator.config.hostconfig.service.InstancePowerService#shutdown(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject)
     */
    @Override
    @RequirePermissions ( POWER_SHUTDOWN )
    public JobInfo shutdown ( InstanceStructuralObject instance ) throws ModelServiceException, ModelObjectNotFoundException {
        InstanceStructuralObject persistent = getPersistentInstance(instance);
        this.authz.checkAccess(persistent, POWER_SHUTDOWN);

        if ( log.isDebugEnabled() ) {
            log.debug("Shutting down instance " + persistent); //$NON-NLS-1$
        }

        try {
            ShutdownJob shutdownJob = new ShutdownJob();
            setupAgentJob(persistent, shutdownJob);
            return this.coord.queueJob(shutdownJob);
        }
        catch ( JobQueueException e ) {
            throw new ModelServiceException("Failed to queue reboot job", e); //$NON-NLS-1$
        }
    }


    /**
     * @param persistent
     * @param shutdownJob
     * @throws ModelServiceException
     */
    private static void setupAgentJob ( InstanceStructuralObject persistent, JobImpl shutdownJob ) throws ModelServiceException {
        shutdownJob.setOwner(getUserPrincipal());
        UUID agentId = persistent.getAgentId();

        if ( agentId == null ) {
            throw new ModelServiceException("No agent id known"); //$NON-NLS-1$
        }

        shutdownJob.setTarget(new AgentTarget(agentId));
    }


    /**
     * @param instance
     * @return
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    private InstanceStructuralObject getPersistentInstance ( InstanceStructuralObject instance ) throws ModelObjectNotFoundException,
            ModelServiceException {
        EntityManager em = this.sctx.getConfigEMF().createEntityManager();
        InstanceStructuralObject persistent = this.persistenceUtil.fetch(em, InstanceStructuralObjectImpl.class, instance);

        if ( persistent.getAgentId() == null ) {
            throw new ModelServiceException("Instance is detached"); //$NON-NLS-1$
        }
        return persistent;
    }


    /**
     * @return
     * @throws ModelServiceException
     */
    private static UserPrincipal getUserPrincipal () throws ModelServiceException {
        Collection<UserPrincipal> ups = SecurityUtils.getSubject().getPrincipals().byType(UserPrincipal.class);

        if ( ups.size() != 1 ) {
            throw new ModelServiceException("Failed to determine user principal"); //$NON-NLS-1$
        }

        return ups.iterator().next();
    }

}
