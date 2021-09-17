/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.Set;

import org.joda.time.Duration;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareUserSelfServiceConfig.class )
public interface FileshareUserSelfServiceConfigMutable extends FileshareUserSelfServiceConfig {

    /**
     * 
     * @param passwordRecoveryTokenLifetime
     */
    void setPasswordRecoveryTokenLifetime ( Duration passwordRecoveryTokenLifetime );


    /**
     * 
     * @param localPasswordRecoveryEnabled
     */
    void setLocalPasswordRecoveryEnabled ( Boolean localPasswordRecoveryEnabled );


    /**
     * 
     * @param allowInvitingUserExtension
     */
    void setAllowInvitingUserExtension ( Boolean allowInvitingUserExtension );


    /**
     * 
     * @param trustInvitedUserNames
     */
    void setTrustInvitedUserNames ( Boolean trustInvitedUserNames );


    /**
     * 
     * @param invitationUserRoles
     */
    void setInvitationUserRoles ( Set<String> invitationUserRoles );


    /**
     * 
     * @param invitationUserExpires
     */
    void setInvitationUserExpires ( Boolean invitationUserExpires );


    /**
     * 
     * @param invitationUserExpiration
     */
    void setInvitationUserExpiration ( Duration invitationUserExpiration );


    /**
     * 
     * @param invitationTokenLifetime
     */
    void setInvitationTokenLifetime ( Duration invitationTokenLifetime );


    /**
     * 
     * @param invitationEnabled
     */
    void setInvitationEnabled ( Boolean invitationEnabled );


    /**
     * 
     * @param registrationUserRoles
     */
    void setRegistrationUserRoles ( Set<String> registrationUserRoles );


    /**
     * @param registrationUserExpires
     */
    void setRegistrationUserExpires ( Boolean registrationUserExpires );


    /**
     * 
     * @param registrationUserExpiration
     */
    void setRegistrationUserExpiration ( Duration registrationUserExpiration );


    /**
     * 
     * @param registrationTokenLifetime
     */
    void setRegistrationTokenLifetime ( Duration registrationTokenLifetime );


    /**
     * 
     * @param registrationEnabled
     */
    void setRegistrationEnabled ( Boolean registrationEnabled );

}
