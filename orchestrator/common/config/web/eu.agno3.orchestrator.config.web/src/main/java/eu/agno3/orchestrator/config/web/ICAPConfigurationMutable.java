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
@MapAs ( ICAPConfiguration.class )
public interface ICAPConfigurationMutable extends ICAPConfiguration {

    /**
     * 
     * @param servers
     */
    void setServers ( List<URI> servers );


    /**
     * 
     * @param overrideRequestURI
     */
    void setOverrideRequestURI ( String overrideRequestURI );


    /**
     * 
     * @param socketTimeout
     */
    void setSocketTimeout ( Duration socketTimeout );


    /**
     * 
     * @param sslClientConfiguration
     */
    void setSslClientConfiguration ( SSLClientConfiguration sslClientConfiguration );


    /**
     * 
     * @param sslClientMode
     */
    void setSslClientMode ( SSLClientMode sslClientMode );


    /**
     * @param sendICAPSInRequestUri
     */
    void setSendICAPSInRequestUri ( Boolean sendICAPSInRequestUri );

}
