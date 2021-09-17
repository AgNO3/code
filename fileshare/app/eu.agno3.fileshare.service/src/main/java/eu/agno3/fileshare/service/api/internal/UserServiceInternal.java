/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.02.2015 by mbechler
 */
package eu.agno3.fileshare.service.api.internal;


import java.util.Set;

import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.AuthenticationException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.PasswordChangeException;
import eu.agno3.fileshare.exceptions.UserNotFoundException;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.UserDetails;
import eu.agno3.fileshare.service.admin.UserServiceMBean;
import eu.agno3.runtime.crypto.scrypt.SCryptResult;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.security.UserLicenseLimitExceededException;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public interface UserServiceInternal extends UserServiceMBean {

    /**
     * Get the current user object
     * 
     * The user will be created if it does not yet exist
     * 
     * Access control:
     * - always returns the object for the current user
     * 
     * @param em
     * @return the current user's user object
     * @throws AuthenticationException
     * @throws UserNotFoundException
     */
    User getCurrentUser ( EntityTransactionContext em ) throws AuthenticationException, UserNotFoundException;


    /**
     * @param tx
     * @return the current user's group closure
     * @throws AuthenticationException
     * @throws UserNotFoundException
     */
    Set<Group> getCurrentUserGroupClosure ( EntityTransactionContext tx ) throws AuthenticationException, UserNotFoundException;


    /**
     * Remove the user
     * 
     * @param tx
     * @param user
     */
    void deleteUser ( EntityTransactionContext tx, User user );


    /**
     * @param newPassword
     * @return the hashes password
     * @throws PasswordChangeException
     */
    SCryptResult genPasswordHash ( String newPassword ) throws PasswordChangeException;


    /**
     * @param userName
     * @param disabled
     * @param forcePasswordChange
     * @param noRoot
     * @param expires
     * @param pwHash
     * @param tx
     * @param details
     * @param creator
     * @return the created user
     * @throws FileshareException
     * @throws UserLicenseLimitExceededException
     */
    User createUserInternal ( EntityTransactionContext tx, String userName, boolean disabled, boolean forcePasswordChange, boolean noRoot,
            DateTime expires, SCryptResult pwHash, UserDetails details, User creator ) throws FileshareException, UserLicenseLimitExceededException;


    /**
     * @param tx
     * @param userName
     * @return whether a user with this name does already exist
     * @throws FileshareException
     */
    boolean checkUserExists ( EntityTransactionContext tx, String userName ) throws FileshareException;


    /**
     * @param tx
     * @param principal
     * @param pwHash
     * @throws FileshareException
     * @throws UserNotFoundException
     */
    void changePassword ( EntityTransactionContext tx, UserPrincipal principal, SCryptResult pwHash ) throws FileshareException;


    /**
     * @param tx
     * @param princ
     * @param noSubjectRoot
     * @return a set-up user object
     */
    User makeUserInternal ( EntityTransactionContext tx, UserPrincipal princ );


    /**
     * @param tx
     * @param principal
     * @throws FileshareException
     */
    void enableLocalUser ( EntityTransactionContext tx, User principal ) throws FileshareException;


    /**
     * @param tx
     * @param found
     * @return whether the user is disabled
     * @throws FileshareException
     */
    boolean isUserDisabled ( EntityTransactionContext tx, User found ) throws FileshareException;


    /**
     * @param up
     * @return the user
     * @throws FileshareException
     */
    User ensureUserExists ( UserPrincipal up ) throws FileshareException;

}
