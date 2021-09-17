/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.10.2014 by mbechler
 */
package eu.agno3.runtime.security;


import java.util.UUID;

import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public interface UserMapper {

    /**
     * 
     * @return the user count
     */
    long getUserCount ();


    /**
     * @param username
     * @param realmname
     * @param knownUserId
     * @return a user principal for the given username and realm
     * @throws UserLicenseLimitExceededException
     */
    UserPrincipal getMappedUser ( String username, String realmname, UUID knownUserId ) throws UserLicenseLimitExceededException;


    /**
     * @param userName
     * @param realmName
     * @param userId
     * @return a user principal for the given username and realm
     */
    UserPrincipal getExistingMappedUser ( String userName, String realmName, UUID userId );


    /**
     * @param princ
     */
    void removeMapping ( UserPrincipal princ );

}