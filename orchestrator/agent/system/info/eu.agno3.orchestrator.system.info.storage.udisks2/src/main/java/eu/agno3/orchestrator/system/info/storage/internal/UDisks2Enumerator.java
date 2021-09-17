/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.internal;


import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.freedesktop.dbus.DBus.ObjectManager;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.types.Variant;

import eu.agno3.orchestrator.system.info.storage.StorageInformation;
import eu.agno3.orchestrator.system.info.storage.StorageInformationImpl;
import eu.agno3.orchestrator.system.info.storage.drive.AbstractDriveImpl;
import eu.agno3.orchestrator.system.info.storage.drive.Drive;
import eu.agno3.orchestrator.system.info.storage.drive.PhysicalDrive;
import eu.agno3.orchestrator.system.info.storage.drive.PhysicalDriveImpl;
import eu.agno3.orchestrator.system.info.storage.drive.VolumeGroupImpl;
import eu.agno3.orchestrator.system.info.storage.fs.DataFileSystem;
import eu.agno3.orchestrator.system.info.storage.fs.DataFileSystemImpl;
import eu.agno3.orchestrator.system.info.storage.fs.FileSystemType;
import eu.agno3.orchestrator.system.info.storage.volume.SystemVolume;
import eu.agno3.orchestrator.system.info.storage.volume.Volume;


/**
 * @author mbechler
 * 
 */
public class UDisks2Enumerator {

    private static final Logger log = Logger.getLogger(UDisks2Enumerator.class);

    /**
     * Unpartitioned space for alignment + GTP reserved size
     */
    private static final long FREE_SIZE_THRESHOLD = 512 * ( 2048 + 2048 );

    private final CompoundDriveCollector driveCollector = new CompoundDriveCollector();
    private final VolumeCollector volumeCollector = new VolumeCollector();


    protected StorageInformation enumerateObjects ( ObjectManager manager, DBUSReferenceResolver refResolver ) {
        Map<DBusInterface, Map<String, Map<String, Variant<?>>>> objects = manager.GetManagedObjects();
        Map<String, Drive> drives = this.driveCollector.collectDrives(objects);
        Map<Drive, List<Volume>> volumes = this.volumeCollector.collectVolumes(this, refResolver, objects, drives);
        StorageInformationImpl info = new StorageInformationImpl();

        dumpVolumes(volumes);

        Set<Drive> res = new HashSet<>();
        for ( Entry<Drive, List<Volume>> driveEntry : volumes.entrySet() ) {
            Drive d = driveEntry.getKey();
            if ( d instanceof AbstractDriveImpl ) {
                ( (AbstractDriveImpl) d ).setVolumes(driveEntry.getValue());
            }
            else if ( d instanceof VolumeGroupImpl ) {
                ( (VolumeGroupImpl) d ).setVolumes(driveEntry.getValue());
            }
            res.add(d);
        }

        info.setDrives(res);

        enhance(info);
        return info;
    }


    /**
     * @param info
     */
    private static void enhance ( StorageInformationImpl info ) {
        for ( Drive d : info.getDrives() ) {
            enhanceDrive(d);
            for ( Volume v : d.getVolumes() ) {
                enhanceVolume(v);
            }
        }
    }


    /**
     * @param d
     */
    private static void enhanceDrive ( Drive d ) {
        if ( d instanceof PhysicalDrive ) {
            PhysicalDrive pd = (PhysicalDrive) d;
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Device %s: %s", pd.getBlockDeviceName(), pd.getPartitionTableType())); //$NON-NLS-1$
            }
            long totalVolSize = 0;
            for ( Volume v : d.getVolumes() ) {
                totalVolSize += v.getSize();
            }
            long free = d.getSize() - totalVolSize;
            if ( d.getSize() != null && free > FREE_SIZE_THRESHOLD ) {
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Device has unpartitioned space %s: %d", pd.getBlockDeviceName(), free)); //$NON-NLS-1$
                }
                if ( pd instanceof PhysicalDriveImpl ) {
                    ( (PhysicalDriveImpl) pd ).setUncapturedSpace(d.getSize() - totalVolSize - FREE_SIZE_THRESHOLD);
                }
            }
        }
    }


    /**
     * @param v
     */
    private static void enhanceVolume ( Volume v ) {
        if ( v instanceof SystemVolume || ! ( v.getFileSystem() instanceof DataFileSystem ) ) {
            return;
        }
        DataFileSystem dfs = (DataFileSystem) v.getFileSystem();
        if ( dfs.getTotalSpace() != null ) {
            long estVolumeSize = estimateVolumeSize(dfs.getFsType(), v.getSize(), dfs.getTotalSpace());
            long diff = v.getSize() - estVolumeSize;
            if ( diff > 0 ) {
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("%s uncaptured space is ~%d MB", dfs, ( v.getSize() - estVolumeSize ) / 1024 / 1024)); //$NON-NLS-1$
                }
                if ( dfs instanceof DataFileSystemImpl ) {
                    ( (DataFileSystemImpl) dfs ).setUncapturedSpace(diff);
                }
            }
        }
    }


    /**
     * @param fsType
     * @param volumeSize
     * @param fsSpace
     * @return
     */
    private static long estimateVolumeSize ( FileSystemType fsType, long volumeSize, long fsSpace ) {
        switch ( fsType ) {
        case EXT2:
            return estimateUpperVolumeSizeExt(fsSpace, false);
        case EXT3:
        case EXT4:
            return estimateUpperVolumeSizeExt(fsSpace, true);

        default:
            if ( log.isDebugEnabled() ) {
                log.debug("Unsupported fs type for size estimation " + fsType); //$NON-NLS-1$
            }
            return fsSpace;
        }
    }


    /**
     * @param fsSpace
     * @return
     */
    private static long estimateUpperVolumeSizeExt ( long fsSpace, boolean journal ) {
        // these estimations are only correct if the settings are the defaults
        int inodesPerGroup = 8192;
        int blockSize = 4096;
        int blocksPerGroup = 8 * blockSize;
        int inodeSize = 256;
        long blockGroups = fsSpace / ( blockSize * blocksPerGroup );
        // reserved for inode tables
        long inodeOverhead = blockGroups * inodesPerGroup * inodeSize;
        // reserved for superblock, bitmaps, ....
        long otherOverhead = blockSize * ( blockGroups * 2 + 20 * 1025 );
        // reserved for journal
        long journalOverhead = journal ? blockSize * 32768 : 0;
        return fsSpace + inodeOverhead + otherOverhead + journalOverhead;
    }


    private static void dumpVolumes ( Map<Drive, List<Volume>> volumes ) {
        if ( log.isDebugEnabled() ) {
            for ( Entry<Drive, List<Volume>> driveEntry : volumes.entrySet() ) {
                log.debug("Found " + driveEntry.getKey()); //$NON-NLS-1$

                for ( Volume v : driveEntry.getValue() ) {
                    dumpVolume(v);
                }
            }
        }
    }


    /**
     * @param v
     */
    protected static void dumpVolume ( Volume v ) {
        if ( log.isDebugEnabled() ) {
            log.debug("+ " + v); //$NON-NLS-1$

            if ( v.holdsFilesystem() ) {
                log.debug("++  " + v.getFileSystem()); //$NON-NLS-1$
            }
        }
    }


    /**
     * @return
     */
    CompoundDriveCollector getDriveCollector () {
        return this.driveCollector;
    }
}
