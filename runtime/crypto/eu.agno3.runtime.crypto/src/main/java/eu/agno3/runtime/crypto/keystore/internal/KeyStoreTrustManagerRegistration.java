/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.keystore.internal;


import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Provider;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.ServiceTracker;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.KeyStoreConfiguration;
import eu.agno3.runtime.crypto.tls.PKIXParameterFactory;
import eu.agno3.runtime.crypto.truststore.internal.AbstractTrustManagerRegistration;
import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = KeyStoreTrustManagerRegistration.class, immediate = true )
public class KeyStoreTrustManagerRegistration extends AbstractTrustManagerRegistration<KeyStoreConfiguration> {

    private static final Logger log = Logger.getLogger(KeyStoreTrustManagerRegistration.class);

    private ComponentContext componentContext;
    private ServiceTracker<KeyStoreConfiguration, ServiceRegistration<TrustManagerFactory>> serviceTracker;


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }


    @Override
    @Reference
    protected synchronized void setPKIXParameterFactory ( PKIXParameterFactory pf ) {
        super.setPKIXParameterFactory(pf);
    }


    @Override
    protected synchronized void unsetPKIXParameterFactory ( PKIXParameterFactory pf ) {
        super.unsetPKIXParameterFactory(pf);
    }


    @Override
    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.componentContext = ctx;
        this.serviceTracker = new ServiceTracker<>(ctx.getBundleContext(), KeyStoreConfiguration.class, this);
        this.serviceTracker.open();
    }


    @Override
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
    public ServiceRegistration<TrustManagerFactory> addingService ( ServiceReference<KeyStoreConfiguration> cfgRef ) {
        KeyStoreConfiguration service = this.componentContext.getBundleContext().getService(cfgRef);
        if ( service == null ) {
            return null;
        }
        if ( log.isDebugEnabled() ) {
            log.debug("Registering KeyStore trust manager for " + service.getId()); //$NON-NLS-1$
        }
        Dictionary<String, Object> props = new Hashtable<>();
        props.put(
            "instanceId", //$NON-NLS-1$
            "keyStore:" + service.getId()); //$NON-NLS-1$
        return DsUtil.registerSafe(this.componentContext, TrustManagerFactory.class, new KeystoreTrustManagerFactory(service), props);
    }

    private static class KeystoreTrustManagerFactory extends TrustManagerFactory {

        /**
         * @param factorySpi
         * @param provider
         * @param algorithm
         */
        protected KeystoreTrustManagerFactory ( KeyStoreConfiguration ks ) {
            super(new KeystoreTrustManagerFactorySpi(ks), new Provider(KeystoreTrustManagerFactory.class.getName(), 0.1, "") { //$NON-NLS-1$

                private static final long serialVersionUID = -2828701184126892635L;
            }, "Keystore"); //$NON-NLS-1$
        }

    }

    private static class KeystoreTrustManagerFactorySpi extends TrustManagerFactorySpi {

        private KeyStoreConfiguration keyStore;


        /**
         * @param ks
         * 
         */
        public KeystoreTrustManagerFactorySpi ( KeyStoreConfiguration ks ) {
            this.keyStore = ks;
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.TrustManagerFactorySpi#engineInit(java.security.KeyStore)
         */
        @Override
        protected void engineInit ( KeyStore ks ) throws KeyStoreException {}


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.TrustManagerFactorySpi#engineInit(javax.net.ssl.ManagerFactoryParameters)
         */
        @Override
        protected void engineInit ( ManagerFactoryParameters spec ) throws InvalidAlgorithmParameterException {
            throw new InvalidAlgorithmParameterException();
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.TrustManagerFactorySpi#engineGetTrustManagers()
         */
        @Override
        protected TrustManager[] engineGetTrustManagers () {
            return new X509TrustManager[] {
                new KeystoreX509TrustManager(this.keyStore)
            };
        }
    }

    private static class KeystoreX509TrustManager extends X509ExtendedTrustManager {

        private final KeyStoreConfiguration keyStoreConfig;
        private volatile Set<X509Certificate> trustCache = new HashSet<>();


        /**
         * @param keyStore
         */
        public KeystoreX509TrustManager ( KeyStoreConfiguration keyStore ) {
            this.keyStoreConfig = keyStore;
            reload(keyStore.getKeyStore());
        }


        /**
         * @param ks
         * @throws CryptoException
         */
        private void reload ( KeyStore ks ) {
            Set<X509Certificate> trusted = new HashSet<>();

            try {
                if ( ks != null ) {
                    Enumeration<String> aliases = ks.aliases();
                    while ( aliases.hasMoreElements() ) {
                        String alias = aliases.nextElement();
                        Certificate[] certificateChain = ks.getCertificateChain(alias);
                        if ( certificateChain != null ) {
                            if ( getLog().isDebugEnabled() ) {
                                getLog().debug("Adding certificates for alias " + alias); //$NON-NLS-1$
                            }

                            for ( Certificate c : certificateChain ) {
                                trusted.add((X509Certificate) c);
                            }
                        }
                    }
                }
            }
            catch ( KeyStoreException e ) {
                getLog().error("Failed to reload trust KeyStore", e); //$NON-NLS-1$
            }
            this.trustCache = trusted;
        }


        private void checkTrusted ( X509Certificate[] chain, String authType ) throws CertificateException {
            if ( chain == null || chain.length == 0 ) {
                throw new CertificateException("No chain"); //$NON-NLS-1$
            }

            X509Certificate ee = chain[ 0 ];

            // reload if we hit a non match
            if ( !this.trustCache.contains(ee) ) {
                try {
                    reload(this.keyStoreConfig.reloadKeyStore());
                }
                catch ( CryptoException e ) {
                    getLog().error("Failed to reload trust KeyStore", e); //$NON-NLS-1$
                }
            }

            if ( !this.trustCache.contains(ee) ) {
                throw new CertificateException("Certificate is not contained in trust store " + this.keyStoreConfig.getId()); //$NON-NLS-1$
            }
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[],
         *      java.lang.String)
         */
        @Override
        public void checkClientTrusted ( X509Certificate[] chain, String authType ) throws CertificateException {
            checkTrusted(chain, authType);
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[],
         *      java.lang.String)
         */
        @Override
        public void checkServerTrusted ( X509Certificate[] chain, String authType ) throws CertificateException {
            checkTrusted(chain, authType);
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.X509ExtendedTrustManager#checkClientTrusted(java.security.cert.X509Certificate[],
         *      java.lang.String, java.net.Socket)
         */
        @Override
        public void checkClientTrusted ( X509Certificate[] chain, String authType, Socket socket ) throws CertificateException {
            checkClientTrusted(chain, authType);
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.X509ExtendedTrustManager#checkServerTrusted(java.security.cert.X509Certificate[],
         *      java.lang.String, java.net.Socket)
         */
        @Override
        public void checkServerTrusted ( X509Certificate[] chain, String authType, Socket socket ) throws CertificateException {
            checkServerTrusted(chain, authType);
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.X509ExtendedTrustManager#checkClientTrusted(java.security.cert.X509Certificate[],
         *      java.lang.String, javax.net.ssl.SSLEngine)
         */
        @Override
        public void checkClientTrusted ( X509Certificate[] chain, String authType, SSLEngine engine ) throws CertificateException {
            checkClientTrusted(chain, authType);
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.X509ExtendedTrustManager#checkServerTrusted(java.security.cert.X509Certificate[],
         *      java.lang.String, javax.net.ssl.SSLEngine)
         */
        @Override
        public void checkServerTrusted ( X509Certificate[] chain, String authType, SSLEngine engine ) throws CertificateException {
            checkClientTrusted(chain, authType);
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
         */
        @Override
        public X509Certificate[] getAcceptedIssuers () {
            return new X509Certificate[0];
        }

    }
}
