/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 19, 2017 by mbechler
 */
package eu.agno3.runtime.crypto.keystore.internal;


import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.openssl.PEMLoader;
import eu.agno3.runtime.crypto.tls.KeyStoreConfiguration;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */

@Component ( service = KeyStoreConfiguration.class, configurationPolicy = ConfigurationPolicy.REQUIRE, configurationPid = "x509.pem.key" )
public class PEMKeystoreConfigurationImpl implements KeyStoreConfiguration {

    private static final Logger log = Logger.getLogger(PEMKeystoreConfigurationImpl.class);

    private String id;
    private String alias = "key"; //$NON-NLS-1$
    private String password;

    private KeyStore ks;

    private Path keyFilePath;
    private List<Path> certsFilePath;

    private static final String SUN_JSSE = "SunJSSE"; //$NON-NLS-1$
    private static final String PKIX = "PKIX"; //$NON-NLS-1$


    /**
     * 
     */
    public PEMKeystoreConfigurationImpl () {}


    /**
     * 
     * @param id
     * @param keyFilePath
     * @param certsFilePath
     * @param password
     * @throws CryptoException
     */
    public PEMKeystoreConfigurationImpl ( String id, Path keyFilePath, List<Path> certsFilePath, String password ) throws CryptoException {
        this.id = id;
        this.keyFilePath = keyFilePath;
        this.certsFilePath = certsFilePath;
        this.password = password;
        loadKeyStore();
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) throws CryptoException, IOException {
        String idSpec = (String) ctx.getProperties().get(KeyStoreConfiguration.ID);
        if ( StringUtils.isBlank(idSpec) ) {
            throw new CryptoException("No key instanceId given"); //$NON-NLS-1$
        }
        this.id = idSpec.trim();

        String keyFileSpec = (String) ctx.getProperties().get(KeyStoreConfiguration.STORE);
        if ( StringUtils.isBlank(keyFileSpec) ) {
            throw new CryptoException("No key store given for " + this.id); //$NON-NLS-1$
        }
        this.keyFilePath = Paths.get(keyFileSpec.trim());

        this.password = ConfigUtil.parseSecret(ctx.getProperties(), "keyPassword", null); //$NON-NLS-1$

        Collection<String> certPaths = ConfigUtil.parseStringCollection(ctx.getProperties(), "certs", null); //$NON-NLS-1$
        if ( certPaths != null ) {
            List<Path> paths = new ArrayList<>();
            for ( String certPath : certPaths ) {
                paths.add(Paths.get(certPath.trim()));
            }
            this.certsFilePath = paths;
        }

        loadKeyStore();
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
            KeyStore keyStore = getKeyStore();
            kmf.init(keyStore, new char[0]);
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
        return loadKeyStore();
    }


    /**
     * @param keyStoreSpec
     * @param keyStorePassSpec
     * @return
     * @throws CryptoException
     */
    protected synchronized KeyStore loadKeyStore () throws CryptoException {

        try {
            KeyStore rks = KeyStore.getInstance("JKS"); //$NON-NLS-1$
            rks.load(null, null);
            PrivateKey pk;
            List<? extends Certificate> chain = null;
            try ( FileChannel keys = FileChannel.open(this.keyFilePath) ) {
                PEMLoader pem = PEMLoader.load(keys, this.password);

                pk = pem.getPrivateKey();
                if ( this.certsFilePath == null || this.certsFilePath.isEmpty() ) {
                    chain = pem.getCertificates();
                }
            }

            if ( this.certsFilePath != null && !this.certsFilePath.isEmpty() ) {
                for ( Path certPath : this.certsFilePath ) {
                    try ( FileChannel certs = FileChannel.open(certPath) ) {
                        PEMLoader pem = PEMLoader.load(certs, null);
                        chain = pem.getCertificates();
                    }
                }
            }

            if ( pk == null || chain == null || chain.isEmpty() ) {
                log.error("No private key or certificate found in files"); //$NON-NLS-1$
                return rks;
            }

            rks.setKeyEntry(this.alias, pk, new char[0], chain.toArray(new Certificate[0]));
            this.ks = rks;
            return rks;
        }
        catch (
            IOException |
            CertificateException |
            KeyStoreException |
            NoSuchAlgorithmException e ) {
            throw new CryptoException("Failed to load key store " + this.keyFilePath, e); //$NON-NLS-1$
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
        return this.password;
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
