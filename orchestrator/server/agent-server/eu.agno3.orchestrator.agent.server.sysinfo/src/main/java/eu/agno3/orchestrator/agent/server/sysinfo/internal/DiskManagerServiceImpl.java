/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2016 by mbechler
 */
package eu.agno3.orchestrator.agent.server.sysinfo.internal;


import javax.jws.WebService;

import org.apache.shiro.SecurityUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.server.service.AgentServerService;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.system.info.jobs.ExpandVolumeJob;
import eu.agno3.orchestrator.system.info.jobs.InitializeDriveJob;
import eu.agno3.orchestrator.system.info.jobs.RescanDrivesJob;
import eu.agno3.orchestrator.system.info.service.DiskManagerService;
import eu.agno3.orchestrator.system.info.service.DiskManagerServiceDescriptor;
import eu.agno3.orchestrator.system.info.storage.VolumeCreationInformation;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 *
 */
@Component ( service = SOAPWebService.class )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.system.info.service.DiskManagerService",
    targetNamespace = DiskManagerServiceDescriptor.NAMESPACE,
    serviceName = "diskManagerService" )
@WebServiceAddress ( "/agent/diskManager" )
public class DiskManagerServiceImpl implements DiskManagerService {

    private AgentServerService agentService;


    @Reference
    protected synchronized void setAgentService ( AgentServerService iss ) {
        this.agentService = iss;
    }


    protected synchronized void unsetAgentService ( AgentServerService iss ) {
        if ( this.agentService == iss ) {
            this.agentService = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws AgentCommunicationErrorException
     *
     * @see eu.agno3.orchestrator.system.info.service.DiskManagerService#rescanDevices(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject)
     */
    @Override
    @RequirePermissions ( "sysinfo:manage:drives" )
    public JobInfo rescanDevices ( InstanceStructuralObject instance ) throws ModelObjectNotFoundException, ModelServiceException,
            AgentOfflineException, AgentDetachedException, AgentCommunicationErrorException {
        RescanDrivesJob rescanDrivesJob = new RescanDrivesJob();
        rescanDrivesJob.setOwner(SecurityUtils.getSubject().getPrincipals().oneByType(UserPrincipal.class));
        return this.agentService.submitJob(instance, rescanDrivesJob);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws AgentCommunicationErrorException
     *
     * @see eu.agno3.orchestrator.system.info.service.DiskManagerService#expandVolume(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      java.lang.String, java.lang.String)
     */
    @Override
    @RequirePermissions ( "sysinfo:manage:drives" )
    public JobInfo expandVolume ( InstanceStructuralObject instance, String drive, String vol ) throws ModelObjectNotFoundException,
            ModelServiceException, AgentOfflineException, AgentDetachedException, AgentCommunicationErrorException {
        ExpandVolumeJob expandVolumeJob = new ExpandVolumeJob();
        expandVolumeJob.setOwner(SecurityUtils.getSubject().getPrincipals().oneByType(UserPrincipal.class));
        expandVolumeJob.setDriveId(drive);
        expandVolumeJob.setVolume(vol);
        return this.agentService.submitJob(instance, expandVolumeJob);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws AgentCommunicationErrorException
     *
     * @see eu.agno3.orchestrator.system.info.service.DiskManagerService#initialize(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      eu.agno3.orchestrator.system.info.storage.VolumeCreationInformation)
     */
    @Override
    @RequirePermissions ( "sysinfo:manage:drives" )
    public JobInfo initialize ( InstanceStructuralObject instance, VolumeCreationInformation info ) throws ModelObjectNotFoundException,
            ModelServiceException, AgentOfflineException, AgentDetachedException, AgentCommunicationErrorException {
        InitializeDriveJob initializeDriveJob = new InitializeDriveJob();
        initializeDriveJob.setOwner(SecurityUtils.getSubject().getPrincipals().oneByType(UserPrincipal.class));
        initializeDriveJob.setCreationInfo(info);
        return this.agentService.submitJob(instance, initializeDriveJob);
    }

}
