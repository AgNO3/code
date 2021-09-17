/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.util.Collections;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.InsufficentStorageSpaceException;
import eu.agno3.fileshare.exceptions.QuotaExceededException;
import eu.agno3.fileshare.model.ContainerEntity;
import eu.agno3.fileshare.model.ContentEntity;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.FileEntity;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.service.QuotaService;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.QuotaReservation;
import eu.agno3.fileshare.service.api.internal.QuotaServiceInternal;
import eu.agno3.fileshare.service.api.internal.ScrollIterator;
import eu.agno3.fileshare.service.api.internal.VFSServiceInternal;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    QuotaServiceInternal.class, QuotaService.class
} )
public class QuotaServiceImpl implements QuotaServiceInternal {

    private static final Logger log = Logger.getLogger(QuotaServiceImpl.class);

    private static final int QUOTA_MAP_SIZE = 1024;

    private Map<EntityKey, Long> reservations = new ConcurrentHashMap<>();
    private Map<EntityKey, RuntimeQuotaEntry> runtime = Collections.synchronizedMap(new LRUMap<EntityKey, RuntimeQuotaEntry>(QUOTA_MAP_SIZE));
    private Queue<RuntimeQuotaEntry> workQueue = new ConcurrentLinkedQueue<>();

    private DefaultServiceContext ctx;
    private VFSServiceInternal vfs;

    private ScheduledExecutorService fullUpdateExecutor;
    private ScheduledExecutorService workQueueExecutor;

    private boolean exit;


    @Activate
    protected synchronized void activate ( ComponentContext cc ) {
        this.exit = false;
        this.fullUpdateExecutor = Executors.newSingleThreadScheduledExecutor();
        this.workQueueExecutor = Executors.newSingleThreadScheduledExecutor();
        long interval = this.ctx.getConfigurationProvider().getQuotaConfiguration().getDirectoryUpdateInterval().getStandardHours();
        long workQueueInterval = this.ctx.getConfigurationProvider().getQuotaConfiguration().getQuotaPersistenceInterval().getMillis();
        this.fullUpdateExecutor.scheduleAtFixedRate(new FullUpdateRunnable(), interval, interval, TimeUnit.HOURS);
        this.workQueueExecutor.scheduleAtFixedRate(new WorkQueueUpdateRunnable(), workQueueInterval, workQueueInterval, TimeUnit.MILLISECONDS);
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext cc ) {
        this.exit = true;
        if ( this.fullUpdateExecutor != null ) {
            this.fullUpdateExecutor.shutdown();
            try {
                this.fullUpdateExecutor.awaitTermination(10, TimeUnit.SECONDS);
            }
            catch ( InterruptedException e ) {
                log.warn("Interrupted while waiting for executor to finish", e); //$NON-NLS-1$
            }
            this.fullUpdateExecutor = null;
        }

        if ( this.workQueueExecutor != null ) {
            this.workQueueExecutor.shutdown();
            try {
                this.workQueueExecutor.awaitTermination(10, TimeUnit.SECONDS);
            }
            catch ( InterruptedException e ) {
                log.warn("Interrupted while waiting for executor to finish", e); //$NON-NLS-1$
            }
            this.workQueueExecutor = null;
        }

    }


    /**
     * @return the ctx
     */
    DefaultServiceContext getCtx () {
        return this.ctx;
    }


