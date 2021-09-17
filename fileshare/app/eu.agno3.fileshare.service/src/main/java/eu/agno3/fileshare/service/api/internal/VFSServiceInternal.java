/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2015 by mbechler
 */
package eu.agno3.fileshare.service.api.internal;


import java.util.Collection;

import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.MultiVFSException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.service.VFSService;
import eu.agno3.fileshare.vfs.VFS;


/**
 * @author mbechler
 *
 */
public interface VFSServiceInternal extends VFSService {

    /**
     * 
     * @param key
     * @return a VFS implementation for the entity key
     * @throws EntityNotFoundException
     */
    VFS getVFS ( EntityKey key ) throws EntityNotFoundException;


    /**
     * @param ids
     * @return a VFS implementation that can handle all given entity keys
     * @throws EntityNotFoundException
     * @throws MultiVFSException
     */
    VFS getVFS ( Collection<EntityKey> ids ) throws EntityNotFoundException, MultiVFSException;


    /**
     * @return the native VFS implementation
     */
    VFS getNative ();


    /**
     * @param id
     * @return the VFS implementation for the given id
     * @throws EntityNotFoundException
     */
    VFS getVFS ( String id ) throws EntityNotFoundException;
}
