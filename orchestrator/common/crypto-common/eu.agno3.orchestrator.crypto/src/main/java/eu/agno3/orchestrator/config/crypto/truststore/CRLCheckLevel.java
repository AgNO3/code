/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.11.2014 by mbechler
 */
package eu.agno3.orchestrator.config.crypto.truststore;


/**
 * @author mbechler
 *
 */
public enum CRLCheckLevel {

    /**
     * No CRL checking
     */
    DISABLE,

    /**
     * Check CRL if available, even if "expired"
     */
    OPPORTUNISTIC,

    /**
     * Require a fresh CRL
     */
    STRICT,

    /**
     * Require a STRICT CRL check even if OCSP succeeded
     */
    REQUIRE

}
