/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2015 by mbechler
 */
package eu.agno3.fileshare.service.vfs;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.TemporalType;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.InsufficentStorageSpaceException;
import eu.agno3.fileshare.exceptions.StorageException;
import eu.agno3.fileshare.model.ChangeType;
import eu.agno3.fileshare.model.ContainerChangeEntry;
import eu.agno3.fileshare.model.ContainerEntity;
import eu.agno3.fileshare.model.ContentEntity;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.FileEntity;
import eu.agno3.fileshare.model.MappedVFSEntity;
import eu.agno3.fileshare.model.NativeEntityKey;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.service.InputBuffer;
import eu.agno3.fileshare.service.api.internal.ScrollIterator;
import eu.agno3.fileshare.service.util.ServiceUtil;
import eu.agno3.fileshare.vfs.RequestRange;
import eu.agno3.fileshare.vfs.VFSChange;
import eu.agno3.fileshare.vfs.VFSContainerChange;
import eu.agno3.fileshare.vfs.VFSContentHandle;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.fileshare.vfs.VFSEntityChange;
import eu.agno3.fileshare.vfs.VFSStoreHandle;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.util.iter.ClosableIterator;
import eu.agno3.runtime.util.iter.NoCloseIterator;


/**
 * @author mbechler
 *
 */
public class NativeVFSContext implements VFSContext {

    private final NativeVFS vfs;
    private final EntityTransactionContext ets;


    /**
     * @param v
     * @param readOnly
     * @throws EntityTransactionException
     * 
     */
    public NativeVFSContext ( NativeVFS v, boolean readOnly ) throws EntityTransactionException {
        if ( readOnly ) {
            this.ets = v.getEntityTransactionService().startReadOnly();
        }
        else {
            this.ets = v.getEntityTransactionService().start();
        }
        this.vfs = v;
    }


