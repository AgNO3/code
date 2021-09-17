/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.11.2014 by mbechler
 */
package eu.agno3.orchestrator.config.crypto.keystore;


import java.security.KeyPair;
import java.util.List;

import eu.agno3.orchestrator.config.model.base.config.ObjectName;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.model.validation.ValidReferenceAlias;
import eu.agno3.orchestrator.types.entities.crypto.X509CertEntry;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:crypto:keystores:importKeyEntry" )
public interface ImportKeyPairEntry extends ConfigurationObject {

    /**
     * 
     * @return key alias
     */
    @ObjectName
    @ValidReferenceAlias
    String getAlias ();


    /**
     * 
     * @return the key pair
     */
    KeyPair getKeyPair ();


    /**
     * 
     * @return certificate chain to store with the key
     */
    List<X509CertEntry> getCertificateChain ();
}
