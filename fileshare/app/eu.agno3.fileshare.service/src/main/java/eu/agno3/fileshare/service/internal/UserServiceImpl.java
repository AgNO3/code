/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.exceptions.AccessDeniedException;
import eu.agno3.fileshare.exceptions.AuthenticationException;
import eu.agno3.fileshare.exceptions.CannotDeleteCurrentUserException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.InvalidPasswordException;
import eu.agno3.fileshare.exceptions.PasswordChangeException;
import eu.agno3.fileshare.exceptions.SecurityException;
import eu.agno3.fileshare.exceptions.SubjectNotFoundException;
import eu.agno3.fileshare.exceptions.UserLimitExceededException;
import eu.agno3.fileshare.exceptions.UserNotFoundException;
import eu.agno3.fileshare.model.ContainerEntity;
import eu.agno3.fileshare.model.ContentEntity;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.SecurityLabel;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.SubjectGrant;
import eu.agno3.fileshare.model.SubjectType;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.UserCreateData;
import eu.agno3.fileshare.model.UserDetails;
import eu.agno3.fileshare.security.AccessControlService;
import eu.agno3.fileshare.security.LocalUserService;
import eu.agno3.fileshare.service.LinkService;
import eu.agno3.fileshare.service.admin.UserService;
import eu.agno3.fileshare.service.admin.UserServiceMBean;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.NotificationService;
import eu.agno3.fileshare.service.api.internal.QuotaServiceInternal;
import eu.agno3.fileshare.service.api.internal.RecursiveModificationTimeTracker;
import eu.agno3.fileshare.service.api.internal.SingleUseTokenService;
import eu.agno3.fileshare.service.api.internal.UserServiceInternal;
import eu.agno3.fileshare.service.audit.MultiSubjectFileshareAuditBuilder;
import eu.agno3.fileshare.service.audit.SubjectFileshareAuditBuilder;
import eu.agno3.fileshare.service.util.ServiceUtil;
import eu.agno3.runtime.crypto.scrypt.SCryptResult;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.eventlog.AuditContext;
import eu.agno3.runtime.eventlog.AuditStatus;
import eu.agno3.runtime.jmx.JMXSecurityUtil;
import eu.agno3.runtime.jmx.MBean;
import eu.agno3.runtime.security.SecurityManagementException;
import eu.agno3.runtime.security.UserLicenseLimitExceededException;
import eu.agno3.runtime.security.password.PasswordPolicyException;
import eu.agno3.runtime.security.principal.UserInfo;
import eu.agno3.runtime.security.principal.UserInfoImpl;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.security.web.login.token.TokenGenerator;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    UserServiceMBean.class, UserServiceInternal.class, MBean.class
}, property = {
    "objectName=eu.agno3.fileshare:type=UserService"
} )
public class UserServiceImpl extends UserService implements UserServiceMBean, UserServiceInternal {

    private static final Logger log = Logger.getLogger(UserServiceImpl.class);

    /**
     * 
     */
    private static final String LIST_PERM = "manage:subjects:list"; //$NON-NLS-1$

    private static final String USER_UPDATED = "user.updated"; //$NON-NLS-1$

    private DefaultServiceContext ctx;

    private LocalUserService localUserService;

    private AccessControlService accessControl;

    private NotificationService notificationService;

    private TokenGenerator tokenGen;

    private LinkService linkService;

    private SingleUseTokenService tokenTracker;

    private QuotaServiceInternal quotaService;

    private RecursiveModificationTimeTracker modTracker;


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
    protected synchronized void setLocalUserService ( LocalUserService lus ) {
        this.localUserService = lus;
    }


