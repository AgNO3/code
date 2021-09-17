/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.09.2016 by mbechler
 */
package eu.agno3.runtime.eventlog.internal;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.util.threads.NamedThreadFactory;


/**
 * @author mbechler
 *
 */
@Component ( service = EventLoggerMaintenanceJob.class, immediate = true )
public class EventLoggerMaintenanceJob implements Runnable {

    private static final Logger log = Logger.getLogger(EventLoggerMaintenanceJob.class);

    private ScheduledExecutorService executor;
    private EventLoggerImpl logger;


    @Reference
    protected synchronized void setEventLogger ( EventLoggerImpl el ) {
        this.logger = el;
    }


    protected synchronized void unsetEventLogger ( EventLoggerImpl el ) {
        if ( this.logger == el ) {
            this.logger = null;
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("EventLoggerMaintenance")); //$NON-NLS-1$
        this.executor.schedule(this, 1, TimeUnit.HOURS);
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) throws InterruptedException {
        ScheduledExecutorService exec = this.executor;
        this.executor = null;
        exec.shutdown();
        exec.awaitTermination(5, TimeUnit.SECONDS);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run () {
        try {
            synchronized ( this ) {
                log.debug("Running maintenance"); //$NON-NLS-1$
                EventLoggerImpl l = this.logger;
                if ( l != null ) {
                    l.runMaintenance();
                }
            }
        }
        catch ( Exception e ) {
            log.warn("Failed to run log maintenance", e); //$NON-NLS-1$
        }
    }

}
