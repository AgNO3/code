/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 21, 2017 by mbechler
 */
package eu.agno3.runtime.security.credentials.internal;


import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.log4j.Logger;
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

import eu.agno3.runtime.crypto.wrap.CryptUnwrapper;
import eu.agno3.runtime.ldap.filter.FilterBuilder;


/**
 * @author mbechler
 *
 */
@Component ( service = CredentialUnwrapperRegistration.class, immediate = true )
public class CredentialUnwrapperRegistration implements ServiceTrackerCustomizer<CryptUnwrapper, Configuration> {

    /**
     * 
     */
    private static final String INSTANCE_ID = "instanceId"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(CredentialUnwrapperRegistration.class);

    private ServiceTracker<CryptUnwrapper, Configuration> tracker;
    private ConfigurationAdmin configAdmin;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.tracker = new ServiceTracker<>(ctx.getBundleContext(), CryptUnwrapper.class, this);
        this.tracker.open();
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        this.tracker.close();
        this.tracker = null;
    }


    @Reference
    protected synchronized void setConfigurationAdmin ( ConfigurationAdmin cm ) {
        this.configAdmin = cm;
    }


    protected synchronized void unsetConfigurationAdmin ( ConfigurationAdmin cm ) {
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
    public Configuration addingService ( ServiceReference<CryptUnwrapper> ref ) {

        String instanceId = (String) ref.getProperty(INSTANCE_ID); // $NON-NLS-1$

        try {
            Configuration cfg = this.configAdmin.createFactoryConfiguration("credentialUnwrap"); //$NON-NLS-1$
            Dictionary<String, Object> props = new Hashtable<>();
            props.put(INSTANCE_ID, instanceId); // $NON-NLS-1$
            props.put("CryptUnwrapper.target", FilterBuilder.get().eq(INSTANCE_ID, instanceId).toString()); //$NON-NLS-1$
            cfg.update(props);
            return cfg;
        }
        catch ( IOException e ) {
            log.error("Failed to update configuration for crypt unwrapper instance " + instanceId, e); //$NON-NLS-1$
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
    public void modifiedService ( ServiceReference<CryptUnwrapper> ref, Configuration cfg ) {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<CryptUnwrapper> ref, Configuration cfg ) {
        if ( cfg != null ) {
            try {
                cfg.delete();
            }
            catch ( IOException e ) {
                log.error("Failed to remove configuration", e); //$NON-NLS-1$
            }
        }
    }

}
