/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2014 by mbechler
 */
package eu.agno3.runtime.console.ssh.internal;


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
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import eu.agno3.runtime.crypto.tls.KeyStoreConfiguration;
import eu.agno3.runtime.ldap.filter.FilterBuilder;


/**
 * @author mbechler
 *
 */
@Component (
    immediate = true,
    service = KeyStoreHostKeyProviderRegistration.class,
    configurationPid = KeyStoreHostKeyProviderRegistration.PID,
    configurationPolicy = ConfigurationPolicy.REQUIRE )
public class KeyStoreHostKeyProviderRegistration implements ServiceTrackerCustomizer<KeyStoreConfiguration, Configuration> {

    /**
     * 
     */
    private static final String INSTANCE_ID = "instanceId"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(KeyStoreHostKeyProviderRegistration.class);

    /**
     * 
     */
    public static final String PID = "console.ssh.keystore"; //$NON-NLS-1$

    private String keyStoreId;
    private String alias;

    private BundleContext bundleContext;

    private ConfigurationAdmin configAdmin;

    private ServiceTracker<KeyStoreConfiguration, Configuration> serviceTracker;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {

        String keyStoreSpec = (String) ctx.getProperties().get("keyStore"); //$NON-NLS-1$
        if ( StringUtils.isBlank(keyStoreSpec) ) {
            log.warn("No keystore id configured"); //$NON-NLS-1$
            return;
        }
        this.keyStoreId = keyStoreSpec.trim();

        this.alias = (String) ctx.getProperties().get("keyAlias"); //$NON-NLS-1$

        this.bundleContext = ctx.getBundleContext();
        this.serviceTracker = new ServiceTracker<>(this.bundleContext, KeyStoreConfiguration.class, this);
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


    @Reference
    protected synchronized void setConfigAdmin ( ConfigurationAdmin cm ) {
        this.configAdmin = cm;
    }


    protected synchronized void unsetConfigAdmin ( ConfigurationAdmin cm ) {
        if ( this.configAdmin == cm ) {
            this.configAdmin = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public Configuration addingService ( ServiceReference<KeyStoreConfiguration> ref ) {

        if ( this.keyStoreId == null || !this.keyStoreId.equals(ref.getProperty(INSTANCE_ID)) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Keystore does not match the configured one " + ref.getProperty(INSTANCE_ID)); //$NON-NLS-1$
            }
            return null;
        }

        try {
            Configuration cfg = this.configAdmin.createFactoryConfiguration("console.ssh.keypair.provider"); //$NON-NLS-1$
            if ( log.isDebugEnabled() ) {
                log.debug("Creating keypair provider " + this.keyStoreId); //$NON-NLS-1$
            }

            Dictionary<String, String> props = new Hashtable<>();
            props.put("instanceId", this.keyStoreId); //$NON-NLS-1$
            if ( this.alias != null ) {
                props.put("keyAlias", this.alias); //$NON-NLS-1$
            }
            props.put(
                "KeyStoreConfiguration.target", //$NON-NLS-1$
                FilterBuilder.get().eq(
                    "instanceId", //$NON-NLS-1$
                    (String) ref.getProperty("instanceId")).toString()); //$NON-NLS-1$
            cfg.update(props);
            return cfg;
        }
        catch ( IOException e ) {
            log.error("Failed to create keypair provider config", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService ( ServiceReference<KeyStoreConfiguration> ref, Configuration reg ) {
        // unused
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<KeyStoreConfiguration> ref, Configuration reg ) {
        if ( reg != null ) {
            try {
                reg.delete();
            }
            catch ( IOException e ) {
                log.error("Failed to remove keypair provider config", e); //$NON-NLS-1$
            }
        }
    }

}