    /**
     * @return the exit
     */
    boolean isExit () {
        return this.exit;
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    /**
     * @return the vfs
     */
    VFSServiceInternal getVfs () {
        return this.vfs;
    }


    /**
     * @return the workQueue
     */
    public Queue<RuntimeQuotaEntry> getWorkQueue () {
        return this.workQueue;
    }


    @Reference
    protected synchronized void setServiceContext ( DefaultServiceContext sctx ) {
        this.ctx = sctx;
    }


    protected synchronized void unsetServiceContext ( DefaultServiceContext sctx ) {
        if ( this.ctx == sctx ) {
            this.ctx = null;
        }
    }


    @Reference
    protected synchronized void setVFSService ( VFSServiceInternal vs ) {
        this.vfs = vs;
    }


    protected synchronized void unsetVFSService ( VFSServiceInternal vs ) {
        if ( this.vfs == vs ) {
            this.vfs = null;
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.QuotaService#checkFreeSpace(eu.agno3.fileshare.model.VFSEntity, long, long)
     */
    @Override
    public void checkFreeSpace ( VFSEntity parent, long neededSize, long temporarySize ) throws FileshareException {
        EntityKey entityKey = parent.getEntityKey();

        if ( entityKey == null ) {
            throw new EntityNotFoundException();
        }

        try ( VFSContext v = this.vfs.getVFS(entityKey).begin(true) ) {

            VFSEntity persistent = v.load(entityKey);

            if ( persistent instanceof VFSFileEntity ) {
                persistent = v.getParent(persistent);
            }

            if ( persistent == null ) {
                throw new EntityNotFoundException();
            }

            v.checkFreeSpace(neededSize, temporarySize);

            Subject owner = persistent.getOwner();
            if ( owner.getQuota() == null ) {
                return;
            }

            Long reserved = this.reservations.get(owner.getId());
            long currentUsed = owner.getSubjectRoot().getChildrenSize();
            long newUsed = neededSize + ( reserved != null ? reserved : 0 ) + currentUsed;

            long exceed = newUsed - owner.getQuota();
            if ( exceed > 0 ) {
                log.trace("Quota exceeded"); //$NON-NLS-1$
                throw new QuotaExceededException(owner.getQuota(), exceed);
            }
        }

    }


    @Override
    public long getUsedSize ( VFSContainerEntity e ) throws FileshareException {
        try ( VFSContext v = this.vfs.getVFS(e.getEntityKey()).begin(true) ) {
            return getCurrentUsedSize(e);
        }
    }


    @Override
    public QuotaReservation checkAndReserve ( VFSContext v, VFSContainerEntity parent, long sizeDiff, long temporarySize )
            throws QuotaExceededException, InsufficentStorageSpaceException {

        v.checkFreeSpace(sizeDiff, temporarySize);

        Long quota = v.getQuota(parent);
        EntityKey id = v.getQuotaKey(parent);

        if ( quota == null || id == null ) {
            if ( this.ctx.getConfigurationProvider().getQuotaConfiguration().isTrackCombinedSizesWithoutQuota() ) {
                return new QuotaReservation(this, sizeDiff);
            }
            return new QuotaReservation();
        }

        long reserved = addReservation(id, sizeDiff);

        if ( log.isTraceEnabled() ) {
            log.trace("Currently reserved " + reserved); //$NON-NLS-1$
        }

        long newUsed = reserved + v.getUsedQuota(parent);

        long exceed = newUsed - quota;
        if ( exceed > 0 && sizeDiff > 0 ) {
            log.trace("Quota exceeded"); //$NON-NLS-1$
            removeReservation(id, sizeDiff);
            throw new QuotaExceededException(quota, exceed);
        }

        return new QuotaReservation(this, id, sizeDiff);
    }


    /**
     * @param id
     * @param sizeDiff
     */
    private void removeReservation ( EntityKey id, long sizeDiff ) {
        if ( id == null ) {
            return;
        }
        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Remove reservation of %d for %s", sizeDiff, id)); //$NON-NLS-1$
        }
        Long currentReservation = this.reservations.get(id);
        long newReservation;
        if ( currentReservation != null ) {
            newReservation = currentReservation - sizeDiff;
        }
        else {
            newReservation = -sizeDiff;
        }

        if ( newReservation == 0 ) {
            this.reservations.remove(id);
        }
        else {
            this.reservations.put(id, newReservation);
        }
    }


    /**
     * @param id
     * @param sizeDiff
     */
    private long addReservation ( EntityKey id, long sizeDiff ) {
        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Add reservation of %d for %s", sizeDiff, id)); //$NON-NLS-1$
        }
        Long currentReservation = this.reservations.get(id);
        long newReservation;
        if ( currentReservation != null ) {
            newReservation = currentReservation + sizeDiff;
        }
        else {
            newReservation = sizeDiff;
        }

        this.reservations.put(id, newReservation);
        return newReservation;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws FileshareException
     *
     * @see eu.agno3.fileshare.service.api.internal.QuotaServiceInternal#commit(eu.agno3.fileshare.vfs.VFSContext,
     *      eu.agno3.fileshare.model.VFSContainerEntity, long)
     */
    @Override
    public void commit ( VFSContext v, VFSContainerEntity parent, long sizeDiff ) throws FileshareException {
        Long quota = v.getQuota(parent);
        EntityKey id = v.getQuotaKey(parent);

        if ( !v.trackCollectionSizes()
                || ( quota == null && !this.ctx.getConfigurationProvider().getQuotaConfiguration().isTrackCombinedSizesWithoutQuota() ) ) {
            log.debug("NO update"); //$NON-NLS-1$
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Commiting %d on entity %s", sizeDiff, parent.getEntityKey())); //$NON-NLS-1$
        }

        // update sizes up to root
        VFSContainerEntity cur = parent;
        while ( cur != null ) {
            long curQuota = updateUsedSize(cur, sizeDiff);
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Setting container %s size to %d", cur.getEntityKey(), curQuota)); //$NON-NLS-1$
            }
            cur = v.getParent(cur);
        }

        if ( quota != null ) {
            long exceed = getCurrentUsedSize(parent.getOwner().getSubjectRoot()) - quota;
            if ( exceed > 0 && sizeDiff > 0 ) {
                while ( cur != null ) {
                    long curQuota = updateUsedSize(cur, -1 * sizeDiff);
                    if ( log.isDebugEnabled() ) {
                        log.debug(String.format("Setting container %s size to %d", cur.getEntityKey(), curQuota)); //$NON-NLS-1$
                    }
                    cur = v.getParent(cur);
                }

                throw new QuotaExceededException(quota, exceed, "Quota exceeded during update"); //$NON-NLS-1$
            }
        }

        if ( quota != null ) {
            this.removeReservation(id, sizeDiff);
        }

    }


