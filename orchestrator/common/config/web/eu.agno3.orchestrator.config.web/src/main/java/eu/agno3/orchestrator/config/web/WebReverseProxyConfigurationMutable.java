/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.09.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.net.URI;
import java.util.Set;

import eu.agno3.orchestrator.types.net.NetworkSpecification;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( WebReverseProxyConfiguration.class )
public interface WebReverseProxyConfigurationMutable extends WebReverseProxyConfiguration {

    /**
     * @param overrideUri
     */
    void setOverrideURI ( URI overrideUri );


    /**
     * 
     * @param forwardedSSLCiphersHeader
     */
    void setForwardedSSLCiphersHeader ( String forwardedSSLCiphersHeader );


    /**
     * 
     * @param forwardedSSLCiphersType
     */
    void setForwardedSSLCiphersType ( WebReverseProxySSLType forwardedSSLCiphersType );


    /**
     * 
     * @param forwardedSSLMatchValue
     */
    void setForwardedSSLMatchValue ( String forwardedSSLMatchValue );


    /**
     * 
     * @param forwardedSSLMatchHeader
     */
    void setForwardedSSLMatchHeader ( String forwardedSSLMatchHeader );


    /**
     * 
     * @param forwardedRemoteAddrHeader
     */
    void setForwardedRemoteAddrHeader ( String forwardedRemoteAddrHeader );


    /**
     * 
     * @param forwardedPortHeader
     */
    void setForwardedPortHeader ( String forwardedPortHeader );


    /**
     * 
     * @param forwardedHostHeader
     */
    void setForwardedHostHeader ( String forwardedHostHeader );


    /**
     * 
     * @param proxyType
     */
    void setProxyType ( WebReverseProxyType proxyType );


    /**
     * 
     * @param trustedProxies
     */
    void setTrustedProxies ( Set<NetworkSpecification> trustedProxies );

}
