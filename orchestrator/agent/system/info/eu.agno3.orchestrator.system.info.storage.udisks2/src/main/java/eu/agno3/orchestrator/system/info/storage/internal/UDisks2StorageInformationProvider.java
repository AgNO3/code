/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.internal;


import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.freedesktop.UDisks2.Block;
import org.freedesktop.dbus.DBus.ObjectManager;
import org.freedesktop.dbus.DBus.Properties;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.ObjectPath;
import org.freedesktop.dbus.RemoteInvocationHandler;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.types.Variant;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.system.dbus.SystemDBusClient;
import eu.agno3.orchestrator.system.info.storage.StorageInformation;
import eu.agno3.orchestrator.system.info.storage.StorageInformationException;
import eu.agno3.orchestrator.system.info.storage.StorageInformationProvider;
import eu.agno3.orchestrator.system.info.storage.drive.AbstractDriveImpl;
import eu.agno3.orchestrator.system.info.storage.drive.Drive;
import eu.agno3.orchestrator.system.info.storage.drive.PhysicalDrive;
import eu.agno3.orchestrator.system.info.storage.drive.PhysicalDriveImpl;
import eu.agno3.orchestrator.system.info.storage.drive.VolumeGroup;
import eu.agno3.orchestrator.system.info.storage.drive.VolumeGroupImpl;
import eu.agno3.orchestrator.system.info.storage.fs.DataFileSystem;
import eu.agno3.orchestrator.system.info.storage.volume.PhysicalVolume;
import eu.agno3.orchestrator.system.info.storage.volume.SystemVolume;
import eu.agno3.orchestrator.system.info.storage.volume.SystemVolumeType;
import eu.agno3.orchestrator.system.info.storage.volume.Volume;


/**
 * @author mbechler
 * 
 */
@Component ( service = StorageInformationProvider.class )
public class UDisks2StorageInformationProvider implements StorageInformationProvider, DBUSReferenceResolver {

    private static final String STORAGE = "/storage/"; //$NON-NLS-1$
    private static final String UDISKS2_ROOT_PATH = "/org/freedesktop/UDisks2"; //$NON-NLS-1$
    private static final String UDISKS2_MANAGER_PATH = UDISKS2_ROOT_PATH + "/Manager"; //$NON-NLS-1$
    private static final String UDISKS2_SERVICE = "org.freedesktop.UDisks2"; //$NON-NLS-1$

    private static final String UDISKS2_MANAGER_INTERFACE = "org.freedesktop.UDisks2.Manager"; //$NON-NLS-1$

    private static final String UDISKS2_MANAGER_VERSION_PROP = "Version"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(UDisks2StorageInformationProvider.class);

    private SystemDBusClient client;
    private final UDisks2Enumerator enumerator = new UDisks2Enumerator();


    @Reference
    protected synchronized void setDBUSClient ( SystemDBusClient cl ) {
        this.client = cl;
    }


