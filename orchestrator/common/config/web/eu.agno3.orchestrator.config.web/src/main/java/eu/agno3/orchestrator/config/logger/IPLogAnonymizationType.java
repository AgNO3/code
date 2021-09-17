/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.09.2016 by mbechler
 */
package eu.agno3.orchestrator.config.logger;


/**
 * @author mbechler
 *
 */
public enum IPLogAnonymizationType {

    /**
     * No anonymization
     */
    NONE,

    /**
     * Mask lower-bits of address
     */
    MASK,

    /**
     * Completely remove
     */
    REDACT,

}
