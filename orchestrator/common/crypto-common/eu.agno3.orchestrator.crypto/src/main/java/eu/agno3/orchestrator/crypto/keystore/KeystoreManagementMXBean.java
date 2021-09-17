/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.04.2015 by mbechler
 */
package eu.agno3.orchestrator.crypto.keystore;


import java.util.List;


/**
 * @author mbechler
 *
 */
public interface KeystoreManagementMXBean {

    /**
     * 
     * @param includeInternal
     *            whether to include internal keystoresd
     * @return the keystores known to the system
     * @throws KeystoreManagerException
     */
    List<KeyStoreInfo> getKeystores ( boolean includeInternal ) throws KeystoreManagerException;


    /**
     * @param keystore
     * @return the keystore info
     * @throws KeystoreManagerException
     */
    KeyStoreInfo getKeyStoreInfo ( String keystore ) throws KeystoreManagerException;


    /**
     * @return the known keystore aliases
     * @throws KeystoreManagerException
     */
    List<String> getKeystoreAliases () throws KeystoreManagerException;


    /**
     * @param keystore
     * @param keyAlias
     * @param keyType
     * @param algo
     * @param keySize
     * @throws KeystoreManagerException
     */
    void generateKey ( String keystore, String keyAlias, String keyType ) throws KeystoreManagerException;


    /**
     * @param keystore
     * @param keyAlias
     * @param keyType
     * @param keyData
     * @param importChain
     * @throws KeystoreManagerException
     */
    void importKey ( String keystore, String keyAlias, String keyType, String keyData, List<String> importChain ) throws KeystoreManagerException;


    /**
     * @param keystore
     * @param keyAlias
     * @param certs
     * @throws KeystoreManagerException
     */
    void updateChain ( String keystore, String keyAlias, List<String> certs ) throws KeystoreManagerException;


    /**
     * @param keystore
     * @param keyAlias
     * @param req
     * @return the generated CSR
     * @throws KeystoreManagerException
     */
    String generateCSR ( String keystore, String keyAlias, CertRequestData req ) throws KeystoreManagerException;


    /**
     * @param keystore
     * @param keyAlias
     * @param requestPassword
     * @return the generated CSR
     * @throws KeystoreManagerException
     */
    String generateRenewalCSR ( String keystore, String keyAlias, String requestPassword ) throws KeystoreManagerException;


    /**
     * @param keystore
     * @param keyAlias
     * @throws KeystoreManagerException
     */
    void deleteKey ( String keystore, String keyAlias ) throws KeystoreManagerException;


    /**
     * @param keystore
     * @param keyAlias
     * @param req
     * @throws KeystoreManagerException
     */
    void makeSelfSignedCert ( String keystore, String keyAlias, CertRequestData req ) throws KeystoreManagerException;


    /**
     * @param keystore
     * @param keyAlias
     * @return the key info
     * @throws KeystoreManagerException
     */
    KeyInfo getKeyInfo ( String keystore, String keyAlias ) throws KeystoreManagerException;


    /**
     * @param keystore
     * @param checkRevocation
     * @param chain
     * @return whether the certificate validated or not
     * @throws KeystoreManagerException
     */
    String validateChain ( String keystore, boolean checkRevocation, List<String> chain ) throws KeystoreManagerException;

}
