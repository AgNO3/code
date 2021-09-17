/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.02.2016 by mbechler
 */
package eu.agno3.runtime.security.web.gui;


import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.ops4j.pax.cdi.api.OsgiService;

import eu.agno3.runtime.security.login.AuthResponse;
import eu.agno3.runtime.security.login.AuthResponseType;
import eu.agno3.runtime.security.login.LoginChallenge;
import eu.agno3.runtime.security.login.LoginContext;
import eu.agno3.runtime.security.login.LoginRealm;
import eu.agno3.runtime.security.login.LoginRealmManager;
import eu.agno3.runtime.security.password.PasswordPolicyChecker;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.security.web.ContextUtil;
import eu.agno3.runtime.security.web.login.WebLoginConfig;


/**
 * @author mbechler
 *
 */
@Named ( "agsec_passwordChangeBean" )
@ApplicationScoped
public class PasswordChangeBean implements Serializable {

    private static final Logger log = Logger.getLogger(PasswordChangeBean.class);

    /**
     * 
     */
    private static final long serialVersionUID = 1302270291869217571L;

    @Inject
    @OsgiService ( dynamic = true, timeout = 200 )
    private LoginRealmManager realmManager;

    @Inject
    @OsgiService ( dynamic = true, timeout = 200 )
    private PasswordPolicyChecker passwordPolicy;

    @Inject
    private WebLoginConfig loginConfig;

    @Inject
    private LoginSessionBean loginSession;


    @PostConstruct
    protected void init () {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        this.loginSession.clear();
        this.loginSession.setLoginContext(ContextUtil.makeLoginContext(this.loginConfig, request, false));
    }


    /**
     * @return the loginSession
     */
    public LoginSessionBean getLoginSession () {
        return this.loginSession;
    }


    /**
     * @return the applied password policy
     */
    public PasswordPolicyChecker getPasswordPolicy () {
        return this.passwordPolicy;
    }


    /**
     * 
     * @return the current challenges
     */
    public List<LoginChallenge<?>> getChallenges () {
        return this.loginSession.getChallenges();
    }


    /**
     * 
     * 
     */
    public void throttleComplete () {
        log.debug("Unsetting throttle"); //$NON-NLS-1$
        this.loginSession.setThrottleDelay(null);
    }


    /**
     * @return whether a password change is possible
     */
    public boolean canChangePassword () {
        UserPrincipal up = getUserPrincipal();
        if ( up == null ) {
            return false;
        }

        LoginRealm realm = this.realmManager.getRealm(up.getRealmName());
        if ( realm == null ) {
            return false;
        }
        return realm.supportPasswordChange();
    }


    /**
     * @param successOutcome
     * @return successOutcome, if successful
     */
    public String changePassword ( String successOutcome ) {
        UserPrincipal up = getUserPrincipal();
        if ( up == null ) {
            return null;
        }

        LoginRealm realm = this.realmManager.getRealm(up.getRealmName());
        if ( realm == null ) {
            return null;
        }

        if ( this.loginSession.getThrottleDelay() != null ) {
            return null;
        }

        LoginContext loginContext = this.loginSession.getLoginContext();
        try {
            try {
                AuthResponse resp = this.realmManager.changePassword(realm, up, loginContext, this.loginSession);
                if ( resp != null && resp.getType() == AuthResponseType.COMPLETE && resp.getAuthInfo() != null ) {
                    log.debug("Indicates successful auth"); //$NON-NLS-1$
                    FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, LoginMessages.get(LoginMessages.PW_CHANGED), StringUtils.EMPTY));
                    this.loginSession.clear();
                    return successOutcome;
                }
                else if ( resp != null && resp.getType() == AuthResponseType.CONTINUE ) {
                    log.debug("Need to continue with remaining challenges " + this.loginSession.getChallenges()); //$NON-NLS-1$
                    handleChallenges();
                }
                else {
                    log.debug("Invalid auth state"); //$NON-NLS-1$
                    throw new AuthenticationException();
                }
            }
            catch ( UndeclaredThrowableException e ) {
                if ( e.getCause() instanceof InvocationTargetException && e.getCause().getCause() instanceof AuthenticationException ) {
                    AuthenticationException ae = (AuthenticationException) e.getCause().getCause();
                    log.debug("Wrapped exception", ae); //$NON-NLS-1$
                    throw ae;
                }
                throw e;
            }
        }
        catch ( AuthenticationException e ) {
            LoginController.handleAuthenticationException(e, realm, this.loginSession);
        }
        return null;
    }


    /**
     * @param cancelOutcome
     * @return cancelOutcome
     */
    public String reset ( String cancelOutcome ) {
        this.loginSession.clear();
        return cancelOutcome;
    }


    /**
     * 
     */
    protected void handleChallenges () {

    }


    /**
     * @return the logged in principal
     */
    public UserPrincipal getUserPrincipal () {
        return SecurityUtils.getSubject().getPrincipals().oneByType(UserPrincipal.class);
    }

}
