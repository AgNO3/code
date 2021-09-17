/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.09.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.net.URI;
import java.util.Set;

import javax.validation.Valid;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.types.net.NetworkSpecification;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:web:revproxy" )
public interface WebReverseProxyConfiguration extends ConfigurationObject {

    /**
     * 
     * @return the override URI for reverse proxy usage
     */
    URI getOverrideURI ();


    /**
     * 
     * @return proxiy ip addresses to trust
     */
    @Valid
    Set<NetworkSpecification> getTrustedProxies ();


    /**
     * 
     * @return HTTP header proxy integration style
     */
    WebReverseProxyType getProxyType ();


    /**
     * 
     * @return the forwarded remote address header, single entry or X-Forwarded-For style
     */
    String getForwardedRemoteAddrHeader ();


    /**
     * 
     * @return the forwarded endpoint port header, if empty is extracted from host header
     */
    String getForwardedPortHeader ();


    /**
     * 
     * @return the forwarded host header
     */
    String getForwardedHostHeader ();


    /**
     * 
     * @return header to match for determining whether the frontend is accessed via HTTPS
     */
    String getForwardedSSLMatchHeader ();


    /**
     * 
     * @return value to match in ForwardedSSLMatchHeader for assuming that the frontend is accessed via HTTPS
     */
    String getForwardedSSLMatchValue ();


    /**
     * 
     * @return interpret the cipher header according to the given style
     */
    WebReverseProxySSLType getForwardedSSLCiphersType ();


    /**
     * 
     * @return header that specifies the ciphers the client is using with the reverse proxy
     */
    String getForwardedSSLCiphersHeader ();

}
