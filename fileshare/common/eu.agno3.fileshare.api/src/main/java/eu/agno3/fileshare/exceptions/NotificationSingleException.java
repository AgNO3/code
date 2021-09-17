/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.02.2015 by mbechler
 */
package eu.agno3.fileshare.exceptions;


import eu.agno3.fileshare.model.notify.MailNotificationData;


/**
 * @author mbechler
 *
 */
public class NotificationSingleException extends NotificationException {

    /**
     * 
     */
    private static final long serialVersionUID = 5235956056190719363L;
    private MailNotificationData data;


    /**
     * 
     */
    public NotificationSingleException () {
        super();
    }


    /**
     * @param data
     * @param msg
     * @param t
     */
    public NotificationSingleException ( MailNotificationData data, String msg, Throwable t ) {
        super(msg, t);
        this.data = data;
    }


    /**
     * @param data
     * @param msg
     */
    public NotificationSingleException ( MailNotificationData data, String msg ) {
        super(msg);
        this.data = data;
    }


    /**
     * @param data
     * @param cause
     */
    public NotificationSingleException ( MailNotificationData data, Throwable cause ) {
        super(cause);
        this.data = data;
    }


    /**
     * @return the data
     */
    public MailNotificationData getData () {
        return this.data;
    }

}
