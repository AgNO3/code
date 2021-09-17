/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.02.2015 by mbechler
 */
package eu.agno3.runtime.mail;


import java.io.InputStream;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


/**
 * @author mbechler
 *
 */
public interface SMTPClientTransport extends AutoCloseable {

    /**
     * 
     * @throws MessagingException
     */
    void connect () throws MessagingException;


    /**
     * 
     * @param msg
     * @throws MessagingException
     */
    void sendMessage ( MimeMessage msg ) throws MessagingException;


    /**
     * 
     * @param msg
     * @param addresses
     * @throws MessagingException
     */
    void sendMessage ( MimeMessage msg, List<InternetAddress> addresses ) throws MessagingException;


    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    void close () throws MessagingException;


    /**
     * @return new message object
     * @throws MessagingException
     */
    MimeMessage createMimeMessage () throws MessagingException;


    /**
     * @param is
     * @return new message object from stream
     * @throws MessagingException
     */
    MimeMessage createMimeMessage ( InputStream is ) throws MessagingException;


    /**
     * @return Whether startTLS is used (check after connect)
     */
    boolean isStartTLSEstablished ();

}
