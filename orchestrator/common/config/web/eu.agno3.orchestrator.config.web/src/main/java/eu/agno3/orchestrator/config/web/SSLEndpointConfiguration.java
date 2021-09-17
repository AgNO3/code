/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.model.validation.Materialized;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:web:sslendpoint" )
public interface SSLEndpointConfiguration extends ConfigurationObject {

    /**
     * 
     * @return the used keystore alias
     */
    String getKeystoreAlias ();


    /**
     * 
     * @return the used key alias
     */
    String getKeyAlias ();


    /**
     * 
     * @return the ssl security mode
     */
    @NotNull ( groups = {
        Materialized.class
    } )
    SSLSecurityMode getSecurityMode ();


    /**
     * 
     * @return custom ssl protocols specification (see JSSE Standard Algorithm Name Documentation)
     */
    Set<String> getCustomProtocols ();


    /**
     * @return custom ssl ciphers specification (see JSSE Standard Algorithm Name Documentation)
     */
    List<String> getCustomCiphers ();
}
