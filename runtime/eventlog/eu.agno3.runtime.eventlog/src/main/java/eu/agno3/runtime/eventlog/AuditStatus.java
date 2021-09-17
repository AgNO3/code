/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.04.2015 by mbechler
 */
package eu.agno3.runtime.eventlog;


/**
 * @author mbechler
 *
 */
public enum AuditStatus {

    /**
     * 
     */
    SUCCESS,

    /**
     * An internal error has occured
     */
    INTERNAL,

    /**
     * 
     */
    UNAUTHENTICATED,

    /**
     * 
     */
    UNAUTHORIZED,

    /**
     *  
     */
    VALIDATION
}
