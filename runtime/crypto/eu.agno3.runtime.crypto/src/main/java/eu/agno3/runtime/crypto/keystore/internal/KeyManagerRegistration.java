/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.keystore.internal;


import java.util.Dictionary;
import java.util.Hashtable;

import javax.net.ssl.KeyManagerFactory;

import org.apache.log4j.Logger;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.KeyStoreConfiguration;
import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = KeyManagerRegistration.class, immediate = true )
public class KeyManagerRegistration implements ServiceTrackerCustomizer<KeyStoreConfiguration, ServiceRegistration<KeyManagerFactory>> {

    private static final Logger log = Logger.getLogger(KeyManagerRegistration.class);

    private ComponentContext componentContext;
    private ServiceTracker<KeyStoreConfiguration, ServiceRegistration<KeyManagerFactory>> serviceTracker;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.componentContext = ctx;
        this.serviceTracker = new ServiceTracker<>(ctx.getBundleContext(), KeyStoreConfiguration.class, this);
        this.serviceTracker.open();
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        this.serviceTracker.close();
        this.serviceTracker = null;
        this.componentContext = null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public ServiceRegistration<KeyManagerFactory> addingService ( ServiceReference<KeyStoreConfiguration> cfgRef ) {

        KeyStoreConfiguration cfg = this.componentContext.getBundleContext().getService(cfgRef);

        try {
            if ( cfg == null ) {
                return null;
            }
            KeyManagerFactory kmf = cfg.getKeyManagerFactory();

            Dictionary<String, Object> props = new Hashtable<>();
            props.put("instanceId", cfg.getId()); //$NON-NLS-1$
            return DsUtil.registerSafe(this.componentContext, KeyManagerFactory.class, kmf, props);
        }
        catch ( CryptoException e ) {
            log.error("Failed to initialize key manager", e); //$NON-NLS-1$
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
    public void modifiedService ( ServiceReference<KeyStoreConfiguration> cfgRef, ServiceRegistration<KeyManagerFactory> ref ) {
        KeyStoreConfiguration cfg = this.componentContext.getBundleContext().getService(cfgRef);
        if ( cfg == null ) {
            return;
        }

        Dictionary<String, Object> props = new Hashtable<>();
        props.put("instanceId", cfg.getId()); //$NON-NLS-1$
        ref.setProperties(props);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<KeyStoreConfiguration> cfgRef, ServiceRegistration<KeyManagerFactory> ref ) {
        if ( ref != null ) {
            DsUtil.unregisterSafe(this.componentContext, ref);
        }
    }

}