    /**
     * @param v
     * @param ets
     * 
     */
    public NativeVFSContext ( NativeVFS v, EntityTransactionContext ets ) {
        this.vfs = v;
        this.ets = ets;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#getRoot()
     */
    @Override
    public VFSContainerEntity getRoot () {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#load(eu.agno3.fileshare.model.EntityKey)
     */
    @Override
    public VFSEntity load ( EntityKey k ) {
        return this.ets.getEntityManager().find(ContentEntity.class, extractId(k));
    }


    @Override
    public ContentEntity findMappedEntity ( VFSEntity k ) {
        return (ContentEntity) k;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#getOrCreateMappedEntity(eu.agno3.fileshare.model.VFSEntity)
     */
    @Override
    public ContentEntity getOrCreateMappedEntity ( VFSEntity entity ) {
        return (ContentEntity) entity;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#getVfsEntity(eu.agno3.fileshare.model.MappedVFSEntity)
     */
    @Override
    public VFSEntity getVfsEntity ( MappedVFSEntity e ) {
        return e;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#load(eu.agno3.fileshare.model.EntityKey, java.lang.Class)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public <T extends VFSEntity> T load ( EntityKey k, Class<T> type ) {

        if ( VFSEntity.class.isAssignableFrom(type) ) {
            return (T) this.ets.getEntityManager().find(ContentEntity.class, extractId(k));
        }

        throw new IllegalArgumentException("Invalid type"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#getChildren(eu.agno3.fileshare.model.VFSContainerEntity)
     */
    @Override
    public Set<? extends VFSEntity> getChildren ( VFSContainerEntity parent ) {
        if ( parent instanceof ContainerEntity ) {
            return ( (ContainerEntity) parent ).getElements();
        }

        return Collections.EMPTY_SET;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#addChild(eu.agno3.fileshare.model.VFSContainerEntity,
     *      eu.agno3.fileshare.model.VFSEntity)
     */
    @Override
    public void addChild ( VFSContainerEntity parent, VFSEntity child ) {
        if ( parent instanceof ContainerEntity && child instanceof ContentEntity ) {
            ( (ContentEntity) child ).setParent((ContainerEntity) parent);
            ( (ContainerEntity) parent ).getElements().add((ContentEntity) child);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#removeChild(eu.agno3.fileshare.model.VFSContainerEntity,
     *      eu.agno3.fileshare.model.VFSEntity)
     */
    @Override
    public void removeChild ( VFSContainerEntity parent, VFSEntity child ) {
        if ( parent instanceof ContainerEntity && child instanceof ContentEntity ) {
            ( (ContentEntity) child ).setParent(null);
            ( (ContainerEntity) parent ).getElements().remove(child);
        }

    }


    /**
     * @param k
     * @return
     */
    private static UUID extractId ( EntityKey k ) {
        if ( ! ( k instanceof NativeEntityKey ) ) {
            throw new IllegalArgumentException();
        }
        return ( (NativeEntityKey) k ).getId();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#save(eu.agno3.fileshare.model.VFSEntity[])
     */
    @Override
    public void save ( VFSEntity... entity ) {
        saveNoFlush(entity);
        this.ets.getEntityManager().flush();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#delete(eu.agno3.fileshare.model.VFSEntity)
     */
    @Override
    public void delete ( VFSEntity entity ) {
        if ( entity instanceof ContentEntity ) {
            this.ets.getEntityManager().remove(entity);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#removeMapped(eu.agno3.fileshare.model.ContentEntity)
     */
    @Override
    public void removeMapped ( ContentEntity mapped ) {
        ServiceUtil.cleanEntityReferences(this.ets, mapped);
        this.ets.getEntityManager().remove(mapped);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#saveNoFlush(eu.agno3.fileshare.model.VFSEntity[])
     */
    @Override
    public void saveNoFlush ( VFSEntity... entity ) {
        for ( VFSEntity e : entity ) {
            this.ets.getEntityManager().persist(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#refresh(eu.agno3.fileshare.model.VFSEntity[])
     */
    @Override
    public void refresh ( VFSEntity... entity ) {
        for ( VFSEntity e : entity ) {
            this.ets.getEntityManager().refresh(e);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#getParent(eu.agno3.fileshare.model.VFSEntity)
     */
    @Override
    public VFSContainerEntity getParent ( VFSEntity e ) {
        if ( e instanceof ContentEntity ) {
            return ( (ContentEntity) e ).getParent();
        }
        throw new UnsupportedOperationException("Do not known parent for " + e); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#commit()
     */
    @Override
    public void commit () throws FileshareException {
        try {
            if ( !this.ets.isReadOnly() ) {
                this.ets.commit();
            }
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to commit entity transaction", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#close()
     */
    @Override
    public void close () throws FileshareException {
        try {
            this.ets.close();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to close entity transaction", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#canResolveByName()
     */
    @Override
    public boolean canResolveByName () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#resolveRelative(eu.agno3.fileshare.model.VFSContainerEntity,
     *      java.lang.String[])
     */
    @Override
    public VFSEntity resolveRelative ( VFSContainerEntity persistent, String[] relativeSegments ) throws FileshareException {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#getQuotaKey(eu.agno3.fileshare.model.VFSContainerEntity)
     */
    @Override
    public EntityKey getQuotaKey ( VFSContainerEntity root ) {
        if ( root instanceof ContainerEntity ) {
            ContainerEntity subjectRoot = root.getOwner().getSubjectRoot();
            if ( subjectRoot != null ) {
                return subjectRoot.getEntityKey();
            }
        }
        throw new UnsupportedOperationException();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#getUsedQuota(eu.agno3.fileshare.model.VFSContainerEntity)
     */
    @Override
    public long getUsedQuota ( VFSContainerEntity root ) {
        if ( root instanceof ContainerEntity ) {
            return ( (ContainerEntity) root ).getOwner().getSubjectRoot().getChildrenSize();
        }
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#getQuota(eu.agno3.fileshare.model.VFSContainerEntity)
     */
    @Override
    public Long getQuota ( VFSContainerEntity root ) {
        if ( root instanceof ContainerEntity ) {
            return root.getOwner().getQuota();
        }
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#trackCollectionSizes()
     */
    @Override
    public boolean trackCollectionSizes () {
        return true;
    }


    @Override
    public void updateCollectionSize ( VFSContainerEntity entity, long usedSize ) {
        if ( entity instanceof ContainerEntity ) {
            ( (ContainerEntity) entity ).setChildrenSize(usedSize);
            this.ets.getEntityManager().persist(entity);
            this.ets.getEntityManager().flush();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#getRecursiveLastModified(eu.agno3.fileshare.model.VFSContainerEntity)
     */
    @Override
    public DateTime getRecursiveLastModified ( VFSContainerEntity entity ) {
        if ( entity instanceof ContainerEntity ) {
            return ( (ContainerEntity) entity ).getRecursiveLastModified();
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#trackRecursiveLastModificationTimes()
     */
    @Override
    public boolean trackRecursiveLastModificationTimes () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#updateRecursiveLastModifiedTime(eu.agno3.fileshare.model.VFSContainerEntity,
     *      org.joda.time.DateTime)
     */
    @Override
    public void updateRecursiveLastModifiedTime ( VFSContainerEntity entity, DateTime lastModification ) {
        if ( entity instanceof ContainerEntity ) {
            ( (ContainerEntity) entity ).setRecursiveLastModified(lastModification);
            this.ets.getEntityManager().persist(entity);
            this.ets.getEntityManager().flush();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#trackChange(eu.agno3.fileshare.model.VFSContainerEntity,
     *      eu.agno3.fileshare.model.VFSEntity, eu.agno3.fileshare.model.ChangeType)
     */
    @Override
    public void trackChange ( VFSContainerEntity e, VFSEntity child, ChangeType t )
            throws eu.agno3.fileshare.exceptions.UnsupportedOperationException {
        trackChange(e, child, t, DateTime.now());
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#trackChange(eu.agno3.fileshare.model.VFSContainerEntity,
     *      eu.agno3.fileshare.model.VFSEntity, eu.agno3.fileshare.model.ChangeType)
     */
    @Override
    public void trackChange ( VFSContainerEntity parent, VFSEntity child, ChangeType t, DateTime time ) {
        if ( parent instanceof ContainerEntity ) {
            ContainerEntity ce = (ContainerEntity) parent;
            ContainerChangeEntry change = new ContainerChangeEntry();
            change.setLocalName(child.getLocalName());
            change.setContainer(ce);
            change.setEntityType(child.getEntityType());
            change.setChangeTime(time);
            change.setChangeType(t);
            ce.getChanges().add(change);
            this.ets.getEntityManager().persist(change);
        }

        if ( t == ChangeType.MOVE && child instanceof ContentEntity ) {
            ( (ContentEntity) child ).setLastMoved(DateTime.now());
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#findModifiedSince(eu.agno3.fileshare.model.VFSContainerEntity,
     *      java.lang.Long)
     */

    @Override
    public ClosableIterator<VFSChange> findModifiedSince ( VFSContainerEntity root, Long lastMod ) throws FileshareException {
        if ( root instanceof ContainerEntity && ( (ContainerEntity) root ).getParent() == null ) {
            // this is a subject's root container so we can just search for the owned entries
            return optimizedFindModifiedSince(root, lastMod);
        }
        else if ( root instanceof ContainerEntity ) {
            List<VFSChange> modified = new ArrayList<>();
            addRecursiveModifications((ContainerEntity) root, lastMod, modified, false, false);
            return new NoCloseIterator<>(modified.iterator());
        }
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#findModifiedSinceSupported(eu.agno3.fileshare.model.VFSContainerEntity,
     *      java.lang.Long)
     */
    @Override
    public boolean findModifiedSinceSupported ( VFSContainerEntity groupRoot, Long lm ) {
        return true;
    }


    /**
     * @param root
     * @param lastMod
     * @param modified
     */
    private void addRecursiveModifications ( ContentEntity root, Long lastMod, List<VFSChange> modified, boolean addRoot, boolean force ) {
        if ( addRoot && ( force || lastMod == null || root.getLastModified().isAfter(lastMod) ) ) {
            modified.add(new VFSEntityChange(root));
        }
        if ( root instanceof FileEntity ) {
            return;
        }
        else if ( root instanceof ContainerEntity ) {
            ContainerEntity ce = (ContainerEntity) root;
            DateTime recLastMod = ce.getRecursiveLastModified();
            Set<String> foundChildren = new HashSet<>();
            for ( ContentEntity child : ce.getElements() ) {
                foundChildren.add(child.getLocalName());
                if ( lastMod == null || recLastMod == null || recLastMod.isAfter(lastMod) ) {
                    // force all children to be reported as modified if this node was moved
                    boolean moved = lastMod != null && child.getLastMoved() != null && child.getLastMoved().isAfter(lastMod);
                    addRecursiveModifications(child, lastMod, modified, true, force || moved);
                }
            }

            if ( lastMod != null ) {
                for ( ContainerChangeEntry ch : ( (ContainerEntity) root ).getChanges() ) {
                    if ( foundChildren.contains(ch.getLocalName()) ) {
                        // skip elements that exist (i.e. have been readded after they were deleted)
                        continue;
                    }
                    if ( ch.getChangeTime().isAfter(lastMod) ) {
                        modified.add(new VFSContainerChange(ce, ch.getLocalName(), ch.getEntityType(), ch.getChangeType(), ch.getChangeTime()));
                    }
                }
            }
        }
        else {
            throw new UnsupportedOperationException();
        }
    }


    @SuppressWarnings ( "resource" )
    ClosableIterator<VFSChange> optimizedFindModifiedSince ( VFSContainerEntity root, Long lastMod ) {
        ClosableIterator<ContentEntity> modEntries = getModificationIterator(root, lastMod);
        ClosableIterator<ContainerChangeEntry> contEntries = null;
        if ( lastMod != null ) {
            contEntries = getContainerModificationIterator(root, lastMod);
        }
        return new ChangeIterator<>(modEntries, contEntries);
    }


    @SuppressWarnings ( {
        "resource"
    } )
    private ClosableIterator<ContainerChangeEntry> getContainerModificationIterator ( VFSContainerEntity root, long lastMod ) {
        Session s = getEntityManager().unwrap(Session.class);
        Query<ContainerChangeEntry> q = s.createQuery(
            "SELECT ch FROM ContainerChangeEntry ch INNER JOIN ch.container AS e WHERE e.owner = :owner AND ch.changeTime > :lastMod ", //$NON-NLS-1$
            ContainerChangeEntry.class);
        q.setParameter("lastMod", new DateTime(lastMod), TemporalType.TIMESTAMP); //$NON-NLS-1$
        q.setParameter("owner", root.getOwner()); //$NON-NLS-1$
        q.setFetchSize(128);
        q.setReadOnly(true);
        q.setLockMode("e", LockMode.NONE); //$NON-NLS-1$

        return new ScrollIterator<>(ContainerChangeEntry.class, q, null);
    }


    @SuppressWarnings ( {
        "resource"
    } )
    ScrollIterator<ContentEntity> getModificationIterator ( VFSContainerEntity root, Long lastMod ) {
        Query<ContentEntity> q; // $NON-NLS-1$
        Session s = getEntityManager().unwrap(Session.class);
        if ( lastMod == null ) {
            q = s.createQuery("SELECT e FROM ContentEntity e WHERE owner = :owner AND e != :entity", ContentEntity.class); //$NON-NLS-1$
        }
        else {
            q = s.createQuery(
                "SELECT e FROM ContentEntity e WHERE e.owner = :owner AND e.lastModified > :lastMod AND e != :entity", //$NON-NLS-1$
                ContentEntity.class);
            q.setParameter("lastMod", new DateTime(lastMod), TemporalType.DATE); //$NON-NLS-1$
        }
        q.setParameter("owner", root.getOwner()); //$NON-NLS-1$
        q.setParameter("entity", root); //$NON-NLS-1$
        q.setFetchSize(128);
        q.setReadOnly(true);
        q.setLockMode("e", LockMode.NONE); //$NON-NLS-1$

        return new ScrollIterator<>(ContentEntity.class, q, null);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#getContents(eu.agno3.fileshare.model.VFSFileEntity,
     *      eu.agno3.fileshare.vfs.RequestRange)
     */
    @Override
    public VFSContentHandle getContents ( VFSFileEntity file, RequestRange r ) throws StorageException {
        return this.vfs.getBlockStore().getContents(file);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws StorageException
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#storeContents(eu.agno3.fileshare.model.VFSFileEntity,
     *      eu.agno3.fileshare.service.InputBuffer)
     */
    @Override
    public VFSStoreHandle storeContents ( VFSFileEntity f, InputBuffer input ) throws StorageException {
        return this.vfs.getBlockStore().storeContents(f, input);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws StorageException
     * 
     * @see eu.agno3.fileshare.vfs.VFSContext#replaceContents(eu.agno3.fileshare.model.VFSFileEntity,
     *      eu.agno3.fileshare.service.InputBuffer)
     */
    @Override
    public VFSStoreHandle replaceContents ( VFSFileEntity targetFile, InputBuffer input ) throws StorageException {
        return this.vfs.getBlockStore().replaceContents(targetFile, input);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws StorageException
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#removeContents(eu.agno3.fileshare.model.VFSFileEntity)
     */
    @Override
    public void removeContents ( VFSFileEntity targetFile ) throws StorageException {
        this.vfs.getBlockStore().removeContents(targetFile);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#checkFreeSpace(long, long)
     */
    @Override
    public void checkFreeSpace ( long neededSize, long temporarySize ) throws InsufficentStorageSpaceException {
        // No need to check free space as the file is already on the destination volume
    }


    /**
     * @return the used entity manager
     */
    public EntityManager getEntityManager () {
        return this.ets.getEntityManager();
    }
}
