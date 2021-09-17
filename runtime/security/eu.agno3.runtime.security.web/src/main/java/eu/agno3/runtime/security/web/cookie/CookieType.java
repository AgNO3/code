/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.09.2016 by mbechler
 */
package eu.agno3.runtime.security.web.cookie;


/**
 * @author mbechler
 *
 */
public enum CookieType {

    /**
     * Integrity protect cookie values
     */
    SIGN,

    /**
     * Also encrypts the cookie contents
     */
    ENCRYPT,

}
