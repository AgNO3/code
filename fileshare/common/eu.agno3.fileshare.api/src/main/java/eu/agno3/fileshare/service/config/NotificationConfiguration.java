/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.02.2015 by mbechler
 */
package eu.agno3.fileshare.service.config;


import java.util.Locale;

import org.joda.time.Duration;


/**
 * @author mbechler
 *
 */
public interface NotificationConfiguration {

    /**
     * @return whether to disable all notifications
     */
    boolean isMailingDisabled ();


    /**
     * @return whether to always send text mails
     */
    boolean isAlwaysSendText ();


    /**
     * @param address
     * @return whether to send notifications as the user triggering the action
     */
    boolean isSendNotificationsAsUser ( String address );


    /**
     * @return the default notification locale
     */
    Locale getDefaultLocale ();


    /**
     * @return the default sender address
     */
    String getDefaultSenderAddress ();


    /**
     * @return the default sender name
     */
    String getDefaultSenderName ();


    /**
     * @return a footer for all notifications
     */
    String getFooter ();


    /**
     * @return an administractive contact email address
     */
    String getAdminContact ();


    /**
     * @return whether to hide sensitive information e.g. file names and sizes
     */
    boolean isHideSensitiveInformation ();


    /**
     * @return the time period before the actual expiration in which notifications will be sent
     */
    Duration getExpirationNotificationPeriod ();

}
