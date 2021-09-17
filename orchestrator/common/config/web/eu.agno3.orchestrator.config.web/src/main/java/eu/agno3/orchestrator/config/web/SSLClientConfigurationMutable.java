/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.util.Set;

import eu.agno3.orchestrator.types.entities.crypto.PublicKeyEntry;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( SSLClientConfiguration.class )
public interface SSLClientConfigurationMutable extends SSLClientConfiguration {

    /**
     * 
     * @param securityMode
     */
    void setSecurityMode ( SSLSecurityMode securityMode );


    /**
     * 
     * @param disableHostnameVerification
     */
    void setDisableHostnameVerification ( Boolean disableHostnameVerification );


    /**
     * 
     * @param truststoreAlias
     */
    void setTruststoreAlias ( String truststoreAlias );


    /**
     * @param disableCertificateVerification
     */
    void setDisableCertificateVerification ( Boolean disableCertificateVerification );


    /**
     * @param publicKeyPinMode
     */
    void setPublicKeyPinMode ( PublicKeyPinMode publicKeyPinMode );


    /**
     * @param pinnedPublicKeys
     */
    void setPinnedPublicKeys ( Set<PublicKeyEntry> pinnedPublicKeys );

}
