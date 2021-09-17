/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.net.URI;
import java.util.Set;

import org.joda.time.Duration;

import eu.agno3.orchestrator.types.entities.crypto.PublicKeyEntry;
import eu.agno3.orchestrator.types.net.NetworkAddress;


/**
 * @author mbechler
 *
 */
public interface WebEndpointConfigMutable extends WebEndpointConfig {

    /**
     * @param bindAddress
     *            the bindAddress to set
     */
    void setBindAddresses ( Set<NetworkAddress> bindAddress );


    /**
     * @param bindInterface
     *            the bindInterface to set
     */
    void setBindInterface ( String bindInterface );


    /**
     * @param bindPort
     *            the bindPort to set
     */
    void setBindPort ( Integer bindPort );


    /**
     * @param contextPath
     *            the contextPath to set
     */
    void setContextPath ( String contextPath );


    /**
     * @param behindReverseProxy
     *            the behindReverseProxy to set
     */
    void setBehindReverseProxy ( Boolean behindReverseProxy );


    /**
     * @param disableSSL
     *            the disableSSL to set
     */
    void setDisableSSL ( Boolean disableSSL );


    /**
     * @param sessionTimeout
     *            the sessionTimeout to set
     */
    void setSessionInactiveTimeout ( Duration sessionTimeout );


    /**
     * 
     * @param hpkpReportUri
     */
    void setHpkpReportUri ( URI hpkpReportUri );


    /**
     * 
     * @param hpkpPinningTimeout
     */
    void setHpkpPinningTimeout ( Duration hpkpPinningTimeout );


    /**
     * 
     * @param hpkpIncludeSubdomains
     */
    void setHpkpIncludeSubdomains ( Boolean hpkpIncludeSubdomains );


    /**
     * 
     * @param hpkpPinnedCerts
     */
    void setHpkpPinnedKeys ( Set<PublicKeyEntry> hpkpPinnedCerts );


    /**
     * 
     * @param enableHPKP
     */
    void setEnableHPKP ( Boolean enableHPKP );
    

    /**
     * @param hpkpReportOnly
     */
    void setHpkpReportOnly ( Boolean hpkpReportOnly );



    /**
     * 
     * @param hstsTimeout
     */
    void setHstsTimeout ( Duration hstsTimeout );


    /**
     * 
     * @param hstsIncludeSubdomains
     */
    void setHstsIncludeSubdomains ( Boolean hstsIncludeSubdomains );


    /**
     * 
     * @param hstsAcceptPreload
     */
    void setHstsAcceptPreload ( Boolean hstsAcceptPreload );


    /**
     * 
     * @param enableHSTS
     */
    void setEnableHSTS ( Boolean enableHSTS );


    /**
     * @param sslEndpointConfig
     */
    void setSslEndpointConfiguration ( SSLEndpointConfigurationMutable sslEndpointConfig );


    /**
     * @param reverseProxyConfig
     */
    void setReverseProxyConfig ( WebReverseProxyConfigurationMutable reverseProxyConfig );


}