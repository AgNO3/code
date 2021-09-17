/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.fs;


/**
 * @author mbechler
 * 
 */
public interface SwapFileSystem extends FileSystem {

    /**
     * 
     * @return whether this swapspace is currently used by the filesystem
     */
    boolean isActive ();
}
