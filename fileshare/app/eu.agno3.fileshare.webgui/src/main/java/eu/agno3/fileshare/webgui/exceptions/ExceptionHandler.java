/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.exceptions;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.NoSuchFileException;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.session.Session;
import org.apache.shiro.web.util.WebUtils;

import eu.agno3.fileshare.exceptions.AccessDeniedException;
import eu.agno3.fileshare.exceptions.CannotDeleteCurrentUserException;
import eu.agno3.fileshare.exceptions.ChunkUploadCanceledException;
import eu.agno3.fileshare.exceptions.ContentException;
import eu.agno3.fileshare.exceptions.ContentVirusException;
import eu.agno3.fileshare.exceptions.CyclicMoveTargetException;
import eu.agno3.fileshare.exceptions.DisallowedMimeTypeException;
import eu.agno3.fileshare.exceptions.EntityException;
import eu.agno3.fileshare.exceptions.EntityNameBadCharactersException;
import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.GrantAuthenticationRequiredException;
import eu.agno3.fileshare.exceptions.GrantExistsException;
import eu.agno3.fileshare.exceptions.GroupCyclicException;
import eu.agno3.fileshare.exceptions.GroupNameConflictException;
import eu.agno3.fileshare.exceptions.GroupNameInvalidException;
import eu.agno3.fileshare.exceptions.InconsistentSecurityLabelException;
import eu.agno3.fileshare.exceptions.InsufficentStorageSpaceException;
import eu.agno3.fileshare.exceptions.InvalidEntityNameException;
import eu.agno3.fileshare.exceptions.InvalidPasswordException;
import eu.agno3.fileshare.exceptions.MailRateLimitingException;
import eu.agno3.fileshare.exceptions.NamingConflictException;
import eu.agno3.fileshare.exceptions.PasswordInMessageException;
import eu.agno3.fileshare.exceptions.PolicyNotFulfilledException;
import eu.agno3.fileshare.exceptions.QuotaExceededException;
import eu.agno3.fileshare.exceptions.RegistrationOpenException;
import eu.agno3.fileshare.exceptions.SecurityException;
import eu.agno3.fileshare.exceptions.ShareException;
import eu.agno3.fileshare.exceptions.ShareNotFoundException;
import eu.agno3.fileshare.exceptions.StructureException;
import eu.agno3.fileshare.exceptions.TokenReuseException;
import eu.agno3.fileshare.exceptions.TokenValidationException;
import eu.agno3.fileshare.exceptions.UnsupportedOperationException;
import eu.agno3.fileshare.exceptions.UploadException;
import eu.agno3.fileshare.exceptions.UserExistsException;
import eu.agno3.fileshare.exceptions.UserLimitExceededException;
import eu.agno3.fileshare.webgui.i18n.FileshareMessages;
import eu.agno3.runtime.security.password.PasswordChangePolicyException;
import eu.agno3.runtime.security.password.PasswordPolicyException;
import eu.agno3.runtime.util.format.ByteSizeFormatter;


/**
 * @author mbechler
 *
 */
public class ExceptionHandler {

    private static final Logger log = Logger.getLogger(ExceptionHandler.class);


    /**
     * @param e
     */
    public static void handleException ( Exception e ) {

        Exception ex = unwrapException(e);

        if ( log.isDebugEnabled() ) {
            log.debug("Caught", e); //$NON-NLS-1$
        }

        if ( ex instanceof IOException && ! ( ex instanceof NoSuchFileException ) ) {
            log.warn("IO error", e); //$NON-NLS-1$
            return;
        }

        if ( ex instanceof FileshareException ) {
            handleFileshareException((FileshareException) ex);
        }
        else if ( ex instanceof PasswordPolicyException ) {
            handlePasswordPolicyException((PasswordPolicyException) ex);
        }
        else if ( ex instanceof AuthenticationException ) {
            throw (AuthenticationException) ex;
        }
        else {
            unhandledException(e);
        }
    }


    /**
     * @param e
     * @return unwrapped exception if wrapped (undeclared throwable, invocation target), original otherwise
     */
    public static Exception unwrapException ( Exception e ) {
        if ( e instanceof UndeclaredThrowableException && e.getCause() instanceof InvocationTargetException
                && e.getCause().getCause() instanceof Exception ) {
            return (Exception) e.getCause().getCause();
        }

        return e;
    }


    /**
     * @param e
     */
    private static void handlePasswordPolicyException ( PasswordPolicyException e ) {

        if ( e instanceof PasswordChangePolicyException ) {
            addMessage(FacesMessage.SEVERITY_ERROR, "errors.passwordChangePolicy"); //$NON-NLS-1$
        }
        else {
            addMessage(FacesMessage.SEVERITY_ERROR, "errors.passwordPolicy"); //$NON-NLS-1$
        }
    }


    /**
     * @param e
     */
    private static void unhandledException ( Exception e ) {
        log.error("Unhandled exception", e); //$NON-NLS-1$
        addMessage(FacesMessage.SEVERITY_FATAL, "errors.unknown", e.getMessage(), e.getClass().getName()); //$NON-NLS-1$
    }


    /**
     * @param e
     */
    private static void handleFileshareException ( FileshareException e ) {
        if ( e instanceof UnsupportedOperationException ) {
            addMessage(FacesMessage.SEVERITY_ERROR, "errors.unsupportedOperation"); //$NON-NLS-1$
        }
        else if ( e instanceof ChunkUploadCanceledException ) {
            addMessage(FacesMessage.SEVERITY_ERROR, "errors.uploadCancelRemote"); //$NON-NLS-1$
        }
        else if ( e instanceof UploadException ) {
            addMessage(FacesMessage.SEVERITY_ERROR, "errors.uploadFailed"); //$NON-NLS-1$
        }
        else if ( e instanceof InsufficentStorageSpaceException ) {
            addMessage(FacesMessage.SEVERITY_ERROR, "errors.insufficientStorage"); //$NON-NLS-1$
        }
        else if ( e instanceof EntityException ) {
            handleEntityException((EntityException) e);
        }
        else if ( e instanceof ShareException ) {
            handleShareException((ShareException) e);
        }
        else if ( e instanceof SecurityException ) {
            handleSecurityException((SecurityException) e);
        }
        else if ( e instanceof ContentException ) {
            handleContentException((ContentException) e);
        }
        else if ( e instanceof RegistrationOpenException ) {
            handleRegistrationOpenException((RegistrationOpenException) e);
        }
        else if ( e instanceof MailRateLimitingException ) {
            handleMailRateLimitException((MailRateLimitingException) e);
        }
        else {
            unhandledException(e);
        }
    }


    /**
     * @param e
     */
    private static void handleMailRateLimitException ( MailRateLimitingException e ) {
        addMessage(FacesMessage.SEVERITY_ERROR, "errors.mailRateLimit", e.getDelay()); //$NON-NLS-1$
    }


    /**
     * @param e
     */
    private static void handleRegistrationOpenException ( RegistrationOpenException e ) {
        addMessage(FacesMessage.SEVERITY_WARN, "errors.registrationOpen"); //$NON-NLS-1$
    }


    /**
     * @param e
     */
    private static void handleContentException ( ContentException e ) {
        if ( e instanceof ContentVirusException ) {
            addMessage(FacesMessage.SEVERITY_ERROR, "errors.virusDetected", ( (ContentVirusException) e ).getSignature()); //$NON-NLS-1$
        }
        else if ( e instanceof DisallowedMimeTypeException ) {
            addMessage(FacesMessage.SEVERITY_ERROR, "errors.disallowedMimeType", ( (DisallowedMimeTypeException) e ).getMimeType()); //$NON-NLS-1$
        }
        else {
            unhandledException(e);
        }
    }


    /**
     * @param e
     */
    private static void handleShareException ( ShareException e ) {

        if ( e instanceof ShareNotFoundException && !SecurityUtils.getSubject().isAuthenticated() ) {
            throw new AuthenticationException("The used share is no longer valid", e); //$NON-NLS-1$
        }
        else if ( e instanceof ShareNotFoundException ) {
            addMessage(FacesMessage.SEVERITY_ERROR, "errors.shareRevoked"); //$NON-NLS-1$
        }
    }


