/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.01.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.util.WebUtils;
import org.joda.time.DateTime;
import org.joda.time.ReadableDuration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.exceptions.AccessDeniedException;
import eu.agno3.fileshare.exceptions.AuthenticationException;
import eu.agno3.fileshare.exceptions.CannotShareRootEntityException;
import eu.agno3.fileshare.exceptions.CannotShareToSelfException;
import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.GrantExistsException;
import eu.agno3.fileshare.exceptions.MailingDisabledException;
import eu.agno3.fileshare.exceptions.NotificationException;
import eu.agno3.fileshare.exceptions.NotificationMultiException;
import eu.agno3.fileshare.exceptions.PasswordInMessageException;
import eu.agno3.fileshare.exceptions.PolicyNotFoundException;
import eu.agno3.fileshare.exceptions.PolicyNotFulfilledException;
import eu.agno3.fileshare.exceptions.SecurityException;
import eu.agno3.fileshare.exceptions.ShareLifetimeInvalidException;
import eu.agno3.fileshare.exceptions.ShareNotFoundException;
import eu.agno3.fileshare.exceptions.SubjectNotFoundException;
import eu.agno3.fileshare.exceptions.UserNotFoundException;
import eu.agno3.fileshare.model.ContentEntity;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.GrantType;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.LinkShareData;
import eu.agno3.fileshare.model.MailGrant;
import eu.agno3.fileshare.model.MappedVFSEntity;
import eu.agno3.fileshare.model.PolicyViolation;
import eu.agno3.fileshare.model.SecurityLabel;
import eu.agno3.fileshare.model.ShareProperties;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.SubjectGrant;
import eu.agno3.fileshare.model.TokenGrant;
import eu.agno3.fileshare.model.TokenShare;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.model.audit.EntityFileshareEvent;
import eu.agno3.fileshare.model.notify.MailNotificationData;
import eu.agno3.fileshare.model.notify.MailRecipient;
import eu.agno3.fileshare.model.notify.MailSender;
import eu.agno3.fileshare.model.notify.MailShareNotificationData;
import eu.agno3.fileshare.model.notify.ShareNotificationData;
import eu.agno3.fileshare.model.tokens.AnonymousGrantToken;
import eu.agno3.fileshare.security.AccessControlService;
import eu.agno3.fileshare.service.ArchiveType;
import eu.agno3.fileshare.service.LinkService;
import eu.agno3.fileshare.service.ShareService;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.MailNotifier;
import eu.agno3.fileshare.service.api.internal.NotificationService;
import eu.agno3.fileshare.service.api.internal.ShareServiceInternal;
import eu.agno3.fileshare.service.api.internal.UserServiceInternal;
import eu.agno3.fileshare.service.api.internal.VFSServiceInternal;
import eu.agno3.fileshare.service.audit.SingleEntityFileshareAuditBuilder;
import eu.agno3.fileshare.service.config.PolicyConfiguration;
import eu.agno3.fileshare.service.config.SecurityPolicyConfiguration;
import eu.agno3.fileshare.service.config.ViewPolicyConfiguration;
import eu.agno3.fileshare.service.util.ServiceUtil;
import eu.agno3.fileshare.util.GrantComparator;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.eventlog.AuditContext;
import eu.agno3.runtime.eventlog.AuditStatus;
import eu.agno3.runtime.security.password.PasswordCompareUtil;
import eu.agno3.runtime.security.password.PasswordGenerationException;
import eu.agno3.runtime.security.password.PasswordGenerator;
import eu.agno3.runtime.security.password.PasswordPolicyChecker;
import eu.agno3.runtime.security.web.login.token.TokenCreationException;
import eu.agno3.runtime.security.web.login.token.TokenGenerator;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */

@Component ( service = {
    ShareServiceInternal.class, ShareService.class
}, configurationPid = "share" )
public class ShareServiceImpl implements ShareServiceInternal {

    private static final Logger log = Logger.getLogger(ShareServiceImpl.class);

    private DefaultServiceContext sctx;

    private AccessControlService accessControl;

    private UserServiceInternal userService;

    private TokenGenerator tokenGenerator;

    private NotificationService notificationService;

    private LinkService linkService;

    private PasswordPolicyChecker passwordPolicy;

    private MailRateLimiter rateLimiter;

    private VFSServiceInternal vfs;

    private PasswordGenerator passwordGenerator;

    private boolean linksExpiresWithGrant;

    private static final SecureRandom SRAND = new SecureRandom();


    @Activate
    @Modified
    protected synchronized void configure ( ComponentContext ctx ) {
        this.linksExpiresWithGrant = ConfigUtil.parseBoolean(ctx.getProperties(), "linksExpiresWithGrant", true); //$NON-NLS-1$
    }


    @Reference
    protected synchronized void setServiceContext ( DefaultServiceContext ctx ) {
        this.sctx = ctx;
    }


