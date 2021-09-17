/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.pkcs11;


import java.security.AuthProvider;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.spec.AlgorithmParameterSpec;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.keystore.KeyType;


/**
 * @author mbechler
 *
 */
public interface PKCS11Util {

    /**
     * @param libraryName
     * @param name
     * @param pin
     * @param slotIndex
     * @return a PKCS11 provider
     * @throws CryptoException
     */
    AuthProvider getProviderFor ( String libraryName, String name, String pin, int slotIndex ) throws CryptoException;


    /**
     * @param libraryName
     * @param name
     * @param pin
     * @param slotId
     * @return a PKCS11 provider
     * @throws CryptoException
     */
    AuthProvider getProviderFor ( String libraryName, String name, String pin, String slotId ) throws CryptoException;


    /**
     * @param library
     * @param id
     * @param pin
     * @param slotId
     * @param slotIndex
     * @param extraConfig
     * @param initArgs
     *            argument passed via pReserved
     * @return a PKCS11 provider
     * @throws CryptoException
     */
    AuthProvider getProviderFor ( String library, String id, String pin, String slotId, int slotIndex, String extraConfig, String initArgs )
            throws CryptoException;


    /**
     * @param provider
     * @throws CryptoException
     */
    void close ( AuthProvider provider ) throws CryptoException;


    /**
     * @param p
     * @param pin
     * @return an opened key store
     * @throws CryptoException
     */
    KeyStore getKeyStore ( AuthProvider p, String pin ) throws CryptoException;


    /**
     * Generates an RSA key
     * 
     * @param p
     * @param ks
     * @param alias
     * @param type
     * @return a key pair generated on the token, needs to be stored afterwards
     * @throws CryptoException
     */
    KeyPair prepareKeyPair ( AuthProvider p, KeyStore ks, String alias, KeyType type ) throws CryptoException;


    /**
     * @param p
     * @param ks
     * @param algo
     * @param alias
     * @param keyParams
     * @return a key pair generated on the token, needs to be stored afterwards
     * @throws CryptoException
     */
    KeyPair prepareKeyPair ( AuthProvider p, KeyStore ks, String algo, String alias, AlgorithmParameterSpec keyParams ) throws CryptoException;


    /**
     * @param p
     * @param ks
     * @param alias
     * @param kp
     * @param chain
     * @throws CryptoException
     */
    void storeKeyPair ( AuthProvider p, KeyStore ks, String alias, KeyPair kp, Certificate[] chain ) throws CryptoException;


    /**
     * @param p
     * @param ks
     * @param alias
     * @param kp
     * @param chain
     * @throws CryptoException
     */
    void importKeyPair ( AuthProvider p, KeyStore ks, String alias, KeyPair kp, Certificate[] chain ) throws CryptoException;


    /**
     * @param p
     * @param ks
     * @param alias
     * @param chain
     * @throws CryptoException
     */
    void updateCertificate ( AuthProvider p, KeyStore ks, String alias, Certificate[] chain ) throws CryptoException;


    /**
     * @param p
     * @param ks
     * @param alias
     * @throws CryptoException
     */
    void removeKey ( AuthProvider p, KeyStore ks, String alias ) throws CryptoException;


    /**
     * @param libraryName
     * @param name
     * @param pin
     * @return a PKCS11 provider
     * @throws CryptoException
     */
    AuthProvider getProviderFor ( String libraryName, String name, String pin ) throws CryptoException;


    /**
     * Removes all objects (potentially inaccessible through keystore) with id and label equal to alias
     * 
     * @param provider
     * @param ks
     * @param alias
     * @throws CryptoException
     */
    void cleanupAlias ( AuthProvider provider, KeyStore ks, String alias ) throws CryptoException;


    /**
     * @param p
     * @param alias
     * @return whether the key alias exists (but maybe the Java keystore cannot access it)
     * @throws CryptoException
     */
    boolean keyAliasExists ( AuthProvider p, String alias ) throws CryptoException;

}