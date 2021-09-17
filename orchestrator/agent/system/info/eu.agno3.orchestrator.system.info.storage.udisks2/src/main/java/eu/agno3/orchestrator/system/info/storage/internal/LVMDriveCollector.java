/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.internal;


import java.nio.charset.Charset;
import java.util.Map;

import org.apache.log4j.Logger;
import org.freedesktop.UDisks2.Block;
import org.freedesktop.dbus.types.Variant;

import eu.agno3.orchestrator.system.info.SystemInformationException;
import eu.agno3.orchestrator.system.info.storage.drive.Drive;
import eu.agno3.orchestrator.system.info.storage.drive.VolumeGroupImpl;


/**
 * @author mbechler
 * 
 */
public class LVMDriveCollector implements DriveCollector {

    private static final Logger log = Logger.getLogger(LVMDriveCollector.class);


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.info.storage.internal.DriveCollector#collectDrive(eu.agno3.orchestrator.system.info.storage.internal.CompoundDriveCollector,
     *      java.lang.String, java.util.Map, java.util.Map)
     */
    @Override
    public void collectDrive ( CompoundDriveCollector collector, String path, Map<String, Map<String, Variant<?>>> ifs, Map<String, Drive> drives ) {

        if ( !ifs.containsKey(Block.class.getName()) ) {
            return;
        }

        Map<String, Variant<?>> properties = ifs.get(Block.class.getName());

        Charset utf8 = Charset.forName("UTF-8"); //$NON-NLS-1$
        String name = ( new String((byte[]) properties.get("Device").getValue(), utf8) ).trim(); //$NON-NLS-1$
        String preferredName = ( new String((byte[]) properties.get("PreferredDevice").getValue(), utf8) ).trim(); //$NON-NLS-1$

        if ( name.startsWith("/dev/dm") ) { //$NON-NLS-1$
            String vgName = null;
            try {
                vgName = LVMUtil.getVgNameFromDevice(preferredName);
            }
            catch ( SystemInformationException e ) {
                log.warn("Failed to obtain volume group name from device:", e); //$NON-NLS-1$
                return;
            }
            if ( vgName != null && !drives.containsKey(VolumeGroupImpl.vgNameToDriveId(vgName)) ) {
                VolumeGroupImpl vg = new VolumeGroupImpl();
                vg.setVolumeGroupName(vgName);

                if ( log.isTraceEnabled() ) {
                    log.trace("Found volume group " + vg); //$NON-NLS-1$
                }
                drives.put(vg.getId(), vg);
            }
        }
    }
}
