/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.07.2013 by mbechler
 */
package eu.agno3.runtime.db.derby.auth;


/**
 * @author mbechler
 * 
 */
public enum UserAccess {
    /**
     * No access at all
     */
    NONE,
    /**
     * Read-only access
     */
    READ,
    /**
     * Write access
     */
    WRITE,
    /**
     * Administrative access
     */
    ADMIN
}
