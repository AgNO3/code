/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.09.2015 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.internal;


import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.PKIXBuilderParameters;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.PKIXParameterFactory;
import eu.agno3.runtime.crypto.tls.TrustConfiguration;
import eu.agno3.runtime.crypto.truststore.ReloadableTrustManagerFactory;
import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 * @param <S>
 *
 */
public abstract class AbstractTrustManagerRegistration <S> implements ServiceTrackerCustomizer<S, ServiceRegistration<TrustManagerFactory>> {

    private static final Logger log = Logger.getLogger(TrustManagerRegistration.class);
    private static final String SUN_JSSE = "SunJSSE"; //$NON-NLS-1$
    private static final String PKIX = "PKIX"; //$NON-NLS-1$
    private PKIXParameterFactory pkixParameterFactory;
    private ComponentContext componentContext;


    /**
     * 
     */
    public AbstractTrustManagerRegistration () {
        super();
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.componentContext = ctx;
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        this.componentContext = null;
    }


    @Reference
    protected synchronized void setPKIXParameterFactory ( PKIXParameterFactory pf ) {
        this.pkixParameterFactory = pf;
    }


    protected synchronized void unsetPKIXParameterFactory ( PKIXParameterFactory pf ) {
        if ( this.pkixParameterFactory == pf ) {
            this.pkixParameterFactory = null;
        }
    }


    /**
     * @return the pkixParameterFactory
     */
    public PKIXParameterFactory getPkixParameterFactory () {
        return this.pkixParameterFactory;
    }


    /**
     * @param cfg
     * @return
     */
    protected ServiceRegistration<TrustManagerFactory> registerTrustManagerFactory ( ComponentContext ctx, TrustConfiguration cfg ) {
        if ( cfg == null ) {
            return null;
        }
        TrustManagerFactory tmf;
        try {
            tmf = cfg.getTrustManagerFactory();
        }
        catch ( CryptoException e ) {
            log.error("Failed to get trust manager", e); //$NON-NLS-1$
            return null;
        }
        Dictionary<String, Object> props = new Hashtable<>();
        props.put("instanceId", cfg.getId()); //$NON-NLS-1$
        return DsUtil.registerSafe(ctx, TrustManagerFactory.class, tmf, props);
    }


    /**
     * @param ctx
     * @param cfg
     * @param params
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidAlgorithmParameterException
     */
    protected static ServiceRegistration<TrustManagerFactory> registerTrustManagerFactory ( ComponentContext ctx, TrustConfiguration cfg,
            PKIXBuilderParameters params )
                    throws KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(PKIX, SUN_JSSE);
        tmf.init(new CertPathTrustManagerParameters(params));
        Dictionary<String, Object> props = new Hashtable<>();
        props.put("instanceId", cfg.getId()); //$NON-NLS-1$
        return DsUtil.registerSafe(ctx, TrustManagerFactory.class, tmf, props);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService ( ServiceReference<S> cfgRef, ServiceRegistration<TrustManagerFactory> ref ) {
        TrustManagerFactory tmf = this.componentContext.getBundleContext().getService(ref.getReference());
        if ( tmf instanceof ReloadableTrustManagerFactory ) {
            reloadTrustManagerFactory(cfgRef, ref, (ReloadableTrustManagerFactory) tmf);
        }
    }


    /**
     * @param cfgRef
     * @param ref
     * @param tmf
     */
    protected void reloadTrustManagerFactory ( ServiceReference<S> cfgRef, ServiceRegistration<TrustManagerFactory> ref,
            ReloadableTrustManagerFactory tmf ) {

    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<S> cfgRef, ServiceRegistration<TrustManagerFactory> reg ) {
        if ( reg != null ) {
            DsUtil.unregisterSafe(this.componentContext, reg);
        }
    }

}