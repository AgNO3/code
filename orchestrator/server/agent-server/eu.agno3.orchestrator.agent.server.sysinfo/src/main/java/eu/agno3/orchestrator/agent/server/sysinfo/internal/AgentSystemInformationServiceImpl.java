/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.03.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.server.sysinfo.internal;


import java.util.UUID;

import javax.jws.WebService;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.server.service.AgentServerService;
import eu.agno3.orchestrator.config.model.realm.server.service.ObjectAccessControl;
import eu.agno3.orchestrator.system.info.SystemInformationException;
import eu.agno3.orchestrator.system.info.msg.AgentSystemInformation;
import eu.agno3.orchestrator.system.info.network.NetworkInformation;
import eu.agno3.orchestrator.system.info.platform.PlatformInformation;
import eu.agno3.orchestrator.system.info.service.AgentSystemInformationService;
import eu.agno3.orchestrator.system.info.service.AgentSystemInformationServiceDescriptor;
import eu.agno3.orchestrator.system.info.storage.StorageInformation;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 * 
 */
@Component ( service = SOAPWebService.class )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.system.info.service.AgentSystemInformationService",
    targetNamespace = AgentSystemInformationServiceDescriptor.NAMESPACE,
    serviceName = "agentSysInfoService" )
@WebServiceAddress ( "/agent/sysInfo" )
public class AgentSystemInformationServiceImpl implements AgentSystemInformationService {

    private AgentSystemInformationTracker sysInfoTracker;
    private DefaultServerServiceContext context;
    private AgentServerService agentService;
    private ObjectAccessControl authz;


    @Reference
    protected synchronized void setAgentSysInfoTracker ( AgentSystemInformationTracker tracker ) {
        this.sysInfoTracker = tracker;
    }


    protected synchronized void unsetAgentSysInfoTracker ( AgentSystemInformationTracker tracker ) {
        if ( this.sysInfoTracker == tracker ) {
            this.sysInfoTracker = null;
        }
    }


    @Reference
    protected synchronized void setContext ( DefaultServerServiceContext ctx ) {
        this.context = ctx;
    }


    protected synchronized void unsetContext ( DefaultServerServiceContext ctx ) {
        if ( this.context == ctx ) {
            this.context = null;
        }
    }


    @Reference
    protected synchronized void setAgentService ( AgentServerService ass ) {
        this.agentService = ass;
    }


    protected synchronized void unsetAgentService ( AgentServerService ass ) {
        if ( this.agentService == ass ) {
            this.agentService = null;
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


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * 
     * @see eu.agno3.orchestrator.system.info.service.AgentSystemInformationService#triggerRefresh(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject)
     */
    @Override
    @RequirePermissions ( "sysinfo:refresh" )
    public void triggerRefresh ( InstanceStructuralObject host )
            throws ModelObjectNotFoundException, ModelServiceException, AgentOfflineException, AgentDetachedException {
        this.authz.checkAccess(host, "sysinfo:refresh"); //$NON-NLS-1$

        if ( host == null ) {
            throw new ModelServiceException();
        }

        this.sysInfoTracker.refreshAgent(this.agentService.ensureAgentOnline(host).getAgentId());
    }


    private AgentSystemInformation getAgentSystemInformation ( @NonNull InstanceStructuralObject host )
            throws ModelObjectNotFoundException, ModelServiceException, AgentDetachedException, AgentOfflineException {
        UUID agentId = this.agentService.ensureAgentOnline(host).getAgentId();
        return this.sysInfoTracker.getInformation(agentId);
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * 
     * @see eu.agno3.orchestrator.system.info.service.AgentSystemInformationService#getPlatformInformation(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject)
     */
    @Override
    @RequirePermissions ( "sysinfo:view:platform" )
    public PlatformInformation getPlatformInformation ( InstanceStructuralObject host )
            throws SystemInformationException, ModelObjectNotFoundException, ModelServiceException, AgentDetachedException, AgentOfflineException {
        this.authz.checkAccess(host, "sysinfo:view:platform"); //$NON-NLS-1$

        if ( host == null ) {
            throw new ModelServiceException();
        }

        this.authz.checkAccess(host, "sysinfo:view:platform"); //$NON-NLS-1$
        AgentSystemInformation agentSystemInformation = getAgentSystemInformation(host);
        if ( agentSystemInformation == null ) {
            return null;
        }
        return agentSystemInformation.getPlatformInformation();
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * 
     * @see eu.agno3.orchestrator.system.info.service.AgentSystemInformationService#getNetworkInformation(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject)
     */
    @Override
    @RequirePermissions ( "sysinfo:view:network" )
    public NetworkInformation getNetworkInformation ( InstanceStructuralObject host )
            throws SystemInformationException, ModelObjectNotFoundException, ModelServiceException, AgentDetachedException, AgentOfflineException {

        if ( host == null ) {
            throw new ModelServiceException();
        }

        this.authz.checkAccess(host, "sysinfo:view:network"); //$NON-NLS-1$
        AgentSystemInformation agentSystemInformation = getAgentSystemInformation(host);
        if ( agentSystemInformation == null ) {
            return null;
        }
        return agentSystemInformation.getNetworkInformation();
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * 
     * @see eu.agno3.orchestrator.system.info.service.AgentSystemInformationService#getStorageInformation(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject)
     */
    @Override
    @RequirePermissions ( "sysinfo:view:storage" )
    public StorageInformation getStorageInformation ( InstanceStructuralObject host )
            throws SystemInformationException, ModelObjectNotFoundException, ModelServiceException, AgentDetachedException, AgentOfflineException {

        if ( host == null ) {
            throw new ModelServiceException();
        }

        this.authz.checkAccess(host, "sysinfo:view:storage"); //$NON-NLS-1$
        AgentSystemInformation agentSystemInformation = getAgentSystemInformation(host);
        if ( agentSystemInformation == null ) {
            return null;
        }
        return agentSystemInformation.getStorageInformation();

    }

}
