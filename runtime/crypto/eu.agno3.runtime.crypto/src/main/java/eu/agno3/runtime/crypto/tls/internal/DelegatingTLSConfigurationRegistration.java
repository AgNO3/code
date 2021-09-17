/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.commons.lang3.StringUtils;
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

import eu.agno3.runtime.crypto.tls.TLSConfiguration;
import eu.agno3.runtime.ldap.filter.FilterBuilder;


/**
 * @author mbechler
 *
 */
@Component ( service = DelegatingTLSConfigurationRegistration.class, immediate = true )
public class DelegatingTLSConfigurationRegistration implements ServiceTrackerCustomizer<TLSConfiguration, Configuration> {

    private static final Logger log = Logger.getLogger(DelegatingTLSConfigurationRegistration.class);

    static final String[] COPY_PROPERTIES = new String[] {
        TLSConfiguration.ID, TLSConfiguration.PRIORITY, TLSConfiguration.ROLE, TLSConfiguration.SUBSYSTEM, TLSConfiguration.CIPHERS,
        TLSConfiguration.PROTOCOLS, TLSConfiguration.KEY_ALIAS, TLSConfiguration.PINNED_PUBLIC_KEYS, TLSConfiguration.DISABLE_SNI
    };

    private ConfigurationAdmin cm;
    private ServiceTracker<TLSConfiguration, Configuration> serviceTracker;
    private BundleContext bundleContext;


    @Reference
    protected synchronized void setConfigurationAdmin ( ConfigurationAdmin cf ) {
        this.cm = cf;
    }


    protected synchronized void unsetConfigurationAdmin ( ConfigurationAdmin cf ) {
        if ( this.cm == cf ) {
            this.cm = null;
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.bundleContext = ctx.getBundleContext();
        this.serviceTracker = new ServiceTracker<>(this.bundleContext, TLSConfiguration.class, this);
        this.serviceTracker.open();
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        if ( this.serviceTracker != null ) {
            this.serviceTracker.close();
            this.serviceTracker = null;
        }
        this.bundleContext = null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public Configuration addingService ( ServiceReference<TLSConfiguration> cfgRef ) {
        try {
            Configuration cfg = this.cm.createFactoryConfiguration(DelegatingTLSConfiguration.INTERNAL_PID);
            if ( log.isDebugEnabled() ) {
                log.debug("Creating tls configuration " + cfgRef.getProperty(TLSConfiguration.ID)); //$NON-NLS-1$
            }
            // this.bundleContext.getService(cfgRef);
            updateConfiguration(cfgRef, cfg);
            return cfg;
        }
        catch ( IOException e ) {
            log.warn("Failed to create configuration", e); //$NON-NLS-1$
        }

        return null;
    }


    /**
     * @param cfgRef
     * @param cfg
     * @throws IOException
     */
    protected void updateConfiguration ( ServiceReference<TLSConfiguration> cfgRef, Configuration cfg ) throws IOException {

        Dictionary<String, Object> cfgProps = copyProperties(cfgRef);

        FilterBuilder fb = FilterBuilder.get();
        String ksProperty = (String) cfgRef.getProperty(TLSConfiguration.KEY_STORE);
        String hnvProperty = (String) cfgRef.getProperty(TLSConfiguration.HOSTNAME_VERIFIER);
        String tsProperty = (String) cfgRef.getProperty(TLSConfiguration.TRUST_STORE);

        if ( StringUtils.isBlank(ksProperty) ) {
            ksProperty = "noKey"; //$NON-NLS-1$
        }

        if ( StringUtils.isBlank(hnvProperty) ) {
            hnvProperty = "default"; //$NON-NLS-1$
        }

        if ( StringUtils.isBlank(tsProperty) ) {
            tsProperty = "allInvalid"; //$NON-NLS-1$
        }

        Object keyAliasProp = cfgRef.getProperty(TLSConfiguration.KEY_ALIAS);
        if ( keyAliasProp != null ) {
            cfgProps.put(TLSConfiguration.KEY_ALIAS, keyAliasProp);
        }
        cfgProps.put(
            "KeyStoreConfiguration.target", //$NON-NLS-1$
            fb.eq(TLSConfiguration.ID, ksProperty).toString());
        cfgProps.put(
            "HostnameVerifier.target", //$NON-NLS-1$
            fb.eq(TLSConfiguration.ID, hnvProperty).toString());
        cfgProps.put(
            "TrustManagerFactory.target", //$NON-NLS-1$
            fb.eq(TLSConfiguration.ID, tsProperty).toString());
        cfg.update(cfgProps);
    }


    /**
     * @param cfgRef
     * @return
     */
    protected static Dictionary<String, Object> copyProperties ( ServiceReference<?> cfgRef ) {
        Dictionary<String, Object> cfgProps = new Hashtable<>();
        for ( String prop : COPY_PROPERTIES ) {
            Object val = cfgRef.getProperty(prop);
            if ( val != null ) {
                cfgProps.put(prop, val);
            }
        }
        return cfgProps;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService ( ServiceReference<TLSConfiguration> cfg, Configuration ref ) {
        try {
            if ( log.isDebugEnabled() ) {
                log.debug("Updating tls configuration " + cfg.getProperty(TLSConfiguration.ID)); //$NON-NLS-1$
            }
            this.updateConfiguration(cfg, ref);
        }
        catch ( IOException e ) {
            log.warn("Failed to update configuration", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<TLSConfiguration> cfg, Configuration ref ) {
        if ( ref != null ) {
            try {
                if ( log.isDebugEnabled() ) {
                    log.debug("Removing tls configuration " + cfg.getProperty(TLSConfiguration.ID)); //$NON-NLS-1$
                }
                // this.bundleContext.ungetService(cfg);
                ref.delete();
            }
            catch ( IOException e ) {
                log.warn("Failed to remove factory configuration", e); //$NON-NLS-1$
            }
        }
    }

}
