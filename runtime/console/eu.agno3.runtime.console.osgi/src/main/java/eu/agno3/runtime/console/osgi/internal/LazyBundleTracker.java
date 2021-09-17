/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2013 by mbechler
 */
package eu.agno3.runtime.console.osgi.internal;


import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;


/**
 * @author mbechler
 * 
 */
public class LazyBundleTracker implements SynchronousBundleListener {

    private final Set<Bundle> lazyBundles = new HashSet<>();


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.framework.BundleListener#bundleChanged(org.osgi.framework.BundleEvent)
     */
    @Override
    public void bundleChanged ( BundleEvent event ) {

        int eventType = event.getType();
        Bundle b = event.getBundle();

        synchronized ( this.lazyBundles ) {
            if ( eventType == BundleEvent.LAZY_ACTIVATION ) {
                this.lazyBundles.add(b);
            }
            else {
                this.lazyBundles.remove(b);
            }
        }
    }


    protected void init ( BundleContext ctx ) {
        synchronized ( this.lazyBundles ) {
            for ( Bundle b : ctx.getBundles() ) {
                if ( b.getState() == Bundle.STARTING && !b.equals(ctx.getBundle()) ) {
                    this.lazyBundles.add(b);
                }
            }
        }
    }


    /**
     * Checks whether the given bundle uses lazy activation
     * 
     * @param b
     * @return whether b uses lazy activation
     */
    public boolean isLazyBundle ( Bundle b ) {
        return this.lazyBundles.contains(b);
    }

}
