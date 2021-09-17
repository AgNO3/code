/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.10.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.VFSServiceInternal;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.runtime.db.orm.EntityTransactionContext;


/**
 * @author mbechler
 *
 */
@Component ( service = RecursiveModificationTimeUpdateRunner.class )
public class RecursiveModificationTimeUpdateRunner implements Runnable, RecursiveModificationListener {

    private static final Logger log = Logger.getLogger(RecursiveModificationTimeUpdateRunner.class);

    private Queue<RuntimeModificationEntry> workQueue = new ConcurrentLinkedQueue<>();
    private long workQueueInterval = 10000;

    private ScheduledExecutorService workQueueExecutor;
    private DefaultServiceContext ctx;
    private VFSServiceInternal vfs;


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


    @Activate
    protected synchronized void activate ( ComponentContext cc ) {
        this.workQueueExecutor = Executors.newSingleThreadScheduledExecutor();
        this.workQueueExecutor.scheduleAtFixedRate(this, this.workQueueInterval, this.workQueueInterval, TimeUnit.MILLISECONDS);
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext cc ) {
        if ( this.workQueueExecutor != null ) {
            this.workQueueExecutor.shutdown();
            try {
                this.workQueueExecutor.awaitTermination(30, TimeUnit.SECONDS);
            }
            catch ( InterruptedException e ) {
                log.warn("Interrupted while waiting for executor to finish", e); //$NON-NLS-1$
            }
            this.workQueueExecutor = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run () {
        RuntimeModificationEntry e;
        while ( ( e = this.workQueue.poll() ) != null ) {
            EntityKey entityId = e.getEntityId();
            if ( log.isTraceEnabled() ) {
                log.trace("Found entity in queue " + entityId); //$NON-NLS-1$
            }
            try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start();
                  VFSContext v = this.vfs.getVFS(entityId).begin(tx) ) {
                VFSContainerEntity entity = v.load(entityId, VFSContainerEntity.class);
                if ( entity != null ) {
                    v.updateRecursiveLastModifiedTime(entity, new DateTime(e.getLastModification()));
                    log.trace(String.format("Set last modification time of %s to %s", entityId, e.getLastModification())); //$NON-NLS-1$
                }
                else if ( log.isDebugEnabled() ) {
                    log.debug("Could not find entity " + entityId); //$NON-NLS-1$
                }
                tx.commit();
            }
            catch ( Exception ex ) {
                log.warn("Failed to update last modification time", ex); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param modEntry
     */
    public void submit ( RuntimeModificationEntry modEntry ) {
        synchronized ( this.workQueue ) {
            if ( this.workQueue.contains(modEntry) ) {
                this.workQueue.add(modEntry);
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.internal.RecursiveModificationListener#notifyChange(eu.agno3.fileshare.service.internal.RuntimeModificationEntry)
     */
    @Override
    public void notifyChange ( RuntimeModificationEntry modEntry ) {
        submit(modEntry);
    }
}