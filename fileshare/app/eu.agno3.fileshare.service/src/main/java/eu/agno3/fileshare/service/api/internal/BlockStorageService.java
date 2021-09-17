/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.01.2015 by mbechler
 */
package eu.agno3.fileshare.service.api.internal;


import eu.agno3.fileshare.exceptions.InsufficentStorageSpaceException;
import eu.agno3.fileshare.exceptions.StorageException;
import eu.agno3.fileshare.exceptions.StoreFailedException;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.service.InputBuffer;
import eu.agno3.fileshare.vfs.VFSContentHandle;
import eu.agno3.fileshare.vfs.VFSStoreHandle;


/**
 * @author mbechler
 *
 */
public interface BlockStorageService {

    /**
     * Get an input stream of the stored file contents
     * 
     * Access control checks must be performed by the caller
     * 
     * @param entity
     * @return an input stream with the block data
     * @throws StorageException
     */
    VFSContentHandle getContents ( VFSFileEntity entity ) throws StorageException;


    /**
     * Store the contents given by input stream for file
     * 
     * Access control checks must be performed by the caller
     * 
     * @param f
     * @param data
     * @return the number of bytes written
     * @throws StorageException
     */
    VFSStoreHandle storeContents ( VFSFileEntity f, InputBuffer data ) throws StorageException;


    /**
     * @param f
     * @param data
     * @return the number of bytes written
     * @throws StoreFailedException
     * @throws StorageException
     */
    VFSStoreHandle replaceContents ( VFSFileEntity f, InputBuffer data ) throws StorageException;


    /**
     * Remove the given file's stored contents
     * 
     * Access control checks must be performed by the caller
     * 
     * @param f
     * @throws StorageException
     */
    void removeContents ( VFSFileEntity f ) throws StorageException;


    /**
     * @param neededSize
     * @param temporarySize
     * @throws InsufficentStorageSpaceException
     */
    void checkFreeSpace ( long neededSize, long temporarySize ) throws InsufficentStorageSpaceException;

}