    protected synchronized void unsetLocalUserService ( LocalUserService lus ) {
        if ( this.localUserService == lus ) {
            this.localUserService = null;
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
    protected synchronized void setQuotaService ( QuotaServiceInternal qs ) {
        this.quotaService = qs;
    }


    protected synchronized void unsetQuotaService ( QuotaServiceInternal qs ) {
        if ( this.quotaService == qs ) {
            this.quotaService = null;
        }
    }


    @Reference
    protected synchronized void setRecursiveModTracker ( RecursiveModificationTimeTracker rmt ) {
        this.modTracker = rmt;
    }


    protected synchronized void unsetRecursiveModTracker ( RecursiveModificationTimeTracker rmt ) {
        if ( this.modTracker == rmt ) {
            this.modTracker = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#getUser(java.util.UUID)
     */
    @Override
    public User getUser ( UUID userId ) throws FileshareException {
        if ( !JMXSecurityUtil.isManagementCall() ) {
            this.accessControl.checkPermission(LIST_PERM);
        }

        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();
            User u = em.find(User.class, userId);

            if ( u == null ) {
                throw new UserNotFoundException("Cannot find user by id " + userId); //$NON-NLS-1$
            }

            return u.cloneShallow(true);
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to fetch user", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#getLocalUserInfo(java.util.UUID)
     */
    @Override
    public UserInfo getLocalUserInfo ( UUID userId ) throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();

            User u = em.find(User.class, userId);

            if ( u == null ) {
                throw new UserNotFoundException("Cannot find user by id " + userId); //$NON-NLS-1$
            }

            return new UserInfoImpl(this.localUserService.getUser(u.getPrincipal()));
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to fetch user info", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#getCurrentUserGroupClosure()
     */
    @Override
    public Set<Group> getCurrentUserGroupClosure () throws AuthenticationException {
        return this.accessControl.getCurrentUserGroupClosure();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.UserServiceInternal#getCurrentUserGroupClosure(eu.agno3.runtime.db.orm.EntityTransactionContext)
     */
    @Override
    public Set<Group> getCurrentUserGroupClosure ( EntityTransactionContext tx ) throws AuthenticationException, UserNotFoundException {
        return this.accessControl.getCurrentUserGroupClosure(tx);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws UserNotFoundException
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#getCurrentUser()
     */
    @Override
    public User getCurrentUser () throws AuthenticationException, UserNotFoundException {
        if ( !this.accessControl.isUserAuthenticated() ) {
            return null;
        }

        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            User u = getCurrentUser(tx);
            User cloned = new User(u, true);
            tx.commit();
            if ( u.getSubjectRoot() != null ) {
                ContainerEntity subjectRoot = new ContainerEntity(u.getSubjectRoot(), true);
                cloned.setSubjectRoot(subjectRoot);
            }
            else {
                cloned.setSubjectRoot(null);
            }
            return u;
        }
        catch ( EntityTransactionException e ) {
            throw new AuthenticationException("Internal error", e); //$NON-NLS-1$
        }
    }


    @Override
    public User getCurrentUser ( EntityTransactionContext tx ) throws AuthenticationException, UserNotFoundException {
        if ( !this.accessControl.isUserAuthenticated() ) {
            return null;
        }

        UserPrincipal princ = this.accessControl.getCurrentUserPrincipal();
        if ( princ == null ) {
            return null;
        }

        User u = this.accessControl.findByPrincipal(tx, princ);
        if ( u != null ) {
            updateUser(tx, u);
            return u;
        }

        if ( !verifyValidUser(princ) ) {
            throw new UserNotFoundException("The user is no longer valid"); //$NON-NLS-1$
        }
        return createUserInternal(tx, princ);
    }


    /**
     * @param u
     */
    private void updateUser ( EntityTransactionContext tx, User u ) {
        EntityManager em = tx.getEntityManager();
        Session session = SecurityUtils.getSubject().getSession(false);
        if ( session != null && session.getAttribute(USER_UPDATED) == null ) {
            session.setAttribute(USER_UPDATED, true);
            boolean modified = false;
            modified |= setupStaticRoles(u);
            eu.agno3.runtime.security.principal.UserDetails authDetails = SecurityUtils.getSubject().getPrincipals()
                    .oneByType(eu.agno3.runtime.security.principal.UserDetails.class);
            if ( authDetails != null ) {
                modified |= setupUserDetails(u, authDetails);
            }

            if ( modified ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Updating user " + u.getPrincipal()); //$NON-NLS-1$
                }
                em.persist(u);
                em.flush();
                em.refresh(u);
            }
        }
    }


    private User createUserInternal ( EntityTransactionContext tx, UserPrincipal princ ) {
        EntityManager em = tx.getEntityManager();
        User u = makeUserInternal(tx, princ);
        u.setNoSubjectRoot(this.ctx.getConfigurationProvider().getUserConfig().hasNoSubjectRoot(u.getRoles()));
        em.persist(u);
        em.flush();
        em.refresh(u);
        return u;
    }


    @Override
    public User makeUserInternal ( EntityTransactionContext tx, UserPrincipal princ ) {
        EntityManager em = tx.getEntityManager();
        User u = new User();
        u.setId(princ.getUserId());
        u.setPrincipal(princ);
        u.setType(SubjectType.LOCAL);
        UserDetails userDetails = new UserDetails();
        u.setUserDetails(userDetails);
        userDetails.setUser(u);
        u.setSecurityLabel(
            ServiceUtil.getOrCreateSecurityLabel(
                tx,
                this.ctx.getConfigurationProvider().getSecurityPolicyConfiguration().getDefaultUserLabelForSubject(SecurityUtils.getSubject())));
        u.setQuota(this.ctx.getConfigurationProvider().getQuotaConfiguration().getDefaultQuotaForSubject(SecurityUtils.getSubject()));

        DateTime now = DateTime.now();
        u.setCreated(now);
        u.setLastModified(now);

        setupStaticRoles(u, "SYNCHRONIZED_USER"); //$NON-NLS-1$

        if ( SecurityUtils.getSubject() != null && SecurityUtils.getSubject().getPrincipals() != null ) {
            PrincipalCollection principals = SecurityUtils.getSubject().getPrincipals();
            eu.agno3.runtime.security.principal.UserDetails authDetails = principals.oneByType(eu.agno3.runtime.security.principal.UserDetails.class);
            if ( authDetails != null ) {
                setupUserDetails(u, authDetails);
            }
        }

        ContainerEntity rootContainer = ServiceUtil
                .createSubjectRoot(tx, this.modTracker, u, u, this.ctx.getConfigurationProvider().getSecurityPolicyConfiguration());
        u.setSubjectRoot(rootContainer);
        em.persist(rootContainer);
        em.persist(u);
        return u;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     *
     * @see eu.agno3.fileshare.service.api.internal.UserServiceInternal#ensureUserExists(eu.agno3.runtime.security.principal.UserPrincipal)
     */
    @Override
    public User ensureUserExists ( UserPrincipal up ) throws FileshareException {
        User u;
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();
            User find = em.find(User.class, up.getUserId());
            if ( find == null ) {
                u = makeUserInternal(tx, up);
                em.flush();
                em.refresh(u);
            }
            else {
                u = find;
            }
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to ensure user exists", e); //$NON-NLS-1$
        }
        return u;
    }


    /**
     * @param u
     */
    private boolean setupStaticRoles ( User u, String... extraRoles ) {
        Set<String> staticRoles = new HashSet<>();
        for ( String staticRole : this.ctx.getConfigurationProvider().getUserConfig().getStaticSynchronizationRoles() ) {
            if ( SecurityUtils.getSubject().hasRole(staticRole) ) {
                staticRoles.add(staticRole);
            }
        }

        if ( extraRoles != null ) {
            for ( String extraRole : extraRoles ) {
                staticRoles.add(extraRole);
            }
        }
        if ( log.isDebugEnabled() ) {
            log.debug("Setting user roles to static sync roles " + staticRoles); //$NON-NLS-1$
        }

        if ( !Objects.equals(u.getRoles(), staticRoles) ) {
            u.setRoles(staticRoles);
            return true;
        }
        return false;
    }


    /**
     * @param u
     * @param authDetails
     */
    private static boolean setupUserDetails ( User u, eu.agno3.runtime.security.principal.UserDetails authDetails ) {
        boolean anyChanged = false;
        UserDetails ud = u.getUserDetails();

        if ( !StringUtils.isBlank(authDetails.getMailAddress()) && !Objects.equals(ud.getMailAddress(), authDetails.getMailAddress()) ) {
            ud.setMailAddress(authDetails.getMailAddress());
            ud.setMailAddressVerified(true);
            anyChanged = true;
        }

        if ( !StringUtils.isBlank(authDetails.getDisplayName()) && !Objects.equals(ud.getPreferredName(), authDetails.getDisplayName()) ) {
            ud.setPreferredName(authDetails.getDisplayName());
            ud.setPreferredNameVerified(true);
            anyChanged = true;
        }

        if ( !Objects.equals(ud.getOrganization(), authDetails.getOrganization()) ) {
            ud.setOrganization(authDetails.getOrganization());
            anyChanged = true;
        }

        if ( !Objects.equals(ud.getOrganizationUnit(), authDetails.getOrganizationUnit()) ) {
            ud.setOrganizationUnit(authDetails.getOrganizationUnit());
            anyChanged = true;
        }

        if ( !Objects.equals(ud.getJobTitle(), authDetails.getJobTitle()) ) {
            ud.setJobTitle(authDetails.getJobTitle());
            anyChanged = true;
        }

        return anyChanged;
    }


    /**
     * @param princ
     */
    private boolean verifyValidUser ( UserPrincipal princ ) {
        // this would be nice for other mechanisms where it is possible too
        // otherwise a still logged in user will be recreated even if the
        // auth is no longer valid
        if ( "LOCAL".equals(princ.getRealmName()) ) { //$NON-NLS-1$
            // perform an additional check that the user is still valid
            try {
                UserInfo user = this.localUserService.getMappedUser(princ);
                if ( user.getDisabled() != null && user.getDisabled() ) {
                    log.warn("User is disabled " + princ); //$NON-NLS-1$
                    return false;
                }
            }
            catch ( FileshareException e ) {
                log.warn("User does no longer exist", e); //$NON-NLS-1$
                return false;
            }
        }
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#enableLocalUser(java.util.UUID)
     */
    @Override
    public void enableLocalUser ( UUID userId ) throws FileshareException {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("ENABLE_USER"); //$NON-NLS-1$
            try {
                if ( !JMXSecurityUtil.isManagementCall() ) {
                    this.accessControl.checkPermission("manage:users:enable"); //$NON-NLS-1$
                }
                try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                    EntityManager em = tx.getEntityManager();

                    User u = em.find(User.class, userId);

                    audit.builder().subject(u);
                    if ( u == null ) {
                        throw new UserNotFoundException("Cannot find user by id " + userId); //$NON-NLS-1$
                    }

                    this.localUserService.enableUser(u.getPrincipal());
                    tx.commit();
                }
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to enable user", e); //$NON-NLS-1$
            }
        }
    }


    @Override
    public void enableLocalUser ( EntityTransactionContext tx, User u ) throws FileshareException {
        this.localUserService.enableUser(u.getPrincipal());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#disableLocalUser(java.util.UUID)
     */
    @Override
    public void disableLocalUser ( UUID userId ) throws FileshareException {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("DISABLE_USER"); //$NON-NLS-1$
            try {
                if ( !JMXSecurityUtil.isManagementCall() ) {
                    this.accessControl.checkPermission("manage:users:disable"); //$NON-NLS-1$
                }
                try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                    EntityManager em = tx.getEntityManager();

                    User u = em.find(User.class, userId);
                    audit.builder().subject(u);
                    if ( u == null ) {
                        throw new UserNotFoundException("Cannot find user by id " + userId); //$NON-NLS-1$
                    }

                    this.localUserService.disableUser(u.getPrincipal());
                    tx.commit();
                }
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to disable user", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#createLocalUser(eu.agno3.fileshare.model.UserCreateData)
     */
    @Override
    public User createLocalUser ( UserCreateData userData ) throws FileshareException {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("CREATE_USER"); //$NON-NLS-1$
            audit.builder().property("userName", userData.getUserName()); //$NON-NLS-1$
            audit.builder().property("disabled", userData.getDisabled()); //$NON-NLS-1$
            audit.builder().property("forcePasswordChange", userData.getForcePasswordChange()); //$NON-NLS-1$
            audit.builder().property("noRoot", userData.getNoSubjectRoot()); //$NON-NLS-1$
            audit.builder().property("expires", userData.getExpires() != null ? userData.getExpires().getMillis() : null); //$NON-NLS-1$
            audit.builder().property("securityLabel", userData.getSecurityLabel()); //$NON-NLS-1$
            audit.builder().property("roles", new HashSet<>(userData.getRoles())); //$NON-NLS-1$
            audit.builder().property("quota", userData.getQuota()); //$NON-NLS-1$

            try {
                if ( !JMXSecurityUtil.isManagementCall() ) {
                    this.accessControl.checkPermission("manage:users:create"); //$NON-NLS-1$
                }
                SCryptResult pwHash;
                try {
                    pwHash = this.localUserService.generatePasswordHash(userData.getPassword(), true);
                }
                catch ( SecurityManagementException e ) {
                    throw new SecurityException("Failed to generate password hash", e); //$NON-NLS-1$
                }

                try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                    EntityManager em = tx.getEntityManager();
                    User u;
                    try {
                        u = createUserInternal(
                            tx,
                            userData.getUserName(),
                            userData.getDisabled(),
                            userData.getForcePasswordChange(),
                            userData.getNoSubjectRoot(),
                            userData.getExpires(),
                            pwHash,
                            userData.getUserDetails(),
                            this.accessControl.getCurrentUser());
                    }
                    catch ( UserLicenseLimitExceededException e ) {
                        throw new UserLimitExceededException(e);
                    }
                    audit.builder().subject(u);

                    SecurityLabel securityLabel = ServiceUtil.getOrCreateSecurityLabel(tx, userData.getSecurityLabel());
                    u.setSecurityLabel(securityLabel);
                    u.setQuota(userData.getQuota());
                    u.setRoles(new HashSet<>(userData.getRoles()));

                    em.persist(u);
                    em.flush();
                    tx.commit();
                    return u.cloneShallow(true);
                }
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to create user", e); //$NON-NLS-1$
            }
        }
    }


    @Override
    public boolean checkUserExists ( EntityTransactionContext tx, String userName ) throws FileshareException {
        return this.localUserService.userExists(new UserPrincipal("LOCAL", null, userName)); //$NON-NLS-1$
    }


    @Override
    public User createUserInternal ( EntityTransactionContext tx, String userName, boolean disabled, boolean forcePasswordChange, boolean noRoot,
            DateTime expires, SCryptResult pwHash, UserDetails details, User creator ) throws FileshareException, UserLicenseLimitExceededException {
        EntityManager em = tx.getEntityManager();
        UserInfo authUser;
        DateTime pwExpiry = null;
        if ( forcePasswordChange ) {
            pwExpiry = DateTime.now();
        }
        authUser = this.localUserService.createUser(userName, pwHash, disabled, pwExpiry, expires);

        User newUser = new User();
        newUser.setId(authUser.getUserPrincipal().getUserId());
        newUser.setPrincipal(authUser.getUserPrincipal());
        newUser.setType(SubjectType.LOCAL);
        newUser.setNoSubjectRoot(noRoot);
        newUser.setLastModified(DateTime.now());
        newUser.setCreator(creator);

        ContainerEntity rootContainer = ServiceUtil
                .createSubjectRoot(tx, this.modTracker, newUser, newUser, this.ctx.getConfigurationProvider().getSecurityPolicyConfiguration());
        newUser.setSubjectRoot(rootContainer);

        details.setUser(newUser);
        newUser.setUserDetails(details);
        em.persist(details);
        em.persist(rootContainer);
        em.persist(newUser);
        return newUser;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#listGroups(int, int)
     */
    @Override
    public List<User> listUsers ( int off, int limit ) throws FileshareException {
        if ( !JMXSecurityUtil.isManagementCall() ) {
            this.accessControl.checkPermission(LIST_PERM);
        }

        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<User> q = cb.createQuery(User.class);
            q.from(User.class);
            TypedQuery<User> cq = em.createQuery(q);
            if ( off >= 0 ) {
                cq.setFirstResult(off);
            }
            if ( limit >= 0 ) {
                cq.setMaxResults(limit);
            }

            List<User> users = new ArrayList<>();
            for ( User u : cq.getResultList() ) {
                users.add(u.cloneShallow(false));
            }
            return users;
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to list users", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#getUserCount()
     */
    @Override
    public long getUserCount () throws FileshareException {
        if ( !JMXSecurityUtil.isManagementCall() ) {
            this.accessControl.checkPermission(LIST_PERM);
        }
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Long> q = cb.createQuery(Long.class);
            Root<User> user = q.from(User.class);
            q.select(cb.count(user.get("id"))); //$NON-NLS-1$
            return em.createQuery(q).getSingleResult();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get user count", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#getUserGroups(java.util.UUID)
     */
    @Override
    public List<Group> getUserGroups ( UUID userId ) throws FileshareException {
        if ( !JMXSecurityUtil.isManagementCall() ) {
            this.accessControl.checkPermission(LIST_PERM);
        }

        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();

            User user = em.find(User.class, userId);
            if ( user == null ) {
                throw new UserNotFoundException("Not found by id " + userId); //$NON-NLS-1$
            }

            List<Group> groupList = new ArrayList<>();
            for ( Group g : user.getMemberships() ) {
                groupList.add(g.cloneShallow(false));
            }
            return groupList;
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get user groups", e); //$NON-NLS-1$
        }

    }


    @Override
    public List<Group> getUserGroupClosure ( UUID userId ) throws FileshareException {
        if ( !JMXSecurityUtil.isManagementCall() ) {
            this.accessControl.checkPermission(LIST_PERM);
        }
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();

            User user = em.find(User.class, userId);
            if ( user == null ) {
                throw new UserNotFoundException("Not found by id " + userId); //$NON-NLS-1$
            }

            List<Group> groupList = new ArrayList<>();
            addGroupsRecursive(groupList, user.getMemberships());
            return groupList;
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get user group closure", e); //$NON-NLS-1$
        }

    }


    @Override
    public boolean isUserDisabled ( EntityTransactionContext TX, User found ) throws FileshareException {
        try {
            return this.localUserService.getUser(found.getPrincipal()).getDisabled();
        }
        catch ( UserNotFoundException e ) {
            log.debug("User not found", e); //$NON-NLS-1$
            return false;
        }
    }


    @Override
    public boolean isCurrentUserMember ( UUID groupId ) throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();
            Group group = em.find(Group.class, groupId);
            if ( group == null ) {
                throw new SubjectNotFoundException("Failed to find group"); //$NON-NLS-1$
            }
            return this.accessControl.isMember(group);
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to check membership", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#deleteUsers(java.util.List)
     */
    @Override
    public void deleteUsers ( List<UUID> userIds ) throws FileshareException {
        try ( AuditContext<MultiSubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(MultiSubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("DELETE_USERS_OUTER"); //$NON-NLS-1$
            try {
                if ( !JMXSecurityUtil.isManagementCall() ) {
                    this.accessControl.checkPermission("manage:users:delete"); //$NON-NLS-1$
                }
                try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                    EntityManager em = tx.getEntityManager();
                    User currentUser = this.accessControl.getCurrentUser();

                    Set<UserPrincipal> toRemoveFromSystem = new HashSet<>();

                    for ( UUID userId : userIds ) {
                        User u = em.find(User.class, userId);

                        audit.builder().subject(u);

                        if ( u == null ) {
                            throw new UserNotFoundException("Could not find user with id " + userId); //$NON-NLS-1$
                        }

                        if ( u.equals(currentUser) ) {
                            throw new CannotDeleteCurrentUserException();
                        }

                        if ( u.getType() == SubjectType.LOCAL && "LOCAL".equals(u.getPrincipal().getRealmName()) ) { //$NON-NLS-1$
                            toRemoveFromSystem.add(u.getPrincipal());
                        }

                        deleteUser(tx, u);
                        em.flush();
                    }

                    if ( log.isDebugEnabled() ) {
                        log.debug("Removing local authentication users:" + toRemoveFromSystem); //$NON-NLS-1$
                    }

                    this.localUserService.removeUsers(toRemoveFromSystem);
                    tx.commit();
                }
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to delete user(s)", e); //$NON-NLS-1$
            }
        }
    }


    @Override
    public void deleteUser ( EntityTransactionContext tx, User u ) {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("DELETE_USER"); //$NON-NLS-1$
            audit.builder().subject(u);
            cleanupUserReferences(tx, u);
            tx.getEntityManager().remove(u);
        }
    }


    /**
     * @param em
     * @param u
     */
    private static void cleanupUserReferences ( EntityTransactionContext tx, User u ) {

        if ( log.isTraceEnabled() ) {
            log.trace("Cleanup user " + u); //$NON-NLS-1$
        }

        for ( Grant g : u.getCreatedGrants() ) {
            g.setCreator(null);
            tx.getEntityManager().persist(g);
        }

        Collection<SubjectGrant> removeGrants = u.getGrants();
        for ( Grant g : new LinkedList<>(removeGrants) ) {
            ShareServiceImpl.cleanupGrant(tx, g, true);
        }
        u.getGrants().clear();

        for ( ContentEntity e : u.getCreatedEntities() ) {
            e.setCreator(null);
            tx.getEntityManager().persist(e);
        }

        for ( ContentEntity e : u.getLastModifiedEntities() ) {
            e.setLastModifier(null);
            tx.getEntityManager().persist(e);
        }
        u.getLastModifiedEntities().clear();

        for ( Group g : u.getMemberships() ) {
            g.getMembers().remove(u);
            tx.getEntityManager().persist(g);
        }

        if ( u.getUserDetails() != null ) {
            u.getUserDetails().setUser(null);
            tx.getEntityManager().remove(u.getUserDetails());
            u.setUserDetails(null);
        }

        for ( Subject s : u.getCreatorOf() ) {
            s.setCreator(null);
            tx.getEntityManager().persist(s);
        }

        cleanupFavorites(tx, u);
        cleanupHidden(tx, u);
        cleanupEntities(tx, u);
    }


    /**
     * @param em
     * @param u
     * @param subjectRoot
     */
    private static void cleanupEntities ( EntityTransactionContext tx, User u ) {
        EntityManager em = tx.getEntityManager();
        TypedQuery<ContentEntity> query = em.createQuery("SELECT e FROM ContentEntity e WHERE owner = :user", ContentEntity.class); //$NON-NLS-1$
        query.setParameter("user", u); //$NON-NLS-1$
        for ( ContentEntity e : query.getResultList() ) {
            ServiceUtil.cleanEntityReferences(tx, e);
            em.remove(e);
        }
    }


    /**
     * @param em
     * @param u
     */
    private static void cleanupHidden ( EntityTransactionContext tx, User u ) {
        EntityManager em = tx.getEntityManager();
        Set<ContentEntity> hiddenEntities = u.getHiddenEntities();
        if ( hiddenEntities != null ) {
            for ( ContentEntity fav : new LinkedList<>(hiddenEntities) ) {
                fav.getHiddenBy().remove(u);
                em.persist(fav);
            }
            hiddenEntities.clear();
        }

        Set<User> hiddenBy = u.getHiddenBy();
        if ( hiddenBy != null ) {
            for ( User faved : new LinkedList<>(hiddenBy) ) {
                faved.getHiddenSubjects().remove(u);
                em.persist(faved);
            }
            hiddenBy.clear();
        }

        Set<Subject> hiddenSubjects = u.getHiddenSubjects();
        if ( hiddenSubjects != null ) {
            for ( Subject fav : new LinkedList<>(hiddenSubjects) ) {
                fav.getHiddenBy().remove(u);
                em.persist(fav);
            }
            hiddenSubjects.clear();
        }
    }


    /**
     * @param em
     * @param u
     */
    private static void cleanupFavorites ( EntityTransactionContext tx, User u ) {
        EntityManager em = tx.getEntityManager();
        if ( u.getFavoriteEntities() != null ) {
            for ( ContentEntity fav : u.getFavoriteEntities() ) {
                fav.getFavoriteBy().remove(u);
                em.persist(fav);
            }
            u.getFavoriteEntities().clear();
        }

        if ( u.getFavoriteBy() != null ) {
            for ( User faved : u.getFavoriteBy() ) {
                faved.getFavoriteSubjects().remove(u);
                em.persist(faved);
            }
            u.getFavoriteBy().clear();
        }

        if ( u.getFavoriteSubjects() != null ) {
            for ( Subject fav : u.getFavoriteSubjects() ) {
                fav.getFavoriteBy().remove(u);
                em.persist(fav);
            }
            u.getFavoriteSubjects().clear();
        }
    }


    /**
     * @param groups
     * @param memberships
     */
    private void addGroupsRecursive ( Collection<Group> groups, Set<Group> memberships ) {
        for ( Group g : memberships ) {
            if ( !groups.contains(g) ) {
                groups.add(g.cloneShallow(false));
                addGroupsRecursive(groups, g.getMemberships());
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#changePassword(java.util.UUID, java.lang.String)
     */
    @Override
    public void changePassword ( UUID userId, String newPassword ) throws FileshareException {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("ADMIN_CHANGE_PASSWORD"); //$NON-NLS-1$
            try {
                if ( !JMXSecurityUtil.isManagementCall() ) {
                    this.accessControl.checkPermission("manage:users:changePassword"); //$NON-NLS-1$
                }

                SCryptResult pwHash = genPasswordHash(newPassword);

                try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                    EntityManager em = tx.getEntityManager();
                    User u = em.find(User.class, userId);
                    audit.builder().subject(u);
                    if ( u == null ) {
                        throw new UserNotFoundException("Could not find user with id " + userId); //$NON-NLS-1$
                    }

                    UserPrincipal up = u.getPrincipal();
                    this.localUserService.changePassword(up, pwHash);
                    tx.commit();
                }
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to change password", e); //$NON-NLS-1$
            }
        }
    }


    @Override
    public void changePassword ( EntityTransactionContext tx, UserPrincipal principal, SCryptResult pwHash ) throws FileshareException {
        User u = tx.getEntityManager().find(User.class, principal.getUserId());
        if ( u == null ) {
            throw new UserNotFoundException("Could not find user with id " + principal.getUserId()); //$NON-NLS-1$
        }

        UserPrincipal up = u.getPrincipal();
        this.localUserService.changePassword(up, pwHash);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.UserServiceInternal#genPasswordHash(java.lang.String)
     */
    @Override
    public SCryptResult genPasswordHash ( String newPassword ) throws PasswordChangeException {
        SCryptResult pwHash;
        try {
            pwHash = this.localUserService.generatePasswordHash(newPassword, true);
        }
        catch ( SecurityManagementException e ) {
            throw new PasswordChangeException("Failed to generate password hash", e); //$NON-NLS-1$
        }
        return pwHash;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#updateUserLabel(java.util.UUID, java.lang.String)
     */
    @Override
    public void updateUserLabel ( UUID userId, String label ) throws FileshareException {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("SET_USER_LABEL"); //$NON-NLS-1$
            audit.builder().property("newLabel", label); //$NON-NLS-1$
            try {
                if ( !JMXSecurityUtil.isManagementCall() ) {
                    this.accessControl.checkPermission("manage:users:changeSecurityLabel"); //$NON-NLS-1$
                }
                try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                    EntityManager em = tx.getEntityManager();

                    User u = em.find(User.class, userId);
                    audit.builder().subject(u);
                    if ( u == null ) {
                        throw new UserNotFoundException("Could not find user with id " + userId); //$NON-NLS-1$
                    }

                    SecurityLabel securityLabel = ServiceUtil.getOrCreateSecurityLabel(tx, label);
                    u.setSecurityLabel(securityLabel);
                    em.persist(u);
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
                throw new FileshareException("Failed to set user label", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#updateUserQuota(java.util.UUID, java.lang.Long)
     */
    @Override
    public void updateUserQuota ( UUID userId, Long quota ) throws FileshareException {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("SET_QUOTA"); //$NON-NLS-1$
            audit.builder().property("newQuota", quota); //$NON-NLS-1$
            try {
                if ( !JMXSecurityUtil.isManagementCall() ) {
                    this.accessControl.checkPermission("manage:users:changeQuota"); //$NON-NLS-1$
                }
                try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                    EntityManager em = tx.getEntityManager();

                    User u = em.find(User.class, userId);
                    audit.builder().subject(u);
                    if ( u == null ) {
                        throw new UserNotFoundException("Could not find user with id " + userId); //$NON-NLS-1$
                    }
                    boolean previouslyDisabled = u.getQuota() == null && quota != null;

                    u.setQuota(quota);
                    em.persist(u);
                    em.flush();

                    if ( previouslyDisabled ) {
                        this.quotaService.updateDirectorySizes(em, u);
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
                throw new FileshareException("Failed to update user quota", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#disableUserRoot(java.util.UUID)
     */
    @Override
    public void disableUserRoot ( UUID userId ) throws FileshareException {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("DISABLE_USER_ROOT"); //$NON-NLS-1$
            try {
                if ( !JMXSecurityUtil.isManagementCall() ) {
                    this.accessControl.checkPermission("manage:users:userRoot"); //$NON-NLS-1$
                }
                try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                    EntityManager em = tx.getEntityManager();

                    User u = em.find(User.class, userId);
                    audit.builder().subject(u);
                    if ( u == null ) {
                        throw new UserNotFoundException("Could not find user with id " + userId); //$NON-NLS-1$
                    }

                    u.setNoSubjectRoot(true);
                    em.persist(u);
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
                throw new FileshareException("Failed to disable user", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#enableUserRoot(java.util.UUID)
     */
    @Override
    public void enableUserRoot ( UUID userId ) throws FileshareException {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("ENABLE_USER_ROOT"); //$NON-NLS-1$
            try {
                if ( !JMXSecurityUtil.isManagementCall() ) {
                    this.accessControl.checkPermission("manage:users:userRoot"); //$NON-NLS-1$
                }
                try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                    EntityManager em = tx.getEntityManager();

                    User u = em.find(User.class, userId);

                    if ( u == null ) {
                        throw new UserNotFoundException("Could not find user with id " + userId); //$NON-NLS-1$
                    }

                    u.setNoSubjectRoot(false);
                    em.persist(u);
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
                throw new FileshareException("Failed to enable user", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#changeCurrentUserPassword(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void changeCurrentUserPassword ( String oldPassword, String newPassword ) throws FileshareException, PasswordPolicyException {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("SELF_CHANGE_PASSWORD"); //$NON-NLS-1$
            try {
                User currentUser = this.accessControl.getCurrentUser();
                audit.builder().subject(currentUser);
                UserPrincipal up = currentUser.getPrincipal();

                if ( !this.localUserService.verifyPassword(up, oldPassword) ) {
                    throw new InvalidPasswordException();
                }

                SCryptResult pwHash = genPasswordHash(newPassword);

                try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                    this.localUserService.changePassword(up, pwHash);
                    tx.commit();
                }
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to change user password", e); //$NON-NLS-1$
            }
        }

    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#setUserExpiry(java.util.UUID, org.joda.time.DateTime)
     */
    @Override
    public void setUserExpiry ( UUID id, DateTime expiration ) throws FileshareException {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("SET_SUBJECT_EXPIRY"); //$NON-NLS-1$
            audit.builder().property("newExpiry", expiration); //$NON-NLS-1$

            try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                EntityManager em = tx.getEntityManager();
                User persistent = em.find(User.class, id);
                audit.builder().subject(persistent);

                if ( !JMXSecurityUtil.isManagementCall() && !this.accessControl.hasPermission("manage:subjects:expiry") ) { //$NON-NLS-1$
                    if ( !this.ctx.getConfigurationProvider().getUserConfig().isAllowInvitingUserExtension() ) {
                        throw new AccessDeniedException();
                    }
                    if ( persistent == null || persistent.getCreator() == null
                            || !persistent.getCreator().equals(this.accessControl.getCurrentUserCachable()) ) {
                        throw new AccessDeniedException();
                    }
                }
                if ( persistent == null ) {
                    throw new SubjectNotFoundException();
                }

                audit.builder().property("oldExpiry", persistent.getExpiration()); //$NON-NLS-1$
                persistent.setExpiration(expiration);

                this.localUserService.setUserExpiry(persistent.getPrincipal(), expiration);

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
                throw new FileshareException("Failed to set user expiration", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#getUserDetails(java.util.UUID)
     */
    @Override
    public UserDetails getUserDetails ( UUID userId ) throws FileshareException {

        User currentUser = this.accessControl.getCurrentUser();
        if ( !JMXSecurityUtil.isManagementCall() && !this.accessControl.hasPermission("manage:subjects:list") && //$NON-NLS-1$
                !this.accessControl.hasPermission("subjects:query:details") && //$NON-NLS-1$
                !currentUser.getId().equals(userId) ) {

            for ( SubjectGrant g : currentUser.getGrants() ) {
                if ( g.getEntity().getOwner().getId().equals(userId) && g.getEntity().getOwner() instanceof User ) {
                    return ( (User) g.getEntity().getOwner() ).getUserDetails();
                }

                if ( g.getCreator().getId().equals(userId) && g.getCreator() != null ) {
                    return g.getCreator().getUserDetails();
                }
            }
            throw new AccessDeniedException();
        }

        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();

            User u = em.find(User.class, userId);
            if ( u == null ) {
                throw new UserNotFoundException("Could not find user with id " + userId); //$NON-NLS-1$
            }

            UserDetails details = u.getUserDetails();

            if ( details == null ) {
                details = new UserDetails();
                u.setUserDetails(details);
                details.setUser(u);
                em.persist(details);
                em.persist(u);
            }

            em.flush();
            tx.commit();
            return details.cloneShallow(true);
        }
        catch ( FileshareException e ) {
            throw e;
        }
        catch ( Exception e ) {
            throw new FileshareException("Failed to get user details", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.UserServiceMBean#updateUserDetails(java.util.UUID,
     *      eu.agno3.fileshare.model.UserDetails)
     */
    @Override
    public UserDetails updateUserDetails ( UUID userId, UserDetails data ) throws FileshareException {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("UPDATE_USER_DETAIL"); //$NON-NLS-1$
            try {
                boolean selfUpdate = false;
                audit.builder().property("selfUpdate", selfUpdate); //$NON-NLS-1$
                audit.builder().property("newName", data.getFullName()); //$NON-NLS-1$
                audit.builder().property("newMailAddress", data.getMailAddress()); //$NON-NLS-1$

                try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                    if ( !JMXSecurityUtil.isManagementCall() && !this.accessControl.hasPermission("manage:users:updateDetails") ) { //$NON-NLS-1$
                        if ( this.accessControl.hasPermission("user:updateDetails") //$NON-NLS-1$
                                && this.getCurrentUser(tx).getId().equals(userId) ) {
                            selfUpdate = true;
                        }
                        else {
                            throw new AccessDeniedException();
                        }
                    }

                    EntityManager em = tx.getEntityManager();

                    User u = em.find(User.class, userId);
                    audit.builder().subject(u);
                    if ( u == null ) {
                        throw new UserNotFoundException("Could not find user with id " + userId); //$NON-NLS-1$
                    }

                    updateUserDetailsInternal(tx, data, u, selfUpdate);
                    em.flush();
                    em.refresh(u);
                    tx.commit();
                    return u.getUserDetails().cloneShallow(true);
                }
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to update user details", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param data
     * @param em
     * @param u
     * @param selfUpdate
     */
    private static void updateUserDetailsInternal ( EntityTransactionContext tx, UserDetails data, User u, boolean selfUpdate ) {
        data.setId(null);
        if ( u.getUserDetails() != null ) {
            data.setId(u.getUserDetails().getId());
            data.setUser(u);
            tx.getEntityManager().merge(data);
        }
        else {
            data.setVersion(0);
            u.setUserDetails(data);
            data.setUser(u);
            tx.getEntityManager().persist(data);
        }
    }

}
