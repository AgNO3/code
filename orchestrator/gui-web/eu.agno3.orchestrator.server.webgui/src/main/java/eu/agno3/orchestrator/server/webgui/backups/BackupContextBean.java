/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.12.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.backups;


import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.exceptions.AgentCommunicationErrorException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentDetachedException;
import eu.agno3.orchestrator.config.model.base.exceptions.AgentOfflineException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectReferentialIntegrityException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.StructureCacheBean;
import eu.agno3.orchestrator.server.webgui.structure.StructureUtil;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;
import eu.agno3.orchestrator.system.backups.BackupInfo;
import eu.agno3.orchestrator.system.backups.ServiceBackupInfo;
import eu.agno3.orchestrator.system.backups.service.AgentBackupService;


/**
 * @author mbechler
 *
 */
@Named ( "backupContextBean" )
@ViewScoped
public class BackupContextBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2034288183221300961L;

    private static final Logger log = Logger.getLogger(BackupContextBean.class);

    @Inject
    private StructureViewContextBean structureContext;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private StructureCacheBean structureCache;

    @Inject
    private StructureUtil structureUtil;

    private List<BackupInfo> model;


    public void init ( ComponentSystemEvent ev ) {
        getModel();
    }


    public List<BackupInfo> getModel () {
        if ( this.model == null ) {
            try {
                this.model = makeModel();
            }
            catch ( Exception e ) {
                ExceptionHandler.handle(e);
                return Collections.EMPTY_LIST;
            }
        }
        return this.model;
    }


    public Object refresh () {
        try {
            this.model = makeModel();
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    public String getServiceDisplayName ( ServiceBackupInfo sbi ) {
        try {
            return this.structureUtil.getDisplayName(this.structureCache.getById(sbi.getServiceId()));
        }
        catch ( Exception e ) {
            log.debug("Failed to get service name", e); //$NON-NLS-1$
        }

        return this.structureUtil.getServiceTypeDisplayName(sbi.getServiceType());
    }


    /**
     * @return
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws AgentOfflineException
     * @throws AgentDetachedException
     * @throws AgentCommunicationErrorException
     * @throws ModelObjectReferentialIntegrityException
     * @throws ModelObjectNotFoundException
     */
    private List<BackupInfo> makeModel () throws ModelObjectNotFoundException, ModelObjectReferentialIntegrityException,
            AgentCommunicationErrorException, AgentDetachedException, AgentOfflineException, ModelServiceException, GuiWebServiceException {
        return this.ssp.getService(AgentBackupService.class).listBackups(this.structureContext.getSelectedInstance());
    }


    public Object makeBackup () {
        try {
            InstanceStructuralObject instance = this.structureContext.getSelectedInstance();
            JobInfo ji = this.ssp.getService(AgentBackupService.class).makeBackup(instance);
            return "/backup/make.xhtml?faces-redirect=true&instance=" + instance.getId() + "&jobId=" + ji.getJobId(); //$NON-NLS-1$//$NON-NLS-2$
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    public Object restoreBackup ( UUID backupId ) {
        if ( backupId == null ) {
            return null;
        }
        try {
            InstanceStructuralObject instance = this.structureContext.getSelectedInstance();
            JobInfo ji = this.ssp.getService(AgentBackupService.class).restoreBackup(instance, backupId);
            return "/backup/make.xhtml?faces-redirect=true&instance=" + instance.getId() + "&jobId=" + ji.getJobId(); //$NON-NLS-1$//$NON-NLS-2$
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    public Object removeBackup ( UUID backupId ) {
        if ( backupId == null ) {
            return null;
        }
        try {
            this.model = this.ssp.getService(AgentBackupService.class).removeBackup(this.structureContext.getSelectedInstance(), backupId);
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }
}
