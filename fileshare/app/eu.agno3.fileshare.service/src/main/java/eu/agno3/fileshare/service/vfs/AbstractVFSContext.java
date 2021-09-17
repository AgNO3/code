/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.01.2016 by mbechler
 */
package eu.agno3.fileshare.service.vfs;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.UnsupportedOperationException;
import eu.agno3.fileshare.model.ChangeType;
import eu.agno3.fileshare.model.ContentEntity;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.EntityReferenceStorage;
import eu.agno3.fileshare.model.MappedContainerEntity;
import eu.agno3.fileshare.model.MappedFileEntity;
import eu.agno3.fileshare.model.MappedVFSEntity;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSEntityImpl;
import eu.agno3.fileshare.model.VFSEntityKeyWithPath;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.model.VirtualGroup;
import eu.agno3.fileshare.service.InputBuffer;
import eu.agno3.fileshare.service.util.ServiceUtil;
import eu.agno3.fileshare.vfs.VFSChange;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.fileshare.vfs.VFSStoreHandle;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.db.orm.EntityTransactionService;
import eu.agno3.runtime.util.iter.ClosableIterator;

import jcifs.smb.NtStatus;
import jcifs.smb.SmbException;


/**
 * @author mbechler
 * @param <T>
 *
 */
public abstract class AbstractVFSContext <T extends AbstractVFS> implements VFSContext {

    private static final Logger log = Logger.getLogger(AbstractVFSContext.class);
    private final T vfs;
    private final EntityTransactionContext ets;


    /**
     * @param v
     * @param es
     * @param readOnly
     * @throws EntityTransactionException
     * 
     */
    public AbstractVFSContext ( T v, EntityTransactionService es, boolean readOnly ) throws EntityTransactionException {
        if ( readOnly ) {
            this.ets = es.startReadOnly();
        }
        else {
            this.ets = es.start();
        }
        this.vfs = v;
    }


    /**
     * @param v
     * @param ets
     * 
     */
    public AbstractVFSContext ( T v, EntityTransactionContext ets ) {
        this.vfs = v;
        this.ets = ets;
    }


    /**
     * @return the vfs
     */
    public T getVfs () {
        return this.vfs;
    }


    /**
     * @return the em
     */
    public EntityManager getEm () {
        return this.ets.getEntityManager();
    }


    /**
     * @return the txContext
     */
    public EntityTransactionContext getTxContext () {
        return this.ets;
    }


    protected String getRelativePath ( EntityKey k ) throws UnsupportedOperationException {
        if ( ! ( k instanceof VFSEntityKeyWithPath ) ) {
            throw new UnsupportedOperationException();
        }
        VFSEntityKeyWithPath vkp = (VFSEntityKeyWithPath) k;
        checkSameVFS(vkp);

        String decode = vkp.getPath();

        if ( !StringUtils.isBlank(decode) && decode.charAt(0) == '/' ) {
            return decode.substring(1);
        }
        return decode;
    }


