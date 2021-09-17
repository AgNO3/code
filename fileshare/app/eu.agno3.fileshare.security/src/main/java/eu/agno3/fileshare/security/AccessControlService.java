/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.01.2015 by mbechler
 */
package eu.agno3.fileshare.security;


import java.io.Serializable;
import java.security.AccessControlException;
import java.util.Set;

import eu.agno3.fileshare.exceptions.AccessDeniedException;
import eu.agno3.fileshare.exceptions.AuthenticationException;
import eu.agno3.fileshare.exceptions.GrantAuthenticationRequiredException;
import eu.agno3.fileshare.exceptions.UserNotFoundException;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.SubjectGrant;
import eu.agno3.fileshare.model.TokenGrant;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.tokens.AccessToken;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public interface AccessControlService {

    /**
     * Check for access to entity
     * 
     * @param ctx
     * 
     * @param e
     * @param perms
     * @throws AccessDeniedException
     *             if access is denied
     */
    void checkAccess ( VFSContext ctx, VFSEntity e, GrantPermission... perms ) throws AccessDeniedException;


    /**
     * Check for access to entity (if any permission granted)
     * 
     * @param ctx
     * 
     * @param entity
     * @param perms
     * @throws AccessDeniedException
     *             if access is denied
     * @throws GrantAuthenticationRequiredException
     */
    void checkAnyAccess ( VFSContext ctx, VFSEntity entity, GrantPermission... perms )
            throws AccessDeniedException, GrantAuthenticationRequiredException;


    /**
     * @param ctx
     * @param entity
     * @param currentUser
     * @param grant
     * @param perms
     * @throws AccessDeniedException
     * @throws UserNotFoundException
     * @throws AuthenticationException
     */
    void checkUserIsCreatorWithPermRecursive ( VFSContext ctx, VFSEntity entity, User currentUser, Grant grant, GrantPermission... perms )
            throws AccessDeniedException, UserNotFoundException, AuthenticationException;


    /**
     * @param ctx
     * @param entity
     * @param currentUser
     * @param grant
     * @param perms
     * @throws AccessDeniedException
     */
    void checkUserIsCreatorWithPerm ( VFSContext ctx, VFSEntity entity, User currentUser, Grant grant, GrantPermission... perms )
            throws AccessDeniedException;


    /**
     * @param ctx
     * @param entity
     * @param currentUser
     * @param grant
     * @param perms
     * @return whether the current user is the creator of the entities and has the given permissions on them
     * @throws AccessDeniedException
     * @throws UserNotFoundException
     * @throws AuthenticationException
     */
    boolean isUserCreatorWithPermRecursive ( VFSContext ctx, VFSEntity entity, User currentUser, Grant grant, GrantPermission... perms )
            throws AccessDeniedException, UserNotFoundException, AuthenticationException;


    /**
     * @param ctx
     * @param entity
     * @param currentUser
     * @param grant
     * @param perms
     * @return whether the current user is the creator the entities and has thet given permissions
     * @throws AccessDeniedException
     */
    boolean isUserCreatorWithPerm ( VFSContext ctx, VFSEntity entity, User currentUser, Grant grant, GrantPermission... perms )
            throws AccessDeniedException;


    /**
     * Test for read access to an entity
     * 
     * @param ctx
     * 
     * @param e
     * @param perms
     * @return whether all requested permissions are present
     */
    boolean hasAccess ( VFSContext ctx, VFSEntity e, GrantPermission... perms );


    /**
     * @param ctx
     * @param parent
     * @param values
     * @return whether any of the rquested permissions are present
     * @throws GrantAuthenticationRequiredException
     */
    boolean hasAnyAccess ( VFSContext ctx, VFSEntity parent, GrantPermission... values ) throws GrantAuthenticationRequiredException;


    /**
     * Check whether the current user is an owner of the given entity
     * 
     * A user is considered owner if his user is the owner of the object
     * or any group that the user is (transitively) member of owns the object.
     * 
     * @param ctx
     * 
     * @param entity
     * @throws AccessDeniedException
     *             when the user is not an owner
     */
    void checkOwner ( VFSContext ctx, VFSEntity entity ) throws AccessDeniedException;


    /**
     * Test whether the current user is an owner of the given entity
     * 
     * A user is considered owner if his user is the owner of the object
     * or any group that the user is (transitively) member of owns the object.
     * 
     * @param ctx
     * 
     * @param entity
     * @return whether the current user is an owner of the object
     */
    boolean isOwner ( VFSContext ctx, VFSEntity entity );


    /**
     * Check whether the user is a member of the given group (transitive)
     * 
     * @param g
     * @throws AccessDeniedException
     *             when the user is not a member
     */
    void checkMember ( Group g ) throws AccessDeniedException;


    /**
     * Test whether the user is a member of the given group (transitive)
     * 
     * 
     * @param g
     * @return whether the user is a member
     */
    boolean isMember ( Group g );


    /**
     * Get the currently logged in user's user object
     * 
     * @return the current user
     * @throws AuthenticationException
     * @throws UserNotFoundException
     */
    User getCurrentUser () throws AuthenticationException, UserNotFoundException;


    /**
     * Get the currently logged in user's user object
     * 
     * @return the current user
     * @throws AuthenticationException
     * @throws UserNotFoundException
     */
    User getCurrentUserCachable () throws AuthenticationException, UserNotFoundException;


    /**
     * Get the currently logged in user's user object
     * 
     * 
     * @param tx
     * @return the current user
     * @throws AuthenticationException
     * @throws UserNotFoundException
     */
    User getCurrentUser ( EntityTransactionContext tx ) throws AuthenticationException, UserNotFoundException;


    /**
     * Get the currently logged in user's group memberships
     * 
     * @return the groups that the current user is a member of
     * @throws AuthenticationException
     */
    Set<Group> getCurrentUserGroupClosure () throws AuthenticationException;


    /**
     * Get the currently logged in user's group memberships
     * 
     * @param tx
     * 
     * @return the groups that the current user is a member of
     * @throws AuthenticationException
     */
    Set<Group> getCurrentUserGroupClosure ( EntityTransactionContext tx ) throws AuthenticationException;


    /**
     * Check whether the user has a permission
     * 
     * @param perm
     *            permission string, see shiro docs for details
     * @throws AccessDeniedException
     *             if the user does not have the requested permission
     */
    public void checkPermission ( String perm ) throws AccessDeniedException;


    /**
     * Test whether the user has a permission
     * 
     * @param perm
     * @return whether the user has the requested permission
     */
    public boolean hasPermission ( String perm );


    /**
     * Get the user's externally / implicitly mapped roles
     * 
     * @param princ
     * @return the implicitly mapped roles for the user
     */
    Set<String> getMappedRoles ( UserPrincipal princ );


    /**
     * Clear the authorization cache for the given principal
     * 
     * @param princ
     */
    void clearAuthorizationCaches ( UserPrincipal princ );


    /**
     * Check whether the request has a authentication token
     * 
     * @return whether the current request is authenticated via a token
     */
    boolean isTokenAuth ();


    /**
     * Get the value of the authentication token for this request
     * 
     * @return the token auth value
     */
    AccessToken getTokenAuthValue ();


    /**
     * @param tokenValue
     * @return whether the token matches the supplied token
     */
    boolean matchAuthTokenValue ( String tokenValue );


    /**
     * @param ctx
     * @param entity
     * @return the grant for the auth token
     * @throws GrantAuthenticationRequiredException
     */
    TokenGrant getTokenAuthGrant ( VFSContext ctx, VFSEntity entity ) throws GrantAuthenticationRequiredException;


    /**
     * @param ctx
     * @param e
     * @return a valid subject grant for the current user for the given entity
     * @throws AuthenticationException
     */
    SubjectGrant getAnySubjectGrant ( VFSContext ctx, VFSEntity e ) throws AuthenticationException;


    /**
     * @return whether a (real) user is authenticated
     */
    boolean isUserAuthenticated ();


    /**
     * @return the current session id
     */
    Serializable getSessionId ();


    /**
     * @return whether the user has declared intent to perform the action
     * @throws AccessControlException
     */
    boolean haveIntent () throws AccessControlException;


    /**
     * @param tx
     * @param princ
     * @return the user or null if it does not exist
     */
    User findByPrincipal ( EntityTransactionContext tx, UserPrincipal princ );


    /**
     * @return the current user's principal
     * @throws AuthenticationException
     */
    UserPrincipal getCurrentUserPrincipal () throws AuthenticationException;


    /**
     * @param grant
     * @throws AuthenticationException
     */
    void checkGrantPassword ( TokenGrant grant ) throws AuthenticationException;

}
