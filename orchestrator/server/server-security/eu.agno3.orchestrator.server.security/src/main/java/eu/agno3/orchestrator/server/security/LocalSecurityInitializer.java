/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.security;


import java.util.Set;

import eu.agno3.runtime.crypto.scrypt.SCryptResult;
import eu.agno3.runtime.security.SecurityManagementException;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public interface LocalSecurityInitializer {

    /**
     * @throws SecurityManagementException
     */
    void ensureAdminPermissions () throws SecurityManagementException;


    /**
     * @param username
     * @param r
     * @param roles
     * @return the user principal
     * @throws SecurityManagementException
     */
    UserPrincipal ensureUserExists ( String username, SCryptResult r, Set<String> roles ) throws SecurityManagementException;


    /**
     * @param password
     * @return the hashed password
     * @throws SecurityManagementException
     */
    SCryptResult generatePasswordHash ( String password ) throws SecurityManagementException;

}