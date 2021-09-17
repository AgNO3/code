/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.02.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.fileshare.exceptions.MailingDisabledException;
import eu.agno3.fileshare.exceptions.NotificationException;
import eu.agno3.fileshare.exceptions.NotificationMultiException;
import eu.agno3.fileshare.mail.tpl.FileshareMailTemplateBuilder;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.UserDetails;
import eu.agno3.fileshare.model.notify.EntityNotificationData;
import eu.agno3.fileshare.model.notify.LinkNotificationData;
import eu.agno3.fileshare.model.notify.MailNotificationData;
import eu.agno3.fileshare.model.notify.MailRecipient;
import eu.agno3.fileshare.model.notify.MailSender;
import eu.agno3.fileshare.model.notify.MailShareNotificationData;
import eu.agno3.fileshare.model.notify.NotificationTracker;
import eu.agno3.fileshare.model.notify.ShareNotificationData;
import eu.agno3.fileshare.model.notify.UploadNotificationData;
import eu.agno3.fileshare.model.notify.UserExpirationNotificationData;
import eu.agno3.fileshare.model.notify.UserNotificationData;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.MailNotifier;
import eu.agno3.fileshare.service.api.internal.NotificationService;
import eu.agno3.fileshare.service.config.NotificationConfiguration;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.mail.SMTPClientTransport;
import eu.agno3.runtime.mail.SMTPTransportProvider;

import freemarker.template.TemplateException;


/**
 * @author mbechler
 *
 */
@Component ( service = NotificationService.class )
public class NotificationServiceImpl implements NotificationService {

    private static final String UTF8 = "UTF-8"; //$NON-NLS-1$
    private DefaultServiceContext ctx;
    private FileshareMailTemplateBuilder tplBuilder;
    private SMTPTransportProvider smtpTransportProvider;


    @Reference
    protected synchronized void setServiceContext ( DefaultServiceContext sctx ) {
        this.ctx = sctx;
    }


    protected synchronized void unsetServiceContext ( DefaultServiceContext sctx ) {
        if ( this.ctx == sctx ) {
            this.ctx = null;
        }
    }


    @Reference
    protected synchronized void setMailTemplateBuilder ( FileshareMailTemplateBuilder mtb ) {
        this.tplBuilder = mtb;
    }


    protected synchronized void unsetMailTemplateBuilder ( FileshareMailTemplateBuilder mtb ) {
        if ( this.tplBuilder == mtb ) {
            this.tplBuilder = null;
        }
    }


    @Reference ( policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL )
    protected synchronized void setSMTPTransportProvider ( SMTPTransportProvider stp ) {
        this.smtpTransportProvider = stp;
    }


    protected synchronized void unsetSMTPTransportProvider ( SMTPTransportProvider stp ) {
        if ( this.smtpTransportProvider == stp ) {
            this.smtpTransportProvider = null;
        }
    }


    @Override
    public MailSender getSenderForUser ( User u ) {
        MailSender send = new MailSender();
        send.setDetails(u.getUserDetails());
        send.setPrincipal(u.getPrincipal());
        String overrideLocale = u.getPreferences().get("overrideLocale"); //$NON-NLS-1$

        if ( !StringUtils.isBlank(overrideLocale) ) {
            send.setUserLocale(Locale.forLanguageTag(overrideLocale));
        }
        return send;
    }


    @Override
    public MailRecipient getRecipientForUser ( User u, NotificationChecker checker ) {
        if ( checker != null && !checker.shouldNotify(u) ) {
            return null;
        }

        MailRecipient recp = new MailRecipient();
        UserDetails details = u.getUserDetails();

        if ( details == null || StringUtils.isBlank(details.getMailAddress()) ) {
            return null;
        }

        recp.setMailAddress(details.getMailAddress());
        recp.setCallingName(details.getSalutationName());
        recp.setFullName(details.getPreferredName());
        if ( details.getPreferTextMail() != null ) {
            recp.setNoHtml(details.getPreferTextMail());
        }
        String overrideLocale = u.getPreferences().get("overrideLocale"); //$NON-NLS-1$

        if ( !StringUtils.isBlank(overrideLocale) ) {
            recp.setDesiredLocale(Locale.forLanguageTag(overrideLocale));
        }

        return recp;
    }


    /**
     * @param g
     * @return
     */
    private static MailRecipient makeGroupRecipient ( Group g ) {
        MailRecipient recp = new MailRecipient();
        recp.setMailAddress(g.getNotificationOverrideAddress());
        recp.setFullName(g.getName());
        recp.getCallingName();
        recp.setDesiredLocale(g.getGroupLocale());
        return recp;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.NotificationService#getRecipientsForGroup(eu.agno3.fileshare.model.Group,
     *      eu.agno3.fileshare.service.internal.NotificationChecker)
     */
    @Override
    public Set<MailRecipient> getRecipientsForGroup ( Group g, NotificationChecker checker ) {

        Set<Subject> resolved = new HashSet<>();
        Set<MailRecipient> recpts = new HashSet<>();

        recursiveGroupRecipients(g, resolved, recpts, checker);

        return recpts;
    }


    /**
     * @param subj
     * @param resolved
     * @param recpts
     */
    private void recursiveGroupRecipients ( Subject subj, Set<Subject> resolved, Set<MailRecipient> recpts, NotificationChecker checker ) {

        if ( resolved.contains(subj) ) {
            return;
        }

        resolved.add(subj);
        if ( subj instanceof User ) {
            MailRecipient recpt = getRecipientForUser((User) subj, checker);
            if ( recpt != null ) {
                recpts.add(recpt);
            }
        }
        else if ( subj instanceof Group ) {
            Group g = (Group) subj;

            if ( checker != null && !checker.shouldNotify(g) ) {
                return;
            }

            if ( g.getDisableNotifications() ) {
                return;
            }

            if ( !StringUtils.isBlank(g.getNotificationOverrideAddress()) ) {
                recpts.add(makeGroupRecipient(g));
            }
            else {
                for ( Subject member : g.getMembers() ) {
                    recursiveGroupRecipients(member, resolved, recpts, checker);
                }
            }
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.NotificationService#getRecipientsForSubject(eu.agno3.fileshare.model.Subject,
     *      eu.agno3.fileshare.service.internal.NotificationChecker)
     */
    @Override
    public Set<MailRecipient> getRecipientsForSubject ( Subject subj, NotificationChecker checker ) {

        if ( subj instanceof User ) {
            MailRecipient recpt = getRecipientForUser((User) subj, checker);
            if ( recpt == null ) {
                return Collections.EMPTY_SET;
            }
            return new HashSet<>(Arrays.asList(recpt));
        }
        else if ( subj instanceof Group ) {
            return getRecipientsForGroup((Group) subj, checker);
        }

        return Collections.EMPTY_SET;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.NotificationService#getAdminRecipients()
     */
    @Override
    public Set<MailRecipient> getAdminRecipients () {

        if ( StringUtils.isBlank(this.ctx.getConfigurationProvider().getNotificationConfiguration().getAdminContact()) ) {
            return Collections.EMPTY_SET;
        }

        MailRecipient recpt = new MailRecipient();
        recpt.setMailAddress(this.ctx.getConfigurationProvider().getNotificationConfiguration().getAdminContact());
        return new HashSet<>(Arrays.asList(recpt));
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.NotificationService#makeMailShareNotifier()
     */
    @Override
    public MailNotifier<MailShareNotificationData> makeMailShareNotifier () {
        return new MailNotifierImpl<>(MailShareNotificationData.class, this, "mailShare"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.NotificationService#makeExpirationNotification()
     */
    @Override
    public MailNotifier<EntityNotificationData> makeExpirationNotification () {
        return new MailNotifierImpl<>(EntityNotificationData.class, this, "entityExpiry"); //$NON-NLS-1$
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.NotificationService#makeUploadNotification()
     */
    @Override
    public MailNotifier<UploadNotificationData> makeUploadNotification () {
        return new MailNotifierImpl<>(UploadNotificationData.class, this, "entityUpload"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.NotificationService#makeInvitationNotification()
     */
    @Override
    public MailNotifier<LinkNotificationData> makeInvitationNotification () {
        return new MailNotifierImpl<>(LinkNotificationData.class, this, "invitation"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.NotificationService#makePasswordResetNotification()
     */
    @Override
    public MailNotifier<LinkNotificationData> makePasswordResetNotification () {
        return new MailNotifierImpl<>(LinkNotificationData.class, this, "resetPassword"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.NotificationService#makeRegistrationVerification()
     */
    @Override
    public MailNotifier<LinkNotificationData> makeRegistrationVerification () {
        return new MailNotifierImpl<>(LinkNotificationData.class, this, "registration"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.NotificationService#makeShareNotification()
     */
    @Override
    public MailNotifier<ShareNotificationData> makeShareNotification () {
        return new MailNotifierImpl<>(ShareNotificationData.class, this, "share"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.NotificationService#makeUserExpirationNotification()
     */
    @Override
    public MailNotifier<UserExpirationNotificationData> makeUserExpirationNotification () {
        return new MailNotifierImpl<>(UserExpirationNotificationData.class, this, "userExpiry"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.NotificationService#makeUserExpirationNotification()
     */
    @Override
    public MailNotifier<UserNotificationData> makeInvitationCompleteNotification () {
        return new MailNotifierImpl<>(UserNotificationData.class, this, "invitationComplete"); //$NON-NLS-1$
    }


    /**
     * @param tplId
     * @param data
     * @throws NotificationException
     */
    void doNotify ( String tplId, MailNotificationData data ) throws NotificationException {
        SMTPTransportProvider stp = checkNotify();
        List<NotificationException> failures = new LinkedList<>();

        for ( MailRecipient recpt : data.getRecipients() ) {
            try {

                MimeMessage msg = stp.createMimeMessage();
                this.tplBuilder.makeMessage(msg, tplId, wrapObject(data, recpt), isNoHTML(data, recpt), deriveLocale(data, recpt));
                if ( !StringUtils.isBlank(data.getOverrideSubject()) ) {
                    String subject = StringUtils.replaceChars(data.getOverrideSubject(), "\r\n", StringUtils.EMPTY); //$NON-NLS-1$
                    msg.setSubject(subject, "UTF-8"); //$NON-NLS-1$
                }
                msg.addRecipient(RecipientType.TO, makeAddress(recpt.getMailAddress(), recpt.getFullName()));
                setFrom(msg, data);

                try ( SMTPClientTransport smtp = stp.getTransport() ) {
                    smtp.sendMessage(msg);
                }
            }
            catch (
                MessagingException |
                IOException |
                TemplateException e ) {
                failures.add(new NotificationException(e));
            }
        }

        if ( !failures.isEmpty() ) {
            if ( failures.size() == 1 ) {
                throw failures.get(0);
            }
            throw new NotificationMultiException(failures, "Some notifications failed"); //$NON-NLS-1$
        }
    }


    /**
     * @param tplId
     * @param data
     * @return the message preview
     * @throws NotificationException
     */
    public MimeMessage makePreview ( String tplId, MailNotificationData data ) throws NotificationException {
        MailRecipient recpt = getDummyRecipient(data);
        SMTPTransportProvider stp = checkNotify();
        try {
            MimeMessage msg = stp.createMimeMessage();
            this.tplBuilder.makeMessage(msg, tplId, wrapObject(data, recpt), isNoHTML(data, recpt), deriveLocale(data, recpt));
            msg.addRecipient(RecipientType.TO, makeAddress(recpt.getMailAddress(), recpt.getFullName()));
            setFrom(msg, data);

            return msg;
        }
        catch (
            MessagingException |
            IOException |
            TemplateException e ) {
            throw new NotificationException("Failed to generate preview", e); //$NON-NLS-1$
        }
    }


    /**
     * @param data
     * @return
     */
    private static MailRecipient getDummyRecipient ( MailNotificationData data ) {
        MailRecipient recpt;
        if ( data.getRecipients().isEmpty() ) {
            recpt = makeDummyRecipient();
        }
        else {
            recpt = data.getRecipients().iterator().next();
            if ( StringUtils.isEmpty(recpt.getMailAddress()) ) {
                recpt.setMailAddress("tester@example.com"); //$NON-NLS-1$
            }
        }
        return recpt;
    }


    /**
     * @return
     */
    private static MailRecipient makeDummyRecipient () {
        MailRecipient recpt;
        recpt = new MailRecipient();
        recpt.setCallingName("Mr. Test"); //$NON-NLS-1$
        recpt.setFullName("Dummy Tester"); //$NON-NLS-1$
        recpt.setMailAddress("tester@example.com"); //$NON-NLS-1$
        return recpt;
    }


    /**
     * 
     * @param tplId
     * @param data
     * @return the subject string
     * @throws NotificationException
     */
    public String makeSubject ( String tplId, MailNotificationData data ) throws NotificationException {
        MailRecipient recpt = getDummyRecipient(data);
        try {
            return this.tplBuilder.makeSubject(tplId, deriveLocale(data, recpt), wrapObject(data, recpt));
        }
        catch (
            TemplateException |
            IOException e ) {
            throw new NotificationException("Failed to generate subject", e); //$NON-NLS-1$
        }
    }


    /**
     * @throws NotificationException
     * 
     */
    private SMTPTransportProvider checkNotify () throws NotificationException {
        if ( this.ctx.getConfigurationProvider().getNotificationConfiguration().isMailingDisabled() ) {
            throw new MailingDisabledException();
        }

        SMTPTransportProvider stp = this.smtpTransportProvider;

        if ( stp == null ) {
            throw new NotificationException("Mailing unavailable"); //$NON-NLS-1$
        }

        return stp;
    }


    private boolean isNoHTML ( MailNotificationData data, MailRecipient recpt ) {
        if ( this.ctx.getConfigurationProvider().getNotificationConfiguration().isAlwaysSendText() ) {
            return true;
        }

        if ( recpt == null ) {
            return false;
        }
        return recpt.isNoHtml();
    }


    private Locale deriveLocale ( MailNotificationData data, MailRecipient recpt ) {
        if ( recpt != null && recpt.getDesiredLocale() != null ) {
            return recpt.getDesiredLocale();
        }

        return this.ctx.getConfigurationProvider().getNotificationConfiguration().getDefaultLocale();
    }


    /**
     * @param data
     * @throws UnsupportedEncodingException
     * @throws NotificationException
     * @throws MessagingException
     */
    private void setFrom ( MimeMessage msg, MailNotificationData data )
            throws UnsupportedEncodingException, NotificationException, MessagingException {
        UserDetails senderDetails = null;
        if ( data.getSender() != null ) {
            senderDetails = data.getSender().getDetails();
        }
        NotificationConfiguration notificationConfiguration = this.ctx.getConfigurationProvider().getNotificationConfiguration();
        if ( senderDetails != null && !StringUtils.isBlank(senderDetails.getMailAddress()) && senderDetails.getMailAddressVerified()
                && notificationConfiguration.isSendNotificationsAsUser(senderDetails.getMailAddress()) ) {

            msg.setFrom(makeAddress(senderDetails.getMailAddress(), senderDetails.getFullName()));

            if ( !StringUtils.isBlank(notificationConfiguration.getDefaultSenderAddress()) ) {
                msg.setSender(makeAddress(notificationConfiguration.getDefaultSenderAddress(), notificationConfiguration.getDefaultSenderName()));
            }
            return;
        }

        if ( !StringUtils.isBlank(notificationConfiguration.getDefaultSenderAddress()) ) {
            msg.setFrom(makeAddress(notificationConfiguration.getDefaultSenderAddress(), notificationConfiguration.getDefaultSenderName()));
        }
    }


    /**
     * @param mailAddress
     * @param fullName
     * @return
     * @throws UnsupportedEncodingException
     * @throws AddressException
     * @throws NotificationException
     */
    private static Address makeAddress ( String mailAddress, String fullName )
            throws UnsupportedEncodingException, AddressException, NotificationException {

        if ( !StringUtils.isBlank(fullName) && !StringUtils.isBlank(mailAddress) ) {
            return new InternetAddress(mailAddress, fullName, UTF8);
        }
        else if ( !StringUtils.isBlank(mailAddress) ) {
            return new InternetAddress(mailAddress);
        }

        throw new NotificationException("No valid address found"); //$NON-NLS-1$
    }


    /**
     * @param data
     * @return
     */
    private Map<String, Object> wrapObject ( MailNotificationData data, MailRecipient recpt ) {
        Map<String, Object> map = new HashMap<>();
        map.put("data", data); //$NON-NLS-1$

        map.put("adminContact", this.ctx.getConfigurationProvider().getNotificationConfiguration().getAdminContact()); //$NON-NLS-1$

        map.put("recipient", recpt); //$NON-NLS-1$
        map.put("footerText", this.ctx.getConfigurationProvider().getNotificationConfiguration().getFooter()); //$NON-NLS-1$

        if ( data.getSender() != null && data.getSender().getDetails() != null ) {
            map.put("senderDisplayName", makeSenderDisplayName(data)); //$NON-NLS-1$
        }
        return map;
    }


    /**
     * @param data
     * @return
     */
    private static String makeSenderDisplayName ( MailNotificationData data ) {
        String senderDisplayName = data.getSender().getDetails().getPreferredNameVerified() ? data.getSender().getDetails().getPreferredName() : null;

        if ( StringUtils.isBlank(senderDisplayName) ) {
            senderDisplayName = data.getSender().getPrincipal().getUserName();
        }
        return senderDisplayName;
    }


    @Override
    public void trackNotification ( EntityTransactionContext tx, String dedupId, DateTime expires ) {
        EntityManager em = tx.getEntityManager();
        NotificationTracker existing = em.find(NotificationTracker.class, dedupId);

        if ( existing == null ) {
            existing = new NotificationTracker();
            existing.setDedupId(dedupId);
            existing.setExpiration(expires);
        }
        else if ( existing.getExpiration() != null && existing.getExpiration().isBeforeNow() ) {
            existing.setExpiration(expires);
        }
        else {
            return;
        }
        em.persist(existing);
        em.flush();
    }


    @Override
    public boolean haveNotified ( EntityTransactionContext tx, String dedupId ) {
        NotificationTracker existing = tx.getEntityManager().find(NotificationTracker.class, dedupId);
        if ( existing == null ) {
            return false;
        }

        if ( existing.getExpiration() != null && existing.getExpiration().isBeforeNow() ) {
            return false;
        }

        return true;
    }


    @Override
    public int cleanupNotificationTrackers ( EntityTransactionContext tx ) {
        EntityManager em = tx.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<NotificationTracker> query = cb.createCriteriaDelete(NotificationTracker.class);
        Root<NotificationTracker> from = query.from(NotificationTracker.class);
        SingularAttribute<? super NotificationTracker, DateTime> expires = em.getMetamodel().entity(NotificationTracker.class)
                .getSingularAttribute("expiration", DateTime.class); //$NON-NLS-1$
        query.where(cb.lessThanOrEqualTo(from.get(expires), DateTime.now()));

        return em.createQuery(query).executeUpdate();
    }

}
