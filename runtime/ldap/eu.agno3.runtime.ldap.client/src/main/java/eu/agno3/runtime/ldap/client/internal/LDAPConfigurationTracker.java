/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2015 by mbechler
 */
package eu.agno3.runtime.ldap.client.internal;


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
import eu.agno3.runtime.ldap.client.LDAPConfiguration;
import eu.agno3.runtime.ldap.filter.FilterBuilder;


/**
 * @author mbechler
 *
 */
@Component ( service = LDAPConfigurationTracker.class, immediate = true )
public class LDAPConfigurationTracker implements ServiceTrackerCustomizer<LDAPConfiguration, Configuration> {

    /**
     * 
     */
    static final String INSTANCE_ID = "instanceId"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(LDAPConfigurationTracker.class);

    private ConfigurationAdmin cm;
    private BundleContext bundleContext;
    private ServiceTracker<LDAPConfiguration, Configuration> serviceTracker;


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
        this.serviceTracker = new ServiceTracker<>(ctx.getBundleContext(), LDAPConfiguration.class, this);
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
     * @param cfgRef
     * @param cfg
     * @throws IOException
     */
    protected void updateConfiguration ( LDAPConfiguration c, Configuration cfg ) throws IOException {
        Dictionary<String, Object> cfgProps = new Hashtable<>();
        String cfgInstanceId = c.getInstanceId();
        String tlsContextId = c.getTLSContextName();
        cfgProps.put(INSTANCE_ID, cfgInstanceId);

        FilterBuilder fb = FilterBuilder.get();
        cfgProps.put(
            "LDAPConfiguration.target", //$NON-NLS-1$
            fb.eq(INSTANCE_ID, cfgInstanceId).toString());

        if ( !StringUtils.isBlank(tlsContextId) ) {
            cfgProps.put(
                "TLSContext.target", //$NON-NLS-1$
                fb.eq(INSTANCE_ID, tlsContextId).toString());
        }
        else if ( c.useSSL() || c.useStartTLS() ) {
            cfgProps.put(
                "TLSContext.target", //$NON-NLS-1$
                fb.or(
                    fb.eq(TLSConfiguration.ROLE, "client"), //$NON-NLS-1$
                    fb.eq(TLSConfiguration.ROLE, "default")).toString()); //$NON-NLS-1$
        }
        cfg.update(cfgProps);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public Configuration addingService ( ServiceReference<LDAPConfiguration> ref ) {
        LDAPConfiguration cfg = this.bundleContext.getService(ref);
        if ( cfg == null ) {
            return null;
        }

        String instanceId = cfg.getInstanceId();
        if ( StringUtils.isBlank(instanceId) ) {
            log.error("No instanceId configured"); //$NON-NLS-1$
            return null;
        }

        try {
            Configuration cf = this.cm.createFactoryConfiguration(LDAPClientFactoryImpl.INTERNAL_PID);
            if ( log.isDebugEnabled() ) {
                log.debug("Creating internal configuration for " + instanceId); //$NON-NLS-1$
            }
            updateConfiguration(cfg, cf);
        }
        catch ( IOException e ) {
            log.warn("Failed to create ldap configuration", e); //$NON-NLS-1$
            return null;
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
    public void modifiedService ( ServiceReference<LDAPConfiguration> ref, Configuration cfg ) {
        if ( cfg != null ) {
            try {
                if ( log.isDebugEnabled() ) {
                    log.debug("Updating internal configuration for " + ref.getProperty(INSTANCE_ID)); //$NON-NLS-1$
                }
                LDAPConfiguration c = this.bundleContext.getService(ref);
                if ( c != null ) {
                    updateConfiguration(c, cfg);
                }
            }
            catch ( IOException e ) {
                log.warn("Failed to update LDAP configuration", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<LDAPConfiguration> ref, Configuration cfg ) {
        if ( cfg != null ) {
            try {
                if ( log.isDebugEnabled() ) {
                    log.debug("Deleting internal configuration for " + ref.getProperty(INSTANCE_ID)); //$NON-NLS-1$
                }
                cfg.delete();
            }
            catch ( IOException e ) {
                log.warn("Failed to delete ldap configuration", e); //$NON-NLS-1$
            }
        }
    }

}
