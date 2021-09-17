/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.12.2015 by mbechler
 */
package eu.agno3.runtime.crypto.tls;


import java.lang.reflect.Field;
import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509ExtendedKeyManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.runtime.crypto.tls.internal.DelegatingTLSConfiguration;


/**
 * @author mbechler
 *
 */
public class ExtendedKeyManager extends KeyManagerWrapper {

    private static final Logger log = Logger.getLogger(ExtendedKeyManager.class);

    private static Class<?> KM_CLASS;
    private static Field KM_CACHE_FIELD;

    private final DelegatingTLSConfiguration tlsConfig;


    static {
        try {
            KM_CLASS = ExtendedKeyManager.class.getClassLoader().loadClass("sun.security.ssl.X509KeyManagerImpl"); //$NON-NLS-1$
            KM_CACHE_FIELD = KM_CLASS.getDeclaredField("entryCacheMap"); //$NON-NLS-1$
            KM_CACHE_FIELD.setAccessible(true);
        }
        catch (
            ClassNotFoundException |
            NoSuchFieldException |
            SecurityException e ) {
            log.error("Failed to find key manager class", e); //$NON-NLS-1$
        }
    }


    /**
     * @param delegate
     * @param tlsConfig
     */
    public ExtendedKeyManager ( X509ExtendedKeyManager delegate, DelegatingTLSConfiguration tlsConfig ) {
        super(delegate);
        this.tlsConfig = tlsConfig;
    }


    /**
     * @return
     */
    private String getFallbackAlias () {

        if ( this.getDelegate() instanceof AliasSelectingKeyManager ) {
            return ( (AliasSelectingKeyManager) this.getDelegate() ).getKeyAlias();
        }
        return this.tlsConfig.getKeyAlias();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.KeyManagerWrapper#chooseServerAlias(java.lang.String, java.security.Principal[],
     *      java.net.Socket)
     */
    @Override
    public String chooseServerAlias ( String keyType, Principal[] issuers, Socket socket ) {
        if ( ! ( socket instanceof SSLSocket || !this.tlsConfig.isEnableServerSNI() ) ) {
            return super.chooseServerAlias(keyType, issuers, socket);
        }
        SSLSocket s = (SSLSocket) socket;
        SSLParameters sslParameters = s.getSSLParameters();
        SSLSession handshakeSession = s.getHandshakeSession();
        String override = chooseServerAliasInternal(keyType, issuers, sslParameters, handshakeSession);
        if ( override != null ) {
            return override;
        }

        return super.chooseServerAlias(keyType, issuers, socket);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.KeyManagerWrapper#chooseEngineServerAlias(java.lang.String,
     *      java.security.Principal[], javax.net.ssl.SSLEngine)
     */
    @Override
    public String chooseEngineServerAlias ( String keyType, Principal[] issuers, SSLEngine engine ) {
        if ( this.tlsConfig.isEnableServerSNI() ) {
            SSLParameters sslParameters = engine.getSSLParameters();
            SSLSession handshakeSession = engine.getHandshakeSession();
            String override = chooseServerAliasInternal(keyType, issuers, sslParameters, handshakeSession);
            if ( override != null ) {
                return override;
            }
        }

        return super.chooseEngineServerAlias(keyType, issuers, engine);
    }


