/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.registration;


import java.lang.reflect.UndeclaredThrowableException;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.session.Session;
import org.apache.shiro.web.util.WebUtils;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.MailRateLimitingException;
import eu.agno3.fileshare.exceptions.NotificationException;
import eu.agno3.fileshare.exceptions.RegistrationOpenException;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.notify.MailRecipient;
import eu.agno3.fileshare.model.query.SubjectQueryResult;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;
import eu.agno3.runtime.security.password.PasswordPolicyException;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.security.token.RealmUserPasswordToken;


/**
 * @author mbechler
 *
 */
@Named ( "registrationController" )
@ApplicationScoped
public class RegistrationController {

    private static final Logger log = Logger.getLogger(RegistrationController.class);

    @Inject
    private FileshareServiceProvider fsp;


    /**
     * 
     * @param bean
     * @return outcome
     */
    public String register ( UserRegistrationBean bean ) {

        try {
            for ( String accepted : bean.getAcceptedTerms() ) {
                this.fsp.getTermsService().markAccepted(null, accepted);
            }
            MailRecipient recpt = bean.makeRecipient();
            recpt.setDesiredLocale(FacesContext.getCurrentInstance().getViewRoot().getLocale());
            this.fsp.getRegistrationService().register(recpt, bean.getResend());
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {

            Exception unwrapped = ExceptionHandler.unwrapException(e);
            if ( unwrapped instanceof RegistrationOpenException ) {
                bean.setAlreadySent(true);
            }
            else if ( unwrapped instanceof MailRateLimitingException ) {
                bean.setThrottleDelay( ( (MailRateLimitingException) unwrapped ).getDelay());
                return null;
            }

            ExceptionHandler.handleException(e);
            return null;
        }

        return "/auth/registrationComplete.xhtml?faces-redirect=true"; //$NON-NLS-1$
    }


    /**
     * 
     * @param bean
     * @return dialog close if successful
     */
    public String invite ( UserRegistrationBean bean ) {
        try {
            MailRecipient recpt = bean.makeRecipient();
            recpt.setDesiredLocale(bean.getLocale());
            User r = this.fsp.getRegistrationService().invite(recpt, bean.getSubject(), bean.getResend(), bean.getExpires());
            bean.reset();
            SubjectQueryResult res = SubjectQueryResult.fromSubject(r);
            res.setTrustLevel(this.fsp.getConfigurationProvider().getTrustLevelConfiguration().getTrustLevel(r).getId());
            return DialogContext.closeDialog(res);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {

            Exception unwrapped = ExceptionHandler.unwrapException(e);
            if ( unwrapped instanceof RegistrationOpenException ) {
                bean.setAlreadySent(true);
            }
            else if ( unwrapped instanceof MailRateLimitingException ) {
                bean.setThrottleDelay( ( (MailRateLimitingException) unwrapped ).getDelay());
                return null;
            }
            ExceptionHandler.handleException(e);

        }
        return null;
    }


    /**
     * 
     * @param bean
     * @return to home if successful
     */
    public String complete ( RegistrationCompletionBean bean ) {
        String userName = bean.getUserName();
        String password = bean.getNewPassword();
        try {
            User u = this.fsp.getRegistrationService().completeRegistration(userName, password, bean.getUserDetails());
            this.fsp.getTermsService().persistTemporary(u.getPrincipal());
        }
        catch (
            FileshareException |
            UndeclaredThrowableException |
            PasswordPolicyException e ) {
            ExceptionHandler.handleException(e);
            return null;
        }
        return authenticateAsNewUser(userName, password);
    }


    /**
     * @param userName
     * @param password
     * @return
     */
    private static String authenticateAsNewUser ( String userName, String password ) {
        try {
            // TODO: this requires some work for multi step authentication
            killSession();
            RealmUserPasswordToken tok = new RealmUserPasswordToken(userName, password);
            SecurityUtils.getSubject().login(tok);
        }
        catch (
            AuthenticationException |
            UndeclaredThrowableException e ) {
            log.warn("Failed to login as newly created user", e); //$NON-NLS-1$
        }

        return "/registration/done.xhtml?faces-redirect=true"; //$NON-NLS-1$
    }


    /**
     * 
     * @param bean
     * @return outcome
     */
    public String recoverPassword ( PasswordRecoveryBean bean ) {

        try {
            this.fsp.getRegistrationService().resetPassword(bean.getUserName(), bean.getMailAddress(), bean.getResend());
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {

            Exception unwrapped = ExceptionHandler.unwrapException(e);
            if ( unwrapped instanceof NotificationException ) {
                bean.setAlreadySent(true);
            }
            else if ( unwrapped instanceof MailRateLimitingException ) {
                bean.setThrottleDelay( ( (MailRateLimitingException) unwrapped ).getDelay());
                return null;
            }
            ExceptionHandler.handleException(e);
            return null;
        }

        return "/auth/recoverPasswordComplete.xhtml?faces-redirect=true"; //$NON-NLS-1$
    }


    /**
     * 
     * @param bean
     * @return outcome
     */
    public String completePasswordReset ( PasswordResetCompletionBean bean ) {
        UserPrincipal principal = bean.getUserPrincipal();
        String password = bean.getNewPassword();
        try {
            this.fsp.getRegistrationService().completePasswordReset(password);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
            return null;
        }

        try {
            killSession();
            RealmUserPasswordToken tok = new RealmUserPasswordToken(principal.getUserName(), password);
            SecurityUtils.getSubject().login(tok);
        }
        catch (
            AuthenticationException |
            UndeclaredThrowableException e ) {
            log.warn("Failed to login as reset created user", e); //$NON-NLS-1$
        }

        return "/index.xhtml?faces-redirect=true"; //$NON-NLS-1$
    }


    /**
     * 
     * @param reg
     * @return null
     */
    public String generateInvitationPreview ( UserRegistrationBean reg ) {
        try {
            reg.setInPreview(true);
            reg.setMessagePreview(this.fsp.getRegistrationService().getInvitationPreview(reg.makeRecipient()));
        }
        catch (
            FileshareException |
            UndeclaredThrowableException ex ) {
            ExceptionHandler.handleException(ex);
            reg.setMessagePreview(null);
        }
        return null;
    }


    /**
     * 
     * @param reg
     * @return null
     */
    public String closeInvitationPreview ( UserRegistrationBean reg ) {
        reg.setInPreview(false);
        reg.setMessagePreview(null);
        return null;
    }


    /**
     * 
     * @return outcome
     */
    public String backToLogin () {
        return "/auth/index.xhtml?faces-redirect=true"; //$NON-NLS-1$
    }


    /**
     * Destroy current session and create a new one
     */
    private static void killSession () {
        Session session = SecurityUtils.getSubject().getSession();
        if ( session != null ) {
            // make sure not to lose the saved request
            Object savedRequest = session.getAttribute(WebUtils.SAVED_REQUEST_KEY);
            session.stop();
            session = SecurityUtils.getSubject().getSession(true);
            session.setAttribute(WebUtils.SAVED_REQUEST_KEY, savedRequest);
        }
    }
}
