/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.12.2014 by mbechler
 */
package eu.agno3.runtime.update.internal;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.framework.wiring.FrameworkWiring;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.update.BundleUpdater;
import eu.agno3.runtime.update.RefreshListener;


/**
 * @author mbechler
 *
 */
@Component ( service = BundleUpdater.class )
public class BundleUpdaterImpl implements BundleUpdater, FrameworkListener {

    private static final Logger log = Logger.getLogger(BundleUpdaterImpl.class);

    private static final long REFRESH_TIMEOUT = 60 * 1000;
    private final Object refreshedPackageSem = new Object();
    private Boolean packagesRefreshed = false;
    private BundleContext bundleContext;
    private Set<RefreshListener> refreshListeners = new HashSet<>();

    private Object updateLock = new Object();


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.bundleContext = ctx.getBundleContext();
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        this.bundleContext = null;
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindRefreshListener ( RefreshListener l ) {
        synchronized ( this.refreshListeners ) {
            this.refreshListeners.add(l);
        }
    }


    protected synchronized void unbindRefreshListener ( RefreshListener l ) {
        synchronized ( this.refreshListeners ) {
            this.refreshListeners.remove(l);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.framework.FrameworkListener#frameworkEvent(org.osgi.framework.FrameworkEvent)
     */
    @Override
    public void frameworkEvent ( FrameworkEvent ev ) {
        if ( ev.getType() == FrameworkEvent.PACKAGES_REFRESHED ) {
            synchronized ( this.refreshedPackageSem ) {
                this.packagesRefreshed = true;
                this.refreshedPackageSem.notifyAll();
            }
        }
    }


    @Override
    public void updateBundles ( Collection<Bundle> bundles ) throws BundleException {
        synchronized ( this.updateLock ) {
            synchronized ( this.refreshListeners ) {
                for ( RefreshListener l : this.refreshListeners ) {
                    l.startBundleUpdate();
                }
            }

            if ( bundles.contains(this.bundleContext.getBundle()) ) {
                throw new BundleException("Cannot update updater bundle, restart needed"); //$NON-NLS-1$
            }

            Bundle systemBundle = this.bundleContext.getBundle(0);
            FrameworkWiring wiring = systemBundle.adapt(FrameworkWiring.class);
            List<Bundle> dependencyClosure = filterNonStarted(wiring.getDependencyClosure(bundles));
            Collections.sort(dependencyClosure, new StartLevelComparator());
            dumpOrder(dependencyClosure);
            stopAll(dependencyClosure);
            updateBundlesInternal(bundles);
            refreshSynchronous(bundles);
            Collections.reverse(dependencyClosure);
            dumpOrder(dependencyClosure);

            synchronized ( this.refreshListeners ) {
                for ( RefreshListener l : this.refreshListeners ) {
                    try {
                        l.bundlesUpdated();
                    }
                    catch ( Exception e ) {
                        throw new BundleException("Exception in update listener " + l.getClass().getName(), e); //$NON-NLS-1$
                    }
                }
            }
            startAll(dependencyClosure);

            synchronized ( this.refreshListeners ) {
                for ( RefreshListener l : this.refreshListeners ) {
                    try {
                        l.bundlesStarted();
                    }
                    catch ( Exception e ) {
                        throw new BundleException("Exception in update listener " + l.getClass().getName(), e); //$NON-NLS-1$
                    }
                }
            }

            log.debug("Running GC and finalizers"); //$NON-NLS-1$
            System.gc();
            System.runFinalization();
        }
    }


    /**
     * @param dependencyClosure
     * @return
     */
    private static List<Bundle> filterNonStarted ( Collection<Bundle> dependencyClosure ) {
        List<Bundle> started = new ArrayList<>();
        for ( Bundle b : dependencyClosure ) {
            if ( b.getState() == Bundle.ACTIVE ) {
                started.add(b);
            }
        }
        return started;
    }


    /**
     * @param dependencyClosure
     */
    private static void dumpOrder ( List<Bundle> dependencyClosure ) {
        if ( log.isDebugEnabled() ) {
            int i = 0;
            for ( Bundle b : dependencyClosure ) {
                BundleStartLevel startLevel = b.adapt(BundleStartLevel.class);
                log.debug(String.format("%d. %s (%s)", i, b.getSymbolicName(), startLevel != null ? startLevel.getStartLevel() : null)); //$NON-NLS-1$
                i++;
            }
        }
    }


    /**
     * @param bundles
     * @throws BundleException
     */
    protected void stopAll ( Collection<Bundle> bundles ) throws BundleException {
        for ( Bundle b : bundles ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Stopping " + b.getSymbolicName()); //$NON-NLS-1$
            }
            if ( b.getState() == Bundle.ACTIVE ) {
                b.stop();
                waitForState(b, Bundle.RESOLVED);
            }
            else if ( b.getState() == Bundle.RESOLVED || b.getState() == Bundle.INSTALLED ) {
                log.debug("Not stopping bundle in RESOLVED|INSTALLED state"); //$NON-NLS-1$
            }
            else {
                throw new BundleException("Illegal state for stop " + b.getState()); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param b
     * @param resolved
     * @throws BundleException
     */
    private static void waitForState ( Bundle b, int state ) throws BundleException {
        int timeout = 30 * 1000;
        while ( timeout > 0 ) {

            if ( b.getState() == state ) {
                return;
            }

            try {
                Thread.sleep(1000);
            }
            catch ( InterruptedException e ) {
                throw new BundleException("Interrupted while waiting for bundle state", e); //$NON-NLS-1$
            }
            timeout -= 1000;
        }

        throw new BundleException("Timeout waiting for state " + state); //$NON-NLS-1$
    }


    /**
     * @param bundles
     * @throws BundleException
     */
    protected void startAll ( Collection<Bundle> bundles ) throws BundleException {
        for ( Bundle b : bundles ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Starting " + b.getSymbolicName()); //$NON-NLS-1$
            }
            if ( b.getState() != Bundle.ACTIVE ) {
                b.start();
                waitForState(b, Bundle.ACTIVE);
            }
            else {
                throw new BundleException("Illegal state for start " + b.getState()); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param bundles
     * @throws BundleException
     */
    protected void updateBundlesInternal ( Collection<Bundle> bundles ) throws BundleException {
        for ( Bundle b : bundles ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Updating " + b.getSymbolicName()); //$NON-NLS-1$
            }
            b.update();
        }
    }


    protected void refreshSynchronous ( Collection<Bundle> bundles ) throws BundleException {
        Bundle systemBundle = this.bundleContext.getBundle(0);
        FrameworkWiring wiring = systemBundle.adapt(FrameworkWiring.class);
        log.debug("Refreshing bundles"); //$NON-NLS-1$
        synchronized ( this.refreshedPackageSem ) {
            this.packagesRefreshed = false;
            wiring.refreshBundles(bundles, this);
            try {
                this.refreshedPackageSem.wait(REFRESH_TIMEOUT);
            }
            catch ( InterruptedException e ) {
                // silent
            }
            if ( !this.packagesRefreshed ) {
                throw new BundleException("Failed to refresh bundles within timeout"); //$NON-NLS-1$
            }
            this.packagesRefreshed = false;

            synchronized ( this.refreshListeners ) {
                for ( RefreshListener l : this.refreshListeners ) {
                    try {
                        l.bundlesRefreshed();
                    }
                    catch ( Exception e ) {
                        throw new BundleException("Exception in refresh listener", e); //$NON-NLS-1$
                    }
                }
            }
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Bundles refreshed " + bundles); //$NON-NLS-1$
        }
    }
}
