/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.instance.sysinfo;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.system.info.service.DiskManagerService;
import eu.agno3.orchestrator.system.info.storage.VolumeCreationInformation;
import eu.agno3.orchestrator.system.info.storage.drive.Drive;
import eu.agno3.orchestrator.system.info.storage.volume.Volume;
import eu.agno3.runtime.jsf.view.stacking.DialogConstants;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "diskManageController" )
public class DiskManageController {

    @Inject
    private ServerServiceProvider ssp;


    public String rescan ( InstanceStructuralObject instance ) {
        try {
            return job(this.ssp.getService(DiskManagerService.class).rescanDevices(instance));
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    public String expandVolume ( InstanceStructuralObject instance, Drive d, Volume v ) {
        try {
            return job(this.ssp.getService(DiskManagerService.class).expandVolume(instance, d.getId(), v.getDevice()));
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    /**
     * @param d
     * @param v
     * @return
     */
    private static VolumeCreationInformation makeVolumeInformation ( Drive d, Volume v, boolean force, FilesystemProperties fsProps ) {
        VolumeCreationInformation volinfo = new VolumeCreationInformation();
        volinfo.setDriveId(d.getId());
        if ( v != null ) {
            volinfo.setVolume(v.getDevice());
        }
        volinfo.setForce(false);
        volinfo.setLabel(fsProps.getLabel());
        return volinfo;
    }


    public String initializeVolume ( InstanceStructuralObject instance, Drive d, Volume v, FilesystemProperties fsProps ) {
        try {
            return job(this.ssp.getService(DiskManagerService.class).initialize(instance, makeVolumeInformation(d, v, false, fsProps)));
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    public String reinitializeVolume ( InstanceStructuralObject instance, Drive d, Volume v, FilesystemProperties fsProps ) {
        try {
            return job(this.ssp.getService(DiskManagerService.class).initialize(instance, makeVolumeInformation(d, v, true, fsProps)));
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    public String initializeDrive ( InstanceStructuralObject instance, Drive d, FilesystemProperties fsProps ) {
        try {
            return job(this.ssp.getService(DiskManagerService.class).initialize(instance, makeVolumeInformation(d, null, false, fsProps)));
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    public String reinitializeDrive ( InstanceStructuralObject instance, Drive d, FilesystemProperties fsProps ) {
        try {
            return job(this.ssp.getService(DiskManagerService.class).initialize(instance, makeVolumeInformation(d, null, true, fsProps)));
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    /**
     * @param job
     * @return
     */
    private static String job ( JobInfo job ) {
        return String.format(
            "/jobs/showJobDetailDialog.xhtml?faces-redirect=true&cid=&jobId=%s&%s=%s", //$NON-NLS-1$
            job.getJobId(),
            DialogConstants.RETURN_TO_ATTR,
            DialogContext.getCurrentParent());
    }
}
