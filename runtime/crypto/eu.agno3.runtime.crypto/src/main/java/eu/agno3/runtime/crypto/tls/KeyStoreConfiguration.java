/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.tls;


import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;

import eu.agno3.runtime.crypto.CryptoException;


/**
 * @author mbechler
 *
 */
public interface KeyStoreConfiguration {

    /**
     * 
     */
    public static final String PID = "x509.key"; //$NON-NLS-1$
    /**
     * 
     */
    public static final String ID = "instanceId"; //$NON-NLS-1$
    /**
     * 
     */
    public static final String STORE = "store"; //$NON-NLS-1$
    /**
     * 
     */
    public static final String STOREPASS = "storePass"; //$NON-NLS-1$


    /**
     * 
     * @return the key store
     */
    KeyStore getKeyStore ();


    /**
     * 
     * @return refreshed key store
     * @throws CryptoException
     */
    KeyStore reloadKeyStore () throws CryptoException;


    /**
     * @return the key manager factory
     * @throws CryptoException
     */
    KeyManagerFactory getKeyManagerFactory () throws CryptoException;


    /**
     * 
     * @return the key store password
     */
    String getKeyStorePassword ();


    /**
     * @return an unique identifier
     */
    String getId ();

}
