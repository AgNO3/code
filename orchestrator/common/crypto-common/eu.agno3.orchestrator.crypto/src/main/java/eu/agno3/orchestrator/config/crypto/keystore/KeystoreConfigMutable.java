/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.11.2014 by mbechler
 */
package eu.agno3.orchestrator.config.crypto.keystore;


import java.util.Set;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( KeystoreConfig.class )
public interface KeystoreConfigMutable extends KeystoreConfig {

    /**
     * @param importKeyPairs
     */
    void setImportKeyPairs ( Set<ImportKeyPairEntry> importKeyPairs );


    /**
     * @param valodationTrustStore
     */
    void setValidationTrustStore ( String valodationTrustStore );


    /**
     * @param alias
     */
    void setAlias ( String alias );

}
