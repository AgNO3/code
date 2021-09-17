/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.util.List;
import java.util.Set;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( SSLEndpointConfiguration.class )
public interface SSLEndpointConfigurationMutable extends SSLEndpointConfiguration {

    /**
     * @param keystoreAlias
     *            the keystoreAlias to set
     */
    void setKeystoreAlias ( String keystoreAlias );


    /**
     * @param keyAlias
     *            the keyAlias to set
     */
    void setKeyAlias ( String keyAlias );


    /**
     * 
     * @param customCiphers
     */
    void setCustomCiphers ( List<String> customCiphers );


    /**
     * 
     * @param customProtocols
     */
    void setCustomProtocols ( Set<String> customProtocols );


    /**
     * 
     * @param securityMode
     */
    void setSecurityMode ( SSLSecurityMode securityMode );
}
