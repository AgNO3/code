/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.fs;


import java.util.Set;


/**
 * @author mbechler
 * 
 */
public interface DataFileSystem extends FileSystem {

    /**
     * @return the mountpoints under which this filesystem is mounted
     */
    Set<String> getMountPoints ();


    /**
     * @return the total extents of this file system (null if unknown)
     */
    Long getTotalSpace ();


    /**
     * @return the usable space on this file system (null if unknown)
     */
    Long getUsableSpace ();


    /**
     * @return estimated space in the partition/drive that is not captured by this filesystem
     */
    Long getUncapturedSpace ();
}
