/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.02.2015 by mbechler
 */
package eu.agno3.fileshare.model.notify;


import java.util.Locale;

import eu.agno3.fileshare.model.UserDetails;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public class MailSender {

    private UserPrincipal principal;
    private UserDetails details;
    private Locale userLocale;


    /**
     * @return the principal
     */
    public UserPrincipal getPrincipal () {
        return this.principal;
    }


    /**
     * @param principal
     *            the principal to set
     */
    public void setPrincipal ( UserPrincipal principal ) {
        this.principal = principal;
    }


    /**
     * @return the details
     */
    public UserDetails getDetails () {
        return this.details;
    }


    /**
     * @param details
     *            the details to set
     */
    public void setDetails ( UserDetails details ) {
        this.details = details;
    }


    /**
     * @return the userLocale
     */
    public Locale getUserLocale () {
        return this.userLocale;
    }


    /**
     * @param userLocale
     *            the userLocale to set
     */
    public void setUserLocale ( Locale userLocale ) {
        this.userLocale = userLocale;
    }

}
