/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedKeyManager;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.InternalTLSConfiguration;
import eu.agno3.runtime.crypto.tls.TLSConfiguration;
import eu.agno3.runtime.crypto.tls.TLSContext;


/**
 * @author mbechler
 *
 */
public class TLSContextImpl implements TLSContext {

    private SecureRandom secureRandom;
    private InternalTLSConfiguration config;
    private KeyManager[] keyManagers;
    private TrustManager[] trustManagers;
    private HostnameVerifier hostnameVerifier;
    private SSLContext context;
    private SSLSocketFactory socketFactory;
    private SSLServerSocketFactory serverSocketFactory;
    private SSLContextWrapper contextWrapper;
    private TLSParameterFactory tlsParameterFactory;


    /**
     * @param tlsContextFactory
     * @param cfg
     * @param randomSource
     * @throws CryptoException
     * 
     */
    public TLSContextImpl ( TLSContextFactory tlsContextFactory, InternalTLSConfiguration cfg, SecureRandom randomSource ) throws CryptoException {
        init(tlsContextFactory, cfg, randomSource);
    }


    /**
     * @param tlsContextFactory
     * @param cfg
     * @throws CryptoException
     */
    protected final synchronized void init ( TLSContextFactory tlsContextFactory, InternalTLSConfiguration cfg, SecureRandom randomSource )
            throws CryptoException {
        this.secureRandom = randomSource;
        this.config = cfg;
        this.keyManagers = tlsContextFactory.getKeyManagers(cfg);
        this.trustManagers = tlsContextFactory.getTrustManagers(cfg);
        this.hostnameVerifier = tlsContextFactory.getHostnameVerifier(cfg);
        this.context = tlsContextFactory.getContext(cfg, randomSource);
        this.socketFactory = tlsContextFactory.getSocketFactory(cfg, randomSource);
        this.serverSocketFactory = tlsContextFactory.getServerSocketFactory(cfg, randomSource);
        this.tlsParameterFactory = tlsContextFactory.getParameterFactory(cfg);
        this.contextWrapper = new SSLContextWrapper(cfg, this.tlsParameterFactory, this.context, this.socketFactory, this.serverSocketFactory);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSContext#getContext()
     */
    @Override
    public synchronized SSLContext getContext () throws CryptoException {
        return this.contextWrapper;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSContext#getSocketFactory()
     */
    @Override
    public synchronized SSLSocketFactory getSocketFactory () throws CryptoException {
        return this.socketFactory;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSContext#getServerSocketFactory()
     */
    @Override
    public SSLServerSocketFactory getServerSocketFactory () throws CryptoException {
        return this.serverSocketFactory;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSContext#createSSLEngine()
     */
    @Override
    public SSLEngine createSSLEngine () throws CryptoException {
        return setupSSLEngine(this.context.createSSLEngine());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSContext#createSSLEngine(java.lang.String, int)
     */
    @Override
    public SSLEngine createSSLEngine ( String peerHost, int peerPort ) throws CryptoException {
        return setupSSLEngine(this.context.createSSLEngine(peerHost, peerPort));
    }


    /**
     * @param createSSLEngine
     * @return
     * @throws CryptoException
     */
    private SSLEngine setupSSLEngine ( SSLEngine engine ) throws CryptoException {
        engine.setSSLParameters(
            this.tlsParameterFactory.makeSSLParameters(this.config, engine.getSupportedCipherSuites(), engine.getSupportedProtocols()));
        return engine;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSContext#getHostnameVerifier()
     */
    @Override
    public synchronized HostnameVerifier getHostnameVerifier () throws CryptoException {
        return this.hostnameVerifier;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSContext#getConfig()
     */
    @Override
    public synchronized TLSConfiguration getConfig () throws CryptoException {
        return this.config;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSContext#getRandomSource()
     */
    @Override
    public synchronized SecureRandom getRandomSource () {
        return this.secureRandom;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSContext#getTrustManagers()
     */
    @Override
    public synchronized TrustManager[] getTrustManagers () throws CryptoException {
        if ( this.trustManagers != null ) {
            return Arrays.copyOf(this.trustManagers, this.trustManagers.length);
        }
        return new TrustManager[] {};
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSContext#getKeyManagers()
     */
    @Override
    public synchronized KeyManager[] getKeyManagers () throws CryptoException {
        if ( this.keyManagers != null ) {
            return Arrays.copyOf(this.keyManagers, this.keyManagers.length);
        }
        return new KeyManager[] {};
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.TLSContext#getPrimaryCertificatePubKey()
     */
    @Override
    public PublicKey getPrimaryCertificatePubKey () {
        if ( this.keyManagers == null || this.keyManagers.length < 1 || ! ( this.keyManagers[ 0 ] instanceof X509ExtendedKeyManager ) ) {
            return null;
        }

        X509ExtendedKeyManager km = (X509ExtendedKeyManager) this.keyManagers[ 0 ];

        String serverAlias = km.chooseServerAlias("RSA", null, null); //$NON-NLS-1$
        if ( serverAlias == null ) {
            return null;
        }

        X509Certificate[] chain = km.getCertificateChain(serverAlias);
        if ( chain == null || chain.length < 1 ) {
            return null;
        }

        return chain[ 0 ].getPublicKey();
    }
}
