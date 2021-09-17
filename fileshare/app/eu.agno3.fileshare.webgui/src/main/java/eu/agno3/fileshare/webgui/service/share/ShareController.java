/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.share;


import java.lang.reflect.UndeclaredThrowableException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.MailRateLimitingException;
import eu.agno3.fileshare.exceptions.PolicyNotFulfilledException;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.MailGrant;
import eu.agno3.fileshare.model.PolicyViolation;
import eu.agno3.fileshare.model.ShareProperties;
import eu.agno3.fileshare.model.SubjectGrant;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.i18n.FileshareMessages;
import eu.agno3.fileshare.webgui.policy.PolicyBean;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.subject.ShareSubjectAutoCompleteBean;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;


/**
 * @author mbechler
 *
 */
@Named ( "shareController" )
@ApplicationScoped
public class ShareController {

    private static final Logger log = Logger.getLogger(ShareController.class);

    @Inject
    private FileshareServiceProvider fsp;

    @Inject
    private FileSharesBean fileShares;

    @Inject
    private ShareSubjectAutoCompleteBean subjAutoComplete;

    @Inject
    private ShareTabsBean shareTabs;

    @Inject
    private TokenSharesBean tokenShare;

    @Inject
    private PolicyBean policyBean;

    @Inject
    private ShareThrottleBean throttle;


    /**
     * 
     * @param e
     * @param share
     * @return outcome
     */
    public String shareEntityToSubjects ( VFSEntity e, SubjectShareBean share ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Sharing %s to %s", e, share.getShareTo())); //$NON-NLS-1$
        }

