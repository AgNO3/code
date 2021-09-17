/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.07.2013 by mbechler
 */
package eu.agno3.runtime.configloader.internal;


import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import eu.agno3.runtime.configloader.ConfigContribution;
import eu.agno3.runtime.configloader.contribs.BundleConfigContribution;


/**
 * @author mbechler
 * 
 */
class BundleConfigurationTracker implements BundleTrackerCustomizer<ConfigContribution> {

    private Map<Bundle, ConfigContribution> configRegistrations = new HashMap<>();

    private ConfigLoaderImpl loader;


    protected BundleConfigurationTracker ( ConfigLoaderImpl loader ) {
        this.loader = loader;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.BundleTrackerCustomizer#addingBundle(org.osgi.framework.Bundle,
     *      org.osgi.framework.BundleEvent)
     */
    @Override
    public ConfigContribution addingBundle ( Bundle bundle, BundleEvent event ) {

        BundleRevision bundleRevision = bundle.adapt(BundleRevision.class);
        if ( bundleRevision == null || ( bundleRevision.getTypes() & BundleRevision.TYPE_FRAGMENT ) != 0 ) {
            return null;
        }

        if ( bundleIsStarting(bundle, event) ) {
            ConfigContribution contrib = new BundleConfigContribution(bundle);
            this.configRegistrations.put(bundle, contrib);
            this.loader.addConfigSource(contrib);
        }
        else if ( event != null && event.getType() == BundleEvent.STOPPED && this.configRegistrations.containsKey(bundle) ) {
            this.loader.removeConfigSource(this.configRegistrations.remove(bundle));
        }

        return null;
    }


    /**
     * @param bundle
     * @param event
     * @return
     */
    private static boolean bundleIsStarting ( Bundle bundle, BundleEvent event ) {
        return ( event == null && bundle.getState() == Bundle.ACTIVE ) || ( event != null && event.getType() == BundleEvent.STARTED );
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.BundleTrackerCustomizer#modifiedBundle(org.osgi.framework.Bundle,
     *      org.osgi.framework.BundleEvent, java.lang.Object)
     */
    @Override
    public void modifiedBundle ( Bundle bundle, BundleEvent event, ConfigContribution object ) {
        // ununsed
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.BundleTrackerCustomizer#removedBundle(org.osgi.framework.Bundle,
     *      org.osgi.framework.BundleEvent, java.lang.Object)
     */
    @Override
    public void removedBundle ( Bundle bundle, BundleEvent event, ConfigContribution object ) {
        if ( this.configRegistrations.containsKey(bundle) ) {
            this.loader.removeConfigSource(this.configRegistrations.remove(bundle));
        }
    }

}
