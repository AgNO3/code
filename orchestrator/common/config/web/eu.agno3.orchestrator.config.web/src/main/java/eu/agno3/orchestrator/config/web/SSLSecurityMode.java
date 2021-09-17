/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.util.Arrays;
import java.util.List;


/**
 * 
 * 
 * 
 * General notes:
 * - prefer AES 128 over AES 256, this should provide superior performance while being perfectly safe
 * - prefer GCM in most cases, but as javas implementation is very slow provide a setting that changes this order
 * - prefer ECDHE over DHE over no-PFS
 * - use SHA384 for PFS+AES256 as OpenSSL does not support other algos
 * - include TLS_EMPTY_RENEGOTIATION_INFO_SCSV
 * - include only ciphers that are supported by openssl as well as java
 * 
 * Changelog:
 * - 1.2.2016 - mbechler - Dropped SHA1 from default and performance. Dropped DHE from security suites.
 * 
 * @author mbechler
 *
 */
@SuppressWarnings ( "nls" )
public enum SSLSecurityMode {

    /**
     * Do not use any primitives that have known weaknesses, 256 bit only
     */
    SECURITY256(Arrays.asList("TLSv1.2"),
            Arrays.asList(
                "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
                "TLS_ECDHE_ECSDA_WITH_AES_256_CBC_SHA384",
                "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
                "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
                //
                "TLS_EMPTY_RENEGOTIATION_INFO_SCSV")),

    /**
     * Do not use any primitives that have known weaknesses
     */
    SECURITY(Arrays.asList("TLSv1.2"),
            Arrays.asList(
                "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
                "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
                "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
                "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
                "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
                "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
                //
                "TLS_EMPTY_RENEGOTIATION_INFO_SCSV")),

    /**
     * Security + DHE support
     */
    DEFAULT(Arrays.asList("TLSv1.2", "TLSv1.1"),
            Arrays.asList(
                "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
                "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
                "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256",
                "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
                "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
                "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
                "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
                "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
                "TLS_DHE_RSA_WITH_AES_256_CBC_SHA256",
                //
                "TLS_EMPTY_RENEGOTIATION_INFO_SCSV")),

    /**
     * Same as default, but preferring CBC over GCM
     * (to account for Javas failure to have a fast GCM yet)
     * 
     * Also needs to enable SHA1 as most browsers don't announce
     * a non-GCM SHA2 ciphers
     */
    PERFORMANCE(Arrays.asList("TLSv1.2", "TLSv1.1"),
            Arrays.asList(
                "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
                "TLS_ECDHE_ECSDA_WITH_AES_128_CBC_SHA",
                "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
                "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
                "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256",
                "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
                "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
                "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
                "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
                "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
                "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
                "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
                "TLS_DHE_RSA_WITH_AES_256_CBC_SHA256",
                "TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
                "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
                //
                "TLS_EMPTY_RENEGOTIATION_INFO_SCSV")),

    /**
     * Security + non-PFS variants + 3DES CBC SHA1
     */
    COMPATIBILITY(Arrays.asList("TLSv1.2", "TLSv1.1", "TLSv1", "SSLv2Hello"), Arrays.asList(
        "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
        "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
        "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
        "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
        "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
        "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
        "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
        "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256",
        "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
        "TLS_RSA_WITH_AES_128_GCM_SHA256",
        "TLS_RSA_WITH_AES_128_CBC_SHA256",
        "TLS_RSA_WITH_AES_128_CBC_SHA",
        "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
        "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
        "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
        "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
        "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
        "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
        "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
        "TLS_DHE_RSA_WITH_AES_256_CBC_SHA256",
        "TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
        "TLS_RSA_WITH_AES_256_GCM_SHA384",
        "TLS_RSA_WITH_AES_256_CBC_SHA256",
        "TLS_RSA_WITH_AES_256_CBC_SHA",
        "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA",
        "SSL_RSA_WITH_3DES_EDE_CBC_SHA",
        //
        "TLS_EMPTY_RENEGOTIATION_INFO_SCSV"));

    private List<String> protocols;
    private List<String> ciphers;


    private SSLSecurityMode ( List<String> protocols, List<String> ciphers ) {
        this.protocols = protocols;
        this.ciphers = ciphers;
    }


    /**
     * @return the protocols
     */
    public List<String> getProtocols () {
        return this.protocols;
    }


    /**
     * @return the ciphers
     */
    public List<String> getCiphers () {
        return this.ciphers;
    }
}