        if ( share.getShareTo() == null ) {
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, FileshareMessages.get(FileshareMessages.MISSING_SHARE_USER), StringUtils.EMPTY));
            return null;
        }

        try {
            ShareProperties props = share.getShareProperties();
            if ( log.isDebugEnabled() ) {
                log.debug("Permissions are " + props.getPermissions()); //$NON-NLS-1$
                log.debug("Expiration is " + props.getExpiry()); //$NON-NLS-1$
            }
            List<SubjectGrant> issued = this.fsp.getShareService()
                    .shareToSubjects(e.getEntityKey(), Arrays.asList(share.getShareTo().getId()), props, share.getSendNotification());
            this.fileShares.refresh();
            share.reset();
            if ( issued.isEmpty() ) {
                FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(
                        FacesMessage.SEVERITY_INFO,
                        FileshareMessages.get(FileshareMessages.REUSED_EXISTING_SUBJECT_SHARE),
                        StringUtils.EMPTY));
                this.subjAutoComplete.setValue(null);
                return null;
            }

            share.reset();
            this.subjAutoComplete.setValue(null);
            return makeShareUrl(e);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException ex ) {
            handleException(ex);
        }

        return null;
    }


    /**
     * @return
     */
    private String makeShareUrl ( VFSEntity e ) {
        if ( !this.fileShares.getFileSelection().isFromPicker() ) {
            return null;
        }

        return String.format(
            "/actions/share.xhtml?faces-redirect=true&entity=%s&tab=%s&returnTo=%s", //$NON-NLS-1$
            e.getEntityKey(),
            this.shareTabs.getTab() != null ? this.shareTabs.getTab() : StringUtils.EMPTY,
            DialogContext.getCurrentParent());
    }


    /**
     * @param e
     * @param share
     * @return outcome
     */
    public String shareEntityByMail ( VFSEntity e, MailShareBean share ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Sharing %s to %s", e, share.getShareMailAddress())); //$NON-NLS-1$
        }

        if ( StringUtils.isEmpty(share.getShareMailAddress()) ) {
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, FileshareMessages.get(FileshareMessages.MISSING_MAIL_ADDRESS), StringUtils.EMPTY));
            return null;
        }

        if ( !share.isPasswordProtected() ) {
            share.setPassword(null);
        }

        try {
            List<MailGrant> issued = this.fsp.getShareService()
                    .shareByMail(e.getEntityKey(), share.makeRecipients(), share.getShareProperties(), share.getResend());

            this.fileShares.refresh();
            if ( issued.isEmpty() ) {
                FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, FileshareMessages.get(FileshareMessages.REUSED_EXISTING_SHARE), StringUtils.EMPTY));
                share.setMayResend(true);
                return null;
            }

            share.reset();
            return makeShareUrl(e);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException ex ) {
            handleException(ex);
        }

        return null;
    }


    /**
     * 
     * @param e
     * @param share
     * @return null
     */
    public String generateMailPreview ( VFSEntity e, MailShareBean share ) {
        try {
            share.setInPreview(true);
            share.setMessagePreview(
                this.fsp.getShareService().getMailSharePreview(e.getEntityKey(), share.makeRecipient(), share.getShareProperties()));
        }
        catch (
            FileshareException |
            UndeclaredThrowableException ex ) {
            ExceptionHandler.handleException(ex);
            share.setMessagePreview(null);
        }
        return null;
    }


    /**
     * 
     * @param share
     * @return null
     */
    public String closeMailPreview ( MailShareBean share ) {
        share.setInPreview(false);
        share.setMessagePreview(null);
        return null;
    }


    /**
     * 
     * @param e
     * @param share
     * @return null
     */
    public String generateSubjectPreview ( VFSEntity e, SubjectShareBean share ) {
        if ( share == null || share.getShareTo() == null ) {
            return null;
        }

        try {
            share.setInPreview(true);
            share.setMessagePreview(
                this.fsp.getShareService().getSubjectSharePreview(e.getEntityKey(), share.getShareTo().getId(), share.getShareProperties()));
        }
        catch (
            FileshareException |
            UndeclaredThrowableException ex ) {
            ExceptionHandler.handleException(ex);
            share.setMessagePreview(null);
        }
        return null;
    }


    /**
     * 
     * @param share
     * @return null
     */
    public String closeSubjectPreview ( SubjectShareBean share ) {
        share.setInPreview(false);
        share.setMessagePreview(null);
        return null;
    }


    /**
     * @param e
     * @param shares
     * @return outcome
     */
    public String shareEntityByLink ( VFSEntity e, TokenSharesBean shares ) {

        if ( StringUtils.isEmpty(shares.getTokenIdentifier()) ) {
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, FileshareMessages.get(FileshareMessages.MISSING_TOKEN_IDENTIFIER), StringUtils.EMPTY));
            return null;
        }

        if ( !shares.isPasswordProtected() ) {
            shares.setPassword(null);
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Sharing %s via link", e)); //$NON-NLS-1$
        }

        try {
            shares.setGeneratedTokenShare(
                this.fsp.getShareService().shareToken(e.getEntityKey(), shares.getTokenIdentifier(), shares.getShareProperties()));
            shares.setTokenPassword(shares.getPassword());
            this.fileShares.refresh();
            shares.reset();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException ex ) {
            handleException(ex);
        }

        return null;
    }


    /**
     * @param ex
     */
    private void handleException ( Exception ex ) {

        Exception e = ExceptionHandler.unwrapException(ex);

        PolicyViolation violation = null;
        if ( e instanceof PolicyNotFulfilledException ) {
            violation = ( (PolicyNotFulfilledException) e ).getViolation();
        }
        else if ( e instanceof MailRateLimitingException ) {
            this.throttle.setThrottleDelay( ( (MailRateLimitingException) e ).getDelay());
            return;
        }

        if ( violation != null ) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, this.policyBean.getPolicyViolationMessage(violation), StringUtils.EMPTY);
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        ExceptionHandler.handleException(e);
    }


    /**
     * 
     * @param e
     * @return null
     */
    public String generateMore ( VFSEntity e ) {
        this.tokenShare.generateMore();
        return makeShareUrl(e);
    }


    /**
     * 
     * @param sb
     * @return null
     */
    public String enablePasswordProtection ( AbstractSharesBean sb ) {
        sb.enablePasswordProtection();
        return null;
    }


    /**
     * 
     * @param sb
     * @return null
     */
    public String disablePasswordProtection ( AbstractSharesBean sb ) {
        if ( sb.isRequirePassword() ) {
            return null;
        }
        sb.setPasswordProtected(false);
        sb.setPassword(null);
        sb.onPasswordChange(null);
        return null;
    }


    /**
     * 
     * @param query
     * @return complettion in the user's peer mail addresses
     */
    public List<String> completeMailAddress ( String query ) {
        List<String> res;
        try {
            res = new ArrayList<>(this.fsp.getBrowseService().getPeerMailAddresses(query));
        }
        catch ( FileshareException e ) {
            ExceptionHandler.handleException(e);
            return Collections.EMPTY_LIST;
        }
        Collections.sort(res, Collator.getInstance(FacesContext.getCurrentInstance().getViewRoot().getLocale()));
        return res;
    }


    /**
     * 
     * @param g
     * @return outcome
     */
    public String revokeGrant ( Grant g ) {
        try {
            this.fsp.getShareService().revokeShare(g.getId());
            this.fileShares.refresh();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException ex ) {
            ExceptionHandler.handleException(ex);
        }

        return null;
    }

}
