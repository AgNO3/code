/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage;


import java.io.Serializable;
import java.util.Set;

import eu.agno3.orchestrator.system.info.SystemInformation;
import eu.agno3.orchestrator.system.info.storage.drive.Drive;


/**
 * @author mbechler
 * 
 */
public interface StorageInformation extends SystemInformation, Serializable {

    /**
     * 
     * @return all available drives
     */
    Set<Drive> getDrives ();


    /**
     * @param id
     * @return a specified drive
     */
    Drive getDriveById ( String id );

}
