/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.internal;


import java.nio.charset.Charset;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.freedesktop.UDisks2.Block;
import org.freedesktop.UDisks2.Encrypted;
import org.freedesktop.UDisks2.Loop;
import org.freedesktop.UDisks2.PartitionTable;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.ObjectPath;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.types.Variant;

import eu.agno3.orchestrator.system.info.storage.drive.Drive;
import eu.agno3.orchestrator.system.info.storage.drive.PartitionTableType;
import eu.agno3.orchestrator.system.info.storage.volume.VolumeType;


/**
 * @author mbechler
 * 
 */
public final class VolumeUtils {

    /**
     * 
     */
    private static final String MD_RAID_PROPERTY = "MDRaid"; //$NON-NLS-1$
    private static final String PREFERRED_DEVICE_PROP = "PreferredDevice"; //$NON-NLS-1$
    private static final String LINUX_RAID_MEMBER_TYPE = "linux_raid_member"; //$NON-NLS-1$
    private static final String LVM2_MEMBER_TYPE = "LVM2_member"; //$NON-NLS-1$

    private static final String RAID_USAGE = "raid"; //$NON-NLS-1$
    private static final String ID_TYPE_PROP = "IdType"; //$NON-NLS-1$
    private static final String ID_USAGE_PROP = "IdUsage"; //$NON-NLS-1$

    private static final String HINT_PARTITIONABLE = "HintPartitionable"; //$NON-NLS-1$

    private static final Charset NAME_CHARSET = Charset.forName("UTF-8"); //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(VolumeUtils.class);


    /**
     * 
     */
    private VolumeUtils () {}


    static boolean isCryptoBacked ( Map<String, Map<String, Variant<?>>> ifs ) {
        @SuppressWarnings ( "unchecked" )
        Variant<ObjectPath> cryptoBacking = (Variant<ObjectPath>) ifs.get(Block.class.getName()).get("CryptoBackingDevice"); //$NON-NLS-1$
        if ( !UDisks2StorageInformationProvider.isEmptyPath(cryptoBacking.getValue()) ) {
            log.warn("Skipping encrypted device"); //$NON-NLS-1$
            return true;
        }
        return false;
    }


    static boolean isOnRaid ( Map<String, Variant<?>> properties ) {
        return UDisks2StorageInformationProvider.isEmptyPath((ObjectPath) properties.get(MD_RAID_PROPERTY).getValue());
    }


    static boolean isRaidMember ( Map<String, Variant<?>> properties ) {
        return RAID_USAGE.equals(getVolumeUsage(properties)) && LINUX_RAID_MEMBER_TYPE.equals(getVolumeType(properties));
    }


    static boolean isPhyiscalVolume ( Map<String, Variant<?>> properties, VolumeType partType ) {
        return ( RAID_USAGE.equals(getVolumeUsage(properties)) && LVM2_MEMBER_TYPE.equals(getVolumeType(properties)) )
                || partType == VolumeType.LVM_PV;
    }


    static boolean isLogicalVolume ( Map<String, Variant<?>> properties ) {
        String name = new String((byte[]) properties.get("Device").getValue(), NAME_CHARSET); //$NON-NLS-1$
        return name.trim().startsWith("/dev/dm"); //$NON-NLS-1$
    }


    static boolean isOtherVolume ( Map<String, Map<String, Variant<?>>> ifs ) {
        return !ifs.containsKey(PartitionTable.class.getName());
    }


    @SuppressWarnings ( "unchecked" )
    static Map<String, Map<String, Variant<?>>> fetchDriveProperties ( DBUSReferenceResolver main, DBusInterface obj,
            Map<DBusInterface, Map<String, Map<String, Variant<?>>>> allObjects, Map<String, Variant<?>> properties, String property ) {
        DBusInterface driveInterface = null;
        try {
            driveInterface = main.resolveReferencedObject(obj, (Variant<ObjectPath>) properties.get(property), allObjects);
        }
        catch ( DBusException e ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Failed to resolve referenced drive for partition " + obj, e); //$NON-NLS-1$
            }
        }