    /**
     * @param keyType
     * @param issuers
     * @param sslParameters
     * @param session
     */
    private final String chooseServerAliasInternal ( String keyType, Principal[] issuers, SSLParameters sslParameters, SSLSession session ) {
        String[] candidates = getServerAliases(keyType, issuers);
        if ( candidates == null || candidates.length == 0 || sslParameters.getSNIMatchers() == null || sslParameters.getSNIMatchers().isEmpty()
                || ! ( session instanceof ExtendedSSLSession ) ) {
            return null;
        }

        ExtendedSSLSession es = (ExtendedSSLSession) session;

        List<SNIServerName> requestedServerNames = es.getRequestedServerNames();
        if ( requestedServerNames == null || requestedServerNames.isEmpty() ) {
            return null;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Requested names are " + requestedServerNames); //$NON-NLS-1$
        }

        List<SNIHandler> orderedSNIHandlers = this.tlsConfig.getOrderedSNIHandlers();

        for ( SNIHandler h : orderedSNIHandlers ) {
            String id = h.matches(requestedServerNames);
            if ( id != null ) {
                return String.format("dynamic:%s:%s", h.getHandlerId(), id); //$NON-NLS-1$
            }
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.KeyManagerWrapper#getCertificateChain(java.lang.String)
     */
    @Override
    public X509Certificate[] getCertificateChain ( String alias ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Get certificate chain for " + alias); //$NON-NLS-1$
        }
        String realAlias = alias;

        if ( realAlias != null && realAlias.startsWith("dynamic:") ) { //$NON-NLS-1$
            String[] parts = StringUtils.split(realAlias, ":", 3); //$NON-NLS-1$
            if ( parts.length == 3 ) {
                String handlerId = parts[ 1 ];
                String handlerArg = parts[ 2 ];
                SNIHandler handler = this.tlsConfig.lookupSNIHandler(handlerId);
                if ( handler != null ) {
                    X509Certificate[] x509CertificateChain = handler.getX509CertificateChain(handlerArg);
                    if ( x509CertificateChain != null ) {
                        return x509CertificateChain;
                    }
                }
            }

            // dynamic loopup failed
            realAlias = getFallbackAlias();
            if ( log.isDebugEnabled() ) {
                log.debug("Dynamic lookup failed, falling back to " + realAlias); //$NON-NLS-1$
            }
        }

        return super.getCertificateChain(realAlias);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.KeyManagerWrapper#getPrivateKey(java.lang.String)
     */
    @Override
    public PrivateKey getPrivateKey ( String alias ) {
        String realAlias = alias;
        if ( realAlias != null && realAlias.startsWith("dynamic:") ) { //$NON-NLS-1$
            String[] parts = StringUtils.split(realAlias, ":", 3); //$NON-NLS-1$
            if ( parts.length == 3 ) {
                String handlerId = parts[ 1 ];
                String handlerArg = parts[ 2 ];
                SNIHandler handler = this.tlsConfig.lookupSNIHandler(handlerId);
                if ( handler != null ) {
                    PrivateKey privateKey = handler.getPrivateKey(handlerArg);
                    if ( privateKey != null ) {
                        return privateKey;
                    }
                }
            }

            // dynamic loopup failed
            realAlias = getFallbackAlias();
            if ( log.isDebugEnabled() ) {
                log.debug("Dynamic lookup failed, falling back to " + realAlias); //$NON-NLS-1$
            }
        }

        PrivateKey k = super.getPrivateKey(realAlias);

        try {
            // mainly here to detect pkcs11 keys for which the token was removed
            if ( k != null ) {
                k.getAlgorithm();
            }
        }
        catch ( ProviderException e ) {
            log.warn("Key is no longer valid", e); //$NON-NLS-1$
            if ( clearCache() ) {
                return super.getPrivateKey(realAlias);
            }
        }

        return k;
    }


    /**
     * Clears the underlying KeyManagers cache
     * 
     * @return whether the cache was successfully cleared
     */
    public boolean clearCache () {

        X509ExtendedKeyManager delegate = getDelegate();

        while ( delegate instanceof KeyManagerWrapper ) {
            delegate = ( (KeyManagerWrapper) delegate ).getDelegate();
        }

        if ( KM_CLASS == null || delegate == null || !KM_CLASS.isAssignableFrom(delegate.getClass()) ) {
            log.error("Failed to find proper key manager delegate"); //$NON-NLS-1$
            return false;
        }

        try {
            Map<?, ?> cache = (Map<?, ?>) KM_CACHE_FIELD.get(delegate);
            cache.clear();
            return true;
        }
        catch (
            IllegalArgumentException |
            IllegalAccessException e ) {
            log.error("Failed to clear cache", e); //$NON-NLS-1$
            return false;
        }
    }

}
