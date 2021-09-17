/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.internal;


import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.freedesktop.UDisks2.MDRaid;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.UInt64;
import org.freedesktop.dbus.types.Variant;

import eu.agno3.orchestrator.system.info.storage.drive.Drive;
import eu.agno3.orchestrator.system.info.storage.drive.RAIDDrive;
import eu.agno3.orchestrator.system.info.storage.drive.RAIDDriveImpl;
import eu.agno3.orchestrator.system.info.storage.drive.RAIDLevel;


/**
 * @author mbechler
 * 
 */
public class MDRaidDriveCollector implements DriveCollector {

    private static final Logger log = Logger.getLogger(MDRaidDriveCollector.class);


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.info.storage.internal.DriveCollector#collectDrive(eu.agno3.orchestrator.system.info.storage.internal.CompoundDriveCollector,
     *      java.lang.String, java.util.Map, java.util.Map)
     */
    @Override
    public void collectDrive ( CompoundDriveCollector collector, String path, Map<String, Map<String, Variant<?>>> ifs, Map<String, Drive> drives ) {
        if ( !ifs.containsKey(MDRaid.class.getName()) ) {
            return;
        }
        Map<String, Variant<?>> properties = ifs.get(MDRaid.class.getName());
        RAIDDrive raidDrive = makeRAIDDrive(properties);

        if ( log.isTraceEnabled() ) {
            log.trace("Found " + raidDrive); //$NON-NLS-1$
        }
        drives.put(raidDrive.getId(), raidDrive);
    }


    private static RAIDDrive makeRAIDDrive ( Map<String, Variant<?>> properties ) {
        RAIDDriveImpl raidDrive = new RAIDDriveImpl();
        raidDrive.setId("RAID-" + //$NON-NLS-1$
                properties.get("UUID").getValue()); //$NON-NLS-1$
        raidDrive.setSize( ( (UInt64) properties.get("Size").getValue() ).longValue()); //$NON-NLS-1$
        raidDrive.setRaidLevel(RAIDLevel.fromString((String) properties.get("Level").getValue())); //$NON-NLS-1$
        raidDrive.setNumDevices( ( (UInt32) properties.get("NumDevices").getValue() ).intValue()); //$NON-NLS-1$
        raidDrive.setNumDegraded( ( (UInt32) properties.get("Degraded").getValue() ).intValue()); //$NON-NLS-1$
        int spares = countSpares(properties);
        raidDrive.setNumSpares(spares);
        return raidDrive;
    }


    @SuppressWarnings ( "unchecked" )
    private static int countSpares ( Map<String, Variant<?>> properties ) {
        int spares = 0;
        for ( Object[] o : (Iterable<Object[]>) properties.get("ActiveDevices").getValue() ) { //$NON-NLS-1$
            Vector<String> states = (Vector<String>) o[ 2 ];
            if ( states.contains("spare") ) { //$NON-NLS-1$
                spares++;
            }
        }
        return spares;
    }
}