        return allObjects.get(driveInterface);
    }


    static Drive fetchDriveForPartition ( DBUSReferenceResolver main, DBusInterface obj,
            Map<DBusInterface, Map<String, Map<String, Variant<?>>>> allObjects,
            Map<String, eu.agno3.orchestrator.system.info.storage.drive.Drive> drives, Map<String, Variant<?>> properties ) {
        Map<String, Map<String, Variant<?>>> driveObjProps = fetchDriveProperties(main, obj, allObjects, properties, "Drive"); //$NON-NLS-1$

        if ( driveObjProps == null || driveObjProps.get(org.freedesktop.UDisks2.Drive.class.getName()) == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("No hosting drive for " + obj); //$NON-NLS-1$
            }
            return null;
        }

        Map<String, Variant<?>> driveProps = driveObjProps.get(org.freedesktop.UDisks2.Drive.class.getName());
        String driveId = (String) driveProps.get("Id").getValue(); //$NON-NLS-1$
        if ( StringUtils.isBlank(driveId) ) {
            @SuppressWarnings ( "unchecked" )
            Variant<ObjectPath> p = (Variant<ObjectPath>) properties.get("Drive"); //$NON-NLS-1$
            String path = p.getValue().getPath();
            int lastSep = path.lastIndexOf('/');
            if ( lastSep >= 0 && lastSep < path.length() ) {
                driveId = path.substring(lastSep + 1);
            }
            else {
                driveId = path;
            }
        }
        return drives.get(driveId);
    }


    static Drive fetchRAIDForPartition ( DBUSReferenceResolver main, DBusInterface obj,
            Map<DBusInterface, Map<String, Map<String, Variant<?>>>> allObjects,
            Map<String, eu.agno3.orchestrator.system.info.storage.drive.Drive> drives, Map<String, Variant<?>> properties ) {
        Map<String, Map<String, Variant<?>>> driveObjProps = fetchDriveProperties(main, obj, allObjects, properties, MD_RAID_PROPERTY);

        if ( driveObjProps == null || driveObjProps.get(org.freedesktop.UDisks2.MDRaid.class.getName()) == null ) {
            log.warn("Failed to resolve RAID array for " + obj); //$NON-NLS-1$
            return null;
        }

        Map<String, Variant<?>> driveProps = driveObjProps.get(org.freedesktop.UDisks2.MDRaid.class.getName());
        String raidUUID = (String) driveProps.get("UUID").getValue(); //$NON-NLS-1$
        return drives.get("RAID-" + raidUUID); //$NON-NLS-1$
    }


    static boolean isUnsupportedInterface ( Map<String, Map<String, Variant<?>>> ifs ) {
        return ifs.containsKey(Encrypted.class.getName());
    }


    static String getPreferredDeviceName ( Map<String, Variant<?>> properties ) {
        return ( new String((byte[]) properties.get(PREFERRED_DEVICE_PROP).getValue(), NAME_CHARSET) ).trim();
    }


    static String getVolumeType ( Map<String, Variant<?>> properties ) {
        return (String) properties.get(ID_TYPE_PROP).getValue();
    }


    static String getVolumeUsage ( Map<String, Variant<?>> properties ) {
        return (String) properties.get(ID_USAGE_PROP).getValue();
    }


    static Drive getVolumeDrive ( DBUSReferenceResolver main, DBusInterface obj, Map<DBusInterface, Map<String, Map<String, Variant<?>>>> allObjects,
            Map<String, eu.agno3.orchestrator.system.info.storage.drive.Drive> drives, Map<String, Variant<?>> properties ) {
        Drive drive;
        if ( !isOnRaid(properties) ) {
            drive = fetchRAIDForPartition(main, obj, allObjects, drives, properties);
            if ( drive == null ) {
                log.warn("RAID array does not exists for  " + obj); //$NON-NLS-1$
                return null;
            }
        }
        else if ( obj instanceof Loop ) {
            drive = drives.get(getPreferredDeviceName(properties));
        }
        else {
            drive = fetchDriveForPartition(main, obj, allObjects, drives, properties);
            if ( drive == null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("No drive exists for  " + obj); //$NON-NLS-1$
                }
                return null;
            }
        }
        return drive;
    }


    /**
     * @param obj
     * @param ifs
     * @param drives
     * @param properties
     * @return whether this is a drive root volume
     */
    static boolean isDriveVolume ( DBusInterface obj, Map<String, Variant<?>> properties ) {
        return obj instanceof Block && properties != null && StringUtils.isBlank(getVolumeType(properties))
                && StringUtils.isBlank(getVolumeUsage(properties)) && (Boolean) properties.get(HINT_PARTITIONABLE).getValue();
    }


    /**
     * @param dr
     * @param partProps
     */
    static PartitionTableType getParitionTableType ( Map<String, Variant<?>> partProps ) {
        if ( partProps == null ) {
            return PartitionTableType.NONE;
        }

        String partType = (String) partProps.get("Type").getValue(); //$NON-NLS-1$
        switch ( partType ) {
        case "dos": //$NON-NLS-1$
            return PartitionTableType.DOS;
        case "gpt": //$NON-NLS-1$
            return PartitionTableType.GPT;
        default:
            return PartitionTableType.UNKNOWN;
        }
    }

}
