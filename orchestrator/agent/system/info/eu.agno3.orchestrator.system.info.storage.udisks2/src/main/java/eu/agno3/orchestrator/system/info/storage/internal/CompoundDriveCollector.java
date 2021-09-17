/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.internal;


import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.freedesktop.UDisks2.Drive;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.RemoteInvocationHandler;
import org.freedesktop.dbus.types.Variant;


/**
 * @author mbechler
 * 
 */
public class CompoundDriveCollector {

    private static final Logger log = Logger.getLogger(CompoundDriveCollector.class);

    private static final DriveCollector[] COLLECTORS = new DriveCollector[] {
        new PhysicalDriveCollector(), new LVMDriveCollector(), new MDRaidDriveCollector(), new LoopDriveCollector()
    };


    /**
     * 
     */
    public CompoundDriveCollector () {}


    /**
     * 
     * @param objects
     * @return the system's drives
     */
    public Map<String, eu.agno3.orchestrator.system.info.storage.drive.Drive> collectDrives (
            Map<DBusInterface, Map<String, Map<String, Variant<?>>>> objects ) {
        log.debug("Collecting drives..."); //$NON-NLS-1$
        Map<String, eu.agno3.orchestrator.system.info.storage.drive.Drive> drives = new HashMap<>();
        for ( Entry<DBusInterface, Map<String, Map<String, Variant<?>>>> path : objects.entrySet() ) {
            DBusInterface obj = path.getKey();
            Map<String, Map<String, Variant<?>>> ifs = path.getValue();
            this.collectDrives(obj, getObjectPath(path.getKey()), ifs, objects, drives);
        }
        return drives;
    }


    /**
     * @param key
     * @return
     */
    private static String getObjectPath ( DBusInterface key ) {
        if ( key == null || !key.isRemote() ) {
            return null;
        }

        RemoteInvocationHandler invocationHandler = (RemoteInvocationHandler) Proxy.getInvocationHandler(key);
        return invocationHandler.getRemote().getObjectPath();
    }


    /**
     * @param obj
     * @param ifs
     */
    protected Map<String, eu.agno3.orchestrator.system.info.storage.drive.Drive> collectDrives ( DBusInterface obj, String path,
            Map<String, Map<String, Variant<?>>> ifs, Map<DBusInterface, Map<String, Map<String, Variant<?>>>> allObjects,
            Map<String, eu.agno3.orchestrator.system.info.storage.drive.Drive> drives ) {

        for ( DriveCollector collector : COLLECTORS ) {
            collector.collectDrive(this, path, ifs, drives);
        }

        return drives;
    }


    /**
     * @param ifs
     * @return whether this drive should be skipped
     */
    public boolean shouldSkipDrive ( Map<String, Map<String, Variant<?>>> ifs ) {
        Map<String, Variant<?>> props = ifs.get(Drive.class.getName());
        Variant<?> removable = props.get("Removable"); //$NON-NLS-1$
        if ( (Boolean) removable.getValue() ) {
            log.debug("Skipping removable drive"); //$NON-NLS-1$
            return true;
        }
        return false;
    }

}
