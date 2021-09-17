/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 15, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.web;


/**
 * @author mbechler
 *
 */
public enum PublicKeyPinMode {

    /**
     * Pinned keys are valid in addition to the ones validated by truststore
     */
    ADDITIVE,

    /**
     * Only pinned keys are valid
     */
    EXCLUSIVE
}
