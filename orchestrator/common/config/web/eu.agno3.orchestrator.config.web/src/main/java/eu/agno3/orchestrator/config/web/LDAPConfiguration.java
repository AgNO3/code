/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.net.URI;
import java.util.List;

import javax.validation.Valid;

import org.joda.time.Duration;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( LDAPConfigurationObjectTypeDescriptor.TYPE_NAME )
public interface LDAPConfiguration extends ConfigurationObject {

    /**
     * 
     * @return the type of ldap server
     */
    LDAPServerType getServerType ();


    /**
     * 
     * @return the configured servers
     */
    List<URI> getServers ();


    /**
     * 
     * @return resolve servers via SRV domain
     */
    String getSrvDomain ();


    /**
     * 
     * @return base DN (if not set discovery via subschema will be perfomred)
     */
    String getBaseDN ();


    /**
     * 
     * @return authentication type
     */
    LDAPAuthType getAuthType ();


    /**
     * 
     * @return the bind DN
     */
    String getBindDN ();


    /**
     * @return SASL mechanism to use
     */
    String getSaslMechanism ();


    /**
     * 
     * @return the username for SASL
     */
    String getSaslUsername ();


    /**
     * @return the realm for SASL
     */
    String getSaslRealm ();


    /**
     * @return the QOP setting for SASL
     */
    SASLQOP getSaslQOP ();


    /**
     * 
     * @return the bind password
     */
    String getPassword ();


    /**
     * 
     * @return ssl client configuration
     */
    @ReferencedObject
    @Valid
    SSLClientConfiguration getSslClientConfiguration ();


    /**
     * 
     * @return ssl client mode
     */
    SSLClientMode getSslClientMode ();


    /**
     * @return socket timeout
     */
    Duration getSocketTimeout ();

}
