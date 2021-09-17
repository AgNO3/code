/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.runtime.security.db;


import java.util.Set;

import eu.agno3.runtime.crypto.scrypt.SCryptResult;
import eu.agno3.runtime.security.SecurityManagementException;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public interface BaseSecurityInitializer {

    /**
     * @param password
     * @return the hashed password
     * @throws SecurityManagementException
     */
    SCryptResult generatePasswordHash ( String password ) throws SecurityManagementException;


    /**
     * @throws SecurityManagementException
     */
    void ensureAdminPermissions () throws SecurityManagementException;


    /**
     * @param username
     * @param r
     * @param roles
     *            Roles for user
     * @return the user principal
     * @throws SecurityManagementException
     */
    UserPrincipal ensureUserExists ( String username, SCryptResult r, Set<String> roles ) throws SecurityManagementException;

}
