/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.pkcs11.internal;


import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.pkcs11.PKCS11TokenConfiguration;
import eu.agno3.runtime.crypto.pkcs11.PKCS11Util;
import eu.agno3.runtime.crypto.tls.KeyStoreConfiguration;
import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = PKCS11KeyStoreRegistration.class, immediate = true )
public class PKCS11KeyStoreRegistration implements ServiceTrackerCustomizer<PKCS11TokenConfiguration, ServiceRegistration<KeyStoreConfiguration>> {

    /**
     * 
     */
    private static final String PKCS11 = "PKCS11"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(PKCS11KeyStoreRegistration.class);
    private ServiceTracker<PKCS11TokenConfiguration, ServiceRegistration<KeyStoreConfiguration>> tracker;
    private ComponentContext componentContext;
    private PKCS11Util pkcs11Util;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.componentContext = ctx;
        this.tracker = new ServiceTracker<>(ctx.getBundleContext(), PKCS11TokenConfiguration.class, this);
        this.tracker.open();
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        if ( this.tracker != null ) {
            this.tracker.close();
            this.tracker = null;
        }
        this.componentContext = null;
    }


    @Reference
    protected synchronized void setPKCS11Util ( PKCS11Util p11util ) {
        this.pkcs11Util = p11util;
    }


    protected synchronized void unsetPKCS11Util ( PKCS11Util p11util ) {
        if ( this.pkcs11Util == p11util ) {
            this.pkcs11Util = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public ServiceRegistration<KeyStoreConfiguration> addingService ( ServiceReference<PKCS11TokenConfiguration> ref ) {
        PKCS11TokenConfiguration tokenConfig = this.componentContext.getBundleContext().getService(ref);

        if ( tokenConfig == null ) {
            return null;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Registering keystore for token " + tokenConfig.getInstanceId()); //$NON-NLS-1$
        }
        PKCS11KeyStoreConfiguration ksConfig;
        try {
            ksConfig = new PKCS11KeyStoreConfiguration(tokenConfig, this.pkcs11Util);
        }
        catch ( CryptoException e ) {
            log.error("Failed to initialize PKCS11 keystore " + tokenConfig.getInstanceId(), e); //$NON-NLS-1$
            return null;
        }
        Dictionary<String, Object> props = new Hashtable<>();
        props.put(KeyStoreConfiguration.ID, tokenConfig.getInstanceId());
        props.put("storeType", PKCS11); //$NON-NLS-1$
        return DsUtil.registerSafe(this.componentContext, KeyStoreConfiguration.class, ksConfig, props);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService ( ServiceReference<PKCS11TokenConfiguration> ref, ServiceRegistration<KeyStoreConfiguration> obj ) {
        // unused
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<PKCS11TokenConfiguration> ref, ServiceRegistration<KeyStoreConfiguration> obj ) {
        if ( obj != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Unregistering keystore for token " + ref.getProperty(KeyStoreConfiguration.ID)); //$NON-NLS-1$
            }
            DsUtil.unregisterSafe(this.componentContext, obj);
        }
    }

}
