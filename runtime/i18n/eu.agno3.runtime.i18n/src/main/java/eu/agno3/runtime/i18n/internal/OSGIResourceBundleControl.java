/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.10.2013 by mbechler
 */
package eu.agno3.runtime.i18n.internal;


import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;


/**
 * @author mbechler
 * 
 */
@Component ( service = ResourceBundle.Control.class )
public class OSGIResourceBundleControl extends ResourceBundle.Control implements ServiceTrackerCustomizer<ResourceBundle, Object> {

    /**
     * 
     */
    private static final String DEFAULT_LOCALE = "default"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(OSGIResourceBundleControl.class);

    private Map<String, Map<Locale, ResourceBundle>> resourceBundles = new HashMap<>();
    private BundleContext bundleContext;
    private ServiceTracker<ResourceBundle, Object> tracker;


    @Activate
    protected void activate ( ComponentContext context ) {
        this.bundleContext = context.getBundleContext();
        this.tracker = new ServiceTracker<>(this.bundleContext, ResourceBundle.class, this);
        this.tracker.open();
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        if ( this.tracker != null ) {
            this.tracker.close();
            this.tracker = null;
        }
        this.bundleContext = null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public Object addingService ( ServiceReference<ResourceBundle> reference ) {
        String baseName = (String) reference.getProperty(ResourceBundleTracker.BASE_NAME_PROPERTY);
        String locale = (String) reference.getProperty(ResourceBundleTracker.LOCALE_PROPERTY);

        if ( baseName == null || locale == null ) {
            log.error("Illegal ResourceBundle registration (baseName or locale empty)"); //$NON-NLS-1$
            return null;
        }

        Locale l = getLocale(locale);

        if ( l == null ) {
            log.error("Illegal ResourceBundle registration (cannot resolve locale)"); //$NON-NLS-1$
            return null;
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Bind resource bundle %s for locale %s", baseName, l)); //$NON-NLS-1$
        }

        bindResourceBundle(reference, baseName, l);

        return null;
    }


    /**
     * @param reference
     * @param baseName
     * @param l
     */
    private void bindResourceBundle ( ServiceReference<ResourceBundle> reference, String baseName, Locale l ) {
        if ( !this.resourceBundles.containsKey(baseName) ) {
            this.resourceBundles.put(baseName, new HashMap<Locale, ResourceBundle>());
        }

        this.resourceBundles.get(baseName).put(l, this.bundleContext.getService(reference));
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService ( ServiceReference<ResourceBundle> reference, Object service ) {}


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<ResourceBundle> reference, Object service ) {
        String baseName = (String) reference.getProperty(ResourceBundleTracker.BASE_NAME_PROPERTY);
        String locale = (String) reference.getProperty(ResourceBundleTracker.LOCALE_PROPERTY);

        if ( baseName == null || locale == null ) {
            return;
        }

        Locale l = getLocale(locale);

        if ( l == null ) {
            return;
        }

        unbindResourceBundle(baseName, l);
    }


    /**
     * @param baseName
     * @param l
     */
    private void unbindResourceBundle ( String baseName, Locale l ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Unbind resource bundle %s for locale %s", baseName, l)); //$NON-NLS-1$
        }

        if ( this.resourceBundles.containsKey(baseName) ) {
            if ( this.resourceBundles.get(baseName).containsKey(l) ) {
                this.resourceBundles.get(baseName).remove(l);
            }

            if ( this.resourceBundles.get(baseName).isEmpty() ) {
                this.resourceBundles.remove(baseName);
            }
        }
    }


    /**
     * @param locale
     * @return
     */
    private static Locale getLocale ( String locale ) {
        Locale l = null;
        if ( DEFAULT_LOCALE.equals(locale) ) {
            l = Locale.ROOT;
        }
        else {
            l = Locale.forLanguageTag(locale);
        }
        return l;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.util.ResourceBundle.Control#newBundle(java.lang.String, java.util.Locale, java.lang.String,
     *      java.lang.ClassLoader, boolean)
     */
    @Override
    public ResourceBundle newBundle ( String baseName, Locale locale, String format, ClassLoader loader, boolean reload )
            throws IllegalAccessException, InstantiationException, IOException {

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("newBundle(%s,%s)", baseName, locale.toString())); //$NON-NLS-1$
        }

        if ( this.resourceBundles.containsKey(baseName) && this.resourceBundles.get(baseName).containsKey(locale) ) {
            return this.resourceBundles.get(baseName).get(locale);
        }

        return super.newBundle(baseName, locale, format, loader, reload);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.util.ResourceBundle.Control#getTimeToLive(java.lang.String, java.util.Locale)
     */
    @Override
    public long getTimeToLive ( String baseName, Locale locale ) {
        return TTL_DONT_CACHE;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.util.ResourceBundle.Control#needsReload(java.lang.String, java.util.Locale, java.lang.String,
     *      java.lang.ClassLoader, java.util.ResourceBundle, long)
     */
    @Override
    public boolean needsReload ( String baseName, Locale locale, String format, ClassLoader loader, ResourceBundle bundle, long loadTime ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("needsReload(%s,%s)", baseName, locale.toString())); //$NON-NLS-1$
        }
        return super.needsReload(baseName, locale, format, loader, bundle, loadTime);
    }

}
