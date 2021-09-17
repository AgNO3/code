/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.registration;


import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.shiro.SecurityUtils;

import eu.agno3.fileshare.model.UserDetails;
import eu.agno3.fileshare.model.tokens.RegistrationToken;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.runtime.security.web.login.token.TokenPrincipal;


/**
 * @author mbechler
 *
 */
@Named ( "registrationCompletionBean" )
@ViewScoped
public class RegistrationCompletionBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2720654051496002633L;
    private String userName;
    private String newPassword;
    private UserDetails userDetails;
    private RegistrationToken token;

    @Inject
    private FileshareServiceProvider fsp;


    /**
     * 
     */
    @PostConstruct
    public void init () {
        TokenPrincipal tokPrinc = SecurityUtils.getSubject().getPrincipals().oneByType(TokenPrincipal.class);
        if ( tokPrinc == null || ! ( tokPrinc.getData() instanceof RegistrationToken ) ) {
            return;
        }

        RegistrationToken tok = (RegistrationToken) tokPrinc.getData();

        this.userDetails = new UserDetails();
        this.userDetails.setPreferredName(tok.getRecipient().getFullName());

        if ( this.userDetails.getPreferredName() != null && tok.getInvitingUserId() != null
                && this.fsp.getConfigurationProvider().getUserConfig().isTrustInvitedUserNames() ) {
            this.userDetails.setPreferredNameVerified(true);
        }

        this.userDetails.setSalutationName(tok.getRecipient().getCallingName());
        this.userDetails.setMailAddress(tok.getRecipient().getMailAddress());
        this.userName = tok.getUserName();
        this.token = tok;
    }


    /**
     * 
     * @return whether this is an invitation
     */
    public boolean isInvitation () {
        if ( this.token == null ) {
            return false;
        }

        return this.token.getInvitingUserId() != null;
    }


    /**
     * 
     * @return whether the user will get a user root
     */
    public boolean haveUserRoot () {
        if ( isInvitation() ) {
            return !this.fsp.getConfigurationProvider().getUserConfig()
                    .hasNoSubjectRoot(this.fsp.getConfigurationProvider().getUserConfig().getInvitationUserRoles());
        }
        return !this.fsp.getConfigurationProvider().getUserConfig()
                .hasNoSubjectRoot(this.fsp.getConfigurationProvider().getUserConfig().getRegistrationUserRoles());
    }


    /**
     * 
     * @return the inviting user name
     */
    public String getInvitingUserName () {
        if ( this.token == null ) {
            return null;
        }

        return this.token.getInvitingUserDisplayName();
    }


    /**
     * @return the userName
     */
    public String getUserName () {
        return this.userName;
    }


    /**
     * @param userName
     *            the userName to set
     */
    public void setUserName ( String userName ) {
        this.userName = userName;
    }


    /**
     * @return the newPassword
     */
    public String getNewPassword () {
        return this.newPassword;
    }


    /**
     * @param newPassword
     *            the newPassword to set
     */
    public void setNewPassword ( String newPassword ) {
        this.newPassword = newPassword;
    }


    /**
     * @return the userDetails
     */
    public UserDetails getUserDetails () {
        return this.userDetails;
    }


    /**
     * @return the token
     */
    public RegistrationToken getToken () {
        return this.token;
    }


    /**
     * 
     */
    public void invalidate () {
        this.token = null;
    }

}
