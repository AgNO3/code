/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 5, 2016 by mbechler
 */
package eu.agno3.orchestrator.system.file.hashtracking;


/**
 * @author mbechler
 *
 */
public enum ValidationResult {

    /**
     * 
     */
    UNKNOWN,

    /**
     * Recorded hash value does not match
     */
    HASH_MISMATCH,

    /**
     * File is missing
     */
    MISSING,

    /**
     * Failure to read file
     */
    ERROR
}