    /**
     * @param cur
     * @param sizeDiff
     * @return
     */
    private long updateUsedSize ( VFSContainerEntity e, long sizeDiff ) {
        RuntimeQuotaEntry runtimeQuotaEntry;
        synchronized ( this.runtime ) {
            runtimeQuotaEntry = this.runtime.get(e.getEntityKey());
            if ( runtimeQuotaEntry == null ) {
                runtimeQuotaEntry = new RuntimeQuotaEntry(e.getEntityKey(), e.getChildrenSize());
                this.runtime.put(e.getEntityKey(), runtimeQuotaEntry);
            }
        }

        long cur = runtimeQuotaEntry.updateUsedSize(sizeDiff);
        if ( !this.exit && !this.workQueue.contains(runtimeQuotaEntry) ) {
            this.workQueue.add(runtimeQuotaEntry);
        }
        return cur;
    }


    /**
     * @param subjectRoot
     * @return
     */
    private long getCurrentUsedSize ( VFSContainerEntity e ) {
        RuntimeQuotaEntry runtimeQuotaEntry;
        synchronized ( this.runtime ) {
            runtimeQuotaEntry = this.runtime.get(e.getEntityKey());
            if ( runtimeQuotaEntry == null ) {
                return e.getChildrenSize();
            }
        }

        return runtimeQuotaEntry.getUsedSize();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.QuotaServiceInternal#undoReservation(eu.agno3.fileshare.model.EntityKey,
     *      long)
     */
    @Override
    public void undoReservation ( EntityKey subjectId, long sizeDiff ) {
        this.removeReservation(subjectId, sizeDiff);
    }


