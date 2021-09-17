/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.runtime.security.web.gui;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.CredentialsException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.ops4j.pax.cdi.api.OsgiService;

import eu.agno3.runtime.eventlog.AuditContext;
import eu.agno3.runtime.security.UserLicenseLimitExceededRuntimeException;
import eu.agno3.runtime.security.event.LoginEventBuilder;
import eu.agno3.runtime.security.login.AuthResponse;
import eu.agno3.runtime.security.login.AuthResponseType;
import eu.agno3.runtime.security.login.ChallengeUtils;
import eu.agno3.runtime.security.login.LoginChallenge;
import eu.agno3.runtime.security.login.LoginContext;
import eu.agno3.runtime.security.login.LoginRealm;
import eu.agno3.runtime.security.login.LoginRealmManager;
import eu.agno3.runtime.security.login.LoginSession;
import eu.agno3.runtime.security.login.PasswordChangeLoginChallenge;
import eu.agno3.runtime.security.login.TermsLoginChallenge;
import eu.agno3.runtime.security.login.UsernameLoginChallenge;
import eu.agno3.runtime.security.password.PasswordChangePolicyException;
import eu.agno3.runtime.security.password.PasswordPolicyChecker;
import eu.agno3.runtime.security.password.PasswordPolicyException;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.security.web.ContextUtil;
import eu.agno3.runtime.security.web.login.RedirectLoginChallenge;
import eu.agno3.runtime.security.web.login.WebLoginConfig;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "agsec_loginController" )
public class LoginController {

    /**
     * 
     */
    private static final String REALM = "realm"; //$NON-NLS-1$
    private static final String CUSTOM_RETURN = "authReturn"; //$NON-NLS-1$
    private static final Object RETURN_ID = "authReturnId"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(LoginController.class);

    @Inject
    private LoginSessionBean loginSession;

    @Inject
    private WebLoginConfig loginConfig;

    @Inject
    @OsgiService ( dynamic = true, timeout = 200 )
    private LoginRealmManager loginRealmManager;

    @Inject
    @OsgiService ( dynamic = true, timeout = 200 )
    private PasswordPolicyChecker passwordPolicy;


    /**
     * @return the loginSession
     */
    public LoginSession getLoginSession () {
        return this.loginSession;
    }


    /**
     * @return the loginConfig
     */
    public WebLoginConfig getLoginConfig () {
        return this.loginConfig;
    }


    /**
     * @return the passwordPolicy
     */
    public PasswordPolicyChecker getPasswordPolicy () {
        return this.passwordPolicy;
    }


    /**
     * Should be called in the view displaying the login box
     * 
     * @param ev
     * @throws IOException
     */
    public void preRenderViewListener ( ComponentSystemEvent ev ) throws IOException {
        if ( !checkLoginAllowed() ) {
            return;
        }
        this.redirectToDefaultLoginMethodView();
    }


    /**
     * @return whether login is allowed
     * 
     */
    public boolean checkLoginAllowed () {
        return this.loginRealmManager.getAllowInsecureLogins() || FacesContext.getCurrentInstance().getExternalContext().isSecure();
    }


    /**
     * Redirects to user to the login view for the preferred login method
     * 
     * @throws IOException
     */
    public void redirectToDefaultLoginMethodView () throws IOException {

        Subject subject = SecurityUtils.getSubject();
        if ( subject.isAuthenticated() ) {
            this.redirectToOriginalOrSuccessUrl();
            return;
        }

        String requestedWith = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("X-Requested-With"); //$NON-NLS-1$
        if ( !FacesContext.getCurrentInstance().isPostback() && "XMLHttpRequest".equals(requestedWith) ) { //$NON-NLS-1$
            log.debug("Not a postback, but an AJAX request, sending 403"); //$NON-NLS-1$
            HttpServletResponse resp = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            resp.setStatus(403);
            throw new AuthenticationException();
        }

        boolean interactiveRequest = !FacesContext.getCurrentInstance().isPostback()
                && !FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest();
        if ( interactiveRequest && !handleDisableAutoLogin() ) {
            return;
        }

        if ( interactiveRequest && !handleRealmParam() ) {
            return;
        }

        if ( interactiveRequest && StringUtils.isBlank(this.loginSession.getSelectedRealmId()) && !handlePreferredRealm() ) {
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Selected realm is " + this.getSelectedLoginRealmId()); //$NON-NLS-1$
        }

        doLogin(getDisableAutoLogin());
    }


