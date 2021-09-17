/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.util.WebUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.exceptions.AccessDeniedException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.MailRateLimitingException;
import eu.agno3.fileshare.exceptions.NotificationException;
import eu.agno3.fileshare.exceptions.PasswordChangeException;
import eu.agno3.fileshare.exceptions.RegistrationOpenException;
import eu.agno3.fileshare.exceptions.SecurityException;
import eu.agno3.fileshare.exceptions.TokenValidationException;
import eu.agno3.fileshare.exceptions.UserExistsException;
import eu.agno3.fileshare.exceptions.UserLimitExceededException;
import eu.agno3.fileshare.exceptions.UserNotFoundException;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.UserDetails;
import eu.agno3.fileshare.model.notify.LinkNotificationData;
import eu.agno3.fileshare.model.notify.MailRecipient;
import eu.agno3.fileshare.model.notify.UserNotificationData;
import eu.agno3.fileshare.model.tokens.PasswordResetToken;
import eu.agno3.fileshare.model.tokens.RegistrationToken;
import eu.agno3.fileshare.security.AccessControlService;
import eu.agno3.fileshare.service.LinkService;
import eu.agno3.fileshare.service.RegistrationService;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.MailNotifier;
import eu.agno3.fileshare.service.api.internal.NotificationService;
import eu.agno3.fileshare.service.api.internal.SingleUseTokenService;
import eu.agno3.fileshare.service.api.internal.UserServiceInternal;
import eu.agno3.fileshare.service.audit.GeneralFileshareAuditBuilder;
import eu.agno3.fileshare.service.audit.SubjectFileshareAuditBuilder;
import eu.agno3.fileshare.service.util.ServiceUtil;
import eu.agno3.runtime.crypto.scrypt.SCryptResult;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.eventlog.AuditContext;
import eu.agno3.runtime.eventlog.AuditStatus;
import eu.agno3.runtime.security.UserLicenseLimitExceededException;
import eu.agno3.runtime.security.web.login.token.TokenCreationException;
import eu.agno3.runtime.security.web.login.token.TokenGenerator;
import eu.agno3.runtime.security.web.login.token.TokenPrincipal;


/**
 * @author mbechler
 *
 */
@Component ( service = RegistrationService.class )
public class RegistrationServiceImpl implements RegistrationService {

    private static final Logger log = Logger.getLogger(RegistrationServiceImpl.class);

    private DefaultServiceContext ctx;

    private AccessControlService accessControl;

    private NotificationService notificationService;

    private TokenGenerator tokenGen;

    private LinkService linkService;

    private SingleUseTokenService tokenTracker;

    private UserServiceInternal userService;

    private MailRateLimiter rateLimiter;


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
    protected synchronized void setAccessControlService ( AccessControlService acs ) {
        this.accessControl = acs;
    }


    protected synchronized void unsetAccessControlService ( AccessControlService acs ) {
        if ( this.accessControl == acs ) {
            this.accessControl = null;
        }
    }


    @Reference
    protected synchronized void setUserService ( UserServiceInternal us ) {
        this.userService = us;
    }


    protected synchronized void unsetUserService ( UserServiceInternal us ) {
        if ( this.userService == us ) {
            this.userService = null;
        }
    }


    @Reference
    protected synchronized void setNotificationService ( NotificationService ns ) {
        this.notificationService = ns;
    }


    protected synchronized void unsetNotificationService ( NotificationService ns ) {
        if ( this.notificationService == ns ) {
            this.notificationService = null;
        }
    }


    @Reference
    protected synchronized void setTokenGenerator ( TokenGenerator tg ) {
        this.tokenGen = tg;
    }


    protected synchronized void unsetTokenGenerator ( TokenGenerator tg ) {
        if ( this.tokenGen == tg ) {
            this.tokenGen = null;
        }
    }


    @Reference
    protected synchronized void setLinkService ( LinkService ls ) {
        this.linkService = ls;
    }


    protected synchronized void unsetLinkService ( LinkService ls ) {
        if ( this.linkService == ls ) {
            this.linkService = null;
        }
    }


    @Reference
    protected synchronized void setTokenTracker ( SingleUseTokenService ts ) {
        this.tokenTracker = ts;
    }


    protected synchronized void unsetTokenTracker ( SingleUseTokenService ts ) {
        if ( this.tokenTracker == ts ) {
            this.tokenTracker = null;
        }
    }


