/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.keystore.internal;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.KeyStoreConfiguration;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = KeyStoreConfiguration.class, configurationPolicy = ConfigurationPolicy.REQUIRE, configurationPid = KeyStoreConfiguration.PID )
public class KeyStoreConfigurationImpl implements KeyStoreConfiguration {

    private String id;
    private KeyStore ks;
    private String keyStorePass;
    private String keyStorePath;

    private static final String SUN_JSSE = "SunJSSE"; //$NON-NLS-1$
    private static final String PKIX = "PKIX"; //$NON-NLS-1$


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) throws CryptoException, IOException {
        String idSpec = (String) ctx.getProperties().get(KeyStoreConfiguration.ID);
        if ( StringUtils.isBlank(idSpec) ) {
            throw new CryptoException("No key instanceId given"); //$NON-NLS-1$
        }
        this.id = idSpec.trim();
        String keyStoreSpec = (String) ctx.getProperties().get(KeyStoreConfiguration.STORE);
        if ( StringUtils.isBlank(keyStoreSpec) ) {
            throw new CryptoException("No key store given for " + this.id); //$NON-NLS-1$
        }
        this.keyStorePath = keyStoreSpec;

        String keyStorePassSpec = (String) ctx.getProperties().get(KeyStoreConfiguration.STOREPASS);
        if ( StringUtils.isBlank(keyStorePassSpec) ) {
            throw new CryptoException("No key store password given for " + this.id); //$NON-NLS-1$
        }
        this.keyStorePass = ConfigUtil.parseSecret(ctx.getProperties(), KeyStoreConfiguration.STOREPASS, null);
        loadKeyStore(keyStoreSpec, keyStorePassSpec);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws CryptoException
     *
     * @see eu.agno3.runtime.crypto.tls.KeyStoreConfiguration#getKeyManagerFactory()
     */
    @Override
    public KeyManagerFactory getKeyManagerFactory () throws CryptoException {
        try {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(PKIX, SUN_JSSE);

            String keyStorePassword = this.getKeyStorePassword();
            char[] charPassword = null;
            if ( keyStorePassword != null ) {
                charPassword = keyStorePassword.toCharArray();
            }

            KeyStore keyStore = this.getKeyStore();
            kmf.init(keyStore, charPassword);
            return kmf;
        }
        catch (
            KeyStoreException |
            NoSuchAlgorithmException |
            NoSuchProviderException |
            UnrecoverableKeyException e ) {
            throw new CryptoException("Failed to initialize key store", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws CryptoException
     *
     * @see eu.agno3.runtime.crypto.tls.KeyStoreConfiguration#reloadKeyStore()
     */
    @Override
    public KeyStore reloadKeyStore () throws CryptoException {
        return loadKeyStore(this.keyStorePath, this.keyStorePass);
    }


    /**
     * @param keyStoreSpec
     * @param keyStorePassSpec
     * @return
     * @throws CryptoException
     */
    protected synchronized KeyStore loadKeyStore ( String keyStoreSpec, String keyStorePassSpec ) throws CryptoException {
        File ksFile = new File(keyStoreSpec.trim());

        try ( InputStream is = new FileInputStream(ksFile) ) {
            KeyStore rks = KeyStore.getInstance("JKS"); //$NON-NLS-1$

            rks = KeyStore.getInstance("JKS"); //$NON-NLS-1$
            rks.load(is, keyStorePassSpec.trim().toCharArray());
            this.ks = rks;
            return rks;
        }
        catch (
            IOException |
            NoSuchAlgorithmException |
            CertificateException |
            KeyStoreException e ) {
            throw new CryptoException("Failed to load key store", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.KeyStoreConfiguration#getKeyStore()
     */
    @Override
    public KeyStore getKeyStore () {
        return this.ks;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.KeyStoreConfiguration#getKeyStorePassword()
     */
    @Override
    public String getKeyStorePassword () {
        return this.keyStorePass;
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
