/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.pkcs11.internal;


import java.io.IOException;
import java.security.AuthProvider;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStore.Builder;
import java.security.KeyStoreException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.KeyStoreBuilderParameters;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.log4j.Logger;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.pkcs11.PKCS11TokenConfiguration;
import eu.agno3.runtime.crypto.pkcs11.PKCS11Util;
import eu.agno3.runtime.crypto.tls.KeyStoreConfiguration;


/**
 * @author mbechler
 *
 */
public class PKCS11KeyStoreConfiguration implements KeyStoreConfiguration, CallbackHandler {

    private static final Logger log = Logger.getLogger(PKCS11KeyStoreConfiguration.class);

    private static final String SUN_JSSE = "SunJSSE"; //$NON-NLS-1$
    private static final String PKIX = "PKIX"; //$NON-NLS-1$

    private KeyStore keyStore;

    private final String id;
    private final String pin;
    private final AuthProvider provider;
    private final PKCS11Util pkcs11Util;
    private final PKCS11TokenConfiguration tokenConfig;


    /**
     * @param tokenConfig
     * @param pkcs11Util
     * @throws CryptoException
     * 
     */
    public PKCS11KeyStoreConfiguration ( PKCS11TokenConfiguration tokenConfig, PKCS11Util pkcs11Util ) throws CryptoException {
        this.tokenConfig = tokenConfig;
        this.pkcs11Util = pkcs11Util;
        this.id = tokenConfig.getInstanceId();
        this.pin = tokenConfig.getPIN();
        AuthProvider prov = tokenConfig.getProvider();
        if ( prov == null ) {
            throw new CryptoException("Failed to create PKCS11 provider"); //$NON-NLS-1$
        }
        this.provider = prov;

        loadKeyStore();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.KeyStoreConfiguration#reloadKeyStore()
     */
    @Override
    public KeyStore reloadKeyStore () throws CryptoException {
        return loadKeyStore();
    }


    /**
     * @param tokenConfig
     * @param prov
     * @throws CryptoException
     */
    private synchronized KeyStore loadKeyStore () throws CryptoException {
        KeyStore ks;
        try {
            ks = this.pkcs11Util.getKeyStore(this.provider, this.tokenConfig.getPIN());

            if ( !ks.aliases().hasMoreElements() ) {
                log.warn(String.format("PKCS11 token %s contains no keys", this.tokenConfig.getInstanceId())); //$NON-NLS-1$
            }
        }
        catch ( KeyStoreException e ) {
            log.error("Failed to get key store from token " + this.tokenConfig.getInstanceId(), e); //$NON-NLS-1$
            throw new CryptoException("Keystore initialization failed " + this.tokenConfig.getInstanceId(), e); //$NON-NLS-1$
        }
        this.keyStore = ks;
        return ks;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.KeyStoreConfiguration#getKeyManagerFactory()
     */
    @Override
    public KeyManagerFactory getKeyManagerFactory () throws CryptoException {
        try {
            Builder builder = Builder.newInstance("PKCS11", this.provider, new KeyStore.CallbackHandlerProtection(this)); //$NON-NLS-1$
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(PKIX, SUN_JSSE);
            kmf.init(new KeyStoreBuilderParameters(builder));
            return kmf;
        }
        catch ( GeneralSecurityException e ) {
            throw new CryptoException("Failed to initialize PKCS11 keytore", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
     */
    @Override
    public void handle ( Callback[] callbacks ) throws IOException, UnsupportedCallbackException {
        log.debug("Reauthentication required"); //$NON-NLS-1$
        if ( this.pin == null ) {
            return;
        }
        for ( Callback cb : callbacks ) {
            if ( cb instanceof PasswordCallback ) {
                ( (PasswordCallback) cb ).setPassword(this.pin.toCharArray());
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.KeyStoreConfiguration#getKeyStore()
     */
    @Override
    public KeyStore getKeyStore () {
        return this.keyStore;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.KeyStoreConfiguration#getKeyStorePassword()
     */
    @Override
    public String getKeyStorePassword () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.KeyStoreConfiguration#getId()
     */
    @Override
    public String getId () {
        return this.id;
    }

}
