/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.08.2014 by mbechler
 */
package eu.agno3.runtime.logging.internal;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import eu.agno3.runtime.logging.Appender;


/**
 * @author mbechler
 * 
 */
public class AppenderBridge implements PaxAppender, ServiceTrackerCustomizer<Appender, Appender> {

    private final BundleContext ctx;
    private final ReadWriteLock appenderLock = new ReentrantReadWriteLock();
    private final Set<Appender> appenders = Collections.synchronizedSet(new HashSet<Appender>());
    private ServiceTracker<Appender, Appender> tracker;
    private ColoringConsoleAppender fallback;

    private static final Set<String> DROP = new HashSet<>(Arrays.asList("[SCR] ComponentReference.unbind(): component instance not bound!")); //$NON-NLS-1$


    /**
     * @param ctx
     * @param fallback
     * 
     */
    public AppenderBridge ( BundleContext ctx, ColoringConsoleAppender fallback ) {
        this.ctx = ctx;
        this.fallback = fallback;
        this.tracker = new ServiceTracker<>(this.ctx, Appender.class, this);
    }


    /**
     * 
     */
    public void start () {
        this.tracker.open();
    }


    /**
     * 
     */
    public void stop () {
        this.tracker.close();
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public Appender addingService ( ServiceReference<Appender> reference ) {
        Appender ap = this.ctx.getService(reference);
        this.appenderLock.writeLock().lock();
        try {
            this.appenders.add(ap);
            return ap;
        }
        finally {
            this.appenderLock.writeLock().unlock();
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService ( ServiceReference<Appender> reference, Appender service ) {
        // nothing
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<Appender> reference, Appender service ) {
        this.appenderLock.writeLock().lock();
        try {
            this.appenders.remove(service);
        }
        finally {
            this.appenderLock.writeLock().unlock();
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.ops4j.pax.logging.spi.PaxAppender#doAppend(org.ops4j.pax.logging.spi.PaxLoggingEvent)
     */
    @Override
    public void doAppend ( PaxLoggingEvent ev ) {
        for ( String dropPrefix : DROP ) {
            if ( ev.getMessage().startsWith(dropPrefix) ) {
                return;
            }
        }

        this.appenderLock.readLock().lock();

        if ( this.appenders.isEmpty() ) {
            this.fallback.doAppend(ev);
        }

        for ( Appender ap : this.appenders ) {
            try {
                ap.doAppend(ev);
            }
            catch ( Exception e ) {
                // cannot log, cannot throw
            }
        }
        this.appenderLock.readLock().unlock();
    }
}
