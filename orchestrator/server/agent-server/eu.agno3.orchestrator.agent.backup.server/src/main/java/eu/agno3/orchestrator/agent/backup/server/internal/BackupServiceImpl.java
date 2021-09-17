/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.01.2016 by mbechler
 */
package eu.agno3.orchestrator.agent.backup.server.internal;


import java.util.List;
import java.util.UUID;

import javax.jws.WebService;

import org.apache.shiro.SecurityUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageTarget;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.server.service.AgentServerService;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.orchestrator.system.backups.BackupInfo;
import eu.agno3.orchestrator.system.backups.jobs.BackupJob;
import eu.agno3.orchestrator.system.backups.jobs.RestoreJob;
import eu.agno3.orchestrator.system.backups.msg.BackupListRequest;
import eu.agno3.orchestrator.system.backups.msg.BackupListResponse;
import eu.agno3.orchestrator.system.backups.msg.BackupRemoveRequest;
import eu.agno3.orchestrator.system.backups.service.AgentBackupService;
import eu.agno3.orchestrator.system.backups.service.AgentBackupServiceDescriptor;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.client.MessagingClient;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.RequirePermissions;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    AgentBackupService.class, SOAPWebService.class
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.system.backups.service.AgentBackupService",
    targetNamespace = AgentBackupServiceDescriptor.NAMESPACE,
    serviceName = AgentBackupServiceDescriptor.DEFAULT_SERVICE_NAME )
@WebServiceAddress ( "/agent/backup" )
public class BackupServiceImpl implements AgentBackupService {

    private MessagingClient<ServerMessageSource> msgClient;
    private DefaultServerServiceContext sctx;
    private AgentServerService agentService;


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
     * @see eu.agno3.orchestrator.system.backups.service.AgentBackupService#listBackups(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject)
     */
    @Override
    @RequirePermissions ( "backup:view" )
    public List<BackupInfo> listBackups ( InstanceStructuralObject instance ) throws ModelServiceException, ModelObjectNotFoundException,
            ModelObjectReferentialIntegrityException, AgentCommunicationErrorException, AgentDetachedException, AgentOfflineException {

        @NonNull
        AgentMessageTarget agent = this.agentService.ensureAgentOnline(instance);
        BackupListRequest req = new BackupListRequest(agent);
        req.setOrigin(this.msgClient.getMessageSource());
        try {
            @Nullable
            BackupListResponse resp = this.msgClient.sendMessage(req);
            if ( resp == null ) {
                throw new ModelServiceException();
            }

            return resp.getBackups();
        }
        catch (
            MessagingException |
            InterruptedException e ) {
            throw new AgentCommunicationErrorException(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.backups.service.AgentBackupService#makeBackup(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject)
     */
    @Override
    @RequirePermissions ( "backup:create" )
    public JobInfo makeBackup ( InstanceStructuralObject instance ) throws ModelServiceException, ModelObjectNotFoundException,
            ModelObjectReferentialIntegrityException, AgentCommunicationErrorException, AgentDetachedException, AgentOfflineException {

        BackupJob j = new BackupJob();
        j.setOwner(SecurityUtils.getSubject().getPrincipals().oneByType(UserPrincipal.class));
        return this.agentService.submitJob(instance, j);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.backups.service.AgentBackupService#restoreBackup(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      java.util.UUID)
     */
    @Override
    @RequirePermissions ( "backup:restore" )
    public JobInfo restoreBackup ( InstanceStructuralObject instance, UUID backupId ) throws ModelServiceException, ModelObjectNotFoundException,
            ModelObjectReferentialIntegrityException, AgentCommunicationErrorException, AgentDetachedException, AgentOfflineException {
        RestoreJob j = new RestoreJob();
        j.setBackupId(backupId);
        j.setOwner(SecurityUtils.getSubject().getPrincipals().oneByType(UserPrincipal.class));
        return this.agentService.submitJob(instance, j);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.backups.service.AgentBackupService#removeBackup(eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject,
     *      java.util.UUID)
     */
    @Override
    @RequirePermissions ( "backup:remove" )
    public List<BackupInfo> removeBackup ( InstanceStructuralObject instance, UUID backupId )
            throws ModelServiceException, ModelObjectNotFoundException, ModelObjectReferentialIntegrityException, AgentCommunicationErrorException,
            AgentDetachedException, AgentOfflineException {
        @NonNull
        AgentMessageTarget agent = this.agentService.ensureAgentOnline(instance);
        BackupRemoveRequest req = new BackupRemoveRequest(agent);
        req.setOrigin(this.msgClient.getMessageSource());
        req.setBackupId(backupId);
        try {
            @Nullable
            BackupListResponse resp = this.msgClient.sendMessage(req);
            if ( resp == null ) {
                throw new ModelServiceException();
            }

            return resp.getBackups();
        }
        catch (
            MessagingException |
            InterruptedException e ) {
            throw new AgentCommunicationErrorException(e);
        }
    }

}
