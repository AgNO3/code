/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.02.2015 by mbechler
 */
package eu.agno3.fileshare.service.config;


import java.util.Collection;
import java.util.Set;

import org.joda.time.Duration;


/**
 * @author mbechler
 *
 */
public interface UserConfiguration {

    /**
     * 
     * @return the default roles for an administratively created user
     */
    Set<String> getDefaultRoles ();


    /**
     * @return whether user self registration is enabled
     */
    boolean isRegistrationEnabled ();


    /**
     * @return the duration a registration token will be valid
     */
    Duration getRegistrationTokenLifetime ();


    /**
     * 
     * @return the user expiration time for self registration users
     */
    Duration getRegistrationUserExpiration ();


    /**
     * 
     * @return the roles a registered user will have assigned
     */
    Set<String> getRegistrationUserRoles ();


    /**
     * @return whether user self registration is enabled
     */
    boolean isInvitationEnabled ();


    /**
     * @return the duration a invitation token will be valid
     */
    Duration getInvitationTokenLifetime ();


    /**
     * @return the user expiration time for invited users
     */
    Duration getInvitationUserExpiration ();


    /**
     * 
     * @return the roles a registered user will have assigned
     */
    Set<String> getInvitationUserRoles ();


    /**
     * @return whether password recovery should be enabled
     */
    boolean isLocalPasswordRecoveryEnabled ();


    /**
     * 
     * @return the duration a password recovery token will be valid
     */
    Duration getPasswordRecoveryTokenLifetime ();


    /**
     * @param realmId
     * @return an URL to link to for allowing users to reset their password
     */
    String getLostPasswordUrl ( String realmId );


    /**
     * @return the set of roles that should be statically synchronized
     */
    Collection<String> getStaticSynchronizationRoles ();


    /**
     * @return whether the real names of invited users are considered trusted
     */
    boolean isTrustInvitedUserNames ();


    /**
     * @return whether to allow the inviting user to extend the invited user's account expiration period
     */
    boolean isAllowInvitingUserExtension ();


    /**
     * @param roles
     * @return whether the roles should have no subject root
     */
    boolean hasNoSubjectRoot ( Set<String> roles );

}
