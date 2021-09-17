/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.03.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.instance.sysinfo;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.orchestrator.system.info.storage.drive.Drive;
import eu.agno3.orchestrator.system.info.storage.drive.PartitionTableType;
import eu.agno3.orchestrator.system.info.storage.drive.PhysicalDrive;
import eu.agno3.orchestrator.system.info.storage.drive.RAIDDrive;
import eu.agno3.orchestrator.system.info.storage.drive.RAIDLevel;
import eu.agno3.orchestrator.system.info.storage.drive.VolumeGroup;
import eu.agno3.orchestrator.system.info.storage.fs.DataFileSystem;
import eu.agno3.orchestrator.system.info.storage.fs.FileSystem;
import eu.agno3.orchestrator.system.info.storage.fs.FileSystemType;
import eu.agno3.orchestrator.system.info.storage.fs.SwapFileSystem;
import eu.agno3.orchestrator.system.info.storage.volume.LogicalVolume;
import eu.agno3.orchestrator.system.info.storage.volume.PhysicalVolume;
import eu.agno3.orchestrator.system.info.storage.volume.SystemVolume;
import eu.agno3.orchestrator.system.info.storage.volume.Volume;


/**
 * @author mbechler
 * 
 */
@Named ( "agentSysInfoStorageTable" )
@ViewScoped
public class StorageTableBean implements Serializable {

    private static final Logger log = Logger.getLogger(StorageTableBean.class);
    private static final long serialVersionUID = 1438773421459938335L;

    private Drive selectedDrive;
    private Volume selectedVolume;

    @Inject
    AgentSysInfoContextBean sysInfo;

    private boolean includeSystem;


    /**
     * @return the includeSystem
     */
    public boolean getIncludeSystem () {
        return this.includeSystem;
    }


    /**
     * @param includeSystem
     *            the includeSystem to set
     */
    public void setIncludeSystem ( boolean includeSystem ) {
        this.includeSystem = includeSystem;
    }


    /**
     * @return the root
     * @throws GuiWebServiceException
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public List<Drive> getDrives () throws ModelObjectNotFoundException, ModelServiceException, GuiWebServiceException {

        if ( this.sysInfo.getStorageInformation() == null ) {
            return Collections.EMPTY_LIST;
        }

        List<Drive> drives = new ArrayList<>(this.sysInfo.getStorageInformation().getDrives());

        if ( !this.includeSystem ) {
            Iterator<Drive> iterator = drives.iterator();
            while ( iterator.hasNext() ) {
                Drive d = iterator.next();
                if ( d.getSystem() ) {
                    iterator.remove();
                }
            }
        }

        Collections.sort(drives, new DriveDisplayComparator());
        return drives;

    }


    public static String getVolRowKey ( Volume v ) {
        if ( v instanceof PhysicalVolume ) {
            return v.getDrive().getId().concat(String.valueOf( ( (PhysicalVolume) v ).getIndex()));
        }
        else if ( v instanceof LogicalVolume ) {
            return v.getDrive().getId().concat( ( (LogicalVolume) v ).getName());
        }

        throw new IllegalArgumentException("Failed to generate row key for volume " + v); //$NON-NLS-1$
    }


    public List<Volume> getSelectedDriveVolumes () {
        if ( !this.isDriveSelected() ) {
            return Collections.EMPTY_LIST;
        }

        List<Volume> vols = new ArrayList<>(this.getSelectedDrive().getVolumes());
        Collections.sort(vols, new VolumeDisplayComparator());
        return vols;
    }


    /**
     * @return the selectedDrive
     */
    public Drive getSelectedDrive () {
        return this.selectedDrive;
    }