    /**
     * @param e
     */
    private static void handleSecurityException ( SecurityException e ) {

        if ( e instanceof AccessDeniedException ) {
            log.debug("Caught access denied exception", e); //$NON-NLS-1$
            addMessage(FacesMessage.SEVERITY_ERROR, "errors.accessDenied"); //$NON-NLS-1$
        }
        else if ( e instanceof UserExistsException ) {
            addMessage(FacesMessage.SEVERITY_ERROR, "errors.userExists"); //$NON-NLS-1$
        }
        else if ( e instanceof GroupNameConflictException ) {
            addMessage(FacesMessage.SEVERITY_ERROR, "errors.groupNameConflict", ( (GroupNameConflictException) e ).getInvalidName()); //$NON-NLS-1$
        }
        else if ( e instanceof GroupNameInvalidException ) {
            addMessage(FacesMessage.SEVERITY_ERROR, "errors.groupNameInvalid", ( (GroupNameInvalidException) e ).getInvalidName()); //$NON-NLS-1$
        }
        else if ( e instanceof GroupCyclicException ) {
            addMessage(FacesMessage.SEVERITY_ERROR, "errors.groupCyclic"); //$NON-NLS-1$
        }
        else if ( e instanceof InvalidPasswordException ) {
            addMessage(FacesMessage.SEVERITY_ERROR, "errors.passwordInvalid"); //$NON-NLS-1$
        }
        else if ( e instanceof PolicyNotFulfilledException ) {
            log.debug("Policy violation " + ( (PolicyNotFulfilledException) e ).getViolation().getKey()); //$NON-NLS-1$
            addMessage(FacesMessage.SEVERITY_ERROR, "errors.policyNotFulfilled"); //$NON-NLS-1$
        }
        else if ( e instanceof PasswordInMessageException ) {
            addMessage(FacesMessage.SEVERITY_ERROR, "errors.passwordInMessage"); //$NON-NLS-1$
        }
        else if ( e instanceof CannotDeleteCurrentUserException ) {
            addMessage(FacesMessage.SEVERITY_ERROR, "errors.cannotDeleteCurrentUser"); //$NON-NLS-1$
        }
        else if ( e instanceof TokenReuseException ) {
            addMessage(FacesMessage.SEVERITY_ERROR, "errors.tokenReuse"); //$NON-NLS-1$
        }
        else if ( e instanceof TokenValidationException ) {
            addMessage(FacesMessage.SEVERITY_ERROR, "errors.tokenInvalid"); //$NON-NLS-1$
        }
        else if ( e instanceof GrantAuthenticationRequiredException ) {
            handleGrantAuthRequiredException((GrantAuthenticationRequiredException) e);
        }
        else if ( e instanceof InconsistentSecurityLabelException ) {
            handleInconsistentSecurityLabelException((InconsistentSecurityLabelException) e);
        }
        else if ( e instanceof UserLimitExceededException ) {
            addMessage(FacesMessage.SEVERITY_ERROR, "errors.userLimitExceeded"); //$NON-NLS-1$
        }
        else if ( e instanceof eu.agno3.fileshare.exceptions.AuthenticationException ) {
            // logout user and rethrow as runtime exception for the outer handlers to handle
            log.debug("Logging out"); //$NON-NLS-1$
            if ( SecurityUtils.getSubject().isAuthenticated() ) {
                SecurityUtils.getSubject().logout();
            }
            else {
                ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
                HttpServletRequest request = (HttpServletRequest) ectx.getRequest();
                WebUtils.saveRequest(request);
                String target = String.format("%s/auth/index.xhtml?disableAutoLogin=true", request.getContextPath()); //$NON-NLS-1$
                try {
                    ectx.redirect(target);
                }
                catch ( IOException ex ) {
                    log.warn("Failed to redirect to login", ex); //$NON-NLS-1$
                }
            }
            throw new AuthenticationException(e);
        }
        else {
            unhandledException(e);
        }
    }


    /**
     * @param e
     */
    private static void handleInconsistentSecurityLabelException ( InconsistentSecurityLabelException e ) {
        addMessage(
            FacesMessage.SEVERITY_ERROR,
            "errors.securityLabel.inconsitent", //$NON-NLS-1$
            StringUtils.join(e.getBlockers().keySet(), ", ")); //$NON-NLS-1$
    }


