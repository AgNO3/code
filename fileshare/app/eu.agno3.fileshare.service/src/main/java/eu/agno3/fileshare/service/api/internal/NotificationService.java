/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.02.2015 by mbechler
 */
package eu.agno3.fileshare.service.api.internal;


import java.util.Set;

import org.joda.time.DateTime;

import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.notify.EntityNotificationData;
import eu.agno3.fileshare.model.notify.LinkNotificationData;
import eu.agno3.fileshare.model.notify.MailRecipient;
import eu.agno3.fileshare.model.notify.MailSender;
import eu.agno3.fileshare.model.notify.MailShareNotificationData;
import eu.agno3.fileshare.model.notify.ShareNotificationData;
import eu.agno3.fileshare.model.notify.UploadNotificationData;
import eu.agno3.fileshare.model.notify.UserExpirationNotificationData;
import eu.agno3.fileshare.model.notify.UserNotificationData;
import eu.agno3.fileshare.service.internal.NotificationChecker;
import eu.agno3.runtime.db.orm.EntityTransactionContext;


/**
 * @author mbechler
 *
 */
public interface NotificationService {

    /**
     * @return a notifier
     */
    MailNotifier<MailShareNotificationData> makeMailShareNotifier ();


    /**
     * @return a notifier
     */
    MailNotifier<EntityNotificationData> makeExpirationNotification ();


    /**
     * @return a notifier
     */
    MailNotifier<UploadNotificationData> makeUploadNotification ();


    /**
     * @return a notifier
     */
    MailNotifier<LinkNotificationData> makeRegistrationVerification ();


    /**
     * @return a notifier
     */
    MailNotifier<LinkNotificationData> makePasswordResetNotification ();


    /**
     * @return a notifier
     */
    MailNotifier<LinkNotificationData> makeInvitationNotification ();


    /**
     * 
     * @return a notifier
     */
    MailNotifier<UserNotificationData> makeInvitationCompleteNotification ();


    /**
     * 
     * @return a notifier
     */
    MailNotifier<ShareNotificationData> makeShareNotification ();


    /**
     * @return a notifier
     */
    MailNotifier<UserExpirationNotificationData> makeUserExpirationNotification ();


    /**
     * @param u
     * @param checker
     * @return a recipient object for the given user
     */
    MailRecipient getRecipientForUser ( User u, NotificationChecker checker );


    /**
     * 
     * @param g
     * @param checker
     * @return recipients for the given group
     */
    Set<MailRecipient> getRecipientsForGroup ( Group g, NotificationChecker checker );


    /**
     * @param subj
     * @param checker
     * @return recipients for the given subject
     */
    Set<MailRecipient> getRecipientsForSubject ( Subject subj, NotificationChecker checker );


    /**
     * @return administrative contact recipients
     */
    Set<MailRecipient> getAdminRecipients ();


    /**
     * @param u
     * @return a sender object for the given user
     */
    MailSender getSenderForUser ( User u );


    /**
     * @param tx
     * @return the number of removed trackers
     */
    int cleanupNotificationTrackers ( EntityTransactionContext tx );


    /**
     * @param tx
     * @param dedupId
     * @return whether a non-expired tracker for the given dedupId exists
     */
    boolean haveNotified ( EntityTransactionContext tx, String dedupId );


    /**
     * @param tx
     * @param dedupId
     * @param expires
     *            should always be set
     */
    void trackNotification ( EntityTransactionContext tx, String dedupId, DateTime expires );

}
