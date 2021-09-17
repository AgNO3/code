/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.revocation;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( {
    "javadoc", "nls"
} )
public final class RevocationConfigProperties {

    /**
     * 
     */
    private RevocationConfigProperties () {}

    public static final String CHECK_ONLY_EE = "revocationCheckOnlyEE";

    public static final String CRL_ENABLE = "crlEnable";
    public static final String CRL_REQUIRE = "crlRequire";
    public static final String CRL_IGNORE_EXPIRED = "crlIgnoreExpired";
    public static final String CRL_IGNORE_UNAVAILABLE_CRL = "crlIgnoreUnavailable";
    public static final String CRL_DOWNLOAD_UNAVAILABLE = "crlDownloadUnvailable";
    public static final String CRL_CACHE_SIZE = "crlCacheSize";
    public static final String CRL_CACHE_NEGATIVE_MINUTES = "crlCacheNegativeMinutes";
    public static final String CRL_UPDATE_INTERVAL_MINUTES = "crlUpdateIntervalMinutes";

    public static final String OCSP_ENABLE = "ocspEnable";
    public static final String OCSP_REQUIRE = "ocspRequire";
    public static final String OCSP_CACHE_SIZE = "ocspCacheSize";

    public static final String OCSP_SYSTEM_URI = "ocspSystemResponderUri";
    public static final String OCSP_SYSTEM_CERT = "ocspSystemResponderCert";
    public static final String OCSP_CHECK_ALL_WITH_SYSTEM = "ocspCheckAllWithSystemResponder";

    public static final String HTTP_CONNECT_TIMEOUT = "revocationHttpConnectTimeout";
    public static final String HTTP_READ_TIMEOUT = "revocationHttpReadTimeout";
    public static final String HTTP_MAX_REDIRECTS = "revocationHttpMaxRedirects";
}
