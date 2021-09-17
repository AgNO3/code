/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.11.2014 by mbechler
 */
package eu.agno3.orchestrator.config.crypto.truststore;


import javax.validation.Valid;

import eu.agno3.orchestrator.config.model.base.config.ObjectName;
import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.model.validation.ValidReferenceAlias;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:crypto:truststores:truststore" )
public interface TruststoreConfig extends ConfigurationObject {

    /**
     * @return the truststore alias
     */
    @ObjectName
    @ValidReferenceAlias
    String getAlias ();


    /**
     * @return the revocation configuration
     */
    @ReferencedObject
    @Valid
    RevocationConfig getRevocationConfiguration ();


    /**
     * @return the resource library holding trusted CA certificated
     */
    String getTrustLibrary ();

}
