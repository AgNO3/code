/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.fs;


import java.io.Serializable;
import java.util.UUID;


/**
 * @author mbechler
 * 
 */
public interface FileSystem extends Serializable {

    /**
     * 
     * Typically this will delegate to label and or UUID and probably fallback to the device name
     * 
     * @return an identifier for this filesystem
     */
    String getIdentifier ();


    /**
     * 
     * @return the filesystem type
     */
    FileSystemType getFsType ();


    /**
     * 
     * @return the filesystem's UUID, if it has one
     */
    UUID getUuid ();


    /**
     * 
     * @return the filesystem's label, if it has one
     */
    String getLabel ();

}
