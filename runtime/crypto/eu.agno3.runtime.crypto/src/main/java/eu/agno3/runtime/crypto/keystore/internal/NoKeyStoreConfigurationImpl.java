/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.02.2016 by mbechler
 */
package eu.agno3.runtime.crypto.keystore.internal;


import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;

import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.KeyStoreConfiguration;


/**
 * @author mbechler
 *
 */
@Component ( service = KeyStoreConfiguration.class, property = "instanceId=noKey" )
public class NoKeyStoreConfigurationImpl implements KeyStoreConfiguration {

    /**
     * 
     */
    private static final NoKeyManagerFactory NO_KEY_MANAGER_FACTORY = new NoKeyManagerFactory();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.KeyStoreConfiguration#getKeyStore()
     */
    @Override
    public KeyStore getKeyStore () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.KeyStoreConfiguration#reloadKeyStore()
     */
    @Override
    public KeyStore reloadKeyStore () throws CryptoException {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.KeyStoreConfiguration#getKeyManagerFactory()
     */
    @Override
    public KeyManagerFactory getKeyManagerFactory () throws CryptoException {
        return NO_KEY_MANAGER_FACTORY;
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
        return "noKey"; //$NON-NLS-1$
    }

}
