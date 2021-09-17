/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.volume;


import java.io.Serializable;

import eu.agno3.orchestrator.system.info.storage.drive.Drive;
import eu.agno3.orchestrator.system.info.storage.fs.FileSystem;


/**
 * @author mbechler
 * 
 */
public interface Volume extends Serializable {

    /**
     * 
     * @return the drive on which this volume is located
     */
    Drive getDrive ();


    /**
     * 
     * @return the device this maps to
     */
    String getDevice ();


    /**
     * 
     * @return the volume size
     */
    long getSize ();


    /**
     * 
     * @return whether a filesystem exists on this volume
     */
    boolean holdsFilesystem ();


    /**
     * @return the filesystem contained on this volume, if any
     */
    FileSystem getFileSystem ();


    /**
     * @return volume label
     */
    String getLabel ();

}