/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.02.2015 by mbechler
 */
package eu.agno3.runtime.mail.internal;


import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

import org.apache.log4j.Logger;

import eu.agno3.runtime.mail.SMTPConfiguration;


/**
 * 
 * WARNING: the super impl. is really bad from a thread safety standpoint,
 * but right now it seems that this is only called from the synchronized connect
 * method.
 * 
 * @author mbechler
 *
 */
public class SMTPAuthenticator extends Authenticator {

    private static final Logger log = Logger.getLogger(SMTPAuthenticator.class);


    /**
     * @param mailConfig
     */
    public SMTPAuthenticator ( SMTPConfiguration mailConfig ) {}


    /**
     * {@inheritDoc}
     *
     * @see javax.mail.Authenticator#getPasswordAuthentication()
     */
    @Override
    protected PasswordAuthentication getPasswordAuthentication () {

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Requested password auth %s://%s:%d '%s' defUser:%s", //$NON-NLS-1$
                this.getRequestingProtocol(),
                this.getRequestingSite(),
                this.getRequestingPort(),
                this.getRequestingPrompt(),
                this.getDefaultUserName()));
        }

        return null;
    }
}
