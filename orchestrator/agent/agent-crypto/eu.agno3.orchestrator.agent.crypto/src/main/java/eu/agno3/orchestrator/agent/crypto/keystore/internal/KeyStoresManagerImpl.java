/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.11.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.keystore.internal;


import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.AuthProvider;
import java.security.ProviderException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.crypto.keystore.KeystoreManager;
import eu.agno3.orchestrator.agent.crypto.keystore.KeystoresManager;
import eu.agno3.orchestrator.agent.crypto.truststore.TruststoresManager;
import eu.agno3.orchestrator.crypto.keystore.KeystoreManagerException;
import eu.agno3.orchestrator.crypto.keystore.KeystoreNotFoundException;
import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.SystemServiceType;
import eu.agno3.orchestrator.system.base.service.Service;
import eu.agno3.orchestrator.system.base.service.ServiceException;
import eu.agno3.orchestrator.system.base.service.ServiceSystem;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.keystore.KeyStoreConfigUtil;
import eu.agno3.runtime.crypto.keystore.KeystoresConfig;
import eu.agno3.runtime.crypto.pkcs11.PKCS11Util;
import eu.agno3.runtime.crypto.x509.X509Util;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    KeystoresManager.class, SystemService.class
} )
@SystemServiceType ( KeystoresManager.class )
public class KeyStoresManagerImpl implements KeystoresManager {

    /**
     * 
     */
    private static final String KEYSTORE_SERVICE = "keystore-softhsm"; //$NON-NLS-1$
    private static final String KEYSTORE_INIT_SERVICE = "keystore-init-softhsm"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(KeyStoresManagerImpl.class);

    private static final Charset UTF8 = Charset.forName("UTF-8"); //$NON-NLS-1$

    private KeystoresConfig ksConfig;
    private ServiceSystem serviceSystem;
    private X509Util x509util;
    private PKCS11Util pkcs11util;

    private TruststoresManager trustManager;


    @Reference
    protected synchronized void setKeystoresConfig ( KeystoresConfig ksc ) {
        this.ksConfig = ksc;
    }


    protected synchronized void unsetKeystoresConfig ( KeystoresConfig ksc ) {
        if ( this.ksConfig == ksc ) {
            this.ksConfig = null;
        }
    }


    @Reference
    protected synchronized void setServiceSystem ( ServiceSystem ss ) {
        this.serviceSystem = ss;
    }


    protected synchronized void unsetServiceSystem ( ServiceSystem ss ) {
        if ( this.serviceSystem == ss ) {
            this.serviceSystem = null;
        }
    }


    @Reference
    protected synchronized void setX509Util ( X509Util x509 ) {
        this.x509util = x509;
    }


    protected synchronized void unsetX509Util ( X509Util x509 ) {
        if ( this.x509util == x509 ) {
            this.x509util = null;
        }
    }


    @Reference
    protected synchronized void setPKCS11Util ( PKCS11Util p11util ) {
        this.pkcs11util = p11util;
    }


    protected synchronized void unsetPKCS11Util ( PKCS11Util p11util ) {
        if ( this.pkcs11util == p11util ) {
            this.pkcs11util = null;
        }
    }


    @Reference
    protected synchronized void setTruststoreManager ( TruststoresManager tms ) {
        this.trustManager = tms;
    }