    void updateSubjectDirectorySizes ( UUID subjectId ) {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();
            Subject realSubject = em.find(Subject.class, subjectId);

            if ( realSubject.getQuota() == null && !this.ctx.getConfigurationProvider().getQuotaConfiguration().isTrackCombinedSizesWithoutQuota() ) {
                tx.commit();
                return;
            }

            updateDirectorySizes(em, realSubject);
            em.flush();
            tx.commit();
        }
        catch (
            PersistenceException |
            EntityTransactionException e ) {
            log.warn("Failed to update user directory sizes", e); //$NON-NLS-1$
        }
    }


    /**
     * @param em
     * @param realSubject
     */
    @Override
    public void updateDirectorySizes ( EntityManager em, Subject realSubject ) {
        long totalSize = updateDirectorySizeInternal(em, realSubject.getSubjectRoot());
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Total size %d for %s", totalSize, realSubject)); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws FileshareException
     *
     * @see eu.agno3.fileshare.service.api.internal.QuotaServiceInternal#getCombinedSize(eu.agno3.fileshare.vfs.VFSContext,
     *      eu.agno3.fileshare.model.VFSEntity)
     */
    @Override
    public long getCombinedSize ( VFSContext v, VFSEntity e ) throws FileshareException {
        if ( e instanceof VFSFileEntity ) {
            return ( (VFSFileEntity) e ).getFileSize();
        }
        else if ( e instanceof ContainerEntity ) {
            long combinedSize = 0;
            for ( VFSEntity child : v.getChildren((VFSContainerEntity) e) ) {
                combinedSize += getCombinedSize(v, child);
            }
            return combinedSize;
        }

        return 0;
    }


    /**
     * @param em
     * @param cur
     */
    private long updateDirectorySizeInternal ( EntityManager em, ContentEntity e ) {
        if ( e instanceof FileEntity ) {
            return ( (FileEntity) e ).getFileSize();
        }
        else if ( e instanceof ContainerEntity ) {
            long combinedSize = 0;
            for ( ContentEntity child : ( (ContainerEntity) e ).getElements() ) {
                combinedSize += updateDirectorySizeInternal(em, child);
            }

            ( (ContainerEntity) e ).setChildrenSize(combinedSize);
            em.persist(e);
            return combinedSize;
        }

        return 0;
    }

    private class WorkQueueUpdateRunnable implements Runnable {

        /**
         * 
         */
        public WorkQueueUpdateRunnable () {}


        /**
         * {@inheritDoc}
         *
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run () {
            RuntimeQuotaEntry e;
            while ( ( e = getWorkQueue().poll() ) != null ) {
                EntityKey entityId = e.getEntityId();
                try ( EntityTransactionContext tx = getCtx().getFileshareEntityTS().start();
                      VFSContext v = getVfs().getVFS(entityId).begin(tx) ) {
                    VFSContainerEntity entity = v.load(entityId, VFSContainerEntity.class);
                    if ( entity != null ) {
                        v.updateCollectionSize(entity, e.getUsedSize());
                        if ( getLog().isTraceEnabled() ) {
                            getLog().trace(String.format("Set used size %s to %d", entityId, e.getUsedSize())); //$NON-NLS-1$
                        }
                    }
                    tx.commit();
                }
                catch (
                    FileshareException |
                    EntityTransactionException ex ) {
                    getLog().warn("Failed to locate file in VFS", ex); //$NON-NLS-1$
                }

            }
        }
    }

    private class FullUpdateRunnable implements Runnable {

        /**
         * 
         */
        public FullUpdateRunnable () {

        }


        /**
         * {@inheritDoc}
         *
         * @see java.lang.Runnable#run()
         */
        @SuppressWarnings ( "resource" )
        @Override
        public void run () {
            getLog().debug("Running directory size updates"); //$NON-NLS-1$
            try ( EntityTransactionContext tx = getCtx().getFileshareEntityTS().startReadOnly() ) {
                Session session = tx.getEntityManager().unwrap(Session.class);
                Query<UUID> q;
                if ( getCtx().getConfigurationProvider().getQuotaConfiguration().isTrackCombinedSizesWithoutQuota() ) {
                    q = session.createQuery("SELECT s.id FROM Subject s"); //$NON-NLS-1$
                }
                else {
                    q = session.createQuery("SELECT s.id FROM Subject s WHERE NOT s.quota IS NULL"); //$NON-NLS-1$
                }
                q.setReadOnly(true);
                q.setCacheable(false);
                try ( ScrollIterator<UUID> iterator = new ScrollIterator<>(UUID.class, q, tx) ) {
                    while ( iterator.hasNext() ) {
                        UUID subjectId = iterator.next();
                        if ( getLog().isTraceEnabled() ) {
                            getLog().trace("Updating directory sizes for " + subjectId); //$NON-NLS-1$
                        }

                        if ( isExit() ) {
                            return;
                        }
                        updateSubjectDirectorySizes(subjectId);
                    }
                }
                finally {
                    // make sure the entity manager cache is cleared
                    // this used to be close, but that might be dangerous
                    tx.getEntityManager().clear();
                }
            }
            catch ( EntityTransactionException e ) {
                getLog().warn("Failed to update directory sizes", e); //$NON-NLS-1$
            }
        }

    }
}
