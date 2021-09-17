/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.net.URI;
import java.util.Set;

import org.joda.time.Duration;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( SMTPConfiguration.class )
public interface SMTPConfigurationMutable extends SMTPConfiguration {

    /**
     * 
     * @param authMechanisms
     */
    void setAuthMechanisms ( Set<String> authMechanisms );


    /**
     * 
     * @param smtpPassword
     */
    void setSmtpPassword ( String smtpPassword );


    /**
     * 
     * @param smtpUser
     */
    void setSmtpUser ( String smtpUser );


    /**
     * 
     * @param authEnabled
     */
    void setAuthEnabled ( Boolean authEnabled );


    /**
     * 
     * @param overrideDefaultFromName
     */
    void setOverrideDefaultFromName ( String overrideDefaultFromName );


    /**
     * 
     * @param overrideDefaultFromAddress
     */
    void setOverrideDefaultFromAddress ( String overrideDefaultFromAddress );


    /**
     * 
     * @param overrideEhloHostName
     */
    void setOverrideEhloHostName ( String overrideEhloHostName );


    /**
     * 
     * @param socketTimeout
     */
    void setSocketTimeout ( Duration socketTimeout );


    /**
     * 
     * @param sslClientConfiguration
     */
    void setSslClientConfiguration ( SSLClientConfigurationMutable sslClientConfiguration );


    /**
     * 
     * @param sslClientMode
     */
    void setSslClientMode ( SSLClientMode sslClientMode );


    /**
     * 
     * @param serverUri
     */
    void setServerUri ( URI serverUri );

}