    protected synchronized void unsetDBUSClient ( SystemDBusClient cl ) {
        if ( this.client == cl ) {
            this.client = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws StorageInformationException
     * 
     * @see eu.agno3.orchestrator.system.info.storage.StorageInformationProvider#getInformation()
     */
    @Override
    public StorageInformation getInformation () throws StorageInformationException {
        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        log.debug("Fetching storage information"); //$NON-NLS-1$

        try {
            ObjectManager manager = getManager();
            StorageInformation si = this.enumerator.enumerateObjects(manager, this);
            markSystemDrives(si);
            enhance(si);
            return si;
        }
        catch (
            DBusException |
            DBusExecutionException e ) {
            throw new StorageInformationException("Failed to reread partition tables", e); //$NON-NLS-1$
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
    }


    /**
     * @param si
     */
    private static void enhance ( StorageInformation si ) {
        for ( Drive d : si.getDrives() ) {
            String alias = null;
            if ( d.getSystem() ) {
                alias = "system"; //$NON-NLS-1$
            }
            else if ( d.getVolumes().size() == 1 ) {
                Volume volume = d.getVolumes().get(0);
                if ( volume.getFileSystem() instanceof DataFileSystem ) {
                    DataFileSystem dfs = (DataFileSystem) volume.getFileSystem();

                    if ( log.isDebugEnabled() ) {
                        log.debug("Mount points are  " + dfs.getMountPoints()); //$NON-NLS-1$
                    }

                    for ( String mountpoint : dfs.getMountPoints() ) {
                        if ( mountpoint.startsWith(STORAGE) ) {
                            alias = mountpoint.substring(STORAGE.length());
                            break;
                        }
                    }
                }
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Found alias " + alias); //$NON-NLS-1$
            }

            if ( d instanceof AbstractDriveImpl ) {
                ( (AbstractDriveImpl) d ).setAssignedAlias(alias);
            }
            else if ( d instanceof VolumeGroupImpl ) {
                ( (VolumeGroupImpl) d ).setAssignedAlias(alias);
            }
        }
    }


    /**
     * @param si
     */
    void markSystemDrives ( StorageInformation si ) {
        for ( Drive d : si.getDrives() ) {
            if ( d instanceof VolumeGroup && "sys".equals( ( (VolumeGroup) d ).getVolumeGroupName()) ) { //$NON-NLS-1$
                ( (VolumeGroupImpl) d ).setSystem(true);
            }
            else if ( d instanceof PhysicalDrive ) {
                // a bit heuristic, partitions are bootPrimary/bootBackup/LVM_PV(sys)
                if ( d.getVolumes().size() < 3 ) {
                    continue;
                }
                Volume part1 = d.getVolumes().get(0);
                Volume part2 = d.getVolumes().get(1);
                Volume part3 = d.getVolumes().get(2);

                if ( ! ( part1 instanceof PhysicalVolume ) || ! ( part2 instanceof PhysicalVolume ) || ! ( part3 instanceof SystemVolume ) ) {
                    continue;
                }
                PhysicalVolume pv1 = (PhysicalVolume) part1;
                PhysicalVolume pv2 = (PhysicalVolume) part2;
                SystemVolume sv = (SystemVolume) part3;
                if ( Objects.equals(pv1.getLabel(), "bootPrimary") && //$NON-NLS-1$
                        Objects.equals(pv2.getLabel(), "bootBackup") && //$NON-NLS-1$
                        sv.getSystemVolumeType() == SystemVolumeType.LVM_PV ) {
                    ( (PhysicalDriveImpl) d ).setSystem(true);
                }
            }
        }
    }


    /**
     * @return
     * @throws DBusException
     */
    private ObjectManager getManager () throws DBusException {
        Properties managerProperties = this.client.getRemoteObject(UDISKS2_SERVICE, UDISKS2_MANAGER_PATH, Properties.class);
        String version = managerProperties.Get(UDISKS2_MANAGER_INTERFACE, UDISKS2_MANAGER_VERSION_PROP);
        if ( log.isDebugEnabled() ) {
            log.debug("Found UDISKS2 version " + version); //$NON-NLS-1$
        }

        return this.client.getRemoteObject(UDISKS2_SERVICE, UDISKS2_ROOT_PATH, ObjectManager.class);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws StorageInformationException
     *
     * @see eu.agno3.orchestrator.system.info.storage.StorageInformationProvider#rescanPartitions()
     */
    @Override
    public void rescanPartitions () throws StorageInformationException {
        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        log.debug("Rereading partition tables"); //$NON-NLS-1$

        try {
            ObjectManager manager = getManager();
            Map<DBusInterface, Map<String, Map<String, Variant<?>>>> objects = manager.GetManagedObjects();
            for ( Entry<DBusInterface, Map<String, Map<String, Variant<?>>>> obj : objects.entrySet() ) {
                Map<String, Variant<?>> blockProps = obj.getValue().get(Block.class.getName());
                if ( VolumeUtils.isDriveVolume(obj.getKey(), blockProps) ) {
                    log.info("Rereading partition table on " + VolumeUtils.getPreferredDeviceName(blockProps)); //$NON-NLS-1$
                    ( (Block) obj.getKey() ).Rescan(Collections.EMPTY_MAP);
                }
            }
        }
        catch (
            DBusException |
            DBusExecutionException e ) {
            throw new StorageInformationException("Failed to enumerate storage objects", e); //$NON-NLS-1$
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.storage.internal.DBUSReferenceResolver#resolveReferencedObject(org.freedesktop.dbus.DBusInterface,
     *      org.freedesktop.dbus.types.Variant, java.util.Map)
     */
    @Override
    public final DBusInterface resolveReferencedObject ( DBusInterface ref, Variant<ObjectPath> variant,
            Map<DBusInterface, Map<String, Map<String, Variant<?>>>> allObjects ) throws DBusException {
        RemoteInvocationHandler handler = (RemoteInvocationHandler) Proxy.getInvocationHandler(ref);
        String busName = handler.getRemote().getBusName();
        String path = variant.getValue().getPath();

        if ( !isEmptyPath(variant.getValue()) ) {
            DBusInterface resolvedObj = this.client.getRemoteObject(busName, path);

            if ( !allObjects.containsKey(resolvedObj) ) {
                log.warn("Illegal reference " + resolvedObj); //$NON-NLS-1$
                return null;
            }
            return resolvedObj;

        }
        return null;
    }


    /**
     * @param value
     * @return
     */
    static boolean isEmptyPath ( ObjectPath value ) {
        return value == null || value.getPath() == null || "/".equals(value.getPath()); //$NON-NLS-1$
    }

}
