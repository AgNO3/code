/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.01.2016 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.internal;


import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.freedesktop.UDisks2.Block;
import org.freedesktop.UDisks2.Loop;
import org.freedesktop.UDisks2.PartitionTable;
import org.freedesktop.dbus.types.UInt64;
import org.freedesktop.dbus.types.Variant;

import eu.agno3.orchestrator.system.info.storage.drive.Drive;
import eu.agno3.orchestrator.system.info.storage.drive.PartitionTableType;
import eu.agno3.orchestrator.system.info.storage.drive.PhysicalDriveImpl;


/**
 * @author mbechler
 *
 */
public class LoopDriveCollector implements DriveCollector {

    private static final Logger log = Logger.getLogger(LoopDriveCollector.class);


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.info.storage.internal.DriveCollector#collectDrive(eu.agno3.orchestrator.system.info.storage.internal.CompoundDriveCollector,
     *      java.lang.String, java.util.Map, java.util.Map)
     */
    @Override
    public void collectDrive ( CompoundDriveCollector collector, String path, Map<String, Map<String, Variant<?>>> ifs, Map<String, Drive> drives ) {

        if ( !ifs.containsKey(Loop.class.getName()) || !ifs.containsKey(Block.class.getName()) ) {
            return;
        }

        Map<String, Variant<?>> properties = ifs.get(Block.class.getName());
        String name = VolumeUtils.getPreferredDeviceName(properties);
        UInt64 size = (UInt64) properties.get("Size").getValue(); //$NON-NLS-1$
        if ( name.startsWith("/dev/loop") && size != null && size.longValue() > 0 ) { //$NON-NLS-1$
            if ( log.isDebugEnabled() ) {
                log.debug("Found active loop device " + name); //$NON-NLS-1$
            }
            PhysicalDriveImpl d = new PhysicalDriveImpl();
            d.setId(name);
            d.setBlockDeviceName(name);
            d.setVendor("Loopback"); //$NON-NLS-1$
            d.setModel(name);
            d.setSerial(StringUtils.EMPTY);
            d.setSize(size.longValue());
            if ( ifs.containsKey(PartitionTable.class.getName()) ) {
                Map<String, Variant<?>> partProps = ifs.get(PartitionTable.class.getName());
                d.setPartitionTableType(VolumeUtils.getParitionTableType(partProps));
            }
            else {
                d.setPartitionTableType(PartitionTableType.NONE);
            }
            drives.put(d.getId(), d);
        }
    }

}
