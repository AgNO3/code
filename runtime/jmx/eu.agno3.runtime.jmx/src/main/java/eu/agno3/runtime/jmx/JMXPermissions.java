/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.03.2017 by mbechler
 */
package eu.agno3.runtime.jmx;


/**
 * @author mbechler
 *
 */
public enum JMXPermissions {

    /**
     * No permission
     */
    INVALID,

    /**
     * Reading attributes
     */
    READ,

    /**
     * Receiving notifications
     */
    NOTIFY,

    /**
     * Writing attributes
     */
    WRITE,

    /**
     * Calling methods
     */
    CALL,

    /**
     * Creating objects
     */
    CREATE
}
