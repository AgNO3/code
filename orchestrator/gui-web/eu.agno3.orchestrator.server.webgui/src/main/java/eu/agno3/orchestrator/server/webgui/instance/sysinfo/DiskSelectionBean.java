/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2016 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.instance.sysinfo;


import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.system.info.storage.StorageInformation;
import eu.agno3.orchestrator.system.info.storage.drive.Drive;
import eu.agno3.orchestrator.system.info.storage.volume.Volume;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "diskSelectionBean" )
public class DiskSelectionBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5039413387017695698L;

    @Inject
    private AgentSysInfoContextBean sysInfoBean;

    private String selectedDriveId;

    private String selectedVolumeId;

    private Drive cachedDrive;

    private Volume selectedVolume;


    public String getSelectedDriveId () {
        return this.selectedDriveId;
    }


    public void setSelectedDriveId ( String selectedDriveId ) {
        this.selectedDriveId = selectedDriveId;
    }


    public String getSelectedVolumeId () {
        return this.selectedVolumeId;
    }


    public void setSelectedVolumeId ( String selectedVolumeId ) {
        this.selectedVolumeId = selectedVolumeId;
    }


    public Drive getSelectedDrive () {
        if ( this.cachedDrive != null ) {
            return this.cachedDrive;

        }
        try {
            StorageInformation storageInformation = this.sysInfoBean.getStorageInformation();
            if ( storageInformation == null ) {
                return null;
            }
            this.cachedDrive = storageInformation.getDriveById(this.selectedDriveId);
            return this.cachedDrive;
        }
        catch ( Exception e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }


    public Volume getSelectedVolume () {
        if ( this.selectedVolume != null ) {
            return this.selectedVolume;
        }
        Drive d = getSelectedDrive();
        if ( d == null || this.selectedVolumeId == null ) {
            return null;
        }
        for ( Volume v : d.getVolumes() ) {
            if ( this.selectedVolumeId.equals(StorageTableBean.getVolRowKey(v)) ) {
                this.selectedVolume = v;
                return v;
            }
        }
        return null;
    }
}
