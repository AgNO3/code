/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.02.2015 by mbechler
 */
package eu.agno3.fileshare.service.api.internal;


import javax.mail.internet.MimeMessage;

import eu.agno3.fileshare.exceptions.NotificationException;
import eu.agno3.fileshare.model.notify.MailNotificationData;


/**
 * @author mbechler
 * @param <T>
 *            the data type
 *
 */
public interface MailNotifier <T extends MailNotificationData> {

    /**
     * 
     * @param data
     * @throws NotificationException
     */
    void notify ( T data ) throws NotificationException;


    /**
     * 
     * @param data
     * @return the generated subject
     * @throws NotificationException
     */
    MimeMessage preview ( T data ) throws NotificationException;


    /**
     * @param data
     * @return the generated subject
     * @throws NotificationException
     */
    String makeSubject ( T data ) throws NotificationException;

}
