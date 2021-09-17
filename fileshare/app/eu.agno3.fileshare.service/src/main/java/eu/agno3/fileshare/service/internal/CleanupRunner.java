/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.NotificationService;
import eu.agno3.fileshare.service.api.internal.SingleUseTokenService;
import eu.agno3.runtime.db.orm.EntityTransactionContext;


/**
 * @author mbechler
 *
 */
@Component ( service = CleanupRunner.class, immediate = true )
public class CleanupRunner implements Runnable {

    private static final Logger log = Logger.getLogger(CleanupRunner.class);

    private DefaultServiceContext sctx;
    private NotificationService notifyService;
    private SingleUseTokenService tokenService;
    private ScheduledExecutorService executor;


    @Reference
    protected synchronized void setServiceContext ( DefaultServiceContext ctx ) {
        this.sctx = ctx;
    }


    protected synchronized void unsetServiceContext ( DefaultServiceContext ctx ) {
        if ( this.sctx == ctx ) {
            this.sctx = null;
        }
    }


    @Reference
    protected synchronized void setNotificationService ( NotificationService ns ) {
        this.notifyService = ns;
    }


    protected synchronized void unsetNotificationService ( NotificationService ns ) {
        if ( this.notifyService == ns ) {
            this.notifyService = null;
        }
    }


    @Reference
    protected synchronized void setTokenTracker ( SingleUseTokenService ts ) {
        this.tokenService = ts;
    }


    protected synchronized void unsetTokenTracker ( SingleUseTokenService ts ) {
        if ( this.tokenService == ts ) {
            this.tokenService = null;
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.executor.scheduleAtFixedRate(this, 2, 60, TimeUnit.MINUTES);
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        if ( this.executor != null ) {
            this.executor.shutdown();
            try {
                this.executor.awaitTermination(10, TimeUnit.SECONDS);
            }
            catch ( InterruptedException e ) {
                log.warn("Interrupted while waiting for executor to finish", e); //$NON-NLS-1$
            }
            this.executor = null;
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run () {
        log.debug("Running cleanups"); //$NON-NLS-1$
        try ( EntityTransactionContext tx = this.sctx.getFileshareEntityTS().start() ) {
            this.notifyService.cleanupNotificationTrackers(tx);
            this.tokenService.cleanup(tx);
            tx.commit();
        }
        catch ( Exception e ) {
            log.warn("Failure during cleanup", e); //$NON-NLS-1$
        }
    }
}
