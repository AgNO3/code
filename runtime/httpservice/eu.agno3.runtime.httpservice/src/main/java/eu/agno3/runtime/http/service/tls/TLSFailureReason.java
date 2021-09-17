/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Mar 2, 2017 by mbechler
 */
package eu.agno3.runtime.http.service.tls;


/**
 * @author mbechler
 *
 */
public enum TLSFailureReason {
    /**
     * Could not determine a more specific failure
     */
    UNKNOWN,

    /**
     * Other endpoint does not share an acceptable protocol
     */
    PROTOCOL_MISMATCH,

    /**
     * Other endpoint does not share an acceptable cipher suite
     */
    CIPHER_MISMATCH,

    /**
     * Other peer is authenticated but did provide an invalid certificate
     */
    BAD_PEER_CERT,

    /**
     * Other peer is authenticated but did not provide a certficate
     */
    NO_PEER_CERT,

    /**
     * Failure to negotiate safe renegotiation
     */
    INSECURE_RENEGOTIATION,

    /**
     * Invalid SNI data
     */
    SNI_INVALID,

}
