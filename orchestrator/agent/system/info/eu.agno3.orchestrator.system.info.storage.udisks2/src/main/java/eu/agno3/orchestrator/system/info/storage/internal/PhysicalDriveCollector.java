/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.internal;


import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.freedesktop.UDisks2.Drive;
import org.freedesktop.dbus.types.UInt64;
import org.freedesktop.dbus.types.Variant;

import eu.agno3.orchestrator.system.info.storage.drive.PhysicalDrive;
import eu.agno3.orchestrator.system.info.storage.drive.PhysicalDriveImpl;


/**
 * @author mbechler
 * 
 */
public class PhysicalDriveCollector implements DriveCollector {

    private static final Logger log = Logger.getLogger(PhysicalDriveCollector.class);


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.info.storage.internal.DriveCollector#collectDrive(eu.agno3.orchestrator.system.info.storage.internal.CompoundDriveCollector,
     *      java.lang.String, java.util.Map, java.util.Map)
     */
    @Override
    public void collectDrive ( CompoundDriveCollector collector, String path, Map<String, Map<String, Variant<?>>> ifs,
            Map<String, eu.agno3.orchestrator.system.info.storage.drive.Drive> drives ) {
        if ( !ifs.containsKey(Drive.class.getName()) || collector.shouldSkipDrive(ifs) ) {
            return;
        }

        Map<String, Variant<?>> properties = ifs.get(Drive.class.getName());
        PhysicalDrive drive = makePhysicalDrive(path, properties);
        if ( log.isTraceEnabled() ) {
            log.trace("Found " + drive); //$NON-NLS-1$
        }

        if ( drives.put(drive.getId(), drive) != null ) {
            log.warn("Conflicting drives for id " + drive.getId()); //$NON-NLS-1$
        }
    }


    static PhysicalDrive makePhysicalDrive ( String path, Map<String, Variant<?>> properties ) {
        PhysicalDriveImpl drive = new PhysicalDriveImpl();
        String id = (String) properties.get("Id").getValue(); //$NON-NLS-1$

        // i.e. VMWare disks do not have any serial, so the Id will be empty via udisks
        // use a fake one ( the dbus object name )
        if ( !StringUtils.isBlank(id) ) {
            drive.setId(id);
        }
        else {
            int lastSep = path.lastIndexOf('/');
            if ( lastSep >= 0 && lastSep < path.length() ) {
                drive.setId(path.substring(lastSep + 1));
            }
            else {
                drive.setId(path);
            }
        }
        drive.setVendor((String) properties.get("Vendor").getValue()); //$NON-NLS-1$
        drive.setModel((String) properties.get("Model").getValue()); //$NON-NLS-1$
        drive.setSerial((String) properties.get("Serial").getValue()); //$NON-NLS-1$
        drive.setSize( ( (UInt64) properties.get("Size").getValue() ).longValue()); //$NON-NLS-1$
        return drive;
    }

}
