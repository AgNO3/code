/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.net.URI;
import java.util.Set;

import javax.validation.Valid;

import org.hibernate.validator.constraints.Range;
import org.joda.time.Duration;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.types.entities.crypto.PublicKeyEntry;
import eu.agno3.orchestrator.types.net.NetworkAddress;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:web:endpoint" )
public interface WebEndpointConfig extends ConfigurationObject {

    /**
     * 
     * @return the addresses to bind to
     */
    @Valid
    Set<NetworkAddress> getBindAddresses ();


    /**
     * 
     * @return the interface to bind to
     */
    String getBindInterface ();


    /**
     * 
     * @return the port to bind to
     */
    @Range ( min = 1, max = 65535 )
    Integer getBindPort ();


    /**
     * 
     * @return the application context path
     */
    String getContextPath ();


    /**
     * 
     * @return whether this host is behind a reverse proxy
     */
    Boolean getBehindReverseProxy ();


    /**
     * 
     * @return the session timeout
     */
    Duration getSessionInactiveTimeout ();


    /**
     * 
     * @return whether to disable ssl
     */
    Boolean getDisableSSL ();


    /**
     * 
     * @return whether to enable HPKP (HTTP public key pinning)
     */
    Boolean getEnableHPKP ();


    /**
     * 
     * @return the certificates that are pinned
     */
    @Valid
    Set<PublicKeyEntry> getHpkpPinnedKeys ();


    /**
     * 
     * @return the time after which a browser may forget about the pinning
     */
    Duration getHpkpPinningTimeout ();


    /**
     * 
     * @return whether subdomains should be included for pinning
     */
    Boolean getHpkpIncludeSubdomains ();


    /**
     * 
     * @return an URI clients may report HPKP failures to
     */
    URI getHpkpReportUri ();


    /**
     * @return whether to send Public-Key-Pins-Report-Only instead of Public-Key-Pins
     */
    Boolean getHpkpReportOnly ();


    /**
     * 
     * @return whether to enable HSTS (HTTP strict transport security)
     */
    Boolean getEnableHSTS ();


    /**
     * 
     * @return the time after which a browser may forget about HSTS
     */
    Duration getHstsTimeout ();


    /**
     * 
     * @return whether submains should be included for HSTS
     */
    Boolean getHstsIncludeSubdomains ();


    /**
     * 
     * @return whether to send the preload acceptance flag
     */
    Boolean getHstsAcceptPreload ();


    /**
     * @return ssl configuration
     */
    @ReferencedObject
    @Valid
    SSLEndpointConfiguration getSslEndpointConfiguration ();


    /**
     * @return reverse proxy config
     */
    @ReferencedObject
    @Valid
    WebReverseProxyConfiguration getReverseProxyConfig ();

}
