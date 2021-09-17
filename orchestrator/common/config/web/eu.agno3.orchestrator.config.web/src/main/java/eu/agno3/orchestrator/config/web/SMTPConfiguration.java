/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.net.URI;
import java.util.Set;

import javax.validation.Valid;

import org.joda.time.Duration;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.runtime.validation.domain.ValidFQDN;
import eu.agno3.runtime.validation.email.ValidEmail;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( SMTPConfigurationObjectTypeDescriptor.TYPE_NAME )
public interface SMTPConfiguration extends ConfigurationObject {

    /**
     * 
     * @return auth mechanisms to accept
     */
    Set<String> getAuthMechanisms ();


    /**
     * 
     * @return auth password
     */
    String getSmtpPassword ();


    /**
     * 
     * @return auth username
     */
    String getSmtpUser ();


    /**
     * 
     * @return enable authentication
     */
    Boolean getAuthEnabled ();


    /**
     * 
     * @return if no from is specified, use this name
     */
    String getOverrideDefaultFromName ();


    /**
     * 
     * @return if no from is specified, use this address
     */
    @ValidEmail
    String getOverrideDefaultFromAddress ();


    /**
     * 
     * @return the hostname to use in EHLO
     */
    @ValidFQDN
    String getOverrideEhloHostName ();


    /**
     * 
     * @return socket timeout
     */
    Duration getSocketTimeout ();


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
     * 
     * @return the URI to the SMTP server
     */
    URI getServerUri ();

}
