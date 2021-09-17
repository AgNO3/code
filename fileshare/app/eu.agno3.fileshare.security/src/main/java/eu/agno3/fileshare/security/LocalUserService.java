/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.fileshare.security;


import java.util.Set;

import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.SecurityException;
import eu.agno3.fileshare.exceptions.UserNotFoundException;
import eu.agno3.runtime.crypto.scrypt.SCryptResult;
import eu.agno3.runtime.security.UserLicenseLimitExceededException;
import eu.agno3.runtime.security.db.BaseLocalUserService;
import eu.agno3.runtime.security.db.BaseUser;
import eu.agno3.runtime.security.principal.UserInfo;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public interface LocalUserService extends BaseLocalUserService<BaseUser> {

    /**
     * Enable a local user
     * 
     * @param princ
     * @throws FileshareException
     */
    void enableUser ( UserPrincipal princ ) throws FileshareException;


    /**
     * Disable a local user
     * 
     * @param princ
     * @throws FileshareException
     */
    void disableUser ( UserPrincipal princ ) throws FileshareException;


    /**
     * Get user authentication info
     * 
     * @param principal
     * @return the authentication user for the principal
     * @throws UserNotFoundException
     * @throws FileshareException
     */
    UserInfo getUser ( UserPrincipal principal ) throws FileshareException;


    /**
     * List local users
     * 
     * @return the known users
     * @throws FileshareException
     */
    Set<BaseUser> getUsers () throws FileshareException;


    /**
     * Create a local user
     * 
     * @param userName
     * @param pwHash
     * @param disabled
     * @param pwExpiry
     * @param expires
     * @return the create user
     * @throws FileshareException
     * @throws UserLicenseLimitExceededException
     */
    UserInfo createUser ( String userName, SCryptResult pwHash, boolean disabled, DateTime pwExpiry, DateTime expires )
            throws FileshareException, UserLicenseLimitExceededException;


    /**
     * Remove a local user
     * 
     * @param remove
     * @throws FileshareException
     */
    void removeUsers ( Set<UserPrincipal> remove ) throws FileshareException;


    /**
     * @param user
     * @param passwordHash
     * @throws FileshareException
     */
    void changePassword ( UserPrincipal user, SCryptResult passwordHash ) throws FileshareException;


    /**
     * @param up
     * @param expiration
     * @throws SecurityException
     */
    void setUserExpiry ( UserPrincipal up, DateTime expiration ) throws SecurityException;


    /**
     * @param up
     * @param oldPassword
     * @return whether the password is valid
     * @throws FileshareException
     */
    boolean verifyPassword ( UserPrincipal up, String oldPassword ) throws FileshareException;


    /**
     * @param princ
     * @return the mapped user
     * @throws UserNotFoundException
     * @throws FileshareException
     */
    UserInfo getMappedUser ( UserPrincipal princ ) throws FileshareException;


    /**
     * @param userPrincipal
     * @return whether the user exists
     * @throws FileshareException
     */
    boolean userExists ( UserPrincipal userPrincipal ) throws FileshareException;

}