/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.02.2016 by mbechler
 */
package eu.agno3.runtime.crypto.truststore;


import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManagerFactorySpi;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public class ReloadableTrustManagerFactory extends TrustManagerFactory {

    private static final Logger log = Logger.getLogger(ReloadableTrustManagerFactory.class);

    private static final String SUN_JSSE = "SunJSSE"; //$NON-NLS-1$
    private static final String PKIX = "PKIX"; //$NON-NLS-1$


    /**
     * @param factorySpi
     * @param provider
     * @param algorithm
     */
    protected ReloadableTrustManagerFactory ( String id, TrustManagerFactory fallback ) {
        super(new ReloadableTrustManagerFactorySpi(id, fallback), new Provider(ReloadableTrustManagerFactory.class.getName(), 0.1, "") { //$NON-NLS-1$

            private static final long serialVersionUID = -2828701184126892635L;
        }, "PKIX"); //$NON-NLS-1$
    }


    /**
     * @return the log
     */
    public static Logger getLog () {
        return log;
    }

    /**
     * @author mbechler
     *
     */
    public static class ReloadManagerFactoryParameters implements ManagerFactoryParameters {

    }

    private static class ReloadableTrustManagerFactorySpi extends TrustManagerFactorySpi {

        private volatile TrustManagerFactory delegate;
        private final TrustManagerFactory fallback;
        private final String id;


        /**
         * @param id
         * @param fallback
         * 
         */
        public ReloadableTrustManagerFactorySpi ( String id, TrustManagerFactory fallback ) {
            this.id = id;
            this.fallback = fallback;
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.TrustManagerFactorySpi#engineInit(java.security.KeyStore)
         */
        @Override
        protected void engineInit ( KeyStore ks ) throws KeyStoreException {
            throw new UnsupportedOperationException();
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.TrustManagerFactorySpi#engineInit(javax.net.ssl.ManagerFactoryParameters)
         */
        @Override
        protected void engineInit ( ManagerFactoryParameters spec ) throws InvalidAlgorithmParameterException {
            if ( spec == null ) {
                if ( getLog().isDebugEnabled() ) {
                    getLog().debug("No config, invalidating trustmanager factory " + this.id); //$NON-NLS-1$
                }
                this.delegate = null;
                return;
            }
            try {
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(PKIX, SUN_JSSE);
                tmf.init(spec);
                if ( getLog().isDebugEnabled() ) {
                    getLog().debug("Initialized new trust manager factory " + this.id); //$NON-NLS-1$
                }
                this.delegate = tmf;
            }
            catch (
                NoSuchAlgorithmException |
                NoSuchProviderException e ) {
                getLog().warn("Failed to create trust manager factory " + this.id, e); //$NON-NLS-1$
                throw new InvalidAlgorithmParameterException("Failed to create delegate", e); //$NON-NLS-1$
            }
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.TrustManagerFactorySpi#engineGetTrustManagers()
         */
        @Override
        protected TrustManager[] engineGetTrustManagers () {
            TrustManagerFactory d = this.delegate;

            if ( getLog().isTraceEnabled() ) {
                getLog().trace("Delegate is " + d); //$NON-NLS-1$
                getLog().trace("Fallback is " + this.fallback); //$NON-NLS-1$
            }

            return d != null ? d.getTrustManagers() : this.fallback.getTrustManagers();
        }

    }
}
