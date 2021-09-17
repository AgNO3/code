/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.fileshare.service.admin;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.AccessDeniedException;
import eu.agno3.fileshare.exceptions.AuthenticationException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.UserLimitExceededException;
import eu.agno3.fileshare.exceptions.UserNotFoundException;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.UserCreateData;
import eu.agno3.fileshare.model.UserDetails;
import eu.agno3.runtime.jmx.MBean;
import eu.agno3.runtime.security.password.PasswordPolicyException;
import eu.agno3.runtime.security.principal.UserInfo;


/**
 * @author mbechler
 *
 */
public interface UserServiceMBean extends MBean {

    /**
     * Get the current user object
     * 
     * The user will be created if it does not yet exist
     * 
     * Access control:
     * - always returns the object for the current user
     * 
     * @return the current user's user object
     * @throws AuthenticationException
     * @throws UserNotFoundException
     */
    User getCurrentUser () throws AuthenticationException, UserNotFoundException;


    /**
     * Get the specified user
     * 
     * Access control:
     * - require manage:subjects:list
     * 
     * @param userId
     * @return the user
     * @throws FileshareException
     */
    User getUser ( UUID userId ) throws FileshareException;


    /**
     * Enable a local user
     * 
     * Access control:
     * - require manage:user:enable
     * 
     * @param userId
     * @throws FileshareException
     */
    void enableLocalUser ( UUID userId ) throws FileshareException;


    /**
     * Disable a local user
     * 
     * Access control:
     * - require manage:user:disable
     * 
     * @param userId
     * @throws FileshareException
     */
    void disableLocalUser ( UUID userId ) throws FileshareException;


    /**
     * Get the local authentication details for user
     * 
     * Access control:
     * - require manage:subjects:list
     * - OR user is current user
     * 
     * @param userId
     * @param principal
     * @return the local user info for the user
     * @throws FileshareException
     * @throws UserNotFoundException
     */
    UserInfo getLocalUserInfo ( UUID userId ) throws FileshareException;


    /**
     * Gets all groups the current user is transitivly member of
     * 
     * Access control:
     * - only returns groups that the user is member of
     * 
     * @return the set of groups the current user is member of
     * @throws FileshareException
     * @throws AuthenticationException
     */
    Set<Group> getCurrentUserGroupClosure () throws FileshareException;


    /**
     * Check whether the current user is member of a given group
     * 
     * Access control:
     * - none required
     * 
     * @param groupId
     * @return whether the current user is member of the given group
     * @throws FileshareException
     */
    boolean isCurrentUserMember ( UUID groupId ) throws FileshareException;


    /**
     * Get all groups the specified user is directly member of
     * 
     * Access control:
     * - require manage:subjects:list
     * 
     * @param userId
     * @return the user's direct memberships
     * @throws FileshareException
     * @throws UserNotFoundException
     */
    List<Group> getUserGroups ( UUID userId ) throws FileshareException;


    /**
     * Get all groups the specified user is transitively member of
     * 
     * Access control:
     * - require manage:subjects:list
     * 
     * @param userId
     * @return the user's effective memberships
     * @throws FileshareException
     * @throws UserNotFoundException
     * @throws AccessDeniedException
     */
    List<Group> getUserGroupClosure ( UUID userId ) throws FileshareException;


    /**
     * Get all users
     * 
     * Access control:
     * - require manage:subjects:list
     * 
     * @param off
     * @param limit
     * @return the known users
     * @throws FileshareException
     */
    List<User> listUsers ( int off, int limit ) throws FileshareException;


    /**
     * Get the total user count
     * 
     * Access control:
     * - require manage:subjects:list
     * 
     * @return the user count
     * @throws FileshareException
     */
    long getUserCount () throws FileshareException;


    /**
     * 
     * Create a local user
     * 
     * Access control:
     * - require manage:users:create
     * 
     * @param userData
     * @return the created user
     * @throws FileshareException
     * @throws UserLimitExceededException
     */
    User createLocalUser ( UserCreateData userData ) throws FileshareException;


    /**
     * Delete users
     * 
     * Access control:
     * - require manage:users:delete
     * 
     * Deletes all user files
     * 
     * @param userIds
     * @throws FileshareException
     */
    void deleteUsers ( List<UUID> userIds ) throws FileshareException;


    /**
     * Change a users password
     * 
     * Access control:
     * - manage:user:changePassword
     * 
     * @param userId
     * @param newPassword
     * @throws FileshareException
     */
    void changePassword ( UUID userId, String newPassword ) throws FileshareException;


    /**
     * Change a users security label
     * 
     * Access control:
     * - manage:user:changeSecurityLabel
     * 
     * @param id
     * @param label
     * @throws FileshareException
     */
    void updateUserLabel ( UUID id, String label ) throws FileshareException;


    /**
     * 
     * Access control:
     * - none
     * 
     * @param oldPassword
     * @param newPassword
     * @throws FileshareException
     * @throws PasswordPolicyException
     */
    void changeCurrentUserPassword ( String oldPassword, String newPassword ) throws FileshareException, PasswordPolicyException;


    /**
     * Fetch user details
     * 
     * Access control:
     * - have manage:subjects:list
     * OR
     * - userID matches current user
     * OR
     * - have subjects:query:details
     * 
     * @param userId
     * @return the user's user details
     * @throws FileshareException
     */
    UserDetails getUserDetails ( UUID userId ) throws FileshareException;


    /**
     * Update user details
     * 
     * Access control:
     * - have manage:users:updateDetails
     * OR
     * - userId matches current user AND have user:updateDetails
     * 
     * 
     * @param userId
     * @param data
     * @return the updated user details
     * @throws FileshareException
     */
    UserDetails updateUserDetails ( UUID userId, UserDetails data ) throws FileshareException;


    /**
     * Update user quota
     * 
     * Access control:
     * - have manage:users:changeQuota
     * 
     * @param userId
     * @param quota
     * @throws FileshareException
     */
    void updateUserQuota ( UUID userId, Long quota ) throws FileshareException;


    /**
     * @param id
     * @throws FileshareException
     */
    void enableUserRoot ( UUID id ) throws FileshareException;


    /**
     * @param id
     * @throws FileshareException
     */
    void disableUserRoot ( UUID id ) throws FileshareException;


    /**
     * 
     * Access control:
     * - have manage:subjects:expiry
     * OR
     * (
     * - allowInvitingUserExtension is enabled in user config
     * AND
     * - the current user is the creator of the target user
     * )
     * 
     * @param id
     * @param expiration
     * @throws FileshareException
     */
    void setUserExpiry ( UUID id, DateTime expiration ) throws FileshareException;

}