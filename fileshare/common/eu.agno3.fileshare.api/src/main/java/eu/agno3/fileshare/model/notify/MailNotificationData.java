/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.02.2015 by mbechler
 */
package eu.agno3.fileshare.model.notify;


import java.util.Set;


/**
 * @author mbechler
 *
 */
public interface MailNotificationData {

    /**
     * @return the recipients
     */
    Set<MailRecipient> getRecipients ();


    /**
     * @return the sender
     */
    MailSender getSender ();


    /**
     * @return the hideSensitive
     */
    boolean getHideSensitive ();


    /**
     * 
     * @return an alternate subject
     */
    String getOverrideSubject ();
}