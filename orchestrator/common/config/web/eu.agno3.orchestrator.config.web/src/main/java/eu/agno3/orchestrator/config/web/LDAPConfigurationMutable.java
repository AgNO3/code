/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.net.URI;
import java.util.List;

import org.joda.time.Duration;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( LDAPConfiguration.class )
public interface LDAPConfigurationMutable extends LDAPConfiguration {

    /**
     * 
     * @param sslClientMode
     */
    void setSslClientMode ( SSLClientMode sslClientMode );


    /**
     * 
     * @param sslClientConfiguration
     */
    void setSslClientConfiguration ( SSLClientConfigurationMutable sslClientConfiguration );


    /**
     * 
     * @param password
     */
    void setPassword ( String password );


    /**
     * @param saslMechanism
     */
    void setSaslMechanism ( String saslMechanism );


    /**
     * 
     * @param saslUsername
     */
    void setSaslUsername ( String saslUsername );


    /**
     * @param saslRealm
     */
    void setSaslRealm ( String saslRealm );


    /**
     * 
     * @param bindDN
     */
    void setBindDN ( String bindDN );


    /**
     * 
     * @param authType
     */
    void setAuthType ( LDAPAuthType authType );


    /**
     * 
     * @param baseDN
     */
    void setBaseDN ( String baseDN );


    /**
     * 
     * @param srvDomain
     */
    void setSrvDomain ( String srvDomain );


    /**
     * 
     * @param servers
     */
    void setServers ( List<URI> servers );


    /**
     * 
     * @param serverType
     */
    void setServerType ( LDAPServerType serverType );


    /**
     * @param socketTimeout
     */
    void setSocketTimeout ( Duration socketTimeout );

}
