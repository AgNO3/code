/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.internal;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.freedesktop.UDisks2.Block;
import org.freedesktop.UDisks2.Partition;
import org.freedesktop.UDisks2.PartitionTable;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.ObjectPath;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.UInt64;
import org.freedesktop.dbus.types.Variant;

import eu.agno3.orchestrator.system.info.SystemInformationException;
import eu.agno3.orchestrator.system.info.storage.drive.Drive;
import eu.agno3.orchestrator.system.info.storage.drive.PhysicalDriveImpl;
import eu.agno3.orchestrator.system.info.storage.drive.VolumeGroupImpl;
import eu.agno3.orchestrator.system.info.storage.volume.AbstractVolumeImpl;
import eu.agno3.orchestrator.system.info.storage.volume.LVMLogicalVolumeImpl;
import eu.agno3.orchestrator.system.info.storage.volume.PhysicalVolumeImpl;
import eu.agno3.orchestrator.system.info.storage.volume.SystemVolumeImpl;
import eu.agno3.orchestrator.system.info.storage.volume.SystemVolumeType;
import eu.agno3.orchestrator.system.info.storage.volume.Volume;
import eu.agno3.orchestrator.system.info.storage.volume.VolumeType;


/**
 * @author mbechler
 * 
 */
public class VolumeCollector {

    private static final Logger log = Logger.getLogger(VolumeCollector.class);

    private FileSystemCollector fsCollector = new FileSystemCollector();


    /**
     * 
     */
    public VolumeCollector () {}


    /**
     * @param enumerator
     * @param main
     * @param objects
     * @param drives
     * @return a map of drives and their volumes
     */
    public Map<Drive, List<Volume>> collectVolumes ( UDisks2Enumerator enumerator, DBUSReferenceResolver main,
            Map<DBusInterface, Map<String, Map<String, Variant<?>>>> objects,
            Map<String, eu.agno3.orchestrator.system.info.storage.drive.Drive> drives ) {
        log.debug("Collecting volumes..."); //$NON-NLS-1$
        Map<Drive, List<Volume>> volumes = new HashMap<>();
        for ( Entry<DBusInterface, Map<String, Map<String, Variant<?>>>> path : objects.entrySet() ) {
            DBusInterface obj = path.getKey();
            Map<String, Map<String, Variant<?>>> ifs = path.getValue();
            if ( !ifs.containsKey(Block.class.getName()) ) {
                continue;
            }

            if ( this.shouldSkipBlockDevice(enumerator, main, obj, ifs, objects) ) {
                continue;
            }

            try {
                Volume v = this.collectVolume(main, obj, ifs, objects, Collections.unmodifiableMap(drives));

                if ( v != null ) {
                    setupVolume(volumes, v);
                }
            }
            catch ( SystemInformationException e ) {
                log.warn("Failed to collect volume information:", e); //$NON-NLS-1$
            }
        }

        return volumes;
    }


    private static void setupVolume ( Map<Drive, List<Volume>> volumes, Volume v ) {
        List<Volume> driveVolumes = volumes.get(v.getDrive());

        if ( driveVolumes == null ) {
            driveVolumes = new ArrayList<>();
            volumes.put(v.getDrive(), driveVolumes);
        }

        driveVolumes.add(v);
    }


    protected Volume collectVolume ( DBUSReferenceResolver main, DBusInterface obj, Map<String, Map<String, Variant<?>>> ifs,
            Map<DBusInterface, Map<String, Map<String, Variant<?>>>> allObjects,
            Map<String, eu.agno3.orchestrator.system.info.storage.drive.Drive> drives ) throws SystemInformationException {

        Map<String, Variant<?>> properties = ifs.get(Block.class.getName());

        AbstractVolumeImpl v = null;
        if ( VolumeUtils.isLogicalVolume(properties) ) {
            v = collectLogicalVolume(drives, properties);
        }
        else if ( VolumeUtils.isOtherVolume(ifs) ) {
            v = collectOtherVolume(main, obj, ifs, allObjects, drives, properties);
        }
        else if ( VolumeUtils.isDriveVolume(obj, properties) ) {
            Drive volumeDrive = VolumeUtils.getVolumeDrive(main, obj, allObjects, drives, properties);
            enhanceDrive(volumeDrive, obj, ifs, properties);
        }
        else if ( log.isDebugEnabled() ) {
            log.debug("Ignored " + obj); //$NON-NLS-1$
        }

        if ( v != null ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Found " + v); //$NON-NLS-1$
            }
            this.fsCollector.collectFileSystem(v, ifs, properties);
        }

