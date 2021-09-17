/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.registration;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.notify.MailRecipient;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.runtime.security.terms.TermsDefinition;
import eu.agno3.runtime.validation.email.ValidEmail;


/**
 * @author mbechler
 *
 */

@Named ( "userRegistrationBean" )
@ViewScoped
public class UserRegistrationBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1557745834044143827L;

    private String username;

    private String fullName;

    private String callingName;

    private String mailAddress;

    private Locale locale;

    private String message;

    private boolean inPreview;

    private String subject;

    private transient MimeMessage messagePreview;

    @Inject
    private FileshareServiceProvider fsp;

    private boolean resend;

    private boolean alreadySent;

    private Integer throttleDelay;

    private DateTime expires;

    private transient Collection<TermsDefinition> termsToAccept;

    private Collection<String> acceptedTerms = Collections.EMPTY_LIST;


    /**
     * 
     */
    @PostConstruct
    public void init () {
        defaultExpiry();
    }


    /**
     * 
     */
    public void reset () {
        this.username = null;
        this.fullName = null;
        this.callingName = null;
        this.mailAddress = null;

        defaultExpiry();
    }


    /**
     * 
     */
    private void defaultExpiry () {
        Duration expireDuration = this.fsp.getConfigurationProvider().getUserConfig().getInvitationUserExpiration();
        if ( expireDuration != null ) {
            this.expires = DateTime.now().plus(expireDuration).withTime(0, 0, 0, 0);
        }
        else {
            this.expires = null;
        }
    }


    /**
     * @return the username
     */
    public String getUsername () {
        return this.username;
    }


    /**
     * @param username
     *            the username to set
     */
    public void setUsername ( String username ) {
        this.username = username;
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
     * @return the callingName
     */
    public String getCallingName () {
        return this.callingName;
    }


    /**
     * @param callingName
     *            the callingName to set
     */
    public void setCallingName ( String callingName ) {
        this.callingName = callingName;
    }


    /**
     * @return the mailAddress
     */
    @ValidEmail ( hostDNSValidate = true )
    public String getMailAddress () {
        return this.mailAddress;
    }


    /**
     * @param mailAddress
     *            the mailAddress to set
     */
    public void setMailAddress ( String mailAddress ) {
        this.mailAddress = mailAddress;
    }


    /**
     * @return an extra message to the user
     */
    public String getMessage () {
        return this.message;
    }


    /**
     * @param message
     *            the message to set
     */
    public void setMessage ( String message ) {
        this.message = message;
    }


    /**
     * @return the user's locale
     */
    public Locale getLocale () {
        return this.locale;
    }


    /**
     * @param locale
     *            the locale to set
     */
    public void setLocale ( Locale locale ) {

        this.locale = locale;
    }


    /**
     * @return the expires
     */
    public DateTime getExpires () {
        return this.expires;
    }


    /**
     * @param expires
     *            the expires to set
     */
    public void setExpires ( DateTime expires ) {
        this.expires = expires;
    }


    /**
     * 
     * @return the minimum selectable expiry date
     */
    public Date getMinExpires () {
        return DateTime.now().plusDays(1).toDate();
    }


    /**
     * 
     */
    public void unsetExpires () {
        this.expires = null;
    }


    /**
     * @return the inPreview
     */
    public boolean getInPreview () {
        return this.inPreview;
    }


    /**
     * @param inPreview
     *            the inPreview to set
     */
    public void setInPreview ( boolean inPreview ) {
        this.inPreview = inPreview;
    }


    /**
     * @return the messagePreview
     */
    public MimeMessage getMessagePreview () {
        return this.messagePreview;
    }


    /**
     * @param messagePreview
     *            the messagePreview to set
     */
    public void setMessagePreview ( MimeMessage messagePreview ) {
        this.messagePreview = messagePreview;
    }


    /**
     * @return a mail recipient for the invitation
     */
    public MailRecipient makeRecipient () {
        MailRecipient recpt = new MailRecipient();
        recpt.setMailAddress(this.getMailAddress());
        recpt.setFullName(this.getFullName());
        recpt.setCallingName(this.getCallingName());
        recpt.setSalutation(this.getMessage());
        return recpt;
    }


    /**
     * 
     * @return the notification subject
     */
    public String getSubject () {

        if ( this.subject == null ) {
            try {
                this.subject = this.fsp.getRegistrationService().getInvitationSubject(makeRecipient());
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                ExceptionHandler.handleException(e);
                this.subject = StringUtils.EMPTY;
            }
        }
        return this.subject;
    }


    /**
     * @return the termsToAccept
     */
    public Collection<TermsDefinition> getTermsToAccept () {
        if ( this.termsToAccept == null ) {
            this.termsToAccept = this.fsp.getTermsService().getAllTerms(null);
        }
        return this.termsToAccept;
    }


    /**
     * @return the acceptedTerms
     */
    public Collection<String> getAcceptedTerms () {
        return this.acceptedTerms;
    }


    /**
     * @param acceptedTerms
     *            the acceptedTerms to set
     */
    public void setAcceptedTerms ( Collection<String> acceptedTerms ) {
        this.acceptedTerms = acceptedTerms;
    }


    /**
     * 
     * @param subject
     */
    public void setSubject ( String subject ) {
        this.subject = subject;
    }


    /**
     * @return whether resending is desired;
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
     * @return the alreadySent
     */
    public boolean getAlreadySent () {
        return this.alreadySent;
    }


    /**
     * @param alreadySent
     *            the alreadySent to set
     */
    public void setAlreadySent ( boolean alreadySent ) {
        this.alreadySent = alreadySent;
    }


    /**
     * @return the throttleDelay
     */
    public Integer getThrottleDelay () {
        return this.throttleDelay;
    }


    /**
     * @param throttleDelay
     *            the throttleDelay to set
     */
    public void setThrottleDelay ( int throttleDelay ) {
        this.throttleDelay = throttleDelay;
    }


    /**
     * 
     */
    public void throttleComplete () {
        this.throttleDelay = null;
    }
}
