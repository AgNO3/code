/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2015 by mbechler
 */
package eu.agno3.fileshare.vfs;


import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.InsufficentStorageSpaceException;
import eu.agno3.fileshare.exceptions.StorageException;
import eu.agno3.fileshare.exceptions.UnsupportedOperationException;
import eu.agno3.fileshare.model.ChangeType;
import eu.agno3.fileshare.model.ContentEntity;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.MappedVFSEntity;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.service.InputBuffer;
import eu.agno3.runtime.util.iter.ClosableIterator;


/**
 * @author mbechler
 *
 */
public interface VFSContext extends AutoCloseable {

    /**
     * @param ctx
     * @param k
     * @return the loaded entity
     * @throws FileshareException
     */
    VFSEntity load ( EntityKey k ) throws FileshareException;


    /**
     * @param ctx
     * @param k
     * @param type
     * @return the loaded entity
     * @throws FileshareException
     */
    <T extends VFSEntity> @Nullable T load ( EntityKey k, Class<T> type ) throws FileshareException;


    /**
     * @param id
     * @return the vfs root
     * @throws FileshareException
     */
    VFSContainerEntity getRoot () throws FileshareException;


    /**
     * @throws FileshareException
     * 
     */
    void commit () throws FileshareException;


    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close () throws FileshareException;


    /**
     * @param entity
     * @return a persistently mapped entity for the object, created if not exists
     * @throws FileshareException
     */
    ContentEntity getOrCreateMappedEntity ( VFSEntity entity ) throws FileshareException;


    /**
     * 
     * @param entity
     * @param entityId
     * @return a persistencly mapped entity for the object, null if not exists
     */
    ContentEntity findMappedEntity ( VFSEntity entity );


    /**
     * @param parent
     * @return the object children
     * @throws FileshareException
     */
    Set<? extends VFSEntity> getChildren ( VFSContainerEntity parent ) throws FileshareException;


    /**
     * @param parent
     * @param child
     * @param directory
     * @throws UnsupportedOperationException
     */
    void addChild ( VFSContainerEntity parent, VFSEntity child ) throws UnsupportedOperationException;


    /**
     * @param parent
     * @param oldParent
     * @param entity
     * @throws UnsupportedOperationException
     */
    void removeChild ( VFSContainerEntity parent, VFSEntity entity ) throws UnsupportedOperationException;


    /**
     * @param directory
     * @param parent
     */
    void save ( VFSEntity... directory );


    /**
     * @param e
     */
    void saveNoFlush ( VFSEntity... e );


    /**
     * @param directory
     * @param parent
     * @throws FileshareException
     */
    void refresh ( VFSEntity... directory ) throws FileshareException;


    /**
     * @param entity
     * @throws UnsupportedOperationException
     */
    void delete ( VFSEntity entity ) throws UnsupportedOperationException;


    /**
     * @param mapped
     */
    void removeMapped ( ContentEntity mapped );


    /**
     * @param e
     * @return the parent entity
     * @throws FileshareException
     */
    VFSContainerEntity getParent ( VFSEntity e ) throws FileshareException;


    /**
     * @return whether this VFS module has a optimized path resolving routing
     */
    boolean canResolveByName ();


    /**
     * @param persistent
     * @param relativeSegments
     * @return the resolved entity
     * @throws FileshareException
     */
    VFSEntity resolveRelative ( VFSContainerEntity persistent, String[] relativeSegments ) throws FileshareException;


    /**
     * @param e
     * @return the quota used on the entity root
     */
    long getUsedQuota ( VFSContainerEntity e );


    /**
     * @param e
     * @return the quota on the entity root
     */
    Long getQuota ( VFSContainerEntity e );


    /**
     * @param parent
     * @return an identifier for the entity root
     */
    EntityKey getQuotaKey ( VFSContainerEntity parent );


    /**
     * @return whether this VFS needs to track collection sizes
     */
    boolean trackCollectionSizes ();


    /**
     * @param e
     * @param usedSize
     */
    void updateCollectionSize ( VFSContainerEntity e, long usedSize );


    /**
     * @param file
     * @param range
     * @return an input stream with the file's contents
     * @throws FileshareException
     * @throws StorageException
     */
    VFSContentHandle getContents ( VFSFileEntity file, RequestRange range ) throws FileshareException;


    /**
     * @param f
     * @param input
     * @return the written file size
     * @throws FileshareException
     */
    VFSStoreHandle storeContents ( VFSFileEntity f, InputBuffer input ) throws FileshareException;


    /**
     * @param targetFile
     * @param input
     * @return the written file size
     * @throws FileshareException
     */
    VFSStoreHandle replaceContents ( VFSFileEntity targetFile, InputBuffer input ) throws FileshareException;


    /**
     * @param targetFile
     * @throws FileshareException
     */
    void removeContents ( VFSFileEntity targetFile ) throws FileshareException;


    /**
     * @param root
     * @return the last modified time of any child
     */
    DateTime getRecursiveLastModified ( VFSContainerEntity root );


    /**
     * @param entity
     * @param lastModification
     * @throws UnsupportedOperationException
     */
    void updateRecursiveLastModifiedTime ( VFSContainerEntity entity, DateTime lastModification ) throws UnsupportedOperationException;


    /**
     * @return whether to recieve updates on recursive last modification times
     */
    boolean trackRecursiveLastModificationTimes ();


    /**
     * @param e
     * @return a mapped entity for the VFS entity
     * @throws FileshareException
     */
    VFSEntity getVfsEntity ( MappedVFSEntity e ) throws FileshareException;


    /**
     * @param neededSize
     * @param temporarySpace
     * @throws InsufficentStorageSpaceException
     */
    void checkFreeSpace ( long neededSize, long temporarySpace ) throws InsufficentStorageSpaceException;


    /**
     * 
     * @param e
     * @param child
     * @param t
     * @throws UnsupportedOperationException
     */
    void trackChange ( VFSContainerEntity e, VFSEntity child, ChangeType t ) throws UnsupportedOperationException;


    /**
     * @param e
     * @param child
     * @param t
     * @param now
     * @throws UnsupportedOperationException
     */
    void trackChange ( VFSContainerEntity e, VFSEntity child, ChangeType t, DateTime now ) throws UnsupportedOperationException;


    /**
     * @param root
     * @param lastMod
     * @return the entities under the given root modified since the given timestamp
     * @throws FileshareException
     */
    ClosableIterator<VFSChange> findModifiedSince ( VFSContainerEntity root, Long lastMod ) throws FileshareException;


    /**
     * @param groupRoot
     * @param lm
     * @return whether enumerating modification is supported
     */
    boolean findModifiedSinceSupported ( VFSContainerEntity groupRoot, Long lm );

}