        return v;
    }


    /**
     * @param volumeDrive
     * @param obj
     * @param ifs
     * @param properties
     */
    private static void enhanceDrive ( Drive volumeDrive, DBusInterface obj, Map<String, Map<String, Variant<?>>> ifs,
            Map<String, Variant<?>> properties ) {
        if ( volumeDrive instanceof PhysicalDriveImpl ) {
            PhysicalDriveImpl dr = (PhysicalDriveImpl) volumeDrive;
            Map<String, Variant<?>> partProps = ifs.get(PartitionTable.class.getName());
            dr.setBlockDeviceName(VolumeUtils.getPreferredDeviceName(properties)); // $NON-NLS-1$
            if ( log.isDebugEnabled() ) {
                log.debug("Enhancing " + volumeDrive); //$NON-NLS-1$
            }
            dr.setPartitionTableType(VolumeUtils.getParitionTableType(partProps));
        }
    }


    protected AbstractVolumeImpl collectOtherVolume ( DBUSReferenceResolver main, DBusInterface obj, Map<String, Map<String, Variant<?>>> ifs,
            Map<DBusInterface, Map<String, Map<String, Variant<?>>>> allObjects,
            Map<String, eu.agno3.orchestrator.system.info.storage.drive.Drive> drives, Map<String, Variant<?>> properties ) {
        VolumeType partType = VolumeType.UNPARTITIONED;
        int partNum = -1;
        if ( ifs.containsKey(Partition.class.getName()) ) {
            Map<String, Variant<?>> partProps = ifs.get(Partition.class.getName());
            partType = VolumeType.fromTypeString((String) partProps.get("Type").getValue()); //$NON-NLS-1$
            partNum = ( (UInt32) partProps.get("Number").getValue() ).intValue(); //$NON-NLS-1$
        }

        Drive drive = VolumeUtils.getVolumeDrive(main, obj, allObjects, drives, properties);
        if ( drive == null ) {
            return null;
        }
        if ( partType == VolumeType.UNPARTITIONED ) {
            // this is actually a drive volume
            enhanceDrive(drive, obj, ifs, properties);
        }

        return makeOtherVolume(properties, partType, VolumeUtils.getPreferredDeviceName(properties), partNum, drive);
    }


    private static AbstractVolumeImpl makeOtherVolume ( Map<String, Variant<?>> properties, VolumeType partType, String device, int partNum,
            Drive drive ) {
        PhysicalVolumeImpl vol;
        if ( VolumeUtils.isRaidMember(properties) ) {
            vol = new SystemVolumeImpl();
            ( (SystemVolumeImpl) vol ).setSystemVolumeType(SystemVolumeType.RAID_MEMBER);
        }
        else if ( VolumeUtils.isPhyiscalVolume(properties, partType) ) {
            vol = new SystemVolumeImpl();
            ( (SystemVolumeImpl) vol ).setSystemVolumeType(SystemVolumeType.LVM_PV);
        }
        else {
            vol = new PhysicalVolumeImpl();

            Variant<?> variant = properties.get("IdLabel"); //$NON-NLS-1$
            if ( variant != null && variant.getValue() instanceof String ) {
                vol.setLabel((String) variant.getValue());
            }
        }

        vol.setDrive(drive);
        vol.setDevice(device);
        vol.setType(partType);
        vol.setIndex(partNum);
        vol.setSize( ( (UInt64) properties.get("Size").getValue() ).longValue()); //$NON-NLS-1$

        return vol;
    }


    protected AbstractVolumeImpl collectLogicalVolume ( Map<String, eu.agno3.orchestrator.system.info.storage.drive.Drive> drives,
            Map<String, Variant<?>> properties ) throws SystemInformationException {

        String preferredName = VolumeUtils.getPreferredDeviceName(properties);

        String vgName = LVMUtil.getVgNameFromDevice(preferredName);
        String lvName = LVMUtil.getLvNameFromDevice(preferredName);

        Drive d = drives.get(VolumeGroupImpl.vgNameToDriveId(vgName));

        if ( d == null ) {
            throw new SystemInformationException("Failed to locate volume group for volume"); //$NON-NLS-1$
        }

        return makeLVMVolume(properties, lvName, preferredName, d);
    }


    private static AbstractVolumeImpl makeLVMVolume ( Map<String, Variant<?>> properties, String lvName, String device, Drive d ) {
        LVMLogicalVolumeImpl vol = new LVMLogicalVolumeImpl();
        vol.setName(lvName);
        vol.setDevice(device);
        vol.setDrive(d);
        vol.setSize( ( (UInt64) properties.get("Size").getValue() ).longValue()); //$NON-NLS-1$
        return vol;
    }


    protected boolean shouldSkipBlockDevice ( UDisks2Enumerator enumerator, DBUSReferenceResolver main, DBusInterface obj,
            Map<String, Map<String, Variant<?>>> ifs, Map<DBusInterface, Map<String, Map<String, Variant<?>>>> allObjects ) {

        if ( VolumeUtils.isUnsupportedInterface(ifs) ) {
            log.trace("Skipping unsupported"); //$NON-NLS-1$
            return true;
        }

        @SuppressWarnings ( "unchecked" )
        Variant<Boolean> readOnly = (Variant<Boolean>) ifs.get(Block.class.getName()).get("ReadOnly"); //$NON-NLS-1$

        if ( readOnly.getValue() ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Skipping read only " + obj); //$NON-NLS-1$
            }
            return true;
        }

        // do not support crypto (yet?)
        if ( VolumeUtils.isCryptoBacked(ifs) ) {
            log.trace("Skipping crypto"); //$NON-NLS-1$
            return true;
        }

        if ( isBackingDriveIgnored(main, enumerator, obj, ifs, allObjects) ) {
            log.trace("Skipping ignored backing drive"); //$NON-NLS-1$
            return true;
        }

        return false;
    }


    private static boolean isBackingDriveIgnored ( DBUSReferenceResolver main, UDisks2Enumerator enumerator, DBusInterface obj,
            Map<String, Map<String, Variant<?>>> ifs, Map<DBusInterface, Map<String, Map<String, Variant<?>>>> allObjects ) {
        @SuppressWarnings ( "unchecked" )
        Variant<ObjectPath> varDrive = (Variant<ObjectPath>) ifs.get(Block.class.getName()).get("Drive"); //$NON-NLS-1$

        try {
            DBusInterface referencedDrive = main.resolveReferencedObject(obj, varDrive, allObjects);

            if ( referencedDrive != null && enumerator.getDriveCollector().shouldSkipDrive(allObjects.get(referencedDrive)) ) {
                log.debug("Skipping block device on ignore drive"); //$NON-NLS-1$
                return true;
            }
        }
        catch ( DBusException e ) {
            log.warn("Failed to retrieve drive object:", e); //$NON-NLS-1$
        }
        return false;
    }

}