    protected synchronized void unsetServiceContext ( DefaultServiceContext ctx ) {
        if ( this.sctx == ctx ) {
            this.sctx = null;
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
    protected synchronized void setTokenGenerator ( TokenGenerator gen ) {
        this.tokenGenerator = gen;
    }


    protected synchronized void unsetTokenGenerator ( TokenGenerator gen ) {
        if ( this.tokenGenerator == gen ) {
            this.tokenGenerator = null;
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
    protected synchronized void setLinkService ( LinkService ls ) {
        this.linkService = ls;
    }


    protected synchronized void unsetLinkService ( LinkService ls ) {
        if ( this.linkService == ls ) {
            this.linkService = null;
        }
    }


    @Reference
    protected synchronized void setPasswordPolicyChecker ( PasswordPolicyChecker ppc ) {
        this.passwordPolicy = ppc;
    }


    protected synchronized void unsetPasswordPolicyChecker ( PasswordPolicyChecker ppc ) {
        if ( this.passwordPolicy == ppc ) {
            this.passwordPolicy = null;
        }
    }


    @Reference
    protected synchronized void setPasswordGenerator ( PasswordGenerator pg ) {
        this.passwordGenerator = pg;
    }


    protected synchronized void unsetPasswordGenerator ( PasswordGenerator pg ) {
        if ( this.passwordGenerator == pg ) {
            this.passwordGenerator = null;
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


    @Reference
    protected synchronized void setVFSService ( VFSServiceInternal vs ) {
        this.vfs = vs;
    }


    protected synchronized void unsetVFSService ( VFSServiceInternal vs ) {
        if ( this.vfs == vs ) {
            this.vfs = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * @throws EntityNotFoundException
     * 
     * @see eu.agno3.fileshare.service.ShareService#getGrant(java.util.UUID)
     */
    @Override
    public Grant getGrant ( UUID id ) throws EntityNotFoundException, FileshareException {

        try ( EntityTransactionContext tx = this.sctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();
            tx.commit();
            Grant grant = em.find(Grant.class, id);
            if ( grant == null ) {
                throw new ShareNotFoundException("Failed to find grant with id " + id); //$NON-NLS-1$
            }

            Grant g = checkGrantAccess(grant, true);
            ServiceUtil.enhanceVFSGrant(tx, this.vfs, g);
            return g.cloneShallow();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get grant", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ShareService#getGrantUnchecked(java.util.UUID)
     */
    @Override
    public Grant getGrantUnchecked ( UUID id ) throws EntityNotFoundException, FileshareException {
        try ( EntityTransactionContext tx = this.sctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();
            tx.commit();
            Grant grant = em.find(Grant.class, id);
            if ( grant == null ) {
                throw new ShareNotFoundException("Failed to find grant with id " + id); //$NON-NLS-1$
            }
            return grant.cloneShallow(true, true);
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get grant", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ShareService#generateSharePassword(eu.agno3.fileshare.model.SecurityLabel,
     *      java.util.Locale)
     */
    @Override
    public String generateSharePassword ( SecurityLabel securityLabel, Locale l ) throws PasswordGenerationException, PolicyNotFoundException {
        SecurityPolicyConfiguration sp = this.sctx.getConfigurationProvider().getSecurityPolicyConfiguration();
        PolicyConfiguration policy = sp.getPolicy(securityLabel != null ? securityLabel.getLabel() : sp.getDefaultLabel());

        int defaultSharePasswordBits = sp.getSharePasswordBits();
        return this.passwordGenerator.generate(
            sp.getSharePasswordType(),
            policy != null ? Math.max(policy.getMinTokenPasswordEntropy(), defaultSharePasswordBits) : defaultSharePasswordBits,
            l);

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ShareService#authToken(java.util.UUID, java.lang.String)
     */
    @Override
    public void authToken ( UUID id, String password ) throws FileshareException {
        try ( EntityTransactionContext tx = this.sctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();
            Grant grant = em.find(Grant.class, id);

            if ( grant == null ) {
                throw new ShareNotFoundException("Failed to find grant with id " + id); //$NON-NLS-1$
            }

            Grant g = checkGrantAccess(grant, false);
            if ( ! ( g instanceof TokenGrant ) ) {
                throw new ShareNotFoundException("Not a token grant"); //$NON-NLS-1$
            }

            TokenGrant tg = (TokenGrant) g;
            if ( !tg.getPasswordProtected() ) {
                throw new ShareNotFoundException("This grant is not password protected"); //$NON-NLS-1$
            }

            if ( !PasswordCompareUtil.comparePassword(tg.getPassword(), password) ) {
                throw new AuthenticationException("Wrong grant password"); //$NON-NLS-1$
            }
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to authenticate token", e); //$NON-NLS-1$
        }
    }


    /**
     * @param grant
     * @return
     * @throws AuthenticationException
     * @throws AccessDeniedException
     * @throws UserNotFoundException
     */
    private Grant checkGrantAccess ( Grant grant, boolean checkPass ) throws AuthenticationException, AccessDeniedException, UserNotFoundException {
        if ( this.accessControl.isOwner(null, grant.getEntity()) ) {
            return grant;
        }

        if ( grant instanceof SubjectGrant ) {
            Subject target = ( (SubjectGrant) grant ).getTarget();

            if ( target instanceof User && target.equals(this.accessControl.getCurrentUser()) ) {
                return grant;
            }
            else if ( target instanceof Group && this.accessControl.isMember((Group) target) ) {
                return grant;
            }
        }
        else if ( grant instanceof TokenGrant && this.accessControl.isTokenAuth() ) {
            TokenGrant tok = (TokenGrant) grant;
            if ( this.accessControl.matchAuthTokenValue(tok.getToken()) ) {
                if ( checkPass ) {
                    this.accessControl.checkGrantPassword(tok);
                }
                return tok;
            }
        }
        else {
            log.debug("Auth type does not match"); //$NON-NLS-1$
        }

        throw new AccessDeniedException("No matching grant found"); //$NON-NLS-1$
    }


    @Override
    public List<SubjectGrant> shareToSubjects ( EntityKey entityId, Collection<UUID> subjectIds, ShareProperties props, boolean notify )
            throws FileshareException {
        try ( AuditContext<SingleEntityFileshareAuditBuilder> audit = this.sctx.getEventLogger().audit(SingleEntityFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action(EntityFileshareEvent.SHARE_SUBJECT_ACTION);
            audit.builder().property("targetIds", new LinkedList<>(subjectIds)); //$NON-NLS-1$
            audit.builder().property("notify", notify); //$NON-NLS-1$
            auditCommonShareProps(props, audit);
            try {
                this.accessControl.checkPermission("share:subjects"); //$NON-NLS-1$

                List<ShareNotificationData> recipients = new LinkedList<>();
                try ( EntityTransactionContext tx = this.sctx.getFileshareEntityTS().start();
                      VFSContext v = this.vfs.getVFS(entityId).begin(tx) ) {
                    EntityManager em = tx.getEntityManager();
                    VFSEntity vEntity = v.load(entityId);
                    audit.builder().entity(vEntity);
                    this.accessControl.checkOwner(v, vEntity);
                    VFSContainerEntity parent = v.getParent(vEntity);
                    audit.builder().parentEntity(parent);
                    ContentEntity persistentEntity = v.getOrCreateMappedEntity(vEntity);

                    if ( notify ) {
                        this.rateLimiter.checkUserMailDelay(
                            null,
                            this.accessControl.getCurrentUserPrincipal(),
                            WebUtils.getHttpRequest(SecurityUtils.getSubject()).getRemoteAddr());
                    }

                    if ( persistentEntity == null ) {
                        throw new EntityNotFoundException("Failed to find entity by id " + entityId); //$NON-NLS-1$
                    }

                    if ( parent == null ) {
                        throw new CannotShareRootEntityException();
                    }

                    PolicyConfiguration policy = getPolicy(persistentEntity);
                    checkSharePolicy(GrantType.SUBJECT, props, policy);

                    User currentUser = this.userService.getCurrentUser(tx);

                    DateTime realExpires = makeRealShareExpires(policy, props, persistentEntity);
                    List<SubjectGrant> issued = new LinkedList<>();

                    LinkedList<String> targetNames = new LinkedList<>();
                    LinkedList<String> targetTypes = new LinkedList<>();
                    LinkedList<UUID> targetGrant = new LinkedList<>();
                    audit.builder().property("targetTypes", targetTypes); //$NON-NLS-1$
                    audit.builder().property("targetNames", targetNames); //$NON-NLS-1$
                    audit.builder().property("targetGrant", targetGrant); //$NON-NLS-1$

                    MailNotifier<ShareNotificationData> shareNotification = this.notificationService.makeShareNotification();
                    for ( UUID subjId : subjectIds ) {
                        ShareNotificationData data = setupSubjectShare(
                            tx,
                            persistentEntity,
                            props,
                            currentUser,
                            realExpires,
                            subjId,
                            targetTypes,
                            targetNames,
                            targetGrant);
                        if ( data != null ) {
                            if ( notify && subjectIds.size() > 1 ) {
                                recipients.add(data);
                            }
                            else if ( notify ) {
                                doMailNotification(persistentEntity, props, Arrays.asList(data), shareNotification);
                            }
                            issued.add(data.getGrant());
                        }
                    }

                    updateEntityExpiry(policy, persistentEntity, realExpires);

                    em.flush();
                    v.commit();
                    tx.commit();

                    if ( notify && subjectIds.size() > 1 ) {
                        doMailNotification(persistentEntity, props, recipients, shareNotification);
                    }

                    return issued;
                }
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to create share", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param props
     * @param audit
     */
    private static void auditCommonShareProps ( ShareProperties props, AuditContext<SingleEntityFileshareAuditBuilder> audit ) {
        audit.builder().property("passwordProtected", !StringUtils.isBlank(props.getPassword())); //$NON-NLS-1$
        audit.builder().property("permissions", GrantPermission.toInt(props.getPermissions())); //$NON-NLS-1$
        audit.builder().property("expires", props.getExpiry() != null ? props.getExpiry().getMillis() : null); //$NON-NLS-1$
    }


    @Override
    public String getSubjectShareSubject ( EntityKey entityId, UUID subjectId, ShareProperties props ) throws FileshareException {
        if ( this.sctx.getConfigurationProvider().getNotificationConfiguration().isMailingDisabled() ) {
            throw new MailingDisabledException();
        }
        this.accessControl.checkPermission("share:subject"); //$NON-NLS-1$
        MailNotifier<ShareNotificationData> subjectShareNotifier = this.notificationService.makeShareNotification();

        try ( EntityTransactionContext tx = this.sctx.getFileshareEntityTS().startReadOnly();
              VFSContext v = this.vfs.getVFS(entityId).begin(tx) ) {
            EntityManager em = tx.getEntityManager();
            VFSEntity persistentEntity = v.load(entityId);
            Subject s = subjectId != null ? em.find(Subject.class, subjectId) : null;
            User currentUser = this.accessControl.getCurrentUser(tx);
            this.accessControl.checkOwner(v, persistentEntity);
            return subjectShareNotifier.makeSubject(makePreviewSubjectShareData(s, currentUser, props, persistentEntity));
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get share subject", e); //$NON-NLS-1$
        }
    }


    @Override
    public MimeMessage getSubjectSharePreview ( EntityKey entityId, UUID subjectId, ShareProperties props ) throws FileshareException {
        if ( this.sctx.getConfigurationProvider().getNotificationConfiguration().isMailingDisabled() ) {
            throw new MailingDisabledException();
        }
        this.accessControl.checkPermission("share:subject"); //$NON-NLS-1$
        MailNotifier<ShareNotificationData> subjectShareNotifier = this.notificationService.makeShareNotification();

        try ( EntityTransactionContext tx = this.sctx.getFileshareEntityTS().startReadOnly();
              VFSContext v = this.vfs.getVFS(entityId).begin(tx) ) {
            EntityManager em = tx.getEntityManager();
            VFSEntity persistentEntity = v.load(entityId);
            Subject s = em.find(Subject.class, subjectId);
            User currentUser = this.accessControl.getCurrentUser(tx);
            this.accessControl.checkOwner(v, persistentEntity);
            return subjectShareNotifier.preview(makePreviewSubjectShareData(s, currentUser, props, persistentEntity));
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get preview", e); //$NON-NLS-1$
        }
    }


    /**
     * @param s
     * @param currentUser
     * @param props
     * @param e
     * @return
     * @throws FileshareException
     */
    private ShareNotificationData makePreviewSubjectShareData ( Subject s, User currentUser, ShareProperties props, VFSEntity e )
            throws FileshareException {
        SubjectGrant g = new SubjectGrant();
        DateTime now = DateTime.now();
        g.setCreated(now);
        g.setLastModified(now);
        g.setCreator(currentUser);
        g.setPermissions(props.getPermissions());
        g.setTarget(s);
        g.setId(UUID.randomUUID());
        return makeSubjectShareNotificationData(e, currentUser, props, s, g);
    }


    /**
     * @param persistentEntity
     * @return
     * @throws PolicyNotFoundException
     */
    private PolicyConfiguration getPolicy ( VFSEntity persistentEntity ) throws PolicyNotFoundException {

        if ( persistentEntity == null || persistentEntity.getSecurityLabel() == null ) {
            throw new PolicyNotFoundException(null, "Entity has no policy"); //$NON-NLS-1$
        }

        return this.sctx.getConfigurationProvider().getSecurityPolicyConfiguration().getPolicy(persistentEntity.getSecurityLabel().getLabel());
    }


    /**
     * @param policy
     * @param props
     * @param persistentEntity
     * @return
     * @throws ShareLifetimeInvalidException
     */
    private static DateTime makeRealShareExpires ( PolicyConfiguration policy, ShareProperties props, VFSEntity persistentEntity )
            throws ShareLifetimeInvalidException {
        DateTime realExpires = null;
        if ( props.getExpiry() != null ) {
            realExpires = props.getExpiry().withTimeAtStartOfDay();
        }

        checkShareLifetime(policy, realExpires);

        return realExpires;
    }


    /**
     * @param policy
     * @param realExpires
     * @throws ShareLifetimeInvalidException
     */
    private static void checkShareLifetime ( PolicyConfiguration policy, DateTime realExpires ) throws ShareLifetimeInvalidException {
        if ( realExpires == null && policy.getMaximumShareLifetime() != null ) {
            throw new ShareLifetimeInvalidException("No expiry time set but maximum expiration duration is " + policy.getMaximumExpirationDuration()); //$NON-NLS-1$
        }

        if ( policy.getMaximumShareLifetime() != null && DateTime.now().plus(policy.getMaximumShareLifetime()).isBefore(realExpires) ) {
            throw new ShareLifetimeInvalidException("Share lifetime is longer than configured maximum lifetime " //$NON-NLS-1$
                    + policy.getMaximumExpirationDuration());
        }
    }


    @Override
    public List<MailGrant> shareByMail ( EntityKey entityId, Collection<MailRecipient> mailAddresses, ShareProperties props, boolean resend )
            throws FileshareException {
        try ( AuditContext<SingleEntityFileshareAuditBuilder> audit = this.sctx.getEventLogger().audit(SingleEntityFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action(EntityFileshareEvent.SHARE_MAIL_ACTION);
            LinkedList<String> addrs = new LinkedList<>();
            for ( MailRecipient recpt : mailAddresses ) {
                addrs.add(recpt.getMailAddress());
            }
            audit.builder().property("recipients", addrs); //$NON-NLS-1$
            audit.builder().property("resend", resend); //$NON-NLS-1$
            auditCommonShareProps(props, audit);
            try {
                if ( this.sctx.getConfigurationProvider().getNotificationConfiguration().isMailingDisabled() ) {
                    throw new MailingDisabledException();
                }
                this.accessControl.checkPermission("share:mail"); //$NON-NLS-1$
                List<MailShareNotificationData> recipients = new ArrayList<>();
                ContentEntity persistentEntity;
                List<MailGrant> issued = new LinkedList<>();
                MailNotifier<MailShareNotificationData> mailShareNotifier = this.notificationService.makeMailShareNotifier();
                try ( EntityTransactionContext tx = this.sctx.getFileshareEntityTS().start();
                      VFSContext v = this.vfs.getVFS(entityId).begin(tx) ) {
                    EntityManager em = tx.getEntityManager();
                    VFSEntity vEntity = v.load(entityId);
                    audit.builder().entity(vEntity);
                    this.accessControl.checkOwner(v, vEntity);
                    VFSContainerEntity parent = v.getParent(vEntity);
                    audit.builder().parentEntity(parent);
                    persistentEntity = v.getOrCreateMappedEntity(vEntity);

                    audit.builder().entity(persistentEntity);
                    this.accessControl.checkOwner(v, persistentEntity);

                    if ( persistentEntity == null ) {
                        throw new EntityNotFoundException("Failed to find entity by id " + entityId); //$NON-NLS-1$
                    }

                    if ( parent == null ) {
                        throw new CannotShareRootEntityException();
                    }

                    User currentUser = this.userService.getCurrentUser(tx);
                    MailSender sender = this.notificationService.getSenderForUser(currentUser);
                    PolicyConfiguration policy = getPolicy(persistentEntity);

                    checkSharePolicy(GrantType.MAIL, props, policy);

                    LinkedList<UUID> targetGrant = new LinkedList<>();
                    audit.builder().property("targetGrant", targetGrant); //$NON-NLS-1$

                    checkMessage(props);

                    DateTime realExpires = makeRealShareExpires(policy, props, persistentEntity);
                    updateEntityExpiry(policy, persistentEntity, realExpires);
                    for ( MailRecipient mailAddr : mailAddresses ) {
                        this.rateLimiter.checkUserMailDelay(
                            mailAddr,
                            this.accessControl.getCurrentUserPrincipal(),
                            WebUtils.getHttpRequest(SecurityUtils.getSubject()).getRemoteAddr());
                        MailShareNotificationData recpInfo = setupMailShare(
                            tx,
                            persistentEntity,
                            policy,
                            props,
                            currentUser,
                            realExpires,
                            mailAddr,
                            sender,
                            resend,
                            targetGrant);

                        if ( recpInfo != null && mailAddresses.size() > 1 ) {
                            recipients.add(recpInfo);
                            issued.add(recpInfo.getGrant());
                        }
                        else if ( recpInfo != null ) {
                            doMailNotification(persistentEntity, props, Arrays.asList(recpInfo), mailShareNotifier);
                            issued.add(recpInfo.getGrant());
                        }
                    }

                    em.flush();
                    tx.commit();
                }

                doMailNotification(persistentEntity, props, recipients, mailShareNotifier);
                return issued;
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to create mail share", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param props
     * @throws PasswordInMessageException
     */
    private static void checkMessage ( ShareProperties props ) throws PasswordInMessageException {
        if ( !StringUtils.isBlank(props.getPassword()) && !StringUtils.isBlank(props.getMessage()) ) {
            if ( props.getMessage().contains(props.getPassword()) ) {
                throw new PasswordInMessageException();
            }
        }
    }


    @Override
    public String getMailShareSubject ( EntityKey entityId, MailRecipient recpt, ShareProperties props ) throws FileshareException {
        if ( this.sctx.getConfigurationProvider().getNotificationConfiguration().isMailingDisabled() ) {
            throw new MailingDisabledException();
        }

        this.accessControl.checkPermission("share:mail"); //$NON-NLS-1$
        MailNotifier<MailShareNotificationData> mailShareNotifier = this.notificationService.makeMailShareNotifier();

        try ( EntityTransactionContext tx = this.sctx.getFileshareEntityTS().startReadOnly();
              VFSContext v = this.vfs.getVFS(entityId).begin(tx) ) {
            VFSEntity persistentEntity = v.load(entityId);
            this.accessControl.checkOwner(v, persistentEntity);
            return mailShareNotifier.makeSubject(makePreviewMailShareData(tx, recpt, props, persistentEntity));
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to generate mail share subject", e); //$NON-NLS-1$
        }
    }


    @Override
    public MimeMessage getMailSharePreview ( EntityKey entityId, MailRecipient recpt, ShareProperties props ) throws FileshareException {
        if ( this.sctx.getConfigurationProvider().getNotificationConfiguration().isMailingDisabled() ) {
            throw new MailingDisabledException();
        }
        this.accessControl.checkPermission("share:mail"); //$NON-NLS-1$
        MailNotifier<MailShareNotificationData> mailShareNotifier = this.notificationService.makeMailShareNotifier();

        try ( EntityTransactionContext tx = this.sctx.getFileshareEntityTS().startReadOnly();
              VFSContext v = this.vfs.getVFS(entityId).begin(tx) ) {
            VFSEntity persistentEntity = v.load(entityId);
            this.accessControl.checkOwner(v, persistentEntity);
            return mailShareNotifier.preview(makePreviewMailShareData(tx, recpt, props, persistentEntity));
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to generate mail share preview", e); //$NON-NLS-1$
        }
    }


    /**
     * @param recpt
     * @param props
     * @param persistentEntity
     * @return
     * @throws AuthenticationException
     * @throws UserNotFoundException
     * @throws FileshareException
     */
    private MailShareNotificationData makePreviewMailShareData ( EntityTransactionContext tx, MailRecipient recpt, ShareProperties props,
            VFSEntity persistentEntity ) throws AuthenticationException, UserNotFoundException, FileshareException {
        User currentUser = this.userService.getCurrentUser(tx);
        MailSender sender = this.notificationService.getSenderForUser(currentUser);
        MailGrant g = new MailGrant();
        g.setCreator(currentUser);
        g.setExpires(props.getExpiry());
        g.setPermissions(props.getPermissions());
        if ( recpt != null ) {
            g.setMailAddress(recpt.getMailAddress());
        }
        g.setId(UUID.randomUUID());
        MailShareNotificationData data = makeMailShareNotification(persistentEntity, recpt, props, sender, g, null);
        return data;
    }


    /**
     * @param persistentEntity
     * @param props
     * @param recipients
     * @throws NotificationException
     */
    protected <T extends MailNotificationData> void doMailNotification ( VFSEntity persistentEntity, ShareProperties props, List<T> recipients,
            MailNotifier<T> notifier ) throws NotificationException {
        List<NotificationException> failures = new LinkedList<>();
        for ( T recptInfo : recipients ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Send mail to " + recptInfo.getRecipients()); //$NON-NLS-1$
            }

            try {
                notifier.notify(recptInfo);
            }
            catch ( NotificationException e ) {
                log.debug("Notification failed", e); //$NON-NLS-1$
                failures.add(e);
            }
        }

        if ( !failures.isEmpty() ) {
            if ( failures.size() == 1 ) {
                throw failures.get(0);
            }
            throw new NotificationMultiException(failures, "Notification of one or more recipients failed"); //$NON-NLS-1$
        }
    }


    @Override
    public TokenShare shareToken ( EntityKey entityId, String identifier, ShareProperties props ) throws FileshareException {
        try ( AuditContext<SingleEntityFileshareAuditBuilder> audit = this.sctx.getEventLogger().audit(SingleEntityFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action(EntityFileshareEvent.SHARE_LINK_ACTION);
            audit.builder().property("identifier", identifier); //$NON-NLS-1$
            auditCommonShareProps(props, audit);
            try {
                this.accessControl.checkPermission("share:token"); //$NON-NLS-1$
                try ( EntityTransactionContext tx = this.sctx.getFileshareEntityTS().start();
                      VFSContext v = this.vfs.getVFS(entityId).begin(tx) ) {
                    EntityManager em = tx.getEntityManager();
                    VFSEntity vEntity = v.load(entityId);
                    audit.builder().entity(vEntity);
                    this.accessControl.checkOwner(v, vEntity);
                    VFSContainerEntity parent = v.getParent(vEntity);
                    ContentEntity persistentEntity = v.getOrCreateMappedEntity(vEntity);

                    if ( persistentEntity == null ) {
                        throw new EntityNotFoundException("Failed to find entity by id " + entityId); //$NON-NLS-1$
                    }

                    if ( parent == null ) {
                        throw new CannotShareRootEntityException();
                    }

                    User currentUser = this.userService.getCurrentUser(tx);
                    PolicyConfiguration policy = getPolicy(persistentEntity);

                    checkSharePolicy(GrantType.LINK, props, policy);

                    DateTime realExpires = makeRealShareExpires(policy, props, persistentEntity);
                    TokenShare token = setupTokenShare(persistentEntity, policy, props, em, currentUser, realExpires, identifier);

                    updateEntityExpiry(policy, persistentEntity, realExpires);
                    v.save(persistentEntity);
                    v.commit();
                    tx.commit();
                    return token;
                }
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to create token grant", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param type
     * @param props
     * @param policy
     * @throws PolicyNotFulfilledException
     */
    private void checkSharePolicy ( GrantType type, ShareProperties props, PolicyConfiguration policy ) throws PolicyNotFulfilledException {
        if ( !policy.getAllowedShareTypes().isEmpty() && !policy.getAllowedShareTypes().contains(type) ) {
            throw new PolicyNotFulfilledException(new PolicyViolation("shareType.invalidType", type)); //$NON-NLS-1$
        }

        if ( type != GrantType.SUBJECT ) {
            if ( policy.isRequireTokenPassword() && StringUtils.isEmpty(props.getPassword()) ) {
                throw new PolicyNotFulfilledException(new PolicyViolation("tokenPassword.required")); //$NON-NLS-1$
            }
            else if ( !StringUtils.isEmpty(props.getPassword()) && policy.getMinTokenPasswordEntropy() > 0 ) {
                int entropy = this.passwordPolicy.estimateEntropy(props.getPassword());
                if ( entropy < policy.getMinTokenPasswordEntropy() ) {
                    throw new PolicyNotFulfilledException(new PolicyViolation("tokenPassword.entropy", entropy, policy.getMinTokenPasswordEntropy())); //$NON-NLS-1$
                }
            }
        }
    }


    @Override
    public TokenShare recreateTokenShare ( TokenGrant g, ShareProperties props ) throws FileshareException {
        try ( AuditContext<SingleEntityFileshareAuditBuilder> audit = this.sctx.getEventLogger().audit(SingleEntityFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action(EntityFileshareEvent.RECREATE_SHARE_LINK_ACTION);
            audit.builder().property("identifier", g.getIdentifier()); //$NON-NLS-1$
            audit.builder().property("passwordProtected", !StringUtils.isBlank(g.getPassword())); //$NON-NLS-1$
            audit.builder().property("permissions", GrantPermission.toInt(g.getPermissions())); //$NON-NLS-1$
            audit.builder().property("expires", g.getExpires() != null ? g.getExpires().getMillis() : null); //$NON-NLS-1$
            audit.builder().property("grantId", g.getId()); //$NON-NLS-1$
            try {
                this.accessControl.checkPermission("share:token"); //$NON-NLS-1$
                try ( EntityTransactionContext tx = this.sctx.getFileshareEntityTS().startReadOnly() ) {
                    EntityManager em = tx.getEntityManager();

                    TokenGrant persistentGrant = em.find(TokenGrant.class, g.getId());

                    VFSEntity v = ServiceUtil.unwrapEntity(tx, this.vfs, g.getEntity());
                    audit.builder().entity(v);
                    this.accessControl.checkOwner(null, v);
                    PolicyConfiguration policy = getPolicy(v);
                    String token;
                    try {
                        token = recreateTokenForGrant(policy, persistentGrant);
                    }
                    catch ( TokenCreationException e ) {
                        throw new SecurityException("Failed to generate token", e); //$NON-NLS-1$
                    }

                    // tx.commit();

                    TokenShare share = new TokenShare();
                    share.setGrant(persistentGrant.cloneShallow());
                    setupShareLinks(persistentGrant, token, props.getOverrideBaseURI(), share, v);
                    return share;
                }
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to recreate token share", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param policy
     * @param persistentEntity
     * @param realExpires
     * @throws ShareLifetimeInvalidException
     */
    private void updateEntityExpiry ( PolicyConfiguration policy, VFSEntity persistentEntity, DateTime realExpires )
            throws ShareLifetimeInvalidException {
        // adjust
        if ( persistentEntity.getExpires() != null && persistentEntity.getExpires().isBefore(realExpires) ) {

            if ( !this.accessControl.hasPermission("entity:changeExpirationDate") ) { //$NON-NLS-1$
                throw new ShareLifetimeInvalidException(
                    "Share lifetime is after entity expiration, but you don't have the permission to extend it's lifetime"); //$NON-NLS-1$
            }

            DateTime newExpires = realExpires.plus(getAfterShareGracePeriod(policy)).withTime(0, 0, 0, 0);
            DateTime maxEntityExpiry = persistentEntity.getCreated().plus(policy.getMaximumExpirationDuration()).withTime(0, 0, 0, 0);

            if ( newExpires.isAfter(maxEntityExpiry) && realExpires.isBefore(maxEntityExpiry) ) {
                // if we cannot add the grace period, use the maximum usable
                newExpires = maxEntityExpiry;
            }

            if ( newExpires.isAfter(maxEntityExpiry) ) {
                throw new ShareLifetimeInvalidException("Share lifetime is after entity maximum expiration time"); //$NON-NLS-1$
            }

            persistentEntity.setExpires(newExpires);
        }
    }


    /**
     * @param persistentEntity
     * @param props
     * @param em
     * @param persistentEntity2
     * @param currentUser
     * @param realExpires
     * @param identifier
     * @throws FileshareException
     */
    private TokenShare setupTokenShare ( ContentEntity persistentEntity, PolicyConfiguration policy, ShareProperties props, EntityManager em,
            User currentUser, DateTime realExpires, String identifier ) throws FileshareException {
        TokenGrant g = new TokenGrant();
        g.setIdentifier(identifier);
        g.setComment(props.getMessage());
        g.setPassword(props.getPassword());
        setupShare(props, persistentEntity, realExpires, g, currentUser);
        String token = makeTokenGrant(policy, g);
        em.persist(g);
        em.flush();
        em.refresh(g);
        TokenShare s = new TokenShare();
        s.setGrant(g.cloneShallow());
        setupShareLinks(g, token, props.getOverrideBaseURI(), s, persistentEntity);
        return s;
    }


    /**
     * @param realExpires
     * @param g
     * @return
     * @throws SecurityException
     */
    private String makeTokenGrant ( PolicyConfiguration policy, TokenGrant g ) throws SecurityException {
        byte[] nonce = new byte[32];

        String token;
        try {
            SRAND.nextBytes(nonce);
            token = this.tokenGenerator.createToken(new AnonymousGrantToken(nonce, null), makeLinkExpiration(policy, g));
        }
        catch ( TokenCreationException e ) {
            throw new SecurityException("Failed to generate token", e); //$NON-NLS-1$
        }

        g.setToken(Base64.encodeBase64String(nonce));
        return token;
    }


    private String recreateTokenForGrant ( PolicyConfiguration policy, TokenGrant g ) throws TokenCreationException {
        return this.tokenGenerator.createToken(new AnonymousGrantToken(Base64.decodeBase64(g.getToken()), null), makeLinkExpiration(policy, g));
    }


    /**
     * @param policy
     * @param g
     * @return
     */
    private DateTime makeLinkExpiration ( PolicyConfiguration policy, TokenGrant g ) {
        if ( this.linksExpiresWithGrant ) {
            return g.getExpires();
        }
        else if ( policy.getMaximumShareLifetime() != null ) {
            return g.getCreated().plus(policy.getMaximumShareLifetime());
        }
        return null;
    }


    /**
     * @param persistentEntity
     * @param props
     * @param em
     * @param entity
     * @param currentUser
     * @param realExpires
     * @param recipient
     * @param sender
     * @param targetGrant
     * @return
     * @throws FileshareException
     * @throws TokenCreationException
     */
    private MailShareNotificationData setupMailShare ( EntityTransactionContext tx, ContentEntity e, PolicyConfiguration policy,
            ShareProperties props, User currentUser, DateTime realExpires, MailRecipient recipient, MailSender sender, boolean resend,
            LinkedList<UUID> targetGrant ) throws FileshareException {

        List<? extends Grant> existingGrants = getExistingMailGrant(tx, e, recipient);

        Grant usableExistingGrant = useExistingGrant(tx, props, realExpires, existingGrants, e);
        if ( StringUtils.isBlank(props.getPassword()) && usableExistingGrant instanceof MailGrant ) {
            targetGrant.add(usableExistingGrant.getId());
            if ( resend ) {
                try {
                    return makeMailShareNotification(
                        e,
                        recipient,
                        props,
                        sender,
                        (MailGrant) usableExistingGrant,
                        recreateTokenForGrant(policy, (MailGrant) usableExistingGrant));
                }
                catch ( TokenCreationException ex ) {
                    throw new SecurityException("Failed to recreate token", ex); //$NON-NLS-1$
                }
            }
            return null;
        }

        return addGrantForMail(tx, e, policy, props, realExpires, recipient, currentUser, sender, targetGrant);
    }


    /**
     * @param e
     * @param writeable
     * @param e2
     * @param realExpires
     * @param mailAddr
     * @param currentUser
     * @param targetGrant
     * @return
     * @throws FileshareException
     */
    private MailShareNotificationData addGrantForMail ( EntityTransactionContext tx, ContentEntity e, PolicyConfiguration policy,
            ShareProperties props, DateTime realExpires, MailRecipient recipient, User currentUser, MailSender sender, LinkedList<UUID> targetGrant )
            throws FileshareException {
        MailGrant g = new MailGrant();
        g.setMailAddress(recipient.getMailAddress());
        g.setPassword(props.getPassword());
        setupShare(props, e, realExpires, g, currentUser);
        String token = makeTokenGrant(policy, g);
        e.getGrants().add(g);

        tx.getEntityManager().persist(g);
        targetGrant.add(g.getId());

        return makeMailShareNotification(e, recipient, props, sender, g, token);
    }


    /**
     * @param persistentEntity
     * @param recipient
     * @param props
     * @param sender
     * @param g
     * @param token
     * @return
     * @throws FileshareException
     */
    private MailShareNotificationData makeMailShareNotification ( VFSEntity persistentEntity, MailRecipient recipient, ShareProperties props,
            MailSender sender, MailGrant g, String token ) throws FileshareException {
        MailShareNotificationData recpInfo = new MailShareNotificationData();
        recpInfo.setGrant(g.cloneShallow(true, true));
        recpInfo.setToken(token);
        recpInfo.setRecipients(new HashSet<>(Arrays.asList(recipient)));
        recpInfo.setSender(sender);
        recpInfo.setOverrideSubject(props.getNotificationSubject());
        recpInfo.setHideSensitive(shouldHideSensitive(recpInfo));
        recpInfo.setEntity(persistentEntity);
        setupShareLinks(g, token, props.getOverrideBaseURI(), recpInfo, persistentEntity);
        return recpInfo;
    }


    /**
     * @param data
     * @return
     */
    protected boolean shouldHideSensitive ( MailNotificationData data ) {
        return this.sctx.getConfigurationProvider().getNotificationConfiguration().isHideSensitiveInformation();
    }


    /**
     * @param g
     * @param token
     * @param data
     * @param entity
     * @throws FileshareException
     */
    private void setupShareLinks ( Grant g, String token, String overrideBase, LinkShareData data, VFSEntity entity ) throws FileshareException {
        String tokenQuery = token != null ? makeTokenQuery(token) : StringUtils.EMPTY;
        String tokenAndGrantQuery = g != null ? makeGrantAndTokenQuery(g, token) : StringUtils.EMPTY;
        if ( entity instanceof VFSFileEntity ) {
            VFSFileEntity fe = (VFSFileEntity) entity;
            ViewPolicyConfiguration viewPolicyConfig = this.sctx.getConfigurationProvider().getViewPolicyConfig();
            data.setViewable(viewPolicyConfig.isViewable(fe.getContentType()) || viewPolicyConfig.isSafe(fe.getContentType()));

            data.setViewURL(this.linkService.makeFrontendViewLink(fe, tokenAndGrantQuery, overrideBase));
            data.setDownloadURL(this.linkService.makeDownloadLink(fe, tokenQuery, overrideBase, data.getHideSensitive()));
        }
        else if ( entity instanceof VFSContainerEntity ) {
            VFSContainerEntity ce = (VFSContainerEntity) entity;
            String dirViewQuery = tokenAndGrantQuery.concat("&type=share-root&rootType=share-root"); //$NON-NLS-1$
            data.setViewURL(this.linkService.makeDirectoryViewLink(ce, dirViewQuery, overrideBase));
            data.setDownloadURL(this.linkService.makeDirectoryArchiveLink(ce, ArchiveType.ZIP, tokenQuery, overrideBase, data.getHideSensitive())); // $NON-NLS-1$
        }
    }


    /**
     * @param token
     * @return
     */
    private static String makeTokenQuery ( String token ) {
        return String.format("?token=%s", token); //$NON-NLS-1$
    }


    /**
     * @param token
     * @param grant
     * @return
     */
    private static String makeGrantAndTokenQuery ( Grant grant, String token ) {
        return String
                .format("&grant=%s&token=%s", grant.getId() != null ? grant.getId() : StringUtils.EMPTY, token != null ? token : StringUtils.EMPTY); //$NON-NLS-1$
    }


    @Override
    public Set<Grant> getEffectiveGrants ( EntityKey entityId, GrantType type ) throws FileshareException {
        try ( EntityTransactionContext tx = this.sctx.getFileshareEntityTS().start();
              VFSContext v = this.vfs.getVFS(entityId).begin(tx) ) {

            VFSEntity entity = v.load(entityId);
            this.accessControl.checkOwner(null, entity);

            if ( entity == null ) {
                throw new EntityNotFoundException("Failed to find entity by id " + entityId); //$NON-NLS-1$
            }

            Set<Grant> grants = getEffectiveGrantsInternal(v, entity, type);
            tx.commit();
            return grants;
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get effective grants", e); //$NON-NLS-1$
        }
    }


    @Override
    public Set<Grant> getEffectiveGrantsInternal ( VFSContext v, VFSEntity entity, GrantType type ) throws FileshareException {
        Set<Grant> grants = new HashSet<>();

        MultiValuedMap<Subject, SubjectGrant> subjectGrants = new HashSetValuedHashMap<>();
        MultiValuedMap<String, MailGrant> mailGrants = new HashSetValuedHashMap<>();
        Set<TokenGrant> tokenGrants = new HashSet<>();

        VFSEntity cur = entity;
        while ( cur != null ) {
            ContentEntity e = v.findMappedEntity(cur);
            if ( e != null ) {
                addGrantsTypeMatch(subjectGrants, mailGrants, tokenGrants, e.getGrants(), type);
            }
            cur = v.getParent(cur);
        }

        for ( Subject s : subjectGrants.keySet() ) {
            deduplicateGrants(grants, v, entity, subjectGrants, s, false);
        }

        for ( String mail : mailGrants.keySet() ) {
            deduplicateGrants(grants, v, entity, mailGrants, mail, false);
        }

        grants.addAll(tokenGrants);
        return grants;
    }


    /**
     * @param grants
     * @param entity
     * @param subjectGrants
     * @param s
     * @throws FileshareException
     */
    private static <T extends Object, G extends Grant> void deduplicateGrants ( Set<Grant> grants, VFSContext v, VFSEntity entity,
            MultiValuedMap<T, G> subjectGrants, T s, boolean firstOnly ) throws FileshareException {
        int effectivePerms = 0;
        int inheritedPerms = 0;
        UUID inheritedId = null;
        List<Grant> localGrants = new LinkedList<>();
        for ( Grant g : subjectGrants.get(s) ) {
            int intPerms = GrantPermission.toInt(g.getPermissions());
            effectivePerms |= intPerms;

            VFSEntity e = g.getEntity();

            if ( e instanceof MappedVFSEntity ) {
                e = v.getVfsEntity((MappedVFSEntity) e);
            }

            if ( e.equals(entity) ) {
                localGrants.add(g);
            }
            else {
                inheritedPerms |= intPerms;
                inheritedId = g.getId();
            }
        }

        int groupPerms = 0;
        if ( s instanceof Subject ) {
            Subject subj = (Subject) s;
            groupPerms = getGroupEffectivePerms(subjectGrants, subj, new HashSet<>());
        }

        effectivePerms |= groupPerms;

        for ( Grant localGrant : localGrants ) {
            localGrant.setEffectivePerms(effectivePerms);
            localGrant.setInheritedPerms(inheritedPerms);
            localGrant.setInheritedFrom(inheritedId);
            localGrant.setGroupPerms(groupPerms);
        }

        if ( firstOnly && !localGrants.isEmpty() ) {
            grants.add(localGrants.get(0));
        }
        else if ( !firstOnly ) {
            grants.addAll(localGrants);
        }

        if ( localGrants.isEmpty() && entity.hasParent() ) {
            deduplicateGrants(grants, v, v.getParent(entity), subjectGrants, s, firstOnly);
        }
    }


    /**
     * @param subjectGrants
     * @param subj
     * @return
     */
    private static <T, G extends Grant> int getGroupEffectivePerms ( MultiValuedMap<T, G> subjectGrants, Subject subj, Set<Group> handled ) {
        int groupEffective = 0;
        for ( Group memberOf : subj.getMemberships() ) {

            if ( handled.contains(memberOf) ) {
                continue;
            }
            handled.add(memberOf);

            @SuppressWarnings ( "unchecked" )
            Collection<Grant> memberOfGrants = (Collection<Grant>) subjectGrants.get((T) memberOf);
            if ( memberOfGrants != null ) {
                for ( Grant g : memberOfGrants ) {
                    int intPerms = GrantPermission.toInt(g.getPermissions());
                    groupEffective |= intPerms;
                }
            }

            groupEffective |= getGroupEffectivePerms(subjectGrants, memberOf, handled);
        }
        return groupEffective;
    }


    @Override
    public Set<Grant> getEffectiveGrants ( EntityKey entityID ) throws FileshareException {
        return getEffectiveGrants(entityID, null);
    }


    @Override
    public List<Grant> getFirstGrants ( EntityKey id, int limit ) throws FileshareException {
        try ( VFSContext v = this.vfs.getVFS(id).begin(true) ) {
            VFSEntity e = v.load(id);
            this.accessControl.checkOwner(v, e);

            if ( e == null ) {
                throw new EntityNotFoundException();
            }

            return getFirstGrantsInternal(v, e, limit);
        }
    }


    /**
     * @param limit
     * @param res
     * @param entity
     * @return
     * @throws FileshareException
     */
    private static List<Grant> getFirstGrantsInternal ( VFSContext v, VFSEntity entity, int limit ) throws FileshareException {
        Set<Grant> res = new HashSet<>();
        List<Grant> sorted = new ArrayList<>();
        MultiValuedMap<Subject, SubjectGrant> subjectGrants = new HashSetValuedHashMap<>();
        MultiValuedMap<String, MailGrant> mailGrants = new HashSetValuedHashMap<>();
        Set<TokenGrant> tokenGrants = new HashSet<>();

        VFSEntity cur = entity;
        while ( cur != null ) {
            ContentEntity e = v.findMappedEntity(cur);
            if ( e != null ) {
                addGrantsTypeMatch(subjectGrants, mailGrants, tokenGrants, e.getGrants(), null);
            }
            cur = v.getParent(cur);
        }

        for ( Subject s : subjectGrants.keySet() ) {
            deduplicateGrants(res, v, entity, subjectGrants, s, true);
            if ( res.size() >= limit ) {
                break;
            }
        }

        if ( res.size() < limit ) {
            for ( String mail : mailGrants.keySet() ) {
                deduplicateGrants(res, v, entity, mailGrants, mail, true);
                if ( res.size() >= limit ) {
                    break;
                }
            }
        }

        if ( res.size() < limit ) {
            res.addAll(tokenGrants);
        }

        int cnt = 0;
        Iterator<Grant> it = res.iterator();

        while ( cnt < limit && it.hasNext() ) {
            sorted.add(it.next());
            cnt++;
        }

        sorted.sort(new GrantComparator());
        return sorted;
    }


    @Override
    public int getGrantCount ( EntityKey id ) throws FileshareException {
        try ( VFSContext v = this.vfs.getVFS(id).begin(true) ) {
            VFSEntity e = v.load(id);
            this.accessControl.checkOwner(v, e);
            Set<Subject> foundSubjects = new HashSet<>();
            Set<String> foundMails = new HashSet<>();
            return getGrantCountInternal(v, e, foundSubjects, foundMails);
        }
    }


    /**
     * @param e
     * @param foundMails
     * @param foundSubjects
     * @return
     * @throws FileshareException
     */
    private int getGrantCountInternal ( VFSContext v, VFSEntity e, Set<Subject> foundSubjects, Set<String> foundMails ) throws FileshareException {
        int cnt = 0;

        ContentEntity mapped = v.findMappedEntity(e);

        if ( mapped != null ) {
            for ( Grant g : mapped.getGrants() ) {
                if ( g.getExpires() != null && g.getExpires().isBeforeNow() ) {
                    continue;
                }

                if ( g instanceof SubjectGrant && foundSubjects.contains( ( (SubjectGrant) g ).getTarget()) ) {
                    continue;
                }
                else if ( g instanceof SubjectGrant ) {
                    foundSubjects.add( ( (SubjectGrant) g ).getTarget());
                }
                else if ( g instanceof MailGrant && foundMails.contains( ( (MailGrant) g ).getMailAddress()) ) {
                    continue;
                }
                else if ( g instanceof MailGrant ) {
                    foundMails.add( ( (MailGrant) g ).getMailAddress());
                }
                cnt++;
            }
        }

        if ( e.hasParent() ) {
            VFSContainerEntity parent = v.getParent(e);
            if ( parent != null ) {
                cnt += getGrantCountInternal(v, parent, foundSubjects, foundMails);
            }
            else {
                log.warn("Entity indicated has parent, but no parent could be found " + e); //$NON-NLS-1$
            }
        }

        return cnt;
    }


    /**
     * @param grants
     * @param grants2
     * @param type
     */
    private static void addGrantsTypeMatch ( MultiValuedMap<Subject, SubjectGrant> subjectGrants, MultiValuedMap<String, MailGrant> mailGrants,
            Set<TokenGrant> tokenGrants, Set<Grant> grants, GrantType type ) {
        for ( Grant g : grants ) {

            if ( g.getExpires() != null && g.getExpires().isBeforeNow() ) {
                continue;
            }

            if ( ( type == null || type == GrantType.SUBJECT ) && g instanceof SubjectGrant ) {
                SubjectGrant sg = (SubjectGrant) g;
                subjectGrants.put(sg.getTarget(), sg.cloneShallow(true, false));
            }
            else if ( ( type == null || type == GrantType.MAIL ) && g instanceof MailGrant ) {
                MailGrant mg = (MailGrant) g;
                mailGrants.put(mg.getMailAddress(), mg.cloneShallow(true, false));
            }
            else if ( ( type == null || type == GrantType.LINK ) && g instanceof TokenGrant && ! ( g instanceof MailGrant ) ) {
                TokenGrant cloned = ( (TokenGrant) g ).cloneShallow(true, false);
                cloned.setEffectivePerms(g.getPerms());
                tokenGrants.add(cloned);
            }
        }
    }


    /**
     * @param e
     * @param writeable
     * @param em
     * @param persistentEntity
     * @param currentUser
     * @param realExpires
     * @param targetNames
     * @param targetTypes
     * @param targetGrant
     * @param props2
     * @param subj
     * @return
     * @throws FileshareException
     */
    private ShareNotificationData setupSubjectShare ( EntityTransactionContext tx, ContentEntity e, ShareProperties props, User currentUser,
            DateTime realExpires, UUID subjId, LinkedList<String> targetTypes, LinkedList<String> targetNames, LinkedList<UUID> targetGrant )
            throws FileshareException {
        EntityManager em = tx.getEntityManager();
        Subject persistentSubj = em.find(Subject.class, subjId);

        if ( persistentSubj == null ) {
            throw new SubjectNotFoundException("Failed to find subject by id " + subjId); //$NON-NLS-1$
        }

        if ( persistentSubj.equals(currentUser) ) {
            throw new CannotShareToSelfException();
        }

        if ( persistentSubj instanceof User ) {
            targetTypes.add("user"); //$NON-NLS-1$
            targetNames.add( ( (User) persistentSubj ).getPrincipal().toString());
        }
        else if ( persistentSubj instanceof Group ) {
            targetTypes.add("group"); //$NON-NLS-1$
            targetNames.add( ( (Group) persistentSubj ).getName());
        }
        else {
            throw new IllegalArgumentException();
        }

        List<? extends Grant> existingGrants = getExistingSubjectGrant(tx, e, persistentSubj);

        Grant existing = useExistingGrant(tx, props, realExpires, existingGrants, e);
        if ( existing != null ) {
            targetGrant.add(existing.getId());
            return null;
        }

        SubjectGrant newGrant = addGrantForSubject(e, props, realExpires, persistentSubj, currentUser);
        em.persist(newGrant);
        targetGrant.add(newGrant.getId());
        return makeSubjectShareNotificationData(e, currentUser, props, persistentSubj, newGrant);
    }


    /**
     * @param e
     * @param currentUser
     * @param msg
     * @param persistentSubj
     * @param newGrant
     * @return
     * @throws FileshareException
     */
    private ShareNotificationData makeSubjectShareNotificationData ( VFSEntity e, User currentUser, ShareProperties props, Subject persistentSubj,
            SubjectGrant newGrant ) throws FileshareException {
        ShareNotificationData data = new ShareNotificationData();
        data.setSender(this.notificationService.getSenderForUser(currentUser));
        data.setRecipients(this.notificationService.getRecipientsForSubject(persistentSubj, null));
        data.setHideSensitive(shouldHideSensitive(data));
        data.setMessage(props.getMessage());
        data.setOverrideSubject(props.getNotificationSubject());
        data.setGrant(newGrant);
        data.setEntity(e);
        setupShareLinks(newGrant, null, props.getOverrideBaseURI(), data, e);
        return data;
    }


    /**
     * @param props
     * @param em
     * @param newExpires
     * @param existingGrants
     * @param targetEntity
     * @return
     * @throws GrantExistsException
     */
    private static Grant useExistingGrant ( EntityTransactionContext tx, ShareProperties props, DateTime newExpires,
            List<? extends Grant> existingGrants, ContentEntity targetEntity ) throws GrantExistsException {
        // Set<GrantPermission> newPerms = props.getPermissions();
        for ( Grant g : existingGrants ) {
            // if the new grant implies the existing grant and the existing grant has a shorter lifetime
            // it can be reused.

            if ( !g.getEntity().equals(targetEntity) || ( g.getExpires() != null && g.getExpires().isBeforeNow() ) ) {
                log.debug("Grant is not for the same entity or already expired"); //$NON-NLS-1$
                continue;
            }

            throw new GrantExistsException();
        }
        return null;
    }


    /**
     * @param e
     * @param em
     * @param subj
     * @return
     */
    private static List<SubjectGrant> getExistingSubjectGrant ( EntityTransactionContext tx, ContentEntity e, Subject subj ) {
        EntityManager em = tx.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<SubjectGrant> q = cb.createQuery(SubjectGrant.class);
        Root<SubjectGrant> from = q.from(SubjectGrant.class);
        EntityType<SubjectGrant> model = em.getMetamodel().entity(SubjectGrant.class);

        q.where(cb.and(
            cb.equal(from.get(model.getSingularAttribute("entity", ContentEntity.class)), e), //$NON-NLS-1$
            cb.equal(from.get(model.getSingularAttribute("target", Subject.class)), subj))); //$NON-NLS-1$

        return em.createQuery(q).getResultList();
    }


    /**
     * @param e
     * @param em
     * @param mailAddr
     * @return
     */
    private static List<? extends Grant> getExistingMailGrant ( EntityTransactionContext tx, ContentEntity e, MailRecipient recpt ) {
        EntityManager em = tx.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MailGrant> q = cb.createQuery(MailGrant.class);
        Root<MailGrant> from = q.from(MailGrant.class);
        EntityType<MailGrant> model = em.getMetamodel().entity(MailGrant.class);

        q.where(cb.and(
            cb.equal(from.get(model.getSingularAttribute("entity", ContentEntity.class)), e), //$NON-NLS-1$
            cb.equal(from.get(model.getSingularAttribute("mailAddress", String.class)), recpt.getMailAddress()))); //$NON-NLS-1$

        return em.createQuery(q).getResultList();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ShareService#revokeShare(java.util.UUID)
     */
    @Override
    public void revokeShare ( UUID grantId ) throws FileshareException {
        try ( AuditContext<SingleEntityFileshareAuditBuilder> audit = this.sctx.getEventLogger().audit(SingleEntityFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action(EntityFileshareEvent.REVOKE_GRANT_ACTION);
            audit.builder().property("grantId", grantId); //$NON-NLS-1$

            try {
                this.accessControl.checkPermission("share:revoke"); //$NON-NLS-1$
                try ( EntityTransactionContext tx = this.sctx.getFileshareEntityTS().start() ) {
                    EntityManager em = tx.getEntityManager();
                    Grant persistent = em.find(Grant.class, grantId);

                    if ( persistent == null ) {
                        throw new ShareNotFoundException("Cannot find share with id " + grantId); //$NON-NLS-1$
                    }

                    auditGrant(audit, persistent, ServiceUtil.unwrapEntity(tx, this.vfs, persistent.getEntity()));

                    this.accessControl.checkOwner(null, persistent.getEntity());

                    cleanupGrant(tx, persistent, true);

                    em.flush();
                    tx.commit();
                }
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to revoke share", e); //$NON-NLS-1$
            }
        }

    }


    /**
     * @param audit
     * @param persistent
     */
    private static void auditGrant ( AuditContext<SingleEntityFileshareAuditBuilder> audit, Grant persistent, VFSEntity entity ) {
        audit.builder().entity(entity);
        audit.builder().property("permissions", GrantPermission.toInt(persistent.getPermissions())); //$NON-NLS-1$
        audit.builder().property("expires", persistent.getExpires() != null ? persistent.getExpires().getMillis() : null); //$NON-NLS-1$
        audit.builder().property("created", persistent.getCreated() != null ? persistent.getCreated().getMillis() : null); //$NON-NLS-1$

        if ( persistent instanceof SubjectGrant ) {
            SubjectGrant sg = (SubjectGrant) persistent;
            audit.builder().property("type", "subject"); //$NON-NLS-1$ //$NON-NLS-2$
            audit.builder().property("subjectId", sg.getTarget().getId()); //$NON-NLS-1$
            if ( sg.getTarget() instanceof User ) {
                audit.builder().property(
                    "subjectType", //$NON-NLS-1$
                    "user"); //$NON-NLS-1$
                audit.builder().property("subjectName", ( (User) sg.getTarget() ).getPrincipal().toString()); //$NON-NLS-1$
            }
            else if ( sg.getTarget() instanceof Group ) {
                audit.builder().property(
                    "subjectType", //$NON-NLS-1$
                    "group"); //$NON-NLS-1$
                audit.builder().property("subjectName", ( (Group) sg.getTarget() ).getName()); //$NON-NLS-1$
            }
            else {
                throw new IllegalArgumentException();
            }

        }
        else if ( persistent instanceof MailGrant ) {
            MailGrant mg = (MailGrant) persistent;
            audit.builder().property("type", "mail"); //$NON-NLS-1$ //$NON-NLS-2$
            audit.builder().property("mailAddress", mg.getMailAddress()); //$NON-NLS-1$
            audit.builder().property("passwordProtected", !StringUtils.isBlank(mg.getPassword())); //$NON-NLS-1$
        }
        else if ( persistent instanceof TokenGrant ) {
            TokenGrant tg = (TokenGrant) persistent;
            audit.builder().property("type", "link"); //$NON-NLS-1$ //$NON-NLS-2$
            audit.builder().property("identifier", tg.getIdentifier()); //$NON-NLS-1$
            audit.builder().property("passwordProtected", !StringUtils.isBlank(tg.getPassword())); //$NON-NLS-1$
        }
        else {
            throw new IllegalArgumentException();
        }
    }


    @Override
    public void doRevokeGrants ( EntityTransactionContext tx, List<Grant> grants, boolean expire ) {
        for ( Grant g : grants ) {
            try ( AuditContext<SingleEntityFileshareAuditBuilder> audit = this.sctx.getEventLogger()
                    .audit(SingleEntityFileshareAuditBuilder.class) ) {
                audit.builder().action(EntityFileshareEvent.GRANT_EXPIRE_ACTION);

                try {
                    VFSEntity e = ServiceUtil.unwrapEntity(tx, this.vfs, g.getEntity());
                    audit.builder().entity(e);
                    audit.builder().grant(g);
                    auditGrant(audit, g, e);

                    cleanupGrant(tx, g, true);
                }
                catch ( FileshareException e ) {
                    log.error("Failed to get VFS entity", e); //$NON-NLS-1$
                }
            }
        }

        tx.getEntityManager().flush();
    }


    /**
     * @param tx
     * @param g
     * @param delete
     */
    public static void cleanupGrant ( EntityTransactionContext tx, Grant g, boolean delete ) {

        if ( log.isTraceEnabled() ) {
            log.trace("Cleanup grant " + g); //$NON-NLS-1$
        }

        g.getEntity().getGrants().remove(g);

        for ( ContentEntity e : g.getLastModifierOf() ) {
            e.setLastModifiedGrant(null);
            tx.getEntityManager().persist(e);
        }

        g.getLastModifierOf().clear();

        for ( ContentEntity e : g.getCreatorOf() ) {
            e.setCreatorGrant(null);
            tx.getEntityManager().persist(e);
        }

        g.getCreatorOf().clear();

        if ( g instanceof SubjectGrant ) {
            ( (SubjectGrant) g ).getTarget().getGrants().remove(g);
            tx.getEntityManager().persist( ( (SubjectGrant) g ).getTarget());
        }

        if ( delete ) {
            tx.getEntityManager().remove(g);
        }
        else {
            tx.getEntityManager().persist(g);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.ShareService#setExpiry(java.util.UUID, org.joda.time.DateTime)
     */
    @Override
    public void setExpiry ( UUID grantId, DateTime expiry ) throws FileshareException {
        try ( AuditContext<SingleEntityFileshareAuditBuilder> audit = this.sctx.getEventLogger().audit(SingleEntityFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action(EntityFileshareEvent.GRANT_SET_EXPIRY_ACTION);
            audit.builder().property("grantId", grantId); //$NON-NLS-1$
            audit.builder().property("newExpiry", expiry != null ? expiry.getMillis() : null); //$NON-NLS-1$

            try ( EntityTransactionContext tx = this.sctx.getFileshareEntityTS().start() ) {
                EntityManager em = tx.getEntityManager();
                Grant persistent = em.find(Grant.class, grantId);

                if ( persistent == null ) {
                    throw new ShareNotFoundException("Cannot find share with id " + grantId); //$NON-NLS-1$
                }

                VFSEntity entity = ServiceUtil.unwrapEntity(tx, this.vfs, persistent.getEntity());

                auditGrant(audit, persistent, entity);

                this.accessControl.checkOwner(null, entity);
                PolicyConfiguration policy = getPolicy(entity);

                checkShareLifetime(policy, expiry);

                persistent.setExpires(expiry);
                persistent.setLastModified(DateTime.now());
                updateEntityExpiry(policy, entity, expiry);

                em.persist(persistent);

                em.flush();
                tx.commit();
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to set grant expiration", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ShareService#setPermissions(java.util.UUID, java.util.Set)
     */
    @Override
    public void setPermissions ( UUID grantId, Set<GrantPermission> permissions ) throws FileshareException {
        try ( AuditContext<SingleEntityFileshareAuditBuilder> audit = this.sctx.getEventLogger().audit(SingleEntityFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action(EntityFileshareEvent.GRANT_SET_PERMISSIONS_ACTION);
            audit.builder().property("grantId", grantId); //$NON-NLS-1$
            audit.builder().property("newPermissions", GrantPermission.toInt(permissions)); //$NON-NLS-1$

            try ( EntityTransactionContext tx = this.sctx.getFileshareEntityTS().start() ) {
                EntityManager em = tx.getEntityManager();
                Grant persistent = em.find(Grant.class, grantId);

                if ( persistent == null ) {
                    throw new ShareNotFoundException("Cannot find share with id " + grantId); //$NON-NLS-1$
                }

                VFSEntity entity = ServiceUtil.unwrapEntity(tx, this.vfs, persistent.getEntity());
                auditGrant(audit, persistent, entity);

                this.accessControl.checkOwner(null, entity);

                if ( !permissions.equals(persistent.getPermissions()) ) {
                    persistent.setPermissions(permissions);
                    persistent.setLastModified(DateTime.now());
                    em.persist(persistent);
                }
                else {
                    audit.builder().ignore();
                }

                em.flush();
                tx.commit();
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to set grant permissions", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ShareService#updateComment(java.util.UUID, java.lang.String)
     */
    @Override
    public void updateComment ( UUID grantId, String comment ) throws FileshareException {
        try ( AuditContext<SingleEntityFileshareAuditBuilder> audit = this.sctx.getEventLogger().audit(SingleEntityFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action(EntityFileshareEvent.GRANT_SET_COMMENT_ACTION);
            audit.builder().property("grantId", grantId); //$NON-NLS-1$

            try ( EntityTransactionContext tx = this.sctx.getFileshareEntityTS().start() ) {
                EntityManager em = tx.getEntityManager();
                TokenGrant persistent = em.find(TokenGrant.class, grantId);

                if ( persistent == null ) {
                    throw new ShareNotFoundException("Cannot find share with id " + grantId); //$NON-NLS-1$
                }

                VFSEntity entity = ServiceUtil.unwrapEntity(tx, this.vfs, persistent.getEntity());
                auditGrant(audit, persistent, entity);

                this.accessControl.checkOwner(null, entity);
                persistent.setComment(comment);
                persistent.setLastModified(DateTime.now());
                em.persist(persistent);
                em.flush();
                tx.commit();
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to update grant comment", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ShareService#updateIdentifier(java.util.UUID, java.lang.String)
     */
    @Override
    public void updateIdentifier ( UUID grantId, String identifier ) throws FileshareException {
        try ( AuditContext<SingleEntityFileshareAuditBuilder> audit = this.sctx.getEventLogger().audit(SingleEntityFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action(EntityFileshareEvent.GRANT_SET_IDENTIFIER_ACTION);
            audit.builder().property("grantId", grantId); //$NON-NLS-1$
            audit.builder().property("newIdentifier", identifier); //$NON-NLS-1$

            try ( EntityTransactionContext tx = this.sctx.getFileshareEntityTS().start() ) {
                EntityManager em = tx.getEntityManager();
                TokenGrant persistent = em.find(TokenGrant.class, grantId);

                if ( persistent == null ) {
                    throw new ShareNotFoundException("Cannot find share with id " + grantId); //$NON-NLS-1$
                }

                VFSEntity entity = ServiceUtil.unwrapEntity(tx, this.vfs, persistent.getEntity());
                auditGrant(audit, persistent, entity);

                this.accessControl.checkOwner(null, entity);
                persistent.setIdentifier(identifier);
                persistent.setLastModified(DateTime.now());
                em.persist(persistent);
                em.flush();
                tx.commit();
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to update grant identifier", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param e
     * @param props
     * @param persistentEntity
     * @param realExpires
     * @param persistentSubj
     * @param currentUser
     * @return
     * @throws AuthenticationException
     */
    private SubjectGrant addGrantForSubject ( ContentEntity e, ShareProperties props, DateTime realExpires, Subject persistentSubj, User currentUser )
            throws AuthenticationException {
        this.validateShareTarget(e, persistentSubj);
        SubjectGrant g = new SubjectGrant();
        g.setTarget(persistentSubj);
        setupShare(props, e, realExpires, g, currentUser);
        e.getGrants().add(g);
        persistentSubj.getGrants().add(g);
        return g;
    }


    /**
     * @param writeable
     * @param persistentEntity
     * @param realExpires
     * @param g
     * @param currentUser
     * @throws AuthenticationException
     */
    private static void setupShare ( ShareProperties props, ContentEntity persistentEntity, DateTime realExpires, Grant g, User currentUser )
            throws AuthenticationException {
        g.setPermissions(props.getPermissions());
        g.setEntity(persistentEntity);
        DateTime now = DateTime.now();
        g.setCreated(now);
        g.setLastModified(now);
        g.setExpires(realExpires);
        g.setCreator(currentUser);
    }


    /**
     * @return
     */
    private static ReadableDuration getAfterShareGracePeriod ( PolicyConfiguration policy ) {
        return policy.getAfterShareGracePeriod();
    }


    /**
     * @param e
     * @param persistentSubj
     */
    private void validateShareTarget ( ContentEntity e, Subject persistentSubj ) {

    }
}
