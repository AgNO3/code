/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.revocation;


import java.net.URI;
import java.security.cert.X509Certificate;


/**
 * @author mbechler
 *
 */
public interface RevocationConfig {

    /**
     * @return whether to check only end entity certificates (and not intermedia CAs)
     */
    boolean isCheckOnlyEndEntityCerts ();


    /**
     * @return whether to check CRLs at all
     */
    boolean isCheckCRL ();


    /**
     * @return whether to reject certificates that do not have CRL information
     */
    boolean isRequireCRL ();


    /**
     * @return whether to ignore when no stored CRL is available for a certificate
     */
    boolean isIgnoreUnavailableCRL ();


    /**
     * @return whether to ignore when the stored CRL is expired (nextUpdate has passed) for a certificate
     */
    boolean isIgnoreExpiredCRL ();


    /**
     * @return size of cache for dynamically fetched CRLs
     */
    int getCrlCacheSize ();


    /**
     * @return the negative cache time for dynamically fetched CRLs
     */
    int getCrlNegativeCacheMinutes ();


    /**
     * 
     * @return the interval of offline CRL update checks
     */
    int getCrlUpdateIntervalMinutes ();


    /**
     * @return whether to download intermediate CRLs when required
     */
    boolean isDownloadCRLs ();


    /**
     * @return whether to check OCSP at all
     */
    boolean isCheckOCSP ();


    /**
     * @return whether to require the OCSP check to succeed (otherwise fall back to CRL checking, if enabled)
     */
    boolean isRequireOCSP ();


    /**
     * @return check all certificates only against the given OCSP resolver
     * 
     */
    URI getSystemOCSPUri ();


    /**
     * @return check all certificates without authority information access against the given OCSP resolver
     */
    boolean isCheckAllUsingSystemOCSP ();


    /**
     * @return the size of the OCSP response cache
     */
    int getOcspCacheSize ();


    /**
     * @return PKIX params to validate system ocsp
     * 
     */
    X509Certificate getSystemOCSPTrustCert ();


    /**
     * @return the read timeout for http requests (ocsp, loading crls/certs)
     */
    int getReadTimeout ();


    /**
     * @return the connect timeout for http requests (ocsp, loading crls/certs)
     */
    int getConnectTimeout ();


    /**
     * @return the maximum number of redirects for http requests (ocsp, loading crls/certs)
     */
    int getMaxRedirects ();

}