    /**
     * @return
     */
    private boolean handleDisableAutoLogin () {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String disableAutoLogin = externalContext.getRequestParameterMap().get("disableAutoLogin"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(disableAutoLogin) && Boolean.parseBoolean(disableAutoLogin) ) {
            log.debug("Auto login disabled"); //$NON-NLS-1$
            setDisableAutoLogin(true);
        }
        else {
            setDisableAutoLogin(false);
        }
        return true;
    }


    /**
     * @return
     */
    private boolean handleRealmParam () {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String selectRealm = externalContext.getRequestParameterMap().get(REALM);
        String returnParam = externalContext.getRequestParameterMap().get(CUSTOM_RETURN);
        String returnIdParam = externalContext.getRequestParameterMap().get(RETURN_ID);
        if ( selectRealm != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Found realm param " + selectRealm); //$NON-NLS-1$
            }

            LoginRealm realm = this.loginRealmManager.getRealm(selectRealm);
            if ( realm != null && realm.isApplicable(getLoginContext()) ) {
                if ( "restart".equals(returnParam) ) { //$NON-NLS-1$
                    if ( log.isDebugEnabled() ) {
                        log.debug("Restarting with realm " + realm.getId()); //$NON-NLS-1$
                    }
                    this.loginSession.clear();
                    setSelectedLoginRealmId(realm.getId());
                    doLogin(false);
                    return false;
                }
                if ( !StringUtils.isBlank(returnParam) ) {
                    return handleCustomReturn(returnParam, returnIdParam, realm);
                }
            }
        }