    /**
     * @param vkp
     * @throws UnsupportedOperationException
     */
    protected void checkSameVFS ( EntityKey vkp ) throws UnsupportedOperationException {
        if ( ! ( vkp instanceof VFSEntityKeyWithPath ) ) {
            throw new UnsupportedOperationException("Not an VFS entity"); //$NON-NLS-1$
        }
        if ( !this.vfs.getId().equals( ( (VFSEntityKeyWithPath) vkp ).getVFS()) ) {
            throw new UnsupportedOperationException("Group does not match"); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws EntityNotFoundException
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#getOrCreateMappedEntity(eu.agno3.fileshare.model.VFSEntity)
     */
    @Override
    public ContentEntity getOrCreateMappedEntity ( VFSEntity entity ) throws EntityNotFoundException {
        ContentEntity found = findMappedEntity(entity);
        if ( found == null ) {
            MappedVFSEntity mapped = null;
            if ( entity instanceof VFSContainerEntity ) {
                mapped = new MappedContainerEntity();
            }
            else if ( entity instanceof VFSFileEntity ) {
                mapped = new MappedFileEntity();
            }

            if ( mapped != null ) {
                EntityManager em = this.ets.getEntityManager();
                VirtualGroup vgroup = em.find(VirtualGroup.class, getVfs().getGroup().getId());
                if ( vgroup == null ) {
                    throw new EntityNotFoundException("Virtual group does not exist"); //$NON-NLS-1$
                }

                if ( log.isDebugEnabled() ) {
                    log.debug("Creating mapped entity for " + entity); //$NON-NLS-1$
                }

                mapped.setDelegate(entity);
                mapped.setVfs(vgroup);
                mapped.setInode(entity.getInode());
                mapped.setReferenceStorage(new EntityReferenceStorage( ( (VFSEntityImpl) entity ).getRelativePath()));
                entity.setSecurityLabel(ServiceUtil.getOrCreateSecurityLabel(this.ets, entity.getSecurityLabel().getLabel()));

                em.persist(mapped);
                em.flush();
                getVfs().getMappedEntityNegativeCache().remove(entity.getEntityKey());
                found = (ContentEntity) mapped;
            }
        }
        return found;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#findMappedEntity(eu.agno3.fileshare.model.VFSEntity)
     */
    @Override
    public ContentEntity findMappedEntity ( VFSEntity entity ) {
        if ( getVfs().getMappedEntityNegativeCache().contains(entity.getEntityKey()) ) {
            return null;
        }

        if ( entity instanceof VFSContainerEntity ) {
            TypedQuery<MappedContainerEntity> q = this.ets.getEntityManager()
                    .createQuery("SELECT entity FROM MappedContainerEntity entity WHERE vfs = :vfs AND inode = :inode", MappedContainerEntity.class); //$NON-NLS-1$
            q.setParameter("vfs", getVfs().getGroup()); //$NON-NLS-1$
            q.setParameter("inode", entity.getInode()); //$NON-NLS-1$
            q.setMaxResults(1);
            List<MappedContainerEntity> res = q.getResultList();
            if ( res.size() == 1 ) {
                MappedContainerEntity mapped = res.get(0);
                mapped.setDelegate(entity);
                return mapped;
            }
        }
        else if ( entity instanceof VFSFileEntity ) {
            TypedQuery<MappedFileEntity> q = this.ets.getEntityManager()
                    .createQuery("SELECT entity FROM MappedFileEntity entity WHERE vfs = :vfs AND inode = :inode", MappedFileEntity.class); //$NON-NLS-1$
            q.setParameter("vfs", getVfs().getGroup()); //$NON-NLS-1$
            q.setParameter("inode", entity.getInode()); //$NON-NLS-1$
            q.setMaxResults(1);
            List<MappedFileEntity> res = q.getResultList();
            if ( res.size() == 1 ) {
                MappedFileEntity mapped = res.get(0);
                mapped.setDelegate(entity);
                return mapped;
            }
        }

        getVfs().getMappedEntityNegativeCache().add(entity.getEntityKey());
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#getRecursiveLastModified(eu.agno3.fileshare.model.VFSContainerEntity)
     */
    @Override
    public DateTime getRecursiveLastModified ( VFSContainerEntity e ) {
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws eu.agno3.fileshare.exceptions.UnsupportedOperationException
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#addChild(eu.agno3.fileshare.model.VFSContainerEntity,
     *      eu.agno3.fileshare.model.VFSEntity)
     */
    @Override
    public void addChild ( VFSContainerEntity parent, VFSEntity child ) throws eu.agno3.fileshare.exceptions.UnsupportedOperationException {
        // read only
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#removeChild(eu.agno3.fileshare.model.VFSContainerEntity,
     *      eu.agno3.fileshare.model.VFSEntity)
     */
    @Override
    public void removeChild ( VFSContainerEntity parent, VFSEntity entity ) throws UnsupportedOperationException {
        // read only
        throw new UnsupportedOperationException();
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
     * @throws FileshareException
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#getVfsEntity(eu.agno3.fileshare.model.MappedVFSEntity)
     */
    @Override
    public VFSEntity getVfsEntity ( MappedVFSEntity e ) throws FileshareException {

        VFSEntity delegate = e.getDelegate();
        if ( delegate != null ) {
            checkSameVFS(delegate.getEntityKey());
            return delegate;
        }

        if ( !getVfs().getGroup().equals(e.getVfs()) ) {
            throw new UnsupportedOperationException();
        }

        String path = e.getReferenceStorage().getRelativePath();
        VFSEntity resolved = this.resolveRelative(getRoot(), StringUtils.split(path, '/'));
        if ( resolved == null ) {
            throw new EntityNotFoundException();
        }

        if ( !Arrays.equals(resolved.getInode(), e.getInode()) ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format(
                    "Inode mismatch, resolved: %s, selected: %s", //$NON-NLS-1$
                    Arrays.toString(resolved.getInode()),
                    Arrays.toString(e.getInode())));
                log.debug(Hex.encodeHexString(resolved.getInode()));
                log.debug(Hex.encodeHexString(e.getInode()));
            }
            throw new EntityNotFoundException("Inode mismatch"); //$NON-NLS-1$
        }
        return resolved;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#save(eu.agno3.fileshare.model.VFSEntity[])
     */
    @Override
    public void save ( VFSEntity... e ) {
        saveNoFlush(e);
        this.ets.getEntityManager().flush();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#saveNoFlush(eu.agno3.fileshare.model.VFSEntity[])
     */
    @Override
    public void saveNoFlush ( VFSEntity... es ) {
        for ( VFSEntity e : es ) {
            if ( e instanceof MappedVFSEntity ) {
                this.ets.getEntityManager().persist(e);
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#refresh(eu.agno3.fileshare.model.VFSEntity[])
     */
    @Override
    public void refresh ( VFSEntity... directory ) throws FileshareException {

        for ( VFSEntity e : directory ) {
            try {
                if ( e instanceof VFSEntityImpl ) {
                    refreshInternal((VFSEntityImpl) e);
                }
                else if ( e instanceof MappedVFSEntity ) {
                    this.ets.getEntityManager().refresh(e);
                }
            }
            catch ( IOException ex ) {
                throw handleException(e.getEntityKey(), ex);
            }
        }

    }


    /**
     * @param e
     * @return
     * @throws EntityNotFoundException
     */
    protected FileshareException handleException ( EntityKey k, Throwable e ) {
        if ( e instanceof FileNotFoundException ) {
            return new EntityNotFoundException("Failed to locate entity " + k, e); //$NON-NLS-1$
        }

        if ( e instanceof SmbException ) {
            int status = ( (SmbException) e ).getNtStatus();
            if ( status == NtStatus.NT_STATUS_BAD_NETWORK_NAME || status == NtStatus.NT_STATUS_NETWORK_NAME_DELETED ) {
                return new EntityNotFoundException("Cannot access entity", e); //$NON-NLS-1$
            }
        }

        return new FileshareException("Unhandled VFS exception", e); //$NON-NLS-1$
    }


    /**
     * @param e
     */
    protected abstract void refreshInternal ( VFSEntityImpl e ) throws IOException, FileshareException;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#removeMapped(eu.agno3.fileshare.model.ContentEntity)
     */
    @Override
    public void removeMapped ( ContentEntity mapped ) {
        ContentEntity found = findMappedEntity(mapped);
        if ( found instanceof MappedVFSEntity ) {
            ServiceUtil.cleanEntityReferences(this.ets, found);
            this.ets.getEntityManager().remove(found);
            getVfs().getMappedEntityNegativeCache().add(found.getEntityKey());
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
     * @throws UnsupportedOperationException
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#delete(eu.agno3.fileshare.model.VFSEntity)
     */
    @Override
    public void delete ( VFSEntity entity ) throws UnsupportedOperationException {
        // read only
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#getUsedQuota(eu.agno3.fileshare.model.VFSContainerEntity)
     */
    @Override
    public long getUsedQuota ( VFSContainerEntity e ) {
        return 0;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#getQuota(eu.agno3.fileshare.model.VFSContainerEntity)
     */
    @Override
    public Long getQuota ( VFSContainerEntity e ) {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#getQuotaKey(eu.agno3.fileshare.model.VFSContainerEntity)
     */
    @Override
    public EntityKey getQuotaKey ( VFSContainerEntity parent ) {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#trackCollectionSizes()
     */
    @Override
    public boolean trackCollectionSizes () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#updateCollectionSize(eu.agno3.fileshare.model.VFSContainerEntity, long)
     */
    @Override
    public void updateCollectionSize ( VFSContainerEntity e, long usedSize ) {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#trackRecursiveLastModificationTimes()
     */
    @Override
    public boolean trackRecursiveLastModificationTimes () {
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#updateRecursiveLastModifiedTime(eu.agno3.fileshare.model.VFSContainerEntity,
     *      org.joda.time.DateTime)
     */
    @Override
    public void updateRecursiveLastModifiedTime ( VFSContainerEntity entity, DateTime lastModification ) throws UnsupportedOperationException {
        // ignore
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#removeContents(eu.agno3.fileshare.model.VFSFileEntity)
     */
    @Override
    public void removeContents ( VFSFileEntity targetFile ) throws FileshareException {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#replaceContents(eu.agno3.fileshare.model.VFSFileEntity,
     *      eu.agno3.fileshare.service.InputBuffer)
     */
    @Override
    public VFSStoreHandle replaceContents ( VFSFileEntity targetFile, InputBuffer input ) throws FileshareException {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#storeContents(eu.agno3.fileshare.model.VFSFileEntity,
     *      eu.agno3.fileshare.service.InputBuffer)
     */
    @Override
    public VFSStoreHandle storeContents ( VFSFileEntity f, InputBuffer input ) throws FileshareException {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#findModifiedSince(eu.agno3.fileshare.model.VFSContainerEntity,
     *      java.lang.Long)
     */
    @SuppressWarnings ( "resource" )
    @Override
    public ClosableIterator<VFSChange> findModifiedSince ( VFSContainerEntity root, Long lastMod ) throws FileshareException {

        if ( lastMod == null ) {
            log.warn("Enumerating full share " + root); //$NON-NLS-1$
            return new ChangeIterator<>(new RecursiveVFSIterator<>(this, root), null);
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
        return lm == null;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#trackChange(eu.agno3.fileshare.model.VFSContainerEntity,
     *      eu.agno3.fileshare.model.VFSEntity, eu.agno3.fileshare.model.ChangeType)
     */
    @Override
    public void trackChange ( VFSContainerEntity e, VFSEntity child, ChangeType t ) throws UnsupportedOperationException {
        trackChange(e, child, t, DateTime.now());
    }


    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException
     *
     * @see eu.agno3.fileshare.vfs.VFSContext#trackChange(eu.agno3.fileshare.model.VFSContainerEntity,
     *      eu.agno3.fileshare.model.VFSEntity, eu.agno3.fileshare.model.ChangeType, org.joda.time.DateTime)
     */
    @Override
    public void trackChange ( VFSContainerEntity e, VFSEntity child, ChangeType t, DateTime now ) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}