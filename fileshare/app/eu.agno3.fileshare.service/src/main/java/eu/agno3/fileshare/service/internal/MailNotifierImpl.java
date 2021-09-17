/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.02.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import javax.mail.internet.MimeMessage;

import eu.agno3.fileshare.exceptions.NotificationException;
import eu.agno3.fileshare.model.notify.MailNotificationData;
import eu.agno3.fileshare.service.api.internal.MailNotifier;


/**
 * @author mbechler
 * @param <T>
 *            msg data type
 *
 */
public class MailNotifierImpl <T extends MailNotificationData> implements MailNotifier<T> {

    private final NotificationServiceImpl service;
    private String tplId;


    /**
     * @param dataType
     * @param service
     * @param tplId
     */
    public MailNotifierImpl ( Class<T> dataType, NotificationServiceImpl service, String tplId ) {
        this.service = service;
        this.tplId = tplId;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.MailNotifier#notify(eu.agno3.fileshare.model.notify.MailNotificationData)
     */
    @Override
    public void notify ( T data ) throws NotificationException {
        this.service.doNotify(this.tplId, data);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.MailNotifier#preview(eu.agno3.fileshare.model.notify.MailNotificationData)
     */
    @Override
    public MimeMessage preview ( T data ) throws NotificationException {
        return this.service.makePreview(this.tplId, data);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.MailNotifier#makeSubject(eu.agno3.fileshare.model.notify.MailNotificationData)
     */
    @Override
    public String makeSubject ( T data ) throws NotificationException {
        return this.service.makeSubject(this.tplId, data);
    }
}
