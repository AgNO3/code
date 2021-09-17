/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.10.2016 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.server;


import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.hostconfig.jobs.SetAdminPasswordJob;
import eu.agno3.orchestrator.config.hostconfig.service.InstanceManageService;
import eu.agno3.orchestrator.config.hostconfig.service.InstanceManageServiceDescriptor;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.server.service.AgentServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;
import eu.agno3.orchestrator.config.model.realm.server.util.PersistenceUtil;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.runtime.security.credentials.WrappedCredentials;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    InstanceManageService.class, SOAPWebService.class,
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.config.hostconfig.service.InstanceManageService",
    targetNamespace = InstanceManageServiceDescriptor.NAMESPACE,
    serviceName = InstanceManageServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/realm/hostconfig/manage" )
public class InstanceManageServiceImpl implements InstanceManageService {

    private static final Logger log = Logger.getLogger(InstanceManageServiceImpl.class);
    private DefaultServerServiceContext sctx;
    private PersistenceUtil persistenceUtil;
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
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.service.InstanceManageService#setAdministratorPassword(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      eu.agno3.runtime.security.credentials.WrappedCredentials,
     *      eu.agno3.runtime.security.credentials.WrappedCredentials)
     */
    @Override
    @RequirePermissions ( "structure:manage:instance:setAdminPassword" )
    public @NonNull JobInfo setAdministratorPassword ( InstanceStructuralObject host, WrappedCredentials old, WrappedCredentials creds )
            throws ModelServiceException, ModelObjectNotFoundException, AgentDetachedException, AgentCommunicationErrorException {

        try {
            EntityManager em = this.sctx.createConfigEM();
            InstanceStructuralObjectImpl persistent = this.persistenceUtil.fetch(em, InstanceStructuralObjectImpl.class, host);
            this.authz.checkAccess(persistent, "structure:manage:instance:setAdminPassword"); //$NON-NLS-1$

            log.info("Setting new administrator password on " + host.getDisplayName()); //$NON-NLS-1$

            SetAdminPasswordJob j = new SetAdminPasswordJob();
            j.setOldCredentials(old);
            j.setNewCredentials(creds);
            return this.agentService.submitJob(persistent, j);
        }
        catch ( PersistenceException e ) {
            throw new ModelServiceException("Failed to fetch instance", e); //$NON-NLS-1$
        }
    }
}
