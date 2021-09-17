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
public enum OCSPCheckLevel {

    /**
     * No OCSP checking
     */
    DISABLE,

    /**
     * Check OCSP first, fallback to CRL if this fails
     */
    PRIMARY,

    /**
     * Always require a successful OCSP check
     */
    REQUIRE
}
