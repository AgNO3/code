/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.11.2013 by mbechler
 */
package eu.agno3.runtime.http.service.webapp.internal;


import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import eu.agno3.runtime.http.service.webapp.WebAppConfiguration;
import eu.agno3.runtime.ldap.filter.FilterBuilder;


/**
 * @author mbechler
 * 
 */
@Component ( immediate = true )
public class WebappTracker implements ServiceTrackerCustomizer<WebAppConfiguration, Configuration> {

    private static final Logger log = Logger.getLogger(WebappTracker.class);

    private ConfigurationAdmin cm;
    private BundleContext bundleContext;

    private ServiceTracker<WebAppConfiguration, Configuration> serviceTracker;


    @Activate
    protected void activate ( ComponentContext context ) {
        this.bundleContext = context.getBundleContext();
        this.serviceTracker = new ServiceTracker<>(this.bundleContext, WebAppConfiguration.class, this);
        this.serviceTracker.open();
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        if ( this.serviceTracker != null ) {
            this.serviceTracker.close();
            this.serviceTracker = null;
        }

        this.bundleContext = null;
    }


    @Reference
    protected synchronized void setConfigurationAdmin ( ConfigurationAdmin cf ) {
        this.cm = cf;
    }


    protected synchronized void unsetConfigurationAdmin ( ConfigurationAdmin cf ) {
        if ( this.cm == cf ) {
            this.cm = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public Configuration addingService ( ServiceReference<WebAppConfiguration> ref ) {
        WebAppConfiguration cfg = this.bundleContext.getService(ref);
        try {
            Configuration instanceCfg = this.cm.createFactoryConfiguration(WebAppInstance.PID);
            if ( log.isDebugEnabled() ) {
                log.debug("Adding " + instanceCfg.getPid()); //$NON-NLS-1$
            }
            Dictionary<String, Object> cfgProps = new Hashtable<>();
            cfgProps.put(
                "WebAppConfiguration.target", //$NON-NLS-1$
                FilterBuilder.get().eq(WebAppConfiguration.TARGET_BUNDLE_ATTR, cfg.getBundleSymbolicName()).toString());

            cfgProps.put(
                "WebAppDependencies.target", //$NON-NLS-1$
                FilterBuilder.get().eq("instanceId", cfg.getDependencies()).toString()); //$NON-NLS-1$
            instanceCfg.update(cfgProps);
            return instanceCfg;
        }
        catch ( IOException e ) {
            log.warn("Failed to initialize webapp configuration", e); //$NON-NLS-1$
        }

        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService ( ServiceReference<WebAppConfiguration> ref, Configuration cfg ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Updating " + cfg.getPid()); //$NON-NLS-1$
        }
        try {
            cfg.update();
        }
        catch ( IOException e ) {
            log.warn("Failed to update config", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<WebAppConfiguration> ref, Configuration cfg ) {
        if ( cfg != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Removing " + cfg.getPid()); //$NON-NLS-1$
            }
            try {
                cfg.delete();
            }
            catch ( IOException e ) {
                log.warn("Failed to remove webapp configuration", e); //$NON-NLS-1$
            }
        }
    }

}
