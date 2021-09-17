/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.12.2015 by mbechler
 */
package eu.agno3.runtime.crypto.tls;


import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public class KeyManagerWrapper extends X509ExtendedKeyManager implements KeyManager, X509KeyManager {

    private static final Logger log = Logger.getLogger(KeyManagerWrapper.class);
    private X509ExtendedKeyManager delegate;


    /**
     * @param delegate
     * 
     */
    public KeyManagerWrapper ( X509ExtendedKeyManager delegate ) {
        this.delegate = delegate;
    }


    /**
     * @return the delegate
     */
    public X509ExtendedKeyManager getDelegate () {
        return this.delegate;
    }


    @Override
    public int hashCode () {
        return this.delegate.hashCode();
    }


    @Override
    public String chooseEngineClientAlias ( String[] keyType, Principal[] issuers, SSLEngine engine ) {
        return this.delegate.chooseEngineClientAlias(keyType, issuers, engine);
    }


    @Override
    public String[] getClientAliases ( String keyType, Principal[] issuers ) {
        return this.delegate.getClientAliases(keyType, issuers);
    }


    @Override
    public String chooseClientAlias ( String[] keyType, Principal[] issuers, Socket socket ) {
        return this.delegate.chooseClientAlias(keyType, issuers, socket);
    }


    @Override
    public String chooseEngineServerAlias ( String keyType, Principal[] issuers, SSLEngine engine ) {
        return this.delegate.chooseEngineServerAlias(keyType, issuers, engine);
    }


    @Override
    public boolean equals ( Object obj ) {
        return this.delegate.equals(obj);
    }


    @Override
    public String[] getServerAliases ( String keyType, Principal[] issuers ) {
        return this.delegate.getServerAliases(keyType, issuers);
    }


    @Override
    public String chooseServerAlias ( String keyType, Principal[] issuers, Socket socket ) {
        return this.delegate.chooseServerAlias(keyType, issuers, socket);
    }


    @Override
    public X509Certificate[] getCertificateChain ( String alias ) {
        return this.delegate.getCertificateChain(alias);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.net.ssl.X509KeyManager#getPrivateKey(java.lang.String)
     */
    @Override
    public PrivateKey getPrivateKey ( String alias ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Requested private key " + alias); //$NON-NLS-1$
        }
        PrivateKey k = this.delegate.getPrivateKey(alias);

        if ( k == null ) {
            log.warn(String.format("Private key with alias %s not found", alias)); //$NON-NLS-1$
        }

        return k;
    }


    @Override
    public String toString () {
        return this.delegate.toString();
    }

}
