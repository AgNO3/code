/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.11.2014 by mbechler
 */
package eu.agno3.orchestrator.config.crypto.truststore;


import java.net.URI;

import org.joda.time.Duration;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.types.entities.crypto.X509CertEntry;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:crypto:truststores:revocation" )
public interface RevocationConfig extends ConfigurationObject {

    /**
     * 
     * @return network timeout for OCSP and CRL downloading
     */
    Duration getNetworkTimeout ();


    /**
     * 
     * @return check only end-entity (no intermediate CA) certificates
     */
    Boolean getCheckOnlyEndEntity ();


    /**
     * @return whether to use a trusted responder
     */
    Boolean getUseTrustedResponder ();


    /**
     * 
     * @return whether to override all ocsp request to use the trusted responder, otherwise the trusted responder is
     *         used when no autority information access extension is present
     */
    Boolean getTrustedResponderCheckAll ();


    /**
     * 
     * @return certificate (either EE or authority) for validating the ocsp responder
     */
    X509CertEntry getTrustedResponderTrustCertificate ();


    /**
     * 
     * @return URL for a trusted responder
     */
    URI getTrustedResponderUri ();


    /**
     * 
     * @return number of OCSP responses to aache
     */
    Integer getOcspCacheSize ();


    /**
     * 
     * @return ocsp check semantics to apply
     */
    OCSPCheckLevel getOcspCheckLevel ();


    /**
     * 
     * @return interval for updating root CRLs
     */
    Duration getCrlUpdateInterval ();


    /**
     * 
     * @return number of on-demand downloaded CRLs to cache
     */
    Integer getOnDemandCRLCacheSize ();


    /**
     * 
     * @return wether to download (intermediate) CRLs on demand
     */
    Boolean getOnDemandCRLDownload ();


    /**
     * 
     * @return crl check semantics to apply
     */
    CRLCheckLevel getCrlCheckLevel ();

}
