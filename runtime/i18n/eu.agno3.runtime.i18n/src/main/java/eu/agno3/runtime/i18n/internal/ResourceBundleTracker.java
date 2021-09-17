/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.10.2013 by mbechler
 */
package eu.agno3.runtime.i18n.internal;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import eu.agno3.runtime.util.osgi.DsUtil;
import eu.agno3.runtime.util.osgi.ResourceUtil;


/**
 * @author mbechler
 * 
 */
@Component ( immediate = true )
public class ResourceBundleTracker implements BundleTrackerCustomizer<Bundle> {

    /**
     * Resource bundle basename service property
     */
    public static final String BASE_NAME_PROPERTY = "baseName"; //$NON-NLS-1$

    /**
     * Resource bundle locale service property
     */
    public static final String LOCALE_PROPERTY = "locale"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(ResourceBundleTracker.class);

    private static final String I18N_PREFIX = "/i18n/"; //$NON-NLS-1$
    private static final String I18N_PATTERN = "*.properties"; //$NON-NLS-1$

    private Map<Bundle, Set<ServiceRegistration<ResourceBundle>>> registrationMap = new ConcurrentHashMap<>();

    private ComponentContext componentContext;

    private BundleTracker<Bundle> bundleTracker;


    @Activate
    protected void activate ( ComponentContext context ) {
        log.debug("Starting resource bundle tracker"); //$NON-NLS-1$
        this.componentContext = context;
        this.bundleTracker = new BundleTracker<>(context.getBundleContext(), Bundle.ACTIVE, this);
        this.bundleTracker.open();

    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        log.info("Stopping resource bundle tracker"); //$NON-NLS-1$
        this.bundleTracker.close();
        this.componentContext = null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.BundleTrackerCustomizer#addingBundle(org.osgi.framework.Bundle,
     *      org.osgi.framework.BundleEvent)
     */
    @Override
    public Bundle addingBundle ( Bundle bundle, BundleEvent event ) {
        if ( ( event == null || event.getType() == BundleEvent.STARTED ) && !this.registrationMap.containsKey(bundle) ) {
            return makeBundleRegistration(bundle);
        }
        else if ( event != null && event.getType() == BundleEvent.STOPPING ) {
            this.removeBundle(bundle);
        }

        return null;
    }


    /**
     * @param bundle
     * @param bundles
     * @return
     */
    private Bundle makeBundleRegistration ( Bundle bundle ) {
        List<URL> bundles = ResourceUtil.safeFindPattern(bundle, I18N_PREFIX, I18N_PATTERN, true);

        if ( bundles.isEmpty() ) {
            return null;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Bundle contains resource bundles " + bundle.getSymbolicName()); //$NON-NLS-1$
        }

        Set<ServiceRegistration<ResourceBundle>> bundleRegistrations = new HashSet<>();
        this.addBundle(bundles, bundleRegistrations);
        this.registrationMap.put(bundle, bundleRegistrations);

        return bundle;
    }


    /**
     * @param bundles
     * @param bundleRegistrations
     */
    private void addBundle ( List<URL> bundles, Set<ServiceRegistration<ResourceBundle>> bundleRegistrations ) {
        for ( URL bundleUrl : bundles ) {
            try {
                PropertyResourceBundle resourceBundle = new PropertyResourceBundle(bundleUrl.openStream());

                File f = new File(bundleUrl.getFile());
                String baseName = f.getParent().substring(I18N_PREFIX.length());
                String locale = f.getName().substring(0, f.getName().length() - ".properties".length()); //$NON-NLS-1$

                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("Found basename %s in locale %s", baseName, locale)); //$NON-NLS-1$
                }

                Dictionary<String, String> resourceBundleProperties = new Hashtable<>();
                resourceBundleProperties.put(BASE_NAME_PROPERTY, baseName);
                resourceBundleProperties.put(LOCALE_PROPERTY, locale);

                bundleRegistrations.add(DsUtil.registerSafe(this.componentContext, ResourceBundle.class, resourceBundle, resourceBundleProperties));
            }
            catch ( IOException e ) {
                log.error("Failed to open resource bundle " + bundleUrl, e); //$NON-NLS-1$
            }
        }

    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.BundleTrackerCustomizer#modifiedBundle(org.osgi.framework.Bundle,
     *      org.osgi.framework.BundleEvent, java.lang.Object)
     */
    @Override
    public void modifiedBundle ( Bundle bundle, BundleEvent event, Bundle object ) {}


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.BundleTrackerCustomizer#removedBundle(org.osgi.framework.Bundle,
     *      org.osgi.framework.BundleEvent, java.lang.Object)
     */
    @Override
    public void removedBundle ( Bundle bundle, BundleEvent event, Bundle object ) {
        this.removeBundle(bundle);
    }


    /**
     * @param bundle
     */
    private synchronized void removeBundle ( Bundle bundle ) {
        if ( this.registrationMap.containsKey(bundle) ) {

            if ( log.isDebugEnabled() ) {
                log.debug("Remove registrations of bundle " + bundle.getSymbolicName()); //$NON-NLS-1$
            }

            for ( ServiceRegistration<ResourceBundle> registration : this.registrationMap.get(bundle) ) {

                if ( log.isDebugEnabled() ) {
                    log.debug(String.format(
                        "Remove registration for %s locale %s", //$NON-NLS-1$
                        registration.getReference().getProperty(BASE_NAME_PROPERTY),
                        registration.getReference().getProperty(LOCALE_PROPERTY)));
                }

                DsUtil.unregisterSafe(this.componentContext, registration);
            }

            this.registrationMap.remove(bundle);
        }
    }

}
