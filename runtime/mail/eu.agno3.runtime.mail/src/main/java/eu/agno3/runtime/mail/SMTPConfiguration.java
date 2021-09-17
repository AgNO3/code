/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.02.2015 by mbechler
 */
package eu.agno3.runtime.mail;


import java.util.List;
import java.util.Map;


/**
 * @author mbechler
 *
 */
public interface SMTPConfiguration {

    /**
     * 
     */
    public static final String PID = "smtp"; //$NON-NLS-1$


    /**
     * @return configuration instance id
     */
    String getInstanceId ();


    /**
     * @return whether to connect via SSL (smtps)
     */
    boolean isSSL ();


    /**
     * 
     * @return whether to use STARTTLS if available
     */
    boolean isStartTLS ();


    /**
     * 
     * @return whether to require STARTTLS
     */
    boolean isStartTLSRequired ();


    /**
     * @return the smtp host
     */
    String getSMTPHost ();


    /**
     * @return the smtp port (-1 for proto default port)
     */
    int getSMTPPort ();


    /**
     * @return the user to use for authenticating against the smtp server
     */
    String getSMTPUser ();


    /**
     * @return the password to use for authenticating against the smtp server
     */
    String getSMTPPassword ();


    /**
     * @return wehther authentication should be performed
     */
    boolean isAuthEnabled ();


    /**
     * 
     * @return the authentication mechanisms to consider
     */
    List<String> getAuthMechanisms ();


    /**
     * @return the default sender address if not specified in message
     */
    String getDefaultFromAddress ();


    /**
     * @return the default sender name if not specified in message
     */
    String getDefaultFromName ();


    /**
     * @return extra javamail properties to set
     */
    Map<String, String> getExtraProperties ();


    /**
     * @return the hostname to send in EHLO
     */
    String getEhloHostName ();


    /**
     * @return read timeout in milliseconds
     */
    int getReadTimeout ();


    /**
     * @return write timeout in milliseconds
     */
    int getWriteTimeout ();


    /**
     * @return conn timeout in milliseconds
     */
    int getConnTimeout ();


    /**
     * @return whether to inject mails via sendmail
     */
    boolean isUseSendmail ();


    /**
     * @return path to sendmail binary
     */
    String getSendmailPath ();


    /**
     * 
     * @return whether to set the default sender address
     */
    boolean isSetSendmailSender ();


    /**
     * 
     * @return arguments to pass to sendmail
     */
    List<String> getSendmailExtraArgs ();

}
