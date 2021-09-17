/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SNIMatcher;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.AliasSelectingKeyManager;
import eu.agno3.runtime.crypto.tls.ExtendedKeyManager;
import eu.agno3.runtime.crypto.tls.InternalTLSConfiguration;
import eu.agno3.runtime.crypto.tls.KeyStoreConfiguration;
import eu.agno3.runtime.crypto.tls.SNIHandler;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    DelegatingTLSConfiguration.class, InternalTLSConfiguration.class
}, configurationPid = DelegatingTLSConfiguration.INTERNAL_PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class DelegatingTLSConfiguration extends TLSConfigurationImpl implements InternalTLSConfiguration {

    private static final Logger log = Logger.getLogger(DelegatingTLSConfiguration.class);

    static final String INTERNAL_PID = "crypto.tls.delegatingConfig"; //$NON-NLS-1$

    private KeyStoreConfiguration keyStoreConfiguration;
    private TrustManagerFactory trustManagerFactory;
    private HostnameVerifier hostnameVerifier;

    private ExtendedKeyManager[] kms;
    private TrustManager[] tms;

    private DefaultSNIMatcher sniMatcher;

    private Map<String, SNIHandler> sniHandlers = new HashMap<>();
    private List<SNIHandler> orderedSNIHandlers = new ArrayList<>();


    @Override
    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        super.activate(ctx);
        super.configure(ctx.getProperties());
    }


    @Override
    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        super.deactivate(ctx);
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        super.configure(ctx.getProperties());
        if ( this.kms != null ) {
            for ( ExtendedKeyManager reloadingKeyManager : this.kms ) {
                reloadingKeyManager.clearCache();
            }
        }
    }


    @Reference ( updated = "updateKeyStoreConfiguration" )
    protected synchronized void setKeyStoreConfiguration ( KeyStoreConfiguration kmf ) {
        this.keyStoreConfiguration = kmf;
    }


    protected synchronized void unsetKeyStoreConfiguration ( KeyStoreConfiguration kmf ) {
        if ( this.keyStoreConfiguration == kmf ) {
            this.keyStoreConfiguration = null;
        }
    }


    protected synchronized void updateKeyStoreConfiguration ( KeyStoreConfiguration kmf ) {
        this.keyStoreConfiguration = kmf;
        log.debug("KeyStoreConfiguration updated"); //$NON-NLS-1$
    }


    @Reference ( updated = "updateTrustManagerFactory" )
    protected synchronized void setTrustManagerFactory ( TrustManagerFactory tmf ) {
        this.trustManagerFactory = tmf;
    }


    protected synchronized void unsetTrustManagerFactory ( TrustManagerFactory tmf ) {
        if ( this.trustManagerFactory == tmf ) {
            this.trustManagerFactory = null;
        }
    }


    protected synchronized void updateTrustManagerFactory ( TrustManagerFactory tmf ) {
        this.trustManagerFactory = tmf;
        log.debug("TrustManagerFactory updated"); //$NON-NLS-1$
    }


    @Reference
    protected synchronized void setHostnameVerifier ( HostnameVerifier hnv ) {
        this.hostnameVerifier = hnv;
    }


    protected synchronized void unsetHostnameVerifier ( HostnameVerifier hnv ) {
        if ( this.hostnameVerifier == hnv ) {
            this.hostnameVerifier = null;
        }
    }


    @Reference
    protected synchronized void setSNIMatcher ( DefaultSNIMatcher sm ) {
        this.sniMatcher = sm;
    }


    protected synchronized void unsetSNIMatcher ( DefaultSNIMatcher sm ) {
        if ( this.sniMatcher == sm ) {
            this.sniMatcher = null;
        }
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindSNIHandler ( SNIHandler h ) {
        this.sniHandlers.put(h.getHandlerId(), h);
        orderSNIHandlers();
    }


    protected synchronized void unbindSNIHandler ( SNIHandler h ) {
        if ( this.sniHandlers.remove(h.getHandlerId()) != null ) {
            orderSNIHandlers();
        }
    }


    /**
     * 
     */
    private void orderSNIHandlers () {
        List<SNIHandler> ordered = new ArrayList<>(this.sniHandlers.values());
        Collections.sort(ordered, new SNIHandlerComparator());
        this.orderedSNIHandlers = ordered;
    }


    /**
     * @return the orderedSniHandlers
     */
    public List<SNIHandler> getOrderedSNIHandlers () {
        return this.orderedSNIHandlers;
    }


    /**
     * 
     * @param id
     * @return SNI handler if present, or null
     */
    public SNIHandler lookupSNIHandler ( String id ) {
        return this.sniHandlers.get(id);
    }


    /**
     * @return the sniMatcher
     */
    @Override
    public Collection<SNIMatcher> getSniMatchers () {
        return Collections.singletonList(this.sniMatcher);
    }


    /**
     * @return the key managers to use
     */
    @Override
    public KeyManager[] getKeyManagers () {

        if ( this.kms == null ) {
            try {
                this.kms = this.wrapKeyManagers(this.keyStoreConfiguration.getKeyManagerFactory().getKeyManagers());
            }
            catch ( CryptoException e ) {
                log.error("Failed to setup key managers", e); //$NON-NLS-1$
            }
        }

        if ( this.kms != null ) {
            return Arrays.copyOf(this.kms, this.kms.length);
        }
        return new KeyManager[] {};
    }


    /**
     * @param keyManagers
     * @return
     * @throws CryptoException
     */
    private ExtendedKeyManager[] wrapKeyManagers ( KeyManager[] keyManagers ) throws CryptoException {
        if ( keyManagers == null ) {
            return null;
        }
        ExtendedKeyManager[] res = new ExtendedKeyManager[keyManagers.length];

        for ( int i = 0; i < keyManagers.length; i++ ) {
            res[ i ] = wrapKeyManager(keyManagers[ i ]);
        }

        return res;
    }


    /**
     * @param keyManager
     * @return
     * @throws CryptoException
     */
    private ExtendedKeyManager wrapKeyManager ( KeyManager keyManager ) throws CryptoException {
        if ( this.getKeyAlias() != null && keyManager instanceof X509ExtendedKeyManager ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format(
                    "Creating alias selecting key manager for config %s alias %s", //$NON-NLS-1$
                    this.getId(),
                    this.getKeyAlias()));
            }
            return new ExtendedKeyManager(new AliasSelectingKeyManager(this.getKeyAlias(), (X509ExtendedKeyManager) keyManager), this);
        }

        if ( ! ( keyManager instanceof X509ExtendedKeyManager ) ) {
            throw new CryptoException("Not an extended key manager " + keyManager); //$NON-NLS-1$
        }
        return new ExtendedKeyManager((X509ExtendedKeyManager) keyManager, this);
    }


    /**
     * @return the trust managers to use
     * @throws CryptoException
     */
    @Override
    public TrustManager[] getTrustManagers () throws CryptoException {
        if ( this.tms == null ) {
            TrustManager[] trustManagers = this.trustManagerFactory.getTrustManagers();
            for ( int i = 0; i < trustManagers.length; i++ ) {
                trustManagers[ i ] = wrapTrustManager(trustManagers[ i ]);
            }
            this.tms = trustManagers;
        }

        if ( this.tms != null ) {
            return Arrays.copyOf(this.tms, this.tms.length);
        }
        return new TrustManager[] {};
    }


    /**
     * @param trustManager
     * @return
     */
    protected TrustManager wrapTrustManager ( TrustManager trustManager ) {
        if ( ! ( trustManager instanceof X509TrustManager ) ) {
            log.debug("Not an x509 trust manager"); //$NON-NLS-1$
            return trustManager;
        }
        return new PinningX509TrustManager((X509TrustManager) trustManager, this);
    }


    /**
     * @return the hostname verifer to use
     */
    @Override
    public synchronized HostnameVerifier getHostnameVerifier () {
        return new PinningHostnameVerifier(this.hostnameVerifier, this);
    }


    /**
     * @param sslParameters
     * @return modified ssl parameters
     */
    @Override
    public SSLParameters adaptParameters ( SSLParameters sslParameters ) {
        return sslParameters;
    }

}
