/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.registration;


import java.io.Serializable;
import java.util.Collection;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.runtime.security.terms.TermsDefinition;
import eu.agno3.runtime.validation.email.ValidEmail;


/**
 * @author mbechler
 *
 */
@Named ( "passwordRecoveryBean" )
@ViewScoped
public class PasswordRecoveryBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 779328969160249508L;

    private String mailAddress;
    private String userName;
    private boolean resend;

    private boolean alreadySent;

    private Integer throttleDelay;

    @Inject
    private FileshareServiceProvider fsp;

    private transient Collection<TermsDefinition> termsToAccept;


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
     * @return whether to resend
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
     * @param b
     */
    public void setAlreadySent ( boolean b ) {
        this.alreadySent = b;
    }


    /**
     * @return the alreadySent
     */
    public boolean getAlreadySent () {
        return this.alreadySent;
    }


    /**
     * @param delay
     */
    public void setThrottleDelay ( int delay ) {
        this.throttleDelay = delay;
    }


    /**
     * @return the throttleDelay
     */
    public Integer getThrottleDelay () {
        return this.throttleDelay;
    }


    /**
     * 
     */
    public void throttleComplete () {
        this.throttleDelay = null;
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

}
