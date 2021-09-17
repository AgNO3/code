/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.11.2014 by mbechler
 */
package eu.agno3.orchestrator.config.crypto.keystore;


import java.util.Set;

import eu.agno3.orchestrator.config.model.base.config.ObjectName;
import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.model.validation.ValidReferenceAlias;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:crypto:keystores:keystore" )
public interface KeystoreConfig extends ConfigurationObject {

    /**
     * 
     * @return the keystore alias
     */
    @ObjectName
    @ValidReferenceAlias
    String getAlias ();


    /**
     * 
     * @return the alias of the truststore against which the certificate will be validated
     */
    String getValidationTrustStore ();


    /**
     * 
     * @return a keypair to import
     */
    @ReferencedObject
    Set<ImportKeyPairEntry> getImportKeyPairs ();

}
