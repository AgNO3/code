/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.11.2014 by mbechler
 */
package eu.agno3.orchestrator.config.crypto.truststore;


import java.net.URI;

import org.joda.time.Duration;

import eu.agno3.orchestrator.types.entities.crypto.X509CertEntry;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( RevocationConfig.class )
public interface RevocationConfigMutable extends RevocationConfig {

    /**
     * 
     * @param networkTimeout
     */
    void setNetworkTimeout ( Duration networkTimeout );


    /**
     * 
     * @param checkOnlyEndEntity
     */
    void setCheckOnlyEndEntity ( Boolean checkOnlyEndEntity );


    /**
     * @param useTrustedResponder
     */
    void setUseTrustedResponder ( Boolean useTrustedResponder );


    /**
     * 
     * @param trustedResponderCheckAll
     */
    void setTrustedResponderCheckAll ( Boolean trustedResponderCheckAll );


    /**
     * 
     * @param trustedResponderTrustCert
     */
    void setTrustedResponderTrustCertificate ( X509CertEntry trustedResponderTrustCert );


    /**
     * 
     * @param trustedResponderUri
     */
    void setTrustedResponderUri ( URI trustedResponderUri );


    /**
     * 
     * @param ocspCacheSize
     */
    void setOcspCacheSize ( Integer ocspCacheSize );


    /**
     * 
     * @param ocspCheckLevel
     */
    void setOcspCheckLevel ( OCSPCheckLevel ocspCheckLevel );


    /**
     * 
     * @param crlUpdateInterval
     */
    void setCrlUpdateInterval ( Duration crlUpdateInterval );


    /**
     * 
     * @param onDemandCRLCacheSize
     */
    void setOnDemandCRLCacheSize ( Integer onDemandCRLCacheSize );


    /**
     * 
     * @param onDemandCRLDownload
     */
    void setOnDemandCRLDownload ( Boolean onDemandCRLDownload );


    /**
     * 
     * @param crlCheckLevel
     */
    void setCrlCheckLevel ( CRLCheckLevel crlCheckLevel );

}
