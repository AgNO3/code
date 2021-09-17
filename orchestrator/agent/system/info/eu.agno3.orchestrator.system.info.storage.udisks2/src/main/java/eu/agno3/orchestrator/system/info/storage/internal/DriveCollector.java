/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.internal;


import java.util.Map;

import org.freedesktop.dbus.types.Variant;

import eu.agno3.orchestrator.system.info.storage.drive.Drive;


/**
 * @author mbechler
 * 
 */
public interface DriveCollector {

    /**
     * @param collector
     * @param path
     * @param ifs
     *            the interfaces and properties of the drive object
     * @param drives
     */
    void collectDrive ( CompoundDriveCollector collector, String path, Map<String, Map<String, Variant<?>>> ifs, Map<String, Drive> drives );

}