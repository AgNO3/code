/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.tls;


import java.net.Socket;
import java.security.Principal;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;


/**
 * @author mbechler
 *
 */
public class AliasSelectingKeyManager extends KeyManagerWrapper {

    /**
     * 
     */
    private static final String ALIAS_PREFIX = ".0."; //$NON-NLS-1$

    private String keyAlias;


    /**
     * @param keyAlias
     * @param keyManager
     */
    public AliasSelectingKeyManager ( String keyAlias, X509ExtendedKeyManager keyManager ) {
        super(keyManager);
        // prefixing is required, format .<builderIndex>.alias
        // where we only use one builder
        this.keyAlias = ALIAS_PREFIX + keyAlias;
    }


    /**
     * 
     * @return actual key alias used
     */
    public String getKeyAlias () {
        return this.keyAlias;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.net.ssl.X509ExtendedKeyManager#chooseEngineClientAlias(java.lang.String[], java.security.Principal[],
     *      javax.net.ssl.SSLEngine)
     */
    @Override
    public String chooseEngineClientAlias ( String[] keyType, Principal[] issuers, SSLEngine engine ) {
        return this.keyAlias;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.net.ssl.X509ExtendedKeyManager#chooseEngineServerAlias(java.lang.String, java.security.Principal[],
     *      javax.net.ssl.SSLEngine)
     */
    @Override
    public String chooseEngineServerAlias ( String keyType, Principal[] issuers, SSLEngine engine ) {
        return this.keyAlias;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.net.ssl.X509KeyManager#chooseClientAlias(java.lang.String[], java.security.Principal[],
     *      java.net.Socket)
     */
    @Override
    public String chooseClientAlias ( String[] keyType, Principal[] issuers, Socket socket ) {
        return this.keyAlias;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.net.ssl.X509KeyManager#chooseServerAlias(java.lang.String, java.security.Principal[], java.net.Socket)
     */
    @Override
    public String chooseServerAlias ( String keyType, Principal[] issuers, Socket socket ) {
        return this.keyAlias;
    }

}
