/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.11.2014 by mbechler
 */
package eu.agno3.orchestrator.config.crypto.keystore;


import java.security.KeyPair;
import java.util.List;

import eu.agno3.orchestrator.types.entities.crypto.X509CertEntry;


/**
 * @author mbechler
 *
 */
public interface ImportKeyPairEntryMutable extends ImportKeyPairEntry {

    /**
     * @param certificateChain
     */
    void setCertificateChain ( List<X509CertEntry> certificateChain );


    /**
     * @param kp
     */
    void setKeyPair ( KeyPair kp );


    /**
     * @param alias
     */
    void setAlias ( String alias );

}
