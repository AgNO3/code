/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.11.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.keystore;


import java.io.File;
import java.util.List;

import eu.agno3.orchestrator.crypto.keystore.KeystoreManagerException;
import eu.agno3.orchestrator.system.base.SystemService;


/**
 * @author mbechler
 *
 */
public interface KeystoresManager extends SystemService {

    /**
     * 
     */
    public static final String VALIDATION_TRUSTSTORE_FILE = "validation.truststore"; //$NON-NLS-1$
    /**
     * 
     */
    public static final String INTERNAL_TRUSTSTORE_FILE = ".internal"; //$NON-NLS-1$


    /**
     * @return the known key stores
     */
    List<String> getKeyStores ();


    /**
     * @param name
     * @param internal
     * @throws KeystoreManagerException
     */
    void createKeyStore ( String name, boolean internal ) throws KeystoreManagerException;


    /**
     * @param name
     * @throws KeystoreManagerException
     */
    void deleteKeyStore ( String name ) throws KeystoreManagerException;


    /**
     * @param name
     * @return the keystore manager
     * @throws KeystoreManagerException
     */
    KeystoreManager getKeyStoreManager ( String name ) throws KeystoreManagerException;


    /**
     * @param name
     * @return whether a keystore with the given name exists
     */
    boolean hasKeyStore ( String name );


    /**
     * @param name
     * @return the path to the keystore
     * @throws KeystoreManagerException
     */
    File getKeystorePath ( String name ) throws KeystoreManagerException;


    /**
     * @param name
     * @return the validation truststore name
     * @throws KeystoreManagerException
     */
    String getValidationTruststoreName ( String name ) throws KeystoreManagerException;


    /**
     * @param name
     * @return whether this is a internal key store
     * @throws KeystoreManagerException
     */
    boolean isInternalKeyStore ( String name ) throws KeystoreManagerException;

}