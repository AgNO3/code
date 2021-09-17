/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.08.2016 by mbechler
 */
package eu.agno3.runtime.security.login;


/**
 * @author mbechler
 *
 */
public enum AuthResponseType {

    /**
     * Illegal state
     */
    UNKNOWN,

    /**
     * Auth should fail immediately
     */
    FAIL,

    /**
     * Auth should continue
     */
    CONTINUE,

    /**
     * Authentication stack should restart from the beginning
     */
    BREAK,

    /**
     * Authentication is complete
     */
    COMPLETE,

    /**
     * Need to wait
     */
    THROTTLE
}
