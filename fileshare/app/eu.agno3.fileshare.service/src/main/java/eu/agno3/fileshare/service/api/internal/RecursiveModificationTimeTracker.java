/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.10.2015 by mbechler
 */
package eu.agno3.fileshare.service.api.internal;


import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.vfs.VFSContext;


/**
 * @author mbechler
 *
 */
public interface RecursiveModificationTimeTracker {

    /**
     * 
     * @param v
     * @param e
     * @throws FileshareException
     */
    void trackUpdate ( VFSContext v, VFSEntity e ) throws FileshareException;


    /**
     * 
     * @param v
     * @param e
     * @return the recursive last modification time
     * @throws FileshareException
     */
    Long getRecursiveLastModifiedTime ( VFSContext v, VFSContainerEntity e ) throws FileshareException;

}