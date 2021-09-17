/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.util.Set;

import javax.validation.constraints.NotNull;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.model.validation.Materialized;
import eu.agno3.orchestrator.types.entities.crypto.PublicKeyEntry;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:web:sslclient" )
public interface SSLClientConfiguration extends ConfigurationObject {

    /**
     * 
     * @return the used keystore alias
     */
    String getTruststoreAlias ();


    /**
     * 
     * @return whether to disable hostname verification (dangerous)
     */
    Boolean getDisableHostnameVerification ();


    /**
     * 
     * @return the ssl security mode
     */
    @NotNull ( groups = {
        Materialized.class
    } )
    SSLSecurityMode getSecurityMode ();


    /**
     * @return whether to disable certificate trust verification (dangerous)
     */
    Boolean getDisableCertificateVerification ();


    /**
     * 
     * @return the public key pinning mode
     */
    PublicKeyPinMode getPublicKeyPinMode ();


    /**
     * 
     * @return pinned public keys
     */
    Set<PublicKeyEntry> getPinnedPublicKeys ();

}
