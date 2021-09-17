/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.07.2013 by mbechler
 */
package eu.agno3.runtime.logging.config.internal;


import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import eu.agno3.runtime.logging.config.LoggerConfigurationException;


/**
 * @author mbechler
 * 
 */
public class BundleConfigurationTracker implements BundleTrackerCustomizer<BundleConfigurationSource> {

    private DelegatingLoggerConfigurationSource delegator;

    /**
     * Path to bundle logging configuration file
     */
    public static final String LOGGING_CONF_PATH = "/logging.properties"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(BundleConfigurationTracker.class);


    /**
     * @param delegator
     */
    public BundleConfigurationTracker ( DelegatingLoggerConfigurationSource delegator ) {
        this.delegator = delegator;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.BundleTrackerCustomizer#addingBundle(org.osgi.framework.Bundle,
     *      org.osgi.framework.BundleEvent)
     */
    @Override
    public BundleConfigurationSource addingBundle ( Bundle bundle, BundleEvent event ) {

        try {
            BundleConfigurationSource source = new BundleConfigurationSource(bundle, LOGGING_CONF_PATH, 0);
            if ( log.isDebugEnabled() ) {
                log.debug("Adding logger configuration from bundle " + bundle.getSymbolicName()); //$NON-NLS-1$
            }
            this.delegator.addSource(source);
            return source;
        }
        catch ( LoggerConfigurationException e ) {
            log.trace("Failed to add logger configuration:", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.BundleTrackerCustomizer#modifiedBundle(org.osgi.framework.Bundle,
     *      org.osgi.framework.BundleEvent, java.lang.Object)
     */
    @Override
    public void modifiedBundle ( Bundle bundle, BundleEvent event, BundleConfigurationSource object ) {}


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.BundleTrackerCustomizer#removedBundle(org.osgi.framework.Bundle,
     *      org.osgi.framework.BundleEvent, java.lang.Object)
     */
    @Override
    public void removedBundle ( Bundle bundle, BundleEvent event, BundleConfigurationSource object ) {

        if ( object != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Removing logger configuration by bundle " + bundle.getSymbolicName()); //$NON-NLS-1$
            }
            this.delegator.removeSource(object);
        }

    }

}
