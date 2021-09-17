/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.share;


import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.notify.MailRecipient;
import eu.agno3.fileshare.model.query.MailPeerInfo;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.runtime.validation.email.ValidEmail;


/**
 * @author mbechler
 *
 */
@Named ( "mailShareBean" )
@ViewScoped
public class MailShareBean extends AbstractSharesBean {

    /**
     * 
     */
    private static final long serialVersionUID = -3423338922642830351L;

    private String shareMailAddress;
    private String fullName;

    private boolean noHtml;

    private boolean resend;

    private boolean mayResend;

    @Inject
    private ShareTabsBean shareTabs;

    @Inject
    private FileshareServiceProvider fsp;


    @Override
    @PostConstruct
    public void init () {
        if ( this.shareTabs.getPeerInfo() instanceof MailPeerInfo ) {
            this.shareMailAddress = ( (MailPeerInfo) this.shareTabs.getPeerInfo() ).getMailAddress();
        }

        try {
            this.getShareProperties().setNotificationSubject(
                this.fsp.getShareService().getMailShareSubject(getSelectedEntity().getEntityKey(), makeRecipient(), getShareProperties()));
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }

        super.init();

        if ( this.isRequirePassword() ) {
            enablePasswordProtection();
        }
    }


    /**
     * @return whether to send text only mail
     */
    public boolean getNoHtml () {
        return this.noHtml;
    }


    /**
     * @param noHtml
     *            the noHtml to set
     */
    public void setNoHtml ( boolean noHtml ) {
        this.noHtml = noHtml;
    }


    /**
     * @return the shareMailAddresses
     */
    @ValidEmail ( )
    public String getShareMailAddress () {
        return this.shareMailAddress;
    }


    /**
     * 
     * @param shareMailAddress
     */
    public void setShareMailAddress ( String shareMailAddress ) {
        this.shareMailAddress = shareMailAddress;
    }


    /**
     * @return the fullName
     */
    public String getFullName () {
        return this.fullName;
    }


    /**
     * @param fullName
     *            the fullName to set
     */
    public void setFullName ( String fullName ) {
        this.fullName = fullName;
    }


    /**
     * @return the salutation
     */
    public String getMessage () {
        return this.getShareProperties().getMessage();
    }


    /**
     * @param salutation
     *            the salutation to set
     */
    public void setMessage ( String salutation ) {
        this.getShareProperties().setMessage(salutation);
    }


    /**
     * @return the subject
     */
    public String getSubject () {
        return this.getShareProperties().getNotificationSubject();
    }


    /**
     * @param subject
     *            the subject to set
     */
    public void setSubject ( String subject ) {
        this.getShareProperties().setNotificationSubject(subject);
    }


    /**
     * @return recipient info
     */
    public Collection<MailRecipient> makeRecipients () {
        MailRecipient makeRecipient = makeRecipient();
        if ( makeRecipient != null ) {
            return Arrays.asList(makeRecipient);
        }
        return Collections.EMPTY_LIST;
    }


    /**
     * @return the recipient info
     */
    public MailRecipient makeRecipient () {
        MailRecipient recpt = new MailRecipient();
        recpt.setMailAddress(this.getShareMailAddress());
        recpt.setSalutation(this.getMessage());
        recpt.setFullName(this.getFullName());
        recpt.setNoHtml(this.getNoHtml());
        return recpt;
    }


    /**
     * 
     */
    @Override
    public void reset () {
        super.reset();
        this.shareMailAddress = null;
        this.fullName = null;
        this.resend = false;
        this.mayResend = false;
    }


    /**
     * @return whether to resend for existing shares
     */
    public boolean getResend () {
        return this.resend;
    }


    /**
     * @param resend
     *            the resend to set
     */
    public void setResend ( boolean resend ) {
        this.resend = resend;
    }


    /**
     * @return the noMailSend
     */
    public boolean getMayResend () {
        return this.mayResend;
    }


    /**
     * @param mayResend
     *            the noMailSend to set
     */
    public void setMayResend ( boolean mayResend ) {
        this.mayResend = mayResend;
    }
}
