/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.11.2014 by mbechler
 */
package eu.agno3.orchestrator.config.crypto.truststore;


import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( TruststoreConfig.class )
public interface TruststoreConfigMutable extends TruststoreConfig {

    /**
     * @return the revocation configuration
     */
    @Override
    @ReferencedObject
    RevocationConfigMutable getRevocationConfiguration ();


    /**
     * 
     * @param config
     */
    void setRevocationConfiguration ( RevocationConfigMutable config );


    /**
     * @param alias
     */
    void setAlias ( String alias );


    /**
     * @param trustLibrary
     */
    void setTrustLibrary ( String trustLibrary );

}
