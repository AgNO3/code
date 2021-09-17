/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.runtime.security.db;


import java.util.Set;

import org.joda.time.DateTime;

import eu.agno3.runtime.crypto.scrypt.SCryptResult;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.security.SecurityManagementException;
import eu.agno3.runtime.security.principal.UserInfo;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 * @param <TUser>
 *
 */
public interface BaseLocalUserService <TUser extends BaseUser> {

    /**
     * 
     * @param tx
     * @return the known users
     * @throws SecurityManagementException
     */
    Set<TUser> getUsers ( EntityTransactionContext tx ) throws SecurityManagementException;


    /**
     * @param tx
     * @param u
     * @return the user info for the user
     * @throws SecurityManagementException
     */
    UserInfo fetchUser ( EntityTransactionContext tx, UserPrincipal u ) throws SecurityManagementException;


    /**
     * 
     * @param tx
     * @param user
     * @return whether the user exists
     * @throws SecurityManagementException
     */
    boolean userExists ( EntityTransactionContext tx, UserPrincipal user ) throws SecurityManagementException;


    /**
     * 
     * @param tx
     * @param username
     * @param passwordHash
     * @param disabled
     * @param forcePasswordChange
     * @return the user info
     * @throws SecurityManagementException
     */
    UserInfo addUser ( EntityTransactionContext tx, String username, SCryptResult passwordHash ) throws SecurityManagementException;


    /**
     * 
     * @param password
     * @param checkPolicy
     * @return the password hash
     * @throws SecurityManagementException
     */
    SCryptResult generatePasswordHash ( String password, boolean checkPolicy ) throws SecurityManagementException;


    /**
     * 
     * @param tx
     * @param user
     * @param passwordHash
     * @throws SecurityManagementException
     */
    void changePassword ( EntityTransactionContext tx, UserPrincipal user, SCryptResult passwordHash ) throws SecurityManagementException;


    /**
     * 
     * @param tx
     * @param username
     * @throws SecurityManagementException
     */
    void removeUser ( EntityTransactionContext tx, UserPrincipal username ) throws SecurityManagementException;


    /**
     * 
     * @param tx
     * @param user
     * @throws SecurityManagementException
     */
    void enableUser ( EntityTransactionContext tx, UserPrincipal user ) throws SecurityManagementException;


    /**
     * 
     * @param tx
     * @param user
     * @throws SecurityManagementException
     */
    void disableUser ( EntityTransactionContext tx, UserPrincipal user ) throws SecurityManagementException;


    /**
     * 
     * @param tx
     * @param user
     * @param expiry
     * @throws SecurityManagementException
     */
    void setUserExpiry ( EntityTransactionContext tx, UserPrincipal user, DateTime expiry ) throws SecurityManagementException;

}