/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.Set;

import org.joda.time.Duration;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:fileshare:user:selfService" )
public interface FileshareUserSelfServiceConfig extends ConfigurationObject {

    /**
     * 
     * @return lifetime of the password recovery token
     */
    Duration getPasswordRecoveryTokenLifetime ();


    /**
     * 
     * @return enable local password recovery
     */
    Boolean getLocalPasswordRecoveryEnabled ();


    /**
     * 
     * @return allow inviting user to extend the invited user's expiration
     */
    Boolean getAllowInvitingUserExtension ();


    /**
     * 
     * @return mark real names of invited users as verified
     */
    Boolean getTrustInvitedUserNames ();


    /**
     * 
     * @return roles to assign invited users
     */
    Set<String> getInvitationUserRoles ();


    /**
     * @return whether invited user do atomatically expire
     */
    Boolean getInvitationUserExpires ();


    /**
     * 
     * @return amount of time after which the invited user will be expired/removed
     */
    Duration getInvitationUserExpiration ();


    /**
     * 
     * @return lifetime of the invitation token
     */
    Duration getInvitationTokenLifetime ();


    /**
     * 
     * @return invitation enabled
     */
    Boolean getInvitationEnabled ();


    /**
     * 
     * @return roles to assign to registered users
     */
    Set<String> getRegistrationUserRoles ();


    /**
     * @return whether self registered user do atomatically expire
     */
    Boolean getRegistrationUserExpires ();


    /**
     * 
     * @return amount of time after which registered users will be expired/removed
     */
    Duration getRegistrationUserExpiration ();


    /**
     * 
     * @return lifetime of the registration token
     */
    Duration getRegistrationTokenLifetime ();


    /**
     * 
     * @return enable public self registration
     */
    Boolean getRegistrationEnabled ();

}
