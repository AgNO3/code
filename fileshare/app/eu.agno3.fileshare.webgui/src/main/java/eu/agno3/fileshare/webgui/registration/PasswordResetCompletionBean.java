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
import javax.inject.Named;

import org.apache.shiro.SecurityUtils;

import eu.agno3.fileshare.model.tokens.PasswordResetToken;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.security.web.login.token.TokenPrincipal;


/**
 * @author mbechler
 *
 */
@Named ( "passwordResetCompletionBean" )
@ViewScoped
public class PasswordResetCompletionBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2720654051496002633L;
    private String newPassword;
    private PasswordResetToken token;


    /**
     * 
     */
    @PostConstruct
    public void init () {
        TokenPrincipal tokPrinc = SecurityUtils.getSubject().getPrincipals().oneByType(TokenPrincipal.class);
        if ( tokPrinc == null || ! ( tokPrinc.getData() instanceof PasswordResetToken ) ) {
            return;
        }

        PasswordResetToken tok = (PasswordResetToken) tokPrinc.getData();
        this.token = tok;
    }


    /**
     * @return the token
     */
    public PasswordResetToken getToken () {
        return this.token;
    }


    /**
     * @return the user principal under reset
     */
    public UserPrincipal getUserPrincipal () {
        if ( this.token == null ) {
            return null;
        }
        return this.token.getPrincipal();
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
     * 
     */
    public void invalidate () {
        this.token = null;
    }

}