    protected synchronized void unsetTruststoreManager ( TruststoresManager tms ) {
        if ( this.trustManager == tms ) {
            this.trustManager = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.keystore.KeystoresManager#getKeyStores()
     */
    @Override
    public List<String> getKeyStores () {
        List<String> keyStoreNames = new LinkedList<>();
        File[] keystoreDirs = this.ksConfig.getKeystoreBaseDirectory().listFiles(new FileFilter() {

            @Override
            public boolean accept ( File pathname ) {
                File typeFile = new File(pathname, "type"); //$NON-NLS-1$
                return pathname.isDirectory() && typeFile.canRead();
            }
        });

        if ( keystoreDirs == null ) {
            return keyStoreNames;
        }

        for ( File ksDir : keystoreDirs ) {
            keyStoreNames.add(ksDir.getName());
        }

        return keyStoreNames;
    }


    /**
     * @param name
     * @return the path to the keystore
     * @throws KeystoreManagerException
     */
    @Override
    public File getKeystorePath ( String name ) throws KeystoreManagerException {
        File f = new File(this.ksConfig.getKeystoreBaseDirectory(), name);

        if ( !f.isDirectory() || !f.canRead() ) {
            throw new KeystoreNotFoundException("Keystore not found " + name); //$NON-NLS-1$
        }

        return f;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.agent.crypto.keystore.KeystoresManager#hasKeyStore(java.lang.String)
     */
    @Override
    public boolean hasKeyStore ( String name ) {
        File f = new File(this.ksConfig.getKeystoreBaseDirectory(), name);
        return f.exists() && f.isDirectory() && f.canRead();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.keystore.KeystoresManager#createKeyStore(java.lang.String, boolean)
     */
    @Override
    public void createKeyStore ( String name, boolean internal ) throws KeystoreManagerException {
        try {
            Service keyStoreInitService = this.serviceSystem.createInstance(KEYSTORE_INIT_SERVICE, name);
            keyStoreInitService.start();
            Service keyStoreService = this.serviceSystem.createInstance(KEYSTORE_SERVICE, name);
            keyStoreService.start();
            keyStoreService.enableOnBoot();

            if ( internal ) {
                setInternal(name);
            }
        }
        catch ( ServiceException e ) {
            throw new KeystoreManagerException("Failed to create keystore service", e); //$NON-NLS-1$
        }

    }


    /**
     * @param name
     * @throws KeystoreManagerException
     */
    private void setInternal ( String name ) throws KeystoreManagerException {
        File internalFlag = new File(this.getKeystorePath(name), KeystoresManager.INTERNAL_TRUSTSTORE_FILE);
        try {
            Files.write(internalFlag.toPath(), new byte[0], StandardOpenOption.CREATE);
        }
        catch ( IOException e ) {
            log.error("Failed to set internal flag", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.keystore.KeystoresManager#deleteKeyStore(java.lang.String)
     */
    @Override
    public void deleteKeyStore ( String name ) throws KeystoreManagerException {
        try {
            Service keyStoreService = this.serviceSystem.getService(KEYSTORE_SERVICE, name);
            keyStoreService.stop();
            keyStoreService.disableOnBoot();
        }
        catch ( ServiceException e ) {
            throw new KeystoreManagerException("Failed to stop keystore service", e); //$NON-NLS-1$
        }

        File f = this.getKeystorePath(name);
        try {
            FileUtils.deleteDirectory(f);
        }
        catch ( IOException e ) {
            throw new KeystoreManagerException("Failed to remove keystore directory", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.keystore.KeystoresManager#getValidationTruststoreName(java.lang.String)
     */
    @Override
    public String getValidationTruststoreName ( String name ) throws KeystoreManagerException {
        File validationTruststoreFile = new File(this.getKeystorePath(name), KeystoresManager.VALIDATION_TRUSTSTORE_FILE);
        if ( !validationTruststoreFile.exists() || !validationTruststoreFile.canRead() ) {
            return null;
        }

        try {
            return new String(Files.readAllBytes(validationTruststoreFile.toPath()), UTF8);
        }
        catch ( IOException e ) {
            throw new KeystoreManagerException("Failed to read validation truststore file", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.keystore.KeystoresManager#isInternalKeyStore(java.lang.String)
     */
    @Override
    public boolean isInternalKeyStore ( String name ) throws KeystoreManagerException {
        File internalFlag = new File(this.getKeystorePath(name), KeystoresManager.INTERNAL_TRUSTSTORE_FILE);
        if ( !internalFlag.exists() || !internalFlag.canRead() ) {
            return false;
        }

        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.crypto.keystore.KeystoresManager#getKeyStoreManager(java.lang.String)
     */
    @Override
    public KeystoreManager getKeyStoreManager ( String name ) throws KeystoreManagerException {
        File keystorePath = this.getKeystorePath(name);

        try {
            Properties keystoreProps = KeyStoreConfigUtil.getKeystoreProperties(keystorePath);
            String pin = KeyStoreConfigUtil.getPIN(keystorePath);
            if ( log.isDebugEnabled() ) {
                log.debug("Initializing provider for " + name); //$NON-NLS-1$
            }
            AuthProvider provider = this.pkcs11util.getProviderFor(
                KeyStoreConfigUtil.getPKCS11Lib(keystorePath),
                name,
                pin,
                KeyStoreConfigUtil.getSlotId(keystoreProps),
                KeyStoreConfigUtil.getSlotIndex(keystoreProps),
                KeyStoreConfigUtil.getExtraConfig(keystoreProps),
                KeyStoreConfigUtil.getInitArgs(keystoreProps));

            if ( log.isDebugEnabled() ) {
                log.debug("Initialized provider for " + name); //$NON-NLS-1$
            }
            return new PKCS11Keystore(keystorePath, provider, pin, this.pkcs11util, this.x509util);
        }
        catch ( ProviderException e ) {
            throw new KeystoreManagerException("Failed to load keystore", e); //$NON-NLS-1$
        }
        catch ( CryptoException e ) {
            throw new KeystoreManagerException("Failed to load keystore configuration", e); //$NON-NLS-1$
        }
    }
}