    /**
     * @param e
     */
    private static void handleGrantAuthRequiredException ( GrantAuthenticationRequiredException e ) {
        log.debug("Need password for grant"); //$NON-NLS-1$
        Session session = SecurityUtils.getSubject().getSession(false);
        if ( session != null ) {
            session.removeAttribute("grantpw_" + e.getGrant().getId()); //$NON-NLS-1$
        }

        log.debug("Redirect to password page"); //$NON-NLS-1$
        ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) ectx.getRequest();
        WebUtils.saveRequest(request);

        String token = request.getParameter("token"); //$NON-NLS-1$
        String target = String.format("%s/tokenAuth/login.xhtml?grant=%s&token=%s", request.getContextPath(), e.getGrant().getId(), token); //$NON-NLS-1$
        try {
            ectx.redirect(target);
        }
        catch ( IOException ex ) {
            log.warn("Failed to redirect to token login", ex); //$NON-NLS-1$
        }
        return;

    }


    /**
     * @param e
     */
    public static void handleEntityException ( EntityException e ) {

        if ( e instanceof QuotaExceededException ) {
            handleQuotaExceededException((QuotaExceededException) e);
        }
        else if ( e instanceof InvalidEntityNameException ) {
            handleEntityNameException((InvalidEntityNameException) e);
        }
        else if ( e instanceof StructureException ) {
            handleStructureException((StructureException) e);
        }
        else if ( e instanceof GrantExistsException ) {
            handleGrantExistsException((GrantExistsException) e);
        }
        else if ( e instanceof EntityNotFoundException ) {
            addMessage(FacesMessage.SEVERITY_ERROR, "errors.entityNotFound"); //$NON-NLS-1$
        }
        else {
            unhandledException(e);
        }
    }


    /**
     * @param e
     */
    private static void handleGrantExistsException ( GrantExistsException e ) {
        addMessage(FacesMessage.SEVERITY_ERROR, "errors.grantExists"); //$NON-NLS-1$
    }


    /**
     * @param e
     */
    private static void handleQuotaExceededException ( QuotaExceededException e ) {
        addMessage(
            FacesMessage.SEVERITY_ERROR,
            "errors.quotaExceeded", //$NON-NLS-1$
            ByteSizeFormatter.formatByteSizeSI(e.getQuota()),
            ByteSizeFormatter.formatByteSizeSI(e.getExceedBy()));
    }


    /**
     * @param e
     */
    private static void handleStructureException ( StructureException e ) {
        if ( e instanceof CyclicMoveTargetException ) {
            addMessage(FacesMessage.SEVERITY_ERROR, "errors.cyclicMove"); //$NON-NLS-1$
        }
        else {
            addMessage(FacesMessage.SEVERITY_ERROR, "errors.structure"); //$NON-NLS-1$
        }
    }


    /**
     * @param e
     */
    private static void handleEntityNameException ( InvalidEntityNameException e ) {
        if ( e instanceof EntityNameBadCharactersException ) {
            EntityNameBadCharactersException be = (EntityNameBadCharactersException) e;
            addMessage(FacesMessage.SEVERITY_ERROR, "errors.invalidEntityNameBadChars", be.getInvalidName(), be.getBadCharsStr()); //$NON-NLS-1$
        }
        else if ( e instanceof NamingConflictException ) {
            if ( !StringUtils.isBlank( ( (NamingConflictException) e ).getDirectoryName()) ) {
                addMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "errors.namingConflictWithParent", //$NON-NLS-1$
                    ( (NamingConflictException) e ).getInvalidName(),
                    ( (NamingConflictException) e ).getDirectoryName());
            }
            else {
                addMessage(FacesMessage.SEVERITY_ERROR, "errors.namingConflict", ( (NamingConflictException) e ).getInvalidName()); //$NON-NLS-1$
            }
        }
        else {
            addMessage(FacesMessage.SEVERITY_ERROR, "errors.invalidEntityName", e.getInvalidName()); //$NON-NLS-1$
        }
    }


    /**
     * @param severity
     * @param msgId
     * @param args
     */
    public static void addMessage ( Severity severity, String msgId, Object... args ) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, FileshareMessages.format(msgId, args), StringUtils.EMPTY));
    }

}
