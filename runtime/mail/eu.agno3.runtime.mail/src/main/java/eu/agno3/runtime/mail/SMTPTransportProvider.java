/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.02.2015 by mbechler
 */
package eu.agno3.runtime.mail;


import java.io.InputStream;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;


/**
 * @author mbechler
 *
 */
public interface SMTPTransportProvider {

    /**
     * 
     * @return a smtp transport
     * @throws MessagingException
     */
    public SMTPClientTransport getTransport () throws MessagingException;


    /**
     * @return a new message instance
     * @throws MessagingException
     * 
     */
    public MimeMessage createMimeMessage () throws MessagingException;


    /**
     * @param is
     * @return a message parsed from the input stream
     * @throws MessagingException
     */
    MimeMessage createMimeMessage ( InputStream is ) throws MessagingException;
}