    /**
     * @param selectedDrive
     *            the selectedDrive to set
     */
    public void setSelectedDrive ( Drive selectedDrive ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Selected drive " + selectedDrive); //$NON-NLS-1$
        }
        this.selectedDrive = selectedDrive;
        this.selectedVolume = null;
    }


    public boolean isDriveSelected () {
        return this.selectedDrive != null;
    }


    /**
     * @return the selectedVolume
     */
    public Volume getSelectedVolume () {
        return this.selectedVolume;
    }


    /**
     * @param selectedVolume
     *            the selectedVolume to set
     */
    public void setSelectedVolume ( Volume selectedVolume ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Selected volume " + selectedVolume); //$NON-NLS-1$
        }
        this.selectedVolume = selectedVolume;
    }


    public boolean isVolumeSelected () {
        return this.selectedDrive != null && this.selectedVolume != null;
    }


    public String getPanelActive () {
        if ( this.isVolumeSelected() && this.selectedVolume.holdsFilesystem() ) {
            return "0,1,2"; //$NON-NLS-1$
        }
        else if ( this.isDriveSelected() ) {
            return "0,1"; //$NON-NLS-1$
        }
        return "0"; //$NON-NLS-1$
    }


    public void setPanelActive ( String active ) {
        // ignore
    }


    public static String getDriveDisplayName ( Drive d ) {
        if ( d instanceof VolumeGroup ) {
            return getVolumeGroupDisplayName((VolumeGroup) d);
        }
        else if ( d instanceof RAIDDrive ) {
            return getRAIDDriveDisplayName((RAIDDrive) d);
        }
        else if ( d instanceof PhysicalDrive ) {
            return getPhyiscalDriveDisplayName((PhysicalDrive) d);
        }

        return StringUtils.EMPTY;
    }


    /**
     * @param d
     * @return
     */
    private static String getPhyiscalDriveDisplayName ( PhysicalDrive d ) {
        return GuiMessages.format(
            "structure.instance.sysinfo.storage.physicalDiskDisplayFmt", //$NON-NLS-1$
            d.getVendor(),
            d.getModel());
    }


    /**
     * @param d
     * @return
     */
    private static String getRAIDDriveDisplayName ( RAIDDrive d ) {
        return GuiMessages.format(
            "structure.instance.sysinfo.storage.raidDisplayFmt", //$NON-NLS-1$
            translateRaidLevel(d.getRaidLevel()),
            d.getNumDevices());
    }


    /**
     * @param raidLevel
     * @return translation for raid level
     */
    public static String translateRaidLevel ( RAIDLevel raidLevel ) {
        return GuiMessages.get("RAIDLevel." + raidLevel.name());//$NON-NLS-1$
    }


    /**
     * @param fsType
     * @return translation for filesystem type
     */
    public static String translateFileSystemType ( FileSystemType fsType ) {
        return GuiMessages.get("FileSystemType." + fsType.name());//$NON-NLS-1$
    }


    /**
     * @param d
     * @return
     */
    private static String getVolumeGroupDisplayName ( VolumeGroup d ) {
        return GuiMessages.format(
            "structure.instance.sysinfo.storage.volumeGroupDisplayFmt", //$NON-NLS-1$
            d.getVolumeGroupName());
    }


    public String getDriveDisplayDetail ( Drive d ) {

        if ( d instanceof VolumeGroup ) {
            return GuiMessages.format(
                "structure.instance.sysinfo.storage.volumeGroupDetailFmt", //$NON-NLS-1$
                d.getVolumes().size());
        }
        else if ( d instanceof RAIDDrive ) {
            return GuiMessages.format(
                "structure.instance.sysinfo.storage.raidDetailFmt", //$NON-NLS-1$
                ( (RAIDDrive) d ).getNumDevices(),
                ( (RAIDDrive) d ).getNumSpares(),
                ( (RAIDDrive) d ).getNumDegraded());
        }
        else if ( d instanceof PhysicalDrive ) {
            if ( !StringUtils.isBlank( ( (PhysicalDrive) d ).getSerial()) ) {
                return GuiMessages.format(
                    "structure.instance.sysinfo.storage.physicalDiskDetailFmt", //$NON-NLS-1$
                    ( (PhysicalDrive) d ).getSerial());
            }
        }

        return StringUtils.EMPTY;
    }


    public static String getVolumeDisplayName ( Volume v ) {

        if ( v instanceof PhysicalVolume ) {
            if ( ( (PhysicalVolume) v ).getIndex() >= 0 ) {
                return GuiMessages.format(
                    "structure.instance.sysinfo.storage.volumePhysicalFmt", //$NON-NLS-1$
                    ( (PhysicalVolume) v ).getIndex());
            }
            return GuiMessages.get("structure.instance.sysinfo.storage.volumeUnpartitioned"); //$NON-NLS-1$

        }
        else if ( v instanceof LogicalVolume ) {
            return GuiMessages.format(
                "structure.instance.sysinfo.storage.volumeLogicalFmt", //$NON-NLS-1$
                ( (LogicalVolume) v ).getName());
        }

        return StringUtils.EMPTY;
    }


    public String getVolumeDisplayDetail ( Volume v ) {

        if ( v instanceof SystemVolume ) {
            return getSystemVolumeDetail((SystemVolume) v);
        }
        else if ( ( v instanceof PhysicalVolume || v instanceof LogicalVolume ) && v.holdsFilesystem() ) {
            return GuiMessages.format(
                "structure.instance.sysinfo.storage.volumeFsDetailFmt", //$NON-NLS-1$
                translateFileSystemType(v.getFileSystem().getFsType()));
        }
        else if ( ( v instanceof PhysicalVolume || v instanceof LogicalVolume ) && !v.holdsFilesystem() ) {
            return GuiMessages.format("structure.instance.sysinfo.storage.volumeUnformattedDetail"); //$NON-NLS-1$
        }
        return StringUtils.EMPTY;

    }


    private static String getSystemVolumeDetail ( SystemVolume v ) {
        switch ( v.getSystemVolumeType() ) {
        case LVM_PV:
            return GuiMessages.get("structure.instance.sysinfo.storage.volumePv"); //$NON-NLS-1$
        case RAID_MEMBER:
            return GuiMessages.get("structure.instance.sysinfo.storage.volumeRaid"); //$NON-NLS-1$
        default:
            log.warn("Unknown System Volume type " + v); //$NON-NLS-1$
            return v.getSystemVolumeType().toString();
        }
    }


    public boolean isPhysicalDrive ( Drive d ) {
        return d instanceof PhysicalDrive;
    }


    public boolean isDataVolume ( FileSystem fs ) {
        return fs instanceof DataFileSystem;
    }


    public boolean isSwapVolume ( FileSystem fs ) {
        return fs instanceof SwapFileSystem;
    }


    public boolean isActiveSwap ( FileSystem fs ) {
        if ( ! ( fs instanceof SwapFileSystem ) ) {
            return false;
        }
        return ( (SwapFileSystem) fs ).isActive();
    }


    public List<String> getMountPoints ( FileSystem fs ) {
        if ( ! ( fs instanceof DataFileSystem ) ) {
            return Collections.EMPTY_LIST;
        }
        List<String> mountPoints = new ArrayList<>( ( (DataFileSystem) fs ).getMountPoints());
        Collections.sort(mountPoints);
        return mountPoints;
    }


    public long getTotalSpace ( FileSystem fs ) {
        if ( ! ( fs instanceof DataFileSystem ) ) {
            return 0L;
        }

        Long res = ( (DataFileSystem) fs ).getTotalSpace();
        if ( res == null ) {
            return 0;
        }
        return res;
    }


    public long getUsableSpace ( FileSystem fs ) {
        if ( ! ( fs instanceof DataFileSystem ) ) {
            return 0L;
        }

        Long res = ( (DataFileSystem) fs ).getUsableSpace();
        if ( res == null ) {
            return 0;
        }
        return res;
    }


    public long getUncapturedSpace ( FileSystem fs ) {
        if ( ! ( fs instanceof DataFileSystem ) ) {
            return 0L;
        }

        Long res = ( (DataFileSystem) fs ).getUncapturedSpace();
        if ( res == null ) {
            return 0;
        }
        return res;
    }


    public boolean canInitialize ( Drive d ) {
        if ( ! ( d instanceof PhysicalDrive ) ) {
            return false;
        }

        PhysicalDrive pd = (PhysicalDrive) d;
        if ( pd.getPartitionTableType() == PartitionTableType.NONE ) {
            return true;
        }
        else if ( pd.getPartitionTableType() == PartitionTableType.UNKNOWN ) {
            return false;
        }

        if ( pd.getVolumes().isEmpty() ) {
            return true;
        }
        else if ( pd.getVolumes().size() == 1 ) {
            Volume v = pd.getVolumes().get(0);
            return ! ( v instanceof SystemVolume ) && !v.holdsFilesystem();
        }
        return false;
    }


    public boolean canInitializeVolume ( Volume v ) {
        if ( v instanceof SystemVolume ) {
            return false;
        }
        if ( v.holdsFilesystem() ) {
            return false;
        }
        return true;
    }


    public boolean canReinitialize ( Drive d ) {
        if ( ! ( d instanceof PhysicalDrive ) ) {
            return false;
        }

        if ( canInitialize(d) ) {
            return false;
        }

        for ( Volume v : d.getVolumes() ) {
            if ( v instanceof SystemVolume ) {
                return false;
            }

            if ( !v.holdsFilesystem() ) {
                continue;
            }

            if ( isActive(v.getFileSystem()) ) {
                return false;
            }
        }
        return true;
    }


    /**
     * @param fileSystem
     * @return
     */
    private static boolean isActive ( FileSystem fileSystem ) {
        if ( fileSystem instanceof SwapFileSystem ) {
            return ( (SwapFileSystem) fileSystem ).isActive();
        }
        else if ( fileSystem instanceof DataFileSystem ) {
            return ! ( (DataFileSystem) fileSystem ).getMountPoints().isEmpty();
        }
        return true;
    }


    public boolean canExpand ( Drive d, Volume v ) {

        if ( !v.holdsFilesystem() ) {
            return false;
        }
        FileSystem fs = v.getFileSystem();

        if ( ! ( fs instanceof DataFileSystem ) ) {
            return false;
        }

        if ( !EnumSet.of(FileSystemType.EXT2, FileSystemType.EXT3, FileSystemType.EXT4).contains(fs.getFsType()) ) {
            return false;
        }

        Long uncaptured = ( (DataFileSystem) fs ).getUncapturedSpace();
        if ( uncaptured != null && uncaptured > 0 ) {
            return true;
        }

        if ( d instanceof PhysicalDrive && d.getVolumes().size() <= 1 ) {
            uncaptured = ( (PhysicalDrive) d ).getUncapturedSpace();
            if ( uncaptured != null && uncaptured > 0 ) {
                return true;
            }
        }
        return false;
    }
}
