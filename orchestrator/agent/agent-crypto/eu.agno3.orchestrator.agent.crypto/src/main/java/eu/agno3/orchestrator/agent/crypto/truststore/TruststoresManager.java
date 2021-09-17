/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.11.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.truststore;


import java.io.File;
import java.security.KeyStore;
import java.util.List;

import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.runtime.crypto.tls.TrustConfiguration;


/**
 * @author mbechler
 *
 */
public interface TruststoresManager extends SystemService {

    /**
     * @return the known key stores
     */
    List<String> getTrustStores ();


    /**
     * @param name
     * @throws TruststoreManagerException
     */
    void createTrustStore ( String name ) throws TruststoreManagerException;


    /**
     * @param name
     * @throws TruststoreManagerException
     */
    void deleteTrustStore ( String name ) throws TruststoreManagerException;


    /**
     * @param name
     * @return the truststore manager
     * @throws TruststoreManagerException
     */
    TruststoreManager getTrustStoreManager ( String name ) throws TruststoreManagerException;


    /**
     * @param name
     * @return whether a truststore with the given name exists
     */
    boolean hasTrustStore ( String name );


    /**
     * @param name
     * @return the path to the truststore base
     */
    File getTrustStorePath ( String name );


    /**
     * @param name
     * @return whether the truststore is read only
     */
    boolean isReadOnly ( String name );


    /**
     * @param name
     * @return the truststore certificates as a keystore
     * @throws TruststoreManagerException
     */
    KeyStore getTrustStore ( String name ) throws TruststoreManagerException;


    /**
     * 
     * @param name
     * @param checkRevocation
     * @return the truststore configuration
     * @throws TruststoreManagerException
     */
    TrustConfiguration getTrustConfig ( String name, boolean checkRevocation ) throws TruststoreManagerException;

}
