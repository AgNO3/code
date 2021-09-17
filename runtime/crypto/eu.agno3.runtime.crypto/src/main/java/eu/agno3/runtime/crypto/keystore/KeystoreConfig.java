/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.keystore;


import eu.agno3.runtime.crypto.pkcs11.PKCS11TokenConfiguration;


/**
 * @author mbechler
 *
 */
public interface KeystoreConfig extends PKCS11TokenConfiguration {

    /**
     * 
     */
    static final String PID = "keystore"; //$NON-NLS-1$


    /**
     * @return the key store type
     */
    String getKeyStoreType ();

}