    @Reference
    protected synchronized void setMailRateLimiter ( MailRateLimiter rrl ) {
        this.rateLimiter = rrl;
    }


    protected synchronized void unsetMailRateLimiter ( MailRateLimiter rrl ) {
        if ( this.rateLimiter == rrl ) {
            this.rateLimiter = null;
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.RegistrationService#register(eu.agno3.fileshare.model.notify.MailRecipient,
     *      boolean)
     */
    @Override
    public void register ( MailRecipient recpt, boolean resend ) throws FileshareException {

        try ( AuditContext<GeneralFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(GeneralFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("REGISTER"); //$NON-NLS-1$
            audit.builder().property("mailAddress", recpt.getMailAddress()); //$NON-NLS-1$
            audit.builder().policyAccepted();
            try {
                if ( !this.ctx.getConfigurationProvider().getUserConfig().isRegistrationEnabled() ) {
                    throw new AccessDeniedException();
                }

                LinkNotificationData data = new LinkNotificationData();
                doRegistration(
                    recpt.getMailAddress(),
                    data,
                    recpt,
                    this.notificationService.makeRegistrationVerification(),
                    this.ctx.getConfigurationProvider().getUserConfig().getRegistrationTokenLifetime(),
                    resend,
                    null);

            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( RuntimeException e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw e;
            }

        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.RegistrationService#invite(eu.agno3.fileshare.model.notify.MailRecipient,
     *      java.lang.String, boolean, org.joda.time.DateTime)
     */
    @Override
    public User invite ( MailRecipient recpt, String subject, boolean resend, DateTime expires ) throws FileshareException {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("INVITE"); //$NON-NLS-1$
            audit.builder().property("mailAddress", recpt.getMailAddress()); //$NON-NLS-1$
            audit.builder().property("fullName", recpt.getFullName()); //$NON-NLS-1$
            try {
                User invitingUser = null;
                if ( this.ctx.getConfigurationProvider().getUserConfig().isInvitationEnabled() ) {
                    this.accessControl.checkPermission("user:inviteUser"); //$NON-NLS-1$
                    invitingUser = this.accessControl.getCurrentUser();
                }
                else {
                    throw new AccessDeniedException();
                }

                LinkNotificationData data = new LinkNotificationData();
                if ( !StringUtils.isBlank(subject) ) {
                    data.setOverrideSubject(subject);
                }
                try {
                    return this.doInvitation(
                        audit,
                        recpt.getMailAddress(),
                        data,
                        recpt,
                        invitingUser,
                        this.notificationService.makeInvitationNotification(),
                        this.ctx.getConfigurationProvider().getUserConfig().getInvitationTokenLifetime(),
                        resend,
                        expires);
                }
                catch ( UserLicenseLimitExceededException e ) {
                    throw new UserLimitExceededException(e);
                }
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( RuntimeException e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw e;
            }
        }
    }


    /**
     * @param username
     * @param recpt
     * @param invitingUser
     * @param notifier
     * @param expires2
     * @param lifetimeDays
     * @return
     * @throws FileshareException
     * @throws UserLicenseLimitExceededException
     */
    private User doInvitation ( AuditContext<SubjectFileshareAuditBuilder> audit, String username, LinkNotificationData data, MailRecipient recpt,
            User invitingUser, MailNotifier<LinkNotificationData> notifier, Duration lifetime, boolean resend, DateTime userExpires )
            throws FileshareException, UserLicenseLimitExceededException {
        if ( StringUtils.isEmpty(recpt.getMailAddress()) ) {
            return null;
        }

        User created = null;

        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();

            this.rateLimiter.checkUserMailDelay(
                recpt,
                this.accessControl.getCurrentUserPrincipal(),
                WebUtils.getHttpRequest(SecurityUtils.getSubject()).getRemoteAddr());

            created = checkUserExists(tx, username, resend);
            audit.builder().property("invitingUser", invitingUser.getPrincipal().toString()); //$NON-NLS-1$

            DateTime expires = DateTime.now().plus(lifetime);;
            if ( created == null ) {
                Set<String> roles = this.ctx.getConfigurationProvider().getUserConfig().getInvitationUserRoles();

                UserDetails userDetails = new UserDetails();
                userDetails.setMailAddress(recpt.getMailAddress());
                userDetails.setMailAddressVerified(true);

                if ( !StringUtils.isBlank(recpt.getFullName()) && this.ctx.getConfigurationProvider().getUserConfig().isTrustInvitedUserNames() ) {
                    userDetails.setPreferredName(recpt.getFullName());
                    userDetails.setPreferredNameVerified(true);
                }
                else {
                    userDetails.setPreferredNameVerified(false);
                }

                if ( roles == null ) {
                    throw new FileshareException("No roles are configured"); //$NON-NLS-1$
                }

                boolean noRoot = this.ctx.getConfigurationProvider().getUserConfig().hasNoSubjectRoot(roles);

                audit.builder().property("roles", (Serializable) roles); //$NON-NLS-1$

                created = createUser(tx, username, userDetails, roles, recpt.getDesiredLocale(), noRoot, invitingUser, null, userExpires, true, true)
                        .cloneShallow(true);
            }
            audit.builder().subject(created);

            RegistrationToken tok = new RegistrationToken();
            tok.setId(UUID.randomUUID());
            tok.setUserName(username);
            tok.setRecipient(recpt);
            tok.setUserExpires(userExpires);
            tok.setInvitingUserId(invitingUser.getId());
            tok.setInvitingUserDisplayName(invitingUser.getUserDisplayName());
            tok.setInvitedUserId(created.getId());
            em.flush();
            try {
                String token = this.tokenGen.createToken(tok, expires);
                data.setSender(this.notificationService.getSenderForUser(invitingUser));
                data.setRecipients(new HashSet<>(Arrays.asList(recpt)));
                data.setExpirationDate(expires);
                data.setSendingUser(invitingUser);
                data.setLink(this.linkService.makeGenericLink(String.format("/registration/complete.xhtml?token=%s", token), null)); //$NON-NLS-1$
                notifier.notify(data);
            }
            catch ( TokenCreationException e ) {
                throw new SecurityException("Failed to create registration token", e); //$NON-NLS-1$
            }

            tx.commit();
            return created;
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to create invitation", e); //$NON-NLS-1$
        }
    }


    /**
     * @param em
     * @param q
     * @throws FileshareException
     */
    private User checkUserExists ( EntityTransactionContext tx, String username, boolean resend ) throws FileshareException {
        TypedQuery<User> q = tx.getEntityManager()
                .createQuery("SELECT u FROM User u WHERE u.principal.userName = :userName AND u.principal.realmName = :realmName", User.class); //$NON-NLS-1$

        q.setParameter("userName", username); //$NON-NLS-1$
        q.setParameter(
            "realmName", //$NON-NLS-1$
            "LOCAL"); //$NON-NLS-1$

        List<User> resultList = q.getResultList();
        if ( resultList.size() != 0 ) {
            User found = resultList.get(0);
            boolean userDisabled = this.userService.isUserDisabled(tx, found);
            if ( userDisabled && resend ) {
                return found;
            }
            else if ( userDisabled ) {
                throw new RegistrationOpenException("A verification mail has already recently been sent to this address"); //$NON-NLS-1$
            }

            throw new UserExistsException();
        }

        return null;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.RegistrationService#getInvitationSubject(eu.agno3.fileshare.model.notify.MailRecipient)
     */
    @Override
    public String getInvitationSubject ( MailRecipient recpt ) throws FileshareException {
        User invitingUser = null;
        if ( this.ctx.getConfigurationProvider().getUserConfig().isInvitationEnabled() ) {
            this.accessControl.checkPermission("user:inviteUser"); //$NON-NLS-1$
            invitingUser = this.accessControl.getCurrentUser();
        }
        else {
            throw new AccessDeniedException();
        }

        LinkNotificationData data = new LinkNotificationData();
        if ( invitingUser != null ) {
            data.setSender(this.notificationService.getSenderForUser(invitingUser));
        }

        MailNotifier<LinkNotificationData> notifier = this.notificationService.makeInvitationNotification();
        data.setRecipients(new HashSet<>(Arrays.asList(recpt)));
        DateTime expires = DateTime.now().plus(this.ctx.getConfigurationProvider().getUserConfig().getInvitationUserExpiration());
        data.setExpirationDate(expires);
        data.setSendingUser(invitingUser);
        data.setLink(this.linkService.makeGenericLink(String.format("/registration/complete.xhtml?token=%s", StringUtils.EMPTY), null)); //$NON-NLS-1$
        return notifier.makeSubject(data);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.RegistrationService#getInvitationPreview(eu.agno3.fileshare.model.notify.MailRecipient)
     */
    @Override
    public MimeMessage getInvitationPreview ( MailRecipient recpt ) throws FileshareException {
        User invitingUser = null;
        if ( this.ctx.getConfigurationProvider().getUserConfig().isInvitationEnabled() ) {
            this.accessControl.checkPermission("user:inviteUser"); //$NON-NLS-1$
            invitingUser = this.accessControl.getCurrentUser();
        }
        else {
            throw new AccessDeniedException();
        }

        LinkNotificationData data = new LinkNotificationData();
        if ( invitingUser != null ) {
            data.setSender(this.notificationService.getSenderForUser(invitingUser));
        }

        MailNotifier<LinkNotificationData> notifier = this.notificationService.makeInvitationNotification();
        data.setRecipients(new HashSet<>(Arrays.asList(recpt)));
        DateTime expires = DateTime.now().plus(this.ctx.getConfigurationProvider().getUserConfig().getInvitationUserExpiration());
        data.setExpirationDate(expires);
        data.setSendingUser(invitingUser);
        data.setLink(this.linkService.makeGenericLink(String.format("/registration/complete.xhtml?token=%s", StringUtils.EMPTY), null)); //$NON-NLS-1$
        return notifier.preview(data);
    }


    /**
     * @param username
     * @param recpt
     * @param invitingUser
     * @param notifier
     * @param expires2
     * @param lifetimeDays
     * @throws FileshareException
     */
    private void doRegistration ( String username, LinkNotificationData data, MailRecipient recpt, MailNotifier<LinkNotificationData> notifier,
            Duration lifetime, boolean resend, DateTime userExpires ) throws FileshareException {
        if ( StringUtils.isEmpty(recpt.getMailAddress()) ) {
            return;
        }

        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            String dedupId = "register/" + recpt.getMailAddress(); //$NON-NLS-1$

            if ( !resend && this.notificationService.haveNotified(tx, dedupId) ) {
                throw new RegistrationOpenException("A verification mail has already recently been sent to this address"); //$NON-NLS-1$
            }

            this.rateLimiter.checkRegistrationDelay(recpt, WebUtils.getHttpRequest(SecurityUtils.getSubject()).getRemoteAddr());

            RegistrationToken tok = new RegistrationToken();
            tok.setId(UUID.randomUUID());
            tok.setUserName(username);
            tok.setRecipient(recpt);
            tok.setUserExpires(userExpires);

            try {
                DateTime expires = DateTime.now().plus(lifetime);
                String token = this.tokenGen.createToken(tok, expires);
                data.setRecipients(new HashSet<>(Arrays.asList(recpt)));
                data.setExpirationDate(expires);
                data.setLink(this.linkService.makeGenericLink(String.format("/registration/complete.xhtml?token=%s", token), null)); //$NON-NLS-1$
                notifier.notify(data);
                this.notificationService.trackNotification(tx, dedupId, expires);
            }
            catch ( TokenCreationException e ) {
                throw new SecurityException("Failed to create registration token", e); //$NON-NLS-1$
            }
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to create registration", e); //$NON-NLS-1$
        }
    }


    @Override
    public void checkRegistration ( String userName ) throws FileshareException {
        TokenPrincipal tokPrinc = SecurityUtils.getSubject().getPrincipals().oneByType(TokenPrincipal.class);

        if ( tokPrinc == null ) {
            throw new AccessDeniedException("No token present"); //$NON-NLS-1$
        }

        Object data = tokPrinc.getData();
        if ( ! ( data instanceof RegistrationToken ) ) {
            throw new AccessDeniedException();
        }

        RegistrationToken tok = (RegistrationToken) data;

        boolean invitation = tok.getInvitingUserId() != null;
        checkRegistrationPerm(invitation);

        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            this.tokenTracker.checkToken(tx, tok);

            if ( invitation ) {
                User invitingUser = tx.getEntityManager().find(User.class, tok.getInvitingUserId());
                if ( invitingUser == null ) {
                    throw new UserNotFoundException();
                }
            }

            if ( invitation ) {
                if ( !this.userService.checkUserExists(tx, userName) ) {
                    throw new UserNotFoundException();
                }
            }
            else {
                if ( this.userService.checkUserExists(tx, userName) ) {
                    throw new UserExistsException();
                }
            }

            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Internal error", e); //$NON-NLS-1$
        }

    }


    /**
     * 
     * @param userName
     * @param newPassword
     * @param userDetails
     * @return the created user
     * @throws FileshareException
     */
    @Override
    public User completeRegistration ( String userName, String newPassword, UserDetails userDetails ) throws FileshareException {

        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("COMPLETE_REGISTRATION"); //$NON-NLS-1$
            audit.builder().property("userName", userName); //$NON-NLS-1$
            try {

                TokenPrincipal tokPrinc = SecurityUtils.getSubject().getPrincipals().oneByType(TokenPrincipal.class);

                if ( tokPrinc == null ) {
                    throw new AccessDeniedException("No token present"); //$NON-NLS-1$
                }

                Object data = tokPrinc.getData();
                if ( ! ( data instanceof RegistrationToken ) ) {
                    throw new AccessDeniedException();
                }

                RegistrationToken tok = (RegistrationToken) data;
                audit.builder().property("invitingUserId", tok.getInvitingUserId()); //$NON-NLS-1$

                boolean invitation = tok.getInvitingUserId() != null;

                if ( invitation ) {
                    return doCompleteInvitation(userName, newPassword, userDetails, audit, tokPrinc, tok);
                }

                try {
                    return doCompleteRegistration(userName, newPassword, userDetails, audit, tokPrinc, tok);
                }
                catch ( UserLicenseLimitExceededException e ) {
                    throw new UserLimitExceededException(e);
                }
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( RuntimeException e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw e;
            }
        }

    }


    /**
     * @param userName
     * @param newPassword
     * @param userDetails
     * @param audit
     * @param tokPrinc
     * @param tok
     * @return
     * @throws FileshareException
     * @throws AccessDeniedException
     * @throws PasswordChangeException
     * @throws TokenValidationException
     * @throws UserNotFoundException
     * @throws UserLicenseLimitExceededException
     */
    private User doCompleteRegistration ( String userName, String newPassword, UserDetails userDetails,
            AuditContext<SubjectFileshareAuditBuilder> audit, TokenPrincipal tokPrinc, RegistrationToken tok ) throws FileshareException,
            AccessDeniedException, PasswordChangeException, TokenValidationException, UserNotFoundException, UserLicenseLimitExceededException {

        if ( !this.ctx.getConfigurationProvider().getUserConfig().isRegistrationEnabled() ) {
            throw new AccessDeniedException();
        }

        userDetails.setMailAddress(tok.getRecipient().getMailAddress());
        userDetails.setMailAddressVerified(true);

        Duration userExpirationDuration = this.ctx.getConfigurationProvider().getUserConfig().getRegistrationUserExpiration();
        Set<String> roles = this.ctx.getConfigurationProvider().getUserConfig().getRegistrationUserRoles();

        if ( roles == null ) {
            throw new FileshareException("No roles are configured"); //$NON-NLS-1$
        }

        boolean noRoot = this.ctx.getConfigurationProvider().getUserConfig().hasNoSubjectRoot(roles);

        audit.builder().property("roles", (Serializable) roles); //$NON-NLS-1$

        SCryptResult pwHash = this.userService.genPasswordHash(newPassword);

        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();
            this.tokenTracker.checkToken(tx, tok);
            DateTime expire = userExpirationDuration != null ? DateTime.now().plus(userExpirationDuration) : null;
            User user = createUser(
                tx,
                userName,
                userDetails,
                roles,
                tok.getRecipient().getDesiredLocale(),
                noRoot,
                null,
                pwHash,
                expire,
                false,
                false);
            audit.builder().subject(user);
            em.flush();

            this.tokenTracker.invalidateToken(tx, tok, tokPrinc.getExpires());

            tx.commit();
            return user;
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Internal error", e); //$NON-NLS-1$
        }
    }


    private User doCompleteInvitation ( String userName, String newPassword, UserDetails userDetails,
            AuditContext<SubjectFileshareAuditBuilder> audit, TokenPrincipal tokPrinc, RegistrationToken tok )
            throws FileshareException, AccessDeniedException, PasswordChangeException, TokenValidationException, UserNotFoundException {

        audit.builder().action("COMPLETE_INVITATION"); //$NON-NLS-1$

        if ( !this.ctx.getConfigurationProvider().getUserConfig().isInvitationEnabled() ) {
            throw new AccessDeniedException();
        }

        SCryptResult pwHash = this.userService.genPasswordHash(newPassword);
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();
            this.tokenTracker.checkToken(tx, tok);

            User invitingUser = em.find(User.class, tok.getInvitingUserId());
            if ( invitingUser == null ) {
                throw new UserNotFoundException();
            }
            audit.builder().property("invitingUser", invitingUser.getPrincipal().toString()); //$NON-NLS-1$

            User user = em.find(User.class, tok.getInvitedUserId());
            audit.builder().subject(user);

            this.userService.changePassword(tx, user.getPrincipal(), pwHash);
            this.userService.enableLocalUser(tx, user);
            em.flush();

            this.tokenTracker.invalidateToken(tx, tok, tokPrinc.getExpires());

            tx.commit();
            doNotifyInvitingUser(invitingUser, user);
            return user;
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to complete invitation", e); //$NON-NLS-1$
        }
    }


    /**
     * @param invitingUser
     * @param invitation
     * @param user
     */
    private void doNotifyInvitingUser ( User invitingUser, User user ) {
        if ( invitingUser.getUserDetails() != null && invitingUser.getUserDetails().getMailAddress() != null ) {
            MailRecipient notifyRecpt = this.notificationService.getRecipientForUser(invitingUser, null);
            if ( log.isDebugEnabled() ) {
                log.debug("Notifiying " + notifyRecpt.getMailAddress()); //$NON-NLS-1$
            }
            MailNotifier<UserNotificationData> notification = this.notificationService.makeInvitationCompleteNotification();
            UserNotificationData data = new UserNotificationData();
            data.setRecpients(new HashSet<>(Arrays.asList(notifyRecpt)));
            data.setUser(user);
            try {
                notification.notify(data);
            }
            catch ( NotificationException e ) {
                log.warn("Failed to notify inviting user", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param userName
     * @param userDetails
     * @param roles
     * @param tok
     * @param noRoot
     * @param creator
     * @param pwHash
     * @param em
     * @param expire
     * @return
     * @throws FileshareException
     * @throws UserLicenseLimitExceededException
     */
    private User createUser ( EntityTransactionContext tx, String userName, UserDetails userDetails, Set<String> roles, Locale l, boolean noRoot,
            User creator, SCryptResult pwHash, DateTime expire, boolean disabled, boolean forcePasswordChange )
            throws FileshareException, UserLicenseLimitExceededException {
        User user = this.userService.createUserInternal(tx, userName, disabled, forcePasswordChange, noRoot, expire, pwHash, userDetails, creator);
        user.setRoles(roles);
        user.setExpiration(expire);
        user.setSecurityLabel(
            ServiceUtil.getOrCreateSecurityLabel(
                tx,
                this.ctx.getConfigurationProvider().getSecurityPolicyConfiguration().getDefaultUserLabelForRoles(roles)));
        user.setQuota(this.ctx.getConfigurationProvider().getQuotaConfiguration().getDefaultQuotaForRoles(roles));

        if ( l != null ) {
            user.setPreferences(new HashMap<>());
            user.getPreferences().put("overrideLocale", l.toLanguageTag()); //$NON-NLS-1$
        }

        tx.getEntityManager().persist(user);
        return user;
    }


    /**
     * @param invitation
     * @throws AccessDeniedException
     */
    private void checkRegistrationPerm ( boolean invitation ) throws AccessDeniedException {
        if ( invitation && !this.ctx.getConfigurationProvider().getUserConfig().isInvitationEnabled() ) {
            throw new AccessDeniedException();
        }
        else if ( !invitation && !this.ctx.getConfigurationProvider().getUserConfig().isRegistrationEnabled() ) {
            throw new AccessDeniedException();
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.RegistrationService#resetPassword(java.lang.String, java.lang.String, boolean)
     */
    @Override
    public void resetPassword ( String userName, String mailAddress, boolean resend ) throws FileshareException {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("RESET_PASSWORD"); //$NON-NLS-1$
            audit.builder().policyAccepted();
            audit.builder().property("userName", userName); //$NON-NLS-1$
            audit.builder().property("mailAddress", userName); //$NON-NLS-1$

            try {
                if ( !this.ctx.getConfigurationProvider().getUserConfig().isLocalPasswordRecoveryEnabled() ) {
                    throw new AccessDeniedException();
                }

                try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                    EntityManager em = tx.getEntityManager();

                    TypedQuery<User> q = em.createQuery(
                        "SELECT u FROM User u INNER JOIN u.userDetails ud WHERE u.principal.userName = ?1 AND u.principal.realmName = ?2 AND ud.mailAddress = ?3", //$NON-NLS-1$
                        User.class);

                    q.setParameter(1, userName);
                    q.setParameter(2, "LOCAL"); //$NON-NLS-1$
                    q.setParameter(3, mailAddress);
                    q.setMaxResults(2);
                    List<User> matched = q.getResultList();

                    if ( matched.size() == 1 ) {
                        User u = matched.get(0);
                        if ( log.isDebugEnabled() ) {
                            log.debug("Found user " + u); //$NON-NLS-1$
                        }

                        audit.builder().subject(u);

                        doSendPasswordResetToken(tx, u, resend);
                    }
                    else if ( log.isDebugEnabled() ) {
                        log.debug("No, or multiple matches " + matched); //$NON-NLS-1$
                    }

                    tx.commit();
                }
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to reset password", e); //$NON-NLS-1$
            }
        }

    }


    /**
     * @param em
     * @param u
     * @throws NotificationException
     * @throws SecurityException
     * @throws MailRateLimitingException
     */
    private void doSendPasswordResetToken ( EntityTransactionContext tx, User u, boolean resend )
            throws NotificationException, SecurityException, MailRateLimitingException {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setId(UUID.randomUUID());
        resetToken.setPrincipal(u.getPrincipal());

        MailNotifier<LinkNotificationData> notifier = this.notificationService.makePasswordResetNotification();
        LinkNotificationData data = new LinkNotificationData();
        String dedupId = "resetPassword/" + u.getId(); //$NON-NLS-1$
        DateTime expires = DateTime.now().plus(this.ctx.getConfigurationProvider().getUserConfig().getPasswordRecoveryTokenLifetime());

        if ( !resend && this.notificationService.haveNotified(tx, dedupId) ) {
            throw new NotificationException("A password reset notification has already recently been sent"); //$NON-NLS-1$
        }

        this.rateLimiter.checkPasswordResetDelay(u.getPrincipal(), WebUtils.getHttpRequest(SecurityUtils.getSubject()).getRemoteAddr());

        try {
            String token = this.tokenGen.createToken(resetToken, expires);
            data.setRecipients(new HashSet<>(Arrays.asList(this.notificationService.getRecipientForUser(u, null))));
            data.setExpirationDate(expires);
            data.setLink(this.linkService.makeGenericLink(String.format("/registration/resetPassword.xhtml?token=%s", token), null)); //$NON-NLS-1$
            notifier.notify(data);
            // TODO: this might be too harsh if a user looses the verification mail
            // and resending is actually desired - but some form of rate limiting should be there
            this.notificationService.trackNotification(tx, dedupId, expires);
        }
        catch ( TokenCreationException e ) {
            throw new SecurityException("Failed to create password reset token", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.RegistrationService#completePasswordReset(java.lang.String)
     */
    @Override
    public void completePasswordReset ( String password ) throws FileshareException {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("RESET_PASSWORD_COMPLETE"); //$NON-NLS-1$
            try {
                TokenPrincipal tokPrinc = SecurityUtils.getSubject().getPrincipals().oneByType(TokenPrincipal.class);
                if ( tokPrinc == null ) {
                    throw new AccessDeniedException("No token present"); //$NON-NLS-1$
                }

                if ( !this.ctx.getConfigurationProvider().getUserConfig().isLocalPasswordRecoveryEnabled() ) {
                    throw new AccessDeniedException();
                }

                Object data = tokPrinc.getData();
                if ( ! ( data instanceof PasswordResetToken ) ) {
                    throw new AccessDeniedException();
                }

                PasswordResetToken tok = (PasswordResetToken) tokPrinc.getData();
                audit.builder().property("userPrincipal", tok.getPrincipal().toString()); //$NON-NLS-1$
                SCryptResult pwHash = this.userService.genPasswordHash(password);
                try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                    this.tokenTracker.checkToken(tx, tok);
                    this.userService.changePassword(tx, tok.getPrincipal(), pwHash);
                    this.tokenTracker.invalidateToken(tx, tok, tokPrinc.getExpires());
                    tx.commit();
                }
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to complete password reset", e); //$NON-NLS-1$
            }
        }
    }
}