        return true;
    }


    /**
     * @param returnParam
     * @param realm
     */
    private boolean handleCustomReturn ( String returnParam, String returnId, LoginRealm realm ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Return from %s with id %s param %s", realm.getId(), returnId, returnParam)); //$NON-NLS-1$
        }

        RedirectLoginChallenge redir = this.loginSession.getChallenge(RedirectLoginChallenge.class, returnId);

        if ( redir == null ) {
            log.debug("Redirect not found"); //$NON-NLS-1$
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, LoginMessages.get(LoginMessages.FAIL_EXTERNAL), StringUtils.EMPTY));
            loginRealmSelected(null);
            return false;
        }

        redir.setResponse(returnParam);
        redir.markComplete();
        return !doLogin(true);
    }


    protected void handleChallenges ( boolean noAutoLogin ) {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        List<LoginChallenge<?>> challenges = getChallenges();
        if ( log.isDebugEnabled() ) {
            log.debug("Handle challenges " + challenges); //$NON-NLS-1$
        }
        for ( LoginChallenge<?> ch : challenges ) {
            if ( ch instanceof UsernameLoginChallenge ) {
                UsernameLoginChallenge ulc = (UsernameLoginChallenge) ch;
                if ( !ch.isPrompted() && StringUtils.isBlank(ulc.getResponse())
                        && ec.getRequestCookieMap().containsKey(this.loginConfig.getSavedUsernameCookieName()) ) {
                    // restore saved username

                    Cookie su = (Cookie) ec.getRequestCookieMap().get(this.loginConfig.getSavedUsernameCookieName());
                    String uname = su.getValue();
                    if ( log.isDebugEnabled() ) {
                        log.debug("Restoring username " + uname); //$NON-NLS-1$
                    }
                    ulc.setResponse(uname);
                }
            }
            else if ( !noAutoLogin && ch instanceof RedirectLoginChallenge ) {
                RedirectLoginChallenge rlc = (RedirectLoginChallenge) ch;
                if ( ch.isPrompted() ) {
                    continue;
                }

                if ( log.isDebugEnabled() ) {
                    log.debug("Found redirect challenge " + rlc); //$NON-NLS-1$
                    log.debug("Target is " + rlc.getRelativeTarget()); //$NON-NLS-1$
                }

                HttpServletRequest servletRequest = (HttpServletRequest) ec.getRequest();
                String redirectToURI;
                if ( rlc.getAbsoluteTarget() != null ) {
                    redirectToURI = rlc.getAbsoluteTarget().toASCIIString();
                }
                else {
                    redirectToURI = String
                            .format("%s%s%s", servletRequest.getContextPath(), getLoginConfig().getAuthBasePath(), rlc.getRelativeTarget()); //$NON-NLS-1$
                }

                if ( log.isDebugEnabled() ) {
                    log.debug("Final target is " + redirectToURI); //$NON-NLS-1$
                }

                try {
                    ch.markPrompted();
                    ec.redirect(redirectToURI);
                }
                catch ( IOException e ) {
                    log.warn("Redirect challenge failed", e); //$NON-NLS-1$
                }
            }
            else if ( ch instanceof RedirectLoginChallenge ) {
                log.debug("Redirect but no auto login"); //$NON-NLS-1$
            }
            else if ( ch instanceof TermsLoginChallenge ) {
                TermsLoginChallenge tlc = (TermsLoginChallenge) ch;
                if ( !ch.isPrompted() && ch.getResponse() == null && ec.getRequestCookieMap().containsKey("accept-terms-" + ch.getId()) ) { //$NON-NLS-1$
                    tlc.setResponse(true);
                    tlc.markPrompted();
                }
            }
        }
    }


    /**
     * 
     */
    private boolean handlePreferredRealm () {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        if ( externalContext.getRequestCookieMap().containsKey(this.loginConfig.getPreferredRealmCookieName())
                && StringUtils.isBlank(this.loginSession.getSelectedRealmId()) ) {
            Cookie userPreferredLoginMethod = (Cookie) externalContext.getRequestCookieMap().get(this.loginConfig.getPreferredRealmCookieName());

            if ( log.isDebugEnabled() ) {
                log.debug("Found preferred login method cookie " + userPreferredLoginMethod.getValue()); //$NON-NLS-1$
            }

            LoginRealm realm = this.loginRealmManager.getRealm(userPreferredLoginMethod.getValue());

            if ( realm != null && realm.isApplicable(getLoginContext()) ) {
                setSelectedLoginRealmId(realm.getId());
            }
        }
        return true;
    }


    /**
     * @return
     */
    private LoginContext getLoginContext () {
        if ( this.loginSession.getLoginContext() == null ) {
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            LoginContext lctx = ContextUtil
                    .makeLoginContext(getLoginConfig(), request, false, FacesContext.getCurrentInstance().getViewRoot().getLocale());
            this.loginSession.setLoginContext(lctx);
        }
        return this.loginSession.getLoginContext();
    }


    /**
     * 
     * @param r
     * @return the display text for the login realm selection
     */
    public String getLoginRealmDisplayName ( LoginRealm r ) {
        return r.getId();
    }


    /**
     * 
     * @return the selected login realm id
     */
    public String getSelectedLoginRealmId () {
        if ( this.loginSession.getSelectedRealmId() != null ) {
            return this.loginSession.getSelectedRealmId();
        }

        LoginRealm realm = this.loginRealmManager.getStaticDefaultRealm(this.getLoginContext());
        if ( realm == null ) {
            return null;
        }
        return realm.getId();
    }


    /**
     * @return the selected login realm
     */
    public LoginRealm getSelectedLoginRealm () {
        if ( this.loginSession.getSelectedRealmId() != null ) {
            return this.loginRealmManager.getRealm(this.loginSession.getSelectedRealmId());
        }

        return this.loginRealmManager.getStaticDefaultRealm(this.getLoginContext());
    }


    /**
     * 
     * @param realmId
     */
    public void setSelectedLoginRealmId ( String realmId ) {
        this.loginSession.setSelectedRealmId(realmId);
    }


    /**
     * 
     * @return whether to prevent login via automatic mechanisms
     */
    public boolean getDisableAutoLogin () {
        return this.loginSession.getDisabledAutoLogin();
    }


    /**
     * 
     * @param disabledAutoLogin
     */
    public void setDisableAutoLogin ( boolean disabledAutoLogin ) {
        this.loginSession.setDisabledAutoLogin(disabledAutoLogin);
    }


    /**
     * 
     * @return the login realms that are applicable for the requesting user
     */
    public List<LoginRealm> getApplicableLoginRealms () {

        List<String> applicableRealms = this.loginSession.getApplicableRealms();
        if ( applicableRealms == null ) {
            applicableRealms = this.loginRealmManager.getApplicableRealmIds(this.getLoginContext());
            this.loginSession.setApplicableRealms(applicableRealms);
        }

        if ( applicableRealms == null ) {
            return Collections.EMPTY_LIST;
        }

        return this.loginRealmManager.mapRealmIds(applicableRealms);
    }


    /**
     * 
     * @return whether a selection of login realm may be required
     */
    public boolean hasMoreThanOneRealmAvailable () {
        return getApplicableLoginRealms().size() > 1;
    }


    /**
     * @param ev
     */
    public void loginRealmSelected ( AjaxBehaviorEvent ev ) {
        this.loginSession.clear();
        LoginRealm selectedLoginRealm = this.getSelectedLoginRealm();
        if ( selectedLoginRealm == null ) {
            return;
        }
        if ( log.isDebugEnabled() ) {
            log.debug("Selected " + selectedLoginRealm); //$NON-NLS-1$
        }

        this.doLogin();
        handleChallenges(false);
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
     * Reset authentication state
     * 
     * @return null
     */
    public String reset () {
        loginRealmSelected(null);
        return null;
    }


    /**
     * 
     * @return outcome
     */
    public String doLogin () {
        doLogin(false);
        return null;
    }


    /**
     * 
     * @param disableAutoLogin
     * @return outcome
     */
    public boolean doLogin ( boolean disableAutoLogin ) {

        if ( !checkLoginAllowed() ) {
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_FATAL, LoginMessages.get(LoginMessages.DISALLOWED), StringUtils.EMPTY));
            return false;
        }

        LoginRealm realm = this.getSelectedLoginRealm();

        if ( realm == null ) {
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_FATAL, LoginMessages.get(LoginMessages.UNCONFIGURED), StringUtils.EMPTY));
            return false;
        }

        List<LoginChallenge<?>> saved = this.loginSession.getChallenges();
        String uname = ChallengeUtils.getUsername(this.loginSession);

        try {
            doAuthInternal(realm, disableAutoLogin);
        }
        catch ( AuthenticationException e ) {
            handleAuthenticationException(e, realm, this.loginSession);
            this.loginSession.setChallenges(saved);
            this.loginSession.setSelectedRealmId(realm.getId());
            return false;
        }
        catch ( Exception e ) {
            log.warn("Internal authentication error", e); //$NON-NLS-1$
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_FATAL, LoginMessages.get(LoginMessages.FAIL_INTERNAL), StringUtils.EMPTY));
            this.loginSession.setChallenges(saved);
            this.loginSession.setSelectedRealmId(realm.getId());
            return false;
        }

        if ( SecurityUtils.getSubject().isAuthenticated() ) {
            this.saveUsername(uname);
            this.saveAuthMethod(realm.getId());
            this.redirectToOriginalOrSuccessUrl();
            return true;
        }

        return false;

    }


    /**
     * @param realm
     * @param up
     * @param sourceAddress
     * @param audit
     */
    private void doAuthInternal ( LoginRealm realm, boolean disableAutoLogin ) {
        AuthResponse resp;
        try {
            log.debug("Continuing authentication"); //$NON-NLS-1$
            LoginContext lc = this.loginSession.getLoginContext();
            if ( lc == null ) {
                return;
            }
            resp = this.loginRealmManager.authenticate(realm, lc, this.loginSession);
        }
        catch ( UndeclaredThrowableException e ) {
            if ( e.getCause() instanceof InvocationTargetException && e.getCause().getCause() instanceof AuthenticationException ) {
                throw (AuthenticationException) e.getCause().getCause();
            }
            throw e;
        }

        if ( resp != null && resp.getType() == AuthResponseType.COMPLETE && resp.getAuthInfo() != null ) {
            log.debug("Indicates successful auth"); //$NON-NLS-1$
            handleChallenges(false);
        }
        else if ( resp != null && resp.getType() == AuthResponseType.CONTINUE ) {
            log.debug("Need to continue with remaining challenges " + this.loginSession.getChallenges()); //$NON-NLS-1$
            handleChallenges(disableAutoLogin);
        }
        else if ( resp != null && resp.getType() == AuthResponseType.THROTTLE ) {
            return;
        }
        else {
            log.debug("Invalid auth state " + ( resp != null ? resp.getType() : null )); //$NON-NLS-1$
            throw new AuthenticationException("Reached invalid authentication state"); //$NON-NLS-1$
        }
    }


    /**
     * 
     */
    private void saveUsername ( String username ) {
        if ( !StringUtils.isBlank(username) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Saving username " + username); //$NON-NLS-1$
            }
            FacesContext.getCurrentInstance().getExternalContext().addResponseCookie(this.loginConfig.getSavedUsernameCookieName(), username, null);
        }
    }


    /**
     * 
     */
    private void saveAuthMethod ( String realm ) {
        FacesContext.getCurrentInstance().getExternalContext().addResponseCookie(this.loginConfig.getPreferredRealmCookieName(), realm, null);
    }


    /**
     * 
     */
    private void redirectToOriginalOrSuccessUrl () {
        HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        SavedRequest origReq = WebUtils.getAndClearSavedRequest(req);
        String redirectToURI = req.getContextPath() + this.loginConfig.getSuccessUrl();

        if ( origReq != null && this.loginConfig.isDoRedirectToOrigUrl() ) {
            if ( log.isDebugEnabled() ) {
                log.debug(String.format(
                    "Original request URI is %s", //$NON-NLS-1$
                    origReq.getRequestURI()));
            }

            String loginPrefix = req.getContextPath() + this.loginConfig.getAuthBasePath();

            if ( !redirectToURI.startsWith(loginPrefix) ) {
                StringBuilder sb = new StringBuilder();
                sb.append(origReq.getRequestURI());

                if ( origReq.getQueryString() != null && !origReq.getQueryString().isEmpty() ) {
                    sb.append('?');
                    sb.append(origReq.getQueryString());
                }
                redirectToURI = sb.toString();
            }
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Redirecting to URL " + redirectToURI); //$NON-NLS-1$
        }

        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect(redirectToURI);
        }
        catch ( IOException e ) {
            log.warn("Redirection after login failed:", e); //$NON-NLS-1$
        }
    }


    /**
     * @param e
     * @param selectedLoginRealm
     * @param session
     * @param saved
     */
    static void handleAuthenticationException ( AuthenticationException e, LoginRealm selectedLoginRealm, LoginSessionBean session ) {
        String msg = null;
        FacesMessage.Severity severity = FacesMessage.SEVERITY_FATAL;

        if ( selectedLoginRealm.supportPasswordChange() && e instanceof ExpiredCredentialsException ) {
            msg = LoginMessages.get(LoginMessages.PW_CHANGE_REQUIRED);
            severity = FacesMessage.SEVERITY_WARN;
        }
        else if ( e instanceof ExpiredCredentialsException ) {
            msg = LoginMessages.get(LoginMessages.PW_EXPIRED);
        }
        else if ( e instanceof UnknownAccountException || e instanceof CredentialsException ) {
            msg = LoginMessages.get(LoginMessages.FAIL_WRONG_CREDENTIALS);
        }
        else if ( e instanceof DisabledAccountException ) {
            msg = LoginMessages.get(LoginMessages.FAIL_DISABLED_ACCOUNT);
        }
        else if ( e instanceof PasswordChangePolicyException ) {
            msg = LoginMessages.get(LoginMessages.PW_CHANGE_FAIL_POLICY);
        }
        else if ( e instanceof PasswordPolicyException ) {
            msg = LoginMessages.get(LoginMessages.PW_POLICY_FAIL);
            severity = FacesMessage.SEVERITY_WARN;
        }
        else if ( e instanceof UnsupportedTokenException ) {
            msg = LoginMessages.get(LoginMessages.FAIL_UNAVAILABLE);
        }
        else if ( e instanceof UserLicenseLimitExceededRuntimeException ) {
            msg = LoginMessages.get(LoginMessages.LICENSEEXCEEDED);
        }
        else {
            // if multiple realms are present, failure to authenticate through any realm is indicated by general
            // authentication exception
            log.info("Unhandled auth exception", e); //$NON-NLS-1$
            msg = LoginMessages.get(LoginMessages.FAIL_UNKNOWN);
        }

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, msg, StringUtils.EMPTY));
    }


    /**
     * 
     * @return the logout target outcome
     */
    public String logout () {
        String logOutUrl = this.loginConfig.getLogOutUrl();
        if ( !SecurityUtils.getSubject().isAuthenticated() ) {
            return logOutUrl;
        }
        log.debug("Logging out user"); //$NON-NLS-1$

        try ( AuditContext<LoginEventBuilder> audit = this.loginRealmManager.getAuditContext() ) {
            audit.builder().context(this.loginSession.getLoginContext());
            PrincipalCollection principals = SecurityUtils.getSubject().getPrincipals();
            UserPrincipal princ = principals.oneByType(UserPrincipal.class);
            audit.builder().principal(princ);

            String selectRealm = null;

            if ( princ != null ) {
                String realmName = princ.getRealmName();

                LoginRealm realm = this.loginRealmManager.getRealm(realmName);
                if ( !principals.getRealmNames().contains(realmName) || realm == null ) {
                    // the realm name is not necessary the authentication realm name
                    // fallback to the first realm if this is the case
                    realmName = principals.getRealmNames().iterator().next();
                    realm = this.loginRealmManager.getRealm(realmName);
                }

                if ( log.isDebugEnabled() ) {
                    log.debug("User was logged in using realm " + realmName); //$NON-NLS-1$
                }
                audit.builder().realm(realm);

                if ( realm != null ) {
                    selectRealm = realm.getId();
                }
                else {
                    log.warn("Realm not found " + realmName); //$NON-NLS-1$
                }
            }

            SecurityUtils.getSubject().logout();

            if ( selectRealm != null ) {
                saveAuthMethod(selectRealm);
                setSelectedLoginRealmId(selectRealm);
            }

            audit.builder().status("LOGOUT"); //$NON-NLS-1$
        }
        return logOutUrl;
    }


    /**
     * @param ch
     */
    public void updateEntropyEstimate ( Object ch ) {
        if ( ch instanceof PasswordChangeLoginChallenge ) {
            PasswordChangeLoginChallenge pwch = (PasswordChangeLoginChallenge) ch;
            pwch.setEstimatedChangeEntropy(this.passwordPolicy.estimateEntropy(pwch.getResponse()));
        }
    }
}
