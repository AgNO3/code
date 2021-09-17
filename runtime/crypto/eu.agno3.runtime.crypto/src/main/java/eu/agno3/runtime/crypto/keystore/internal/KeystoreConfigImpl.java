/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.keystore.internal;


import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.keystore.KeyStoreConfigUtil;
import eu.agno3.runtime.crypto.keystore.KeystoreConfig;
import eu.agno3.runtime.crypto.keystore.KeystoresConfig;
import eu.agno3.runtime.crypto.pkcs11.PKCS11TokenConfiguration;
import eu.agno3.runtime.crypto.pkcs11.PKCS11Util;
import eu.agno3.runtime.crypto.pkcs11.internal.PKCS11TokenConfigurationImpl;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    KeystoreConfig.class, PKCS11TokenConfiguration.class
}, configurationPid = KeystoreConfig.PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class KeystoreConfigImpl extends PKCS11TokenConfigurationImpl implements KeystoreConfig {

    private String keyStoreType;

    private KeystoresConfig ksConfig;


    @Override
    @Reference
    protected synchronized void setPKCS11Util ( PKCS11Util p11util ) {
        super.setPKCS11Util(p11util);
    }


    @Override
    protected synchronized void unsetPKCS11Util ( PKCS11Util p11util ) {
        super.unsetPKCS11Util(p11util);
    }


    @Reference
    protected synchronized void setKeystoresConfig ( KeystoresConfig ksconf ) {
        this.ksConfig = ksconf;
    }


    protected synchronized void unsetKeystoresConfig ( KeystoresConfig ksconf ) {
        if ( this.ksConfig == ksconf ) {
            this.ksConfig = null;
        }
    }


    @Override
    @Activate
    protected synchronized void activate ( ComponentContext ctx ) throws CryptoException, IOException {
        parseId(ctx);

        parsePropertyConfig(ctx, false);
        parseKeyStoreConfig();

        setupProvider();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.pkcs11.internal.PKCS11TokenConfigurationImpl#deactivate(org.osgi.service.component.ComponentContext)
     */
    @Override
    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        super.deactivate(ctx);
    }


    /**
     * @throws CryptoException
     * 
     */
    private void parseKeyStoreConfig () throws CryptoException {
        File keyStoreDir = new File(this.ksConfig.getKeystoreBaseDirectory(), this.getInstanceId());
        if ( !keyStoreDir.isDirectory() || !keyStoreDir.canRead() ) {
            throw new CryptoException("Key store does not exist or is inaccessible " + keyStoreDir); //$NON-NLS-1$
        }

        try {
            parseFiles(keyStoreDir);
            parseProperties(keyStoreDir);
        }
        catch ( CryptoException e ) {
            throw new CryptoException("Keystore is invalid " + keyStoreDir, e); //$NON-NLS-1$
        }

    }


    /**
     * @param pkcs11LibFile
     * @param typeFile
     * @param pinFile
     * @throws CryptoException
     */
    protected void parseFiles ( File keyStoreDir ) throws CryptoException {
        this.setLibrary(KeyStoreConfigUtil.getPKCS11Lib(keyStoreDir));
        this.setPIN(KeyStoreConfigUtil.getPIN(keyStoreDir));
        this.keyStoreType = KeyStoreConfigUtil.getKeyStoreType(keyStoreDir);
    }


    /**
     * @param propertiesFile
     * @throws CryptoException
     */
    protected void parseProperties ( File keyStoreDir ) throws CryptoException {

        Properties props = KeyStoreConfigUtil.getKeystoreProperties(keyStoreDir);
        this.setSlotId(KeyStoreConfigUtil.getSlotId(props));
        this.setSlotIndex(KeyStoreConfigUtil.getSlotIndex(props));

        this.setInitArgs(KeyStoreConfigUtil.getInitArgs(props));
        this.setExtraConfig(KeyStoreConfigUtil.getExtraConfig(props));
    }


    /**
     * @return the keyStoreType
     */
    @Override
    public String getKeyStoreType () {
        return this.keyStoreType;
    }

}
