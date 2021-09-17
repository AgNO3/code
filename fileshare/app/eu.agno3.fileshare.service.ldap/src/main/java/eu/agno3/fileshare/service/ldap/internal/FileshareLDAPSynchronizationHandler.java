/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.ldap.internal;


import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.SecurityLabel;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.SubjectType;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.UserDetails;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.GroupServiceInternal;
import eu.agno3.fileshare.service.api.internal.ScrollIterator;
import eu.agno3.fileshare.service.api.internal.UserServiceInternal;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.ldap.client.LDAPClientFactory;
import eu.agno3.runtime.security.UserLicenseLimitExceededException;
import eu.agno3.runtime.security.UserMapper;
import eu.agno3.runtime.security.ldap.LDAPGroup;
import eu.agno3.runtime.security.ldap.LDAPRealmConfig;
import eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler;
import eu.agno3.runtime.security.ldap.LDAPSynchronizationRuntimeException;
import eu.agno3.runtime.security.ldap.LDAPUser;
import eu.agno3.runtime.util.iter.ClosableIterator;


/**
 * @author mbechler
 *
 */
public class FileshareLDAPSynchronizationHandler implements LDAPSynchronizationHandler {

    private static final Logger log = Logger.getLogger(FileshareLDAPSynchronizationHandler.class);

    private UserMapper userMapper;
    private DefaultServiceContext ctx;
    private LDAPRealmConfig config;
    private LDAPClientFactory clientFactory;
    private String realm;
    private DateTime lastRun;
    private EntityTransactionContext readEntityTransaction;
    private boolean warnedLicense;

    private UserServiceInternal userService;
    private GroupServiceInternal groupService;


    /**
     * @param ctx
     * @param userMapper
     * @param groupService
     * @param userService
     * @param cfg
     * @param factory
     * @param realm
     * @param lastRun
     * @throws EntityTransactionException
     */
    public FileshareLDAPSynchronizationHandler ( DefaultServiceContext ctx, UserMapper userMapper, UserServiceInternal userService,
            GroupServiceInternal groupService, LDAPRealmConfig cfg, LDAPClientFactory factory, String realm, DateTime lastRun )
            throws EntityTransactionException {
        this.ctx = ctx;
        this.userMapper = userMapper;
        this.userService = userService;
        this.groupService = groupService;
        this.config = cfg;
        this.clientFactory = factory;
        this.realm = realm;
        this.lastRun = lastRun;
        this.readEntityTransaction = ctx.getFileshareEntityTS().startReadOnly();
    }


    /**
     * 
     */
    public void close () {
        try {
            this.readEntityTransaction.close();
        }
        catch ( EntityTransactionException e ) {
            throw e.runtime();
        }
        this.readEntityTransaction = null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getConfig()
     */
    @Override
    public LDAPRealmConfig getConfig () {
        return this.config;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getClientFactory()
     */
    @Override
    public LDAPClientFactory getClientFactory () {
        return this.clientFactory;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getRealm()
     */
    @Override
    public String getRealm () {
        return this.realm;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getLastRun()
     */
    @Override
    public DateTime getLastRun () {
        return this.lastRun;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#userExistsById(java.util.UUID)
     */
    @Override
    public boolean userExistsById ( UUID uuid ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Find user by id %s", uuid)); //$NON-NLS-1$
        }
        return this.readEntityTransaction.getEntityManager().find(User.class, uuid) != null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getUserIds()
     */
    @SuppressWarnings ( {
        "unused", "resource"
    } )
    @Override
    public ClosableIterator<UUID> getUserIds () {
        // Directly use hibernate as JPA does not support scrolled results
        EntityTransactionContext tx;
        try {
            tx = this.ctx.getFileshareEntityTS().startReadOnly();
        }
        catch ( EntityTransactionException e ) {
            throw e.runtime();
        }
        Session session = tx.getEntityManager().unwrap(Session.class);
        session.setHibernateFlushMode(FlushMode.MANUAL);
        Query<UUID> q = session
                .createQuery("SELECT u.id FROM User u WHERE u.principal.realmName = :realm AND u.type = :type AND u.synchronizationHint IS NOT NULL"); //$NON-NLS-1$

        q.setParameter("realm", this.getRealm()); //$NON-NLS-1$
        q.setParameter("type", SubjectType.REMOTE); //$NON-NLS-1$
        q.setReadOnly(true);
        q.setCacheable(false);
        return new ScrollIterator<UUID>(UUID.class, q, tx);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getUserByName(java.lang.String)
     */
    @Override
    public UUID getUserByName ( String name ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Find user by name %s:%s", this.getRealm(), name)); //$NON-NLS-1$
        }
        TypedQuery<UUID> q = this.readEntityTransaction.getEntityManager()
                .createQuery("SELECT u.id FROM User u WHERE u.principal.realmName = :realm AND u.principal.userName = :username", UUID.class); //$NON-NLS-1$

        q.setParameter("realm", this.getRealm()); //$NON-NLS-1$
        q.setParameter("username", name); //$NON-NLS-1$

        List<UUID> resultList = q.getResultList();

        if ( resultList.size() > 1 ) {
            throw new LDAPSynchronizationRuntimeException("Multiple users matched"); //$NON-NLS-1$
        }

        return resultList.isEmpty() ? null : resultList.get(0);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getUserByDN(java.lang.String)
     */
    @Override
    public UUID getUserByDN ( String dn ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Find user by dn %s:%s", this.getRealm(), dn)); //$NON-NLS-1$
        }
        TypedQuery<UUID> q = this.readEntityTransaction.getEntityManager()
                .createQuery("SELECT u.id FROM User u WHERE u.principal.realmName = :realm AND u.synchronizationHint = :hint", UUID.class); //$NON-NLS-1$
        q.setParameter("realm", this.getRealm()); //$NON-NLS-1$
        q.setParameter("hint", truncateDN(dn)); //$NON-NLS-1$
        List<UUID> resultList = q.getResultList();
        if ( resultList.size() > 1 ) {
            throw new LDAPSynchronizationRuntimeException("Multiple users matched"); //$NON-NLS-1$
        }
        return resultList.isEmpty() ? null : resultList.get(0);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getUserDNs()
     */
    @SuppressWarnings ( {
        "unused", "resource"
    } )
    @Override
    public ClosableIterator<String> getUserDNs () {
        // Directly use hibernate as JPA does not support scrolled results
        EntityTransactionContext tx;
        try {
            tx = this.ctx.getFileshareEntityTS().startReadOnly();
        }
        catch ( EntityTransactionException e ) {
            throw e.runtime();
        }
        Session session = tx.getEntityManager().unwrap(Session.class);
        session.setHibernateFlushMode(FlushMode.MANUAL);
        Query<String> q = session.createQuery(
            "SELECT u.synchronizationHint FROM User u WHERE u.principal.realmName = :realm AND u.type = :type AND u.synchronizationHint IS NOT NULL"); //$NON-NLS-1$
        q.setParameter("realm", this.getRealm()); //$NON-NLS-1$
        q.setParameter("type", SubjectType.REMOTE); //$NON-NLS-1$
        q.setReadOnly(true);
        q.setCacheable(false);
        return new ScrollIterator<String>(String.class, q, tx);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#groupExistsById(java.util.UUID)
     */
    @Override
    public boolean groupExistsById ( UUID uuid ) {
        Group g = this.readEntityTransaction.getEntityManager().find(Group.class, uuid);
        return g != null && this.getRealm().equals(g.getRealm());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getGroupIds()
     */
    @SuppressWarnings ( {
        "unused", "resource"
    } )
    @Override
    public ClosableIterator<UUID> getGroupIds () {
        // Directly use hibernate as JPA does not support scrolled results
        EntityTransactionContext tx;
        try {
            tx = this.ctx.getFileshareEntityTS().startReadOnly();
        }
        catch ( EntityTransactionException e ) {
            throw e.runtime();
        }
        Session session = tx.getEntityManager().unwrap(Session.class);
        session.setHibernateFlushMode(FlushMode.MANUAL);
        Query<UUID> q = session
                .createQuery("SELECT g.id FROM Group g WHERE g.type = :type AND g.synchronizationHint IS NOT NULL AND g.realm = :realm"); //$NON-NLS-1$
        q.setParameter("type", SubjectType.REMOTE); //$NON-NLS-1$
        q.setParameter("realm", this.getRealm()); //$NON-NLS-1$
        q.setReadOnly(true);
        q.setCacheable(false);
        return new ScrollIterator<UUID>(UUID.class, q, tx);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getGroupByDN(java.lang.String)
     */
    @Override
    public UUID getGroupByDN ( String dn ) {
        TypedQuery<UUID> q = this.readEntityTransaction.getEntityManager()
                .createQuery("SELECT g.id FROM Group g WHERE g.synchronizationHint = :hint AND g.realm = :realm", UUID.class); //$NON-NLS-1$
        q.setParameter("hint", truncateDN(dn)); //$NON-NLS-1$
        q.setParameter("realm", this.getRealm()); //$NON-NLS-1$
        List<UUID> resultList = q.getResultList();
        if ( resultList.size() > 1 ) {
            throw new LDAPSynchronizationRuntimeException("Multiple groups matched"); //$NON-NLS-1$
        }
        return resultList.isEmpty() ? null : resultList.get(0);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getUserDNs()
     */
    @SuppressWarnings ( {
        "unused", "resource"
    } )
    @Override
    public ClosableIterator<String> getGroupDNs () {
        // Directly use hibernate as JPA does not support scrolled results
        EntityTransactionContext tx;
        try {
            tx = this.ctx.getFileshareEntityTS().startReadOnly();
        }
        catch ( EntityTransactionException e ) {
            throw e.runtime();
        }
        Session session = tx.getEntityManager().unwrap(Session.class);
        session.setHibernateFlushMode(FlushMode.MANUAL);
        Query<String> q = session.createQuery(
            "SELECT g.synchronizationHint FROM Group g WHERE g.type = :type AND g.synchronizationHint IS NOT NULL AND g.realm = :realm"); //$NON-NLS-1$
        q.setParameter("type", SubjectType.REMOTE); //$NON-NLS-1$
        q.setParameter("realm", this.getRealm()); //$NON-NLS-1$
        q.setReadOnly(true);
        q.setCacheable(false);
        return new ScrollIterator<String>(String.class, q, tx);
    }


    /**
     * @param dn
     * @return
     */
    private String truncateDN ( String dn ) {
        if ( dn.length() > 255 ) {
            if ( this.config.isRemoveMissing() && !this.config.isRemovalUseUUIDs() ) {
                throw new LDAPSynchronizationRuntimeException("Cannot use DNs longer than 255 chars when relying on DNs for removal"); //$NON-NLS-1$
            }

            String truncated = dn.substring(255);
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Truncating DN %s to %s", dn, truncated)); //$NON-NLS-1$
            }
            return truncated;
        }

        return dn;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#getGroupByName(java.lang.String)
     */
    @Override
    public UUID getGroupByName ( String name ) {
        TypedQuery<UUID> q = this.readEntityTransaction.getEntityManager()
                .createQuery("SELECT g.id FROM Group g WHERE g.name = :groupname AND g.realm = :realm", UUID.class); //$NON-NLS-1$
        q.setParameter("groupname", name); //$NON-NLS-1$
        q.setParameter("realm", this.getRealm()); //$NON-NLS-1$
        List<UUID> resultList = q.getResultList();
        if ( resultList.size() > 1 ) {
            throw new LDAPSynchronizationRuntimeException("Multiple groups matched"); //$NON-NLS-1$
        }
        return resultList.isEmpty() ? null : resultList.get(0);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#createGroup(java.lang.String, java.util.UUID,
     *      eu.agno3.runtime.security.ldap.LDAPGroup)
     */
    @Override
    public UUID createGroup ( String dn, UUID directoryId, LDAPGroup groupEntry ) {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();
            Group g = new Group();

            if ( directoryId != null ) {
                g.setId(directoryId);
            }
            else {
                g.setId(UUID.randomUUID());
            }

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Creating group %s with id %s", groupEntry.getName(), g.getId())); //$NON-NLS-1$
            }

            g.setName(groupEntry.getName());
            g.setType(SubjectType.REMOTE);
            g.setRealm(this.getRealm());
            g.setSynchronizationHint(dn);
            em.persist(g);
            em.flush();
            tx.commit();
            return g.getId();
        }
        catch ( EntityTransactionException e ) {
            throw e.runtime();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#removeGroups(java.util.Set)
     */
    @Override
    public void removeGroups ( Set<UUID> toDelete ) {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();

            for ( UUID groupId : toDelete ) {
                Group group = em.find(Group.class, groupId);
                if ( group == null ) {
                    throw new LDAPSynchronizationRuntimeException("Group not found " + groupId); //$NON-NLS-1$
                }

                if ( !this.getRealm().equals(group.getRealm()) ) {
                    log.warn("Not removing group that was not created from this realm " + group); //$NON-NLS-1$
                    tx.commit();
                    return;
                }

                log.info(this.getRealm() + ": Removing group " + group); //$NON-NLS-1$
                this.groupService.deleteGroup(tx, group);
            }

            em.flush();
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw e.runtime();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#removeGroup(java.util.UUID)
     */
    @Override
    public void removeGroup ( UUID groupId ) {
        removeGroups(Collections.singleton(groupId));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#updateGroup(java.util.UUID, java.lang.String,
     *      eu.agno3.runtime.security.ldap.LDAPGroup)
     */
    @Override
    public void updateGroup ( UUID groupId, String dn, LDAPGroup groupEntry ) {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();

            Group group = em.find(Group.class, groupId);
            if ( group == null ) {
                throw new LDAPSynchronizationRuntimeException("Group not found " + groupId); //$NON-NLS-1$
            }

            if ( !this.getRealm().equals(group.getRealm()) ) {
                log.warn("Not updating group that was not created from this realm " + group); //$NON-NLS-1$
                tx.commit();
                return;
            }

            group.setName(groupEntry.getName());
            group.setSynchronizationHint(dn);
            em.persist(group);
            em.flush();
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw e.runtime();
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#updateUser(java.util.UUID, java.lang.String,
     *      eu.agno3.runtime.security.ldap.LDAPUser, java.util.Set)
     */
    @Override
    public void updateUser ( UUID userId, String dn, LDAPUser userEntry, Set<String> roles ) {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();

            if ( log.isDebugEnabled() ) {
                log.debug("Updating user with roles " + roles); //$NON-NLS-1$
            }

            User user = em.find(User.class, userId);
            if ( user == null ) {
                throw new LDAPSynchronizationRuntimeException("User not found " + userId); //$NON-NLS-1$
            }

            try {
                user.setPrincipal(this.userMapper.getMappedUser(userEntry.getUsername(), this.getRealm(), userId));
            }
            catch ( UserLicenseLimitExceededException e ) {
                throw new LDAPSynchronizationRuntimeException("License exceeded", e); //$NON-NLS-1$
            }
            setUserAttributes(dn, userEntry, em, user);
            setupFromRoles(em, user, roles);
            em.persist(user);
            em.flush();
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw e.runtime();
        }
    }


    /**
     * @param dn
     * @param userEntry
     * @param em
     * @param user
     */
    private static void setUserAttributes ( String dn, LDAPUser userEntry, EntityManager em, User user ) {
        user.setSynchronizationHint(dn);
        user.setType(SubjectType.REMOTE);

        if ( user.getUserDetails() == null ) {
            user.setUserDetails(new UserDetails());
            user.getUserDetails().setUser(user);
            em.persist(user.getUserDetails());
        }

        if ( !StringUtils.isBlank(userEntry.getDisplayName()) ) {
            user.getUserDetails().setPreferredName(userEntry.getDisplayName());
            user.getUserDetails().setPreferredNameVerified(true);
        }

        if ( !StringUtils.isBlank(userEntry.getMailAddress()) ) {
            user.getUserDetails().setMailAddress(userEntry.getMailAddress());
            user.getUserDetails().setMailAddressVerified(true);
        }

        user.getUserDetails().setJobTitle(userEntry.getJobTitle());
        user.getUserDetails().setOrganization(userEntry.getOrganization());
        user.getUserDetails().setOrganizationUnit(userEntry.getOrganizationUnit());

        Map<String, String> setPropertiesIfNotExists = new HashMap<>();
        setUserLanguage(userEntry, setPropertiesIfNotExists);
        setUserTimezone(userEntry, setPropertiesIfNotExists);

        if ( log.isDebugEnabled() ) {
            log.debug("Setting properties " + setPropertiesIfNotExists); //$NON-NLS-1$
        }

        if ( user.getPreferences() == null ) {
            user.setPreferences(new HashMap<>());
        }

        for ( Entry<String, String> e : setPropertiesIfNotExists.entrySet() ) {
            if ( !user.getPreferences().containsKey(e.getKey()) ) {
                user.getPreferences().put(e.getKey(), e.getValue());
            }
        }
    }


    /**
     * @param user
     * @param roles
     */
    private void setupFromRoles ( EntityManager em, User user, Set<String> roles ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Found roles %s for %s", roles, user)); //$NON-NLS-1$
        }
        String label = this.ctx.getConfigurationProvider().getSecurityPolicyConfiguration().getDefaultUserLabelForRoles(roles);
        if ( label != null && log.isDebugEnabled() ) {
            log.debug("Settings security label " + label); //$NON-NLS-1$
        }
        user.setSecurityLabel(getOrCreateSecurityLabel(em, label));

        Long quota = this.ctx.getConfigurationProvider().getQuotaConfiguration().getDefaultQuotaForRoles(roles);
        if ( quota != null && log.isDebugEnabled() ) {
            log.debug("Setting quota " + quota); //$NON-NLS-1$
        }
        if ( user.getQuota() == null && quota != null ) {
            user.setQuota(quota);
        }
        user.setNoSubjectRoot(this.ctx.getConfigurationProvider().getUserConfig().hasNoSubjectRoot(roles));

        // setup static sync roles
        Collection<String> staticRoles = this.ctx.getConfigurationProvider().getUserConfig().getStaticSynchronizationRoles();
        Set<String> onlyStatic = new HashSet<>(roles);
        onlyStatic.retainAll(staticRoles);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Setting static roles %s for %s (static roles are: %s)", onlyStatic, user, staticRoles)); //$NON-NLS-1$
        }

        user.getRoles().removeAll(staticRoles);
        user.getRoles().addAll(onlyStatic);
    }


    /**
     * @param em
     * @param label
     * @return
     */
    protected static SecurityLabel getOrCreateSecurityLabel ( EntityManager em, String label ) {
        if ( label == null ) {
            return null;
        }
        SecurityLabel found = em.find(SecurityLabel.class, label);
        if ( found == null ) {
            found = new SecurityLabel();
            found.setLabel(label);
            em.persist(found);
        }
        return found;
    }


    /**
     * @param userEntry
     * @param setPropertiesIfNotExists
     */
    private static void setUserTimezone ( LDAPUser userEntry, Map<String, String> setPropertiesIfNotExists ) {

        String tz = userEntry.getTimezone();
        if ( StringUtils.isBlank(tz) ) {
            return;
        }

        try {
            DateTimeZone forID = DateTimeZone.forID(tz.trim());
            setPropertiesIfNotExists.put("overrideTimezone", forID.getID()); //$NON-NLS-1$
        }
        catch ( IllegalArgumentException e ) {
            log.warn("Failed to recognize timezone " + tz); //$NON-NLS-1$
        }

    }


    /**
     * @param userEntry
     * @param setPropertiesIfNotExists
     */
    private static void setUserLanguage ( LDAPUser userEntry, Map<String, String> setPropertiesIfNotExists ) {
        String prefLanguage = userEntry.getPreferredLanguage();
        if ( StringUtils.isBlank(prefLanguage) ) {
            return;
        }
        String[] languages = StringUtils.split(prefLanguage, ',');
        if ( languages == null || languages.length == 0 ) {
            return;
        }
        String first = languages[ 0 ].trim();
        int paramSep = first.indexOf(';');
        if ( paramSep >= 0 ) {
            first = first.substring(0, paramSep);
        }
        if ( log.isDebugEnabled() ) {
            log.debug("Found user locale string " + first); //$NON-NLS-1$
        }
        Locale l = Locale.forLanguageTag(first);

        if ( l != null ) {
            setPropertiesIfNotExists.put("overrideLocale", l.toLanguageTag()); //$NON-NLS-1$
        }

    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#createUser(java.lang.String, java.util.UUID,
     *      eu.agno3.runtime.security.ldap.LDAPUser, java.util.Set)
     */
    @Override
    public UUID createUser ( String dn, UUID directoryId, LDAPUser userEntry, Set<String> roles ) {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();

            User user = this.userService.makeUserInternal(tx, this.userMapper.getMappedUser(userEntry.getUsername(), this.getRealm(), directoryId));
            setupFromRoles(em, user, roles);
            setUserAttributes(dn, userEntry, em, user);
            em.persist(user);
            em.flush();
            tx.commit();
            return user.getId();
        }
        catch ( UserLicenseLimitExceededException e ) {
            if ( this.warnedLicense ) {
                log.debug("License limit exceeded, cannot create user", e); //$NON-NLS-1$
            }
            else {
                this.warnedLicense = true;
                log.warn(String.format("License user limit %d exceeded, cannot create another user", e.getLimit())); //$NON-NLS-1$
            }
            return null;
        }
        catch ( EntityTransactionException e ) {
            throw e.runtime();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#removeUsers(java.util.Set)
     */
    @Override
    public void removeUsers ( Set<UUID> toDelete ) {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();

            for ( UUID userId : toDelete ) {

                User user = em.find(User.class, userId);
                if ( user == null ) {
                    throw new LDAPSynchronizationRuntimeException("User not found " + userId); //$NON-NLS-1$
                }

                log.info("Removing user " + user); //$NON-NLS-1$
                this.userService.deleteUser(tx, user);
            }
            em.flush();
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw e.runtime();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#removeUser(java.util.UUID)
     */
    @Override
    public void removeUser ( UUID userId ) {
        removeUsers(Collections.singleton(userId));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#setMembers(java.util.UUID, java.util.Set)
     */
    @Override
    public void setMembers ( UUID groupId, Set<UUID> memberIds ) {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();

            Group group = em.find(Group.class, groupId);
            if ( group == null ) {
                throw new LDAPSynchronizationRuntimeException("Group not found " + groupId); //$NON-NLS-1$
            }

            Set<UUID> toAdd = new HashSet<>(memberIds);

            removeGoneMembers(em, group, toAdd);
            addNewMembers(em, group, toAdd);

            em.persist(group);
            em.flush();
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw e.runtime();
        }
    }


    /**
     * @param em
     * @param group
     * @param toAdd
     */
    private static void addNewMembers ( EntityManager em, Group group, Set<UUID> toAdd ) {
        for ( UUID toAddId : toAdd ) {
            User userToAdd = em.find(User.class, toAddId);

            if ( userToAdd == null ) {
                throw new LDAPSynchronizationRuntimeException("Could not find member user " + toAddId); //$NON-NLS-1$
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Adding user " + userToAdd); //$NON-NLS-1$
            }

            userToAdd.getMemberships().add(group);
            group.getMembers().add(userToAdd);
            em.persist(userToAdd);
        }
    }


    /**
     * @param em
     * @param group
     * @param toAdd
     */
    private static void removeGoneMembers ( EntityManager em, Group group, Set<UUID> toAdd ) {
        Set<User> toRemove = new HashSet<>();
        for ( Subject member : group.getMembers() ) {
            if ( member.getType() != SubjectType.REMOTE || ! ( member instanceof User ) ) {
                continue;
            }
            User memberUser = (User) member;

            if ( !toAdd.contains(memberUser.getId()) ) {
                log.debug("Removing nested group " + member); //$NON-NLS-1$
                toRemove.add(memberUser);
                memberUser.getMemberships().remove(group);
                em.persist(memberUser);
            }

            toAdd.remove(member.getId());
        }
        group.getMembers().removeAll(toRemove);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#setNestedGroups(java.util.UUID, java.util.Set)
     */
    @Override
    public void setNestedGroups ( UUID groupId, Set<UUID> newNestedGroups ) {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();
            Group group = em.find(Group.class, groupId);
            if ( group == null ) {
                throw new LDAPSynchronizationRuntimeException("Group not found " + groupId); //$NON-NLS-1$
            }

            Set<UUID> toAdd = new HashSet<>(newNestedGroups);
            removeGoneNestedGroups(em, group, toAdd);
            addNewNestedGroups(em, group, toAdd);
            em.persist(group);
            em.flush();
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw e.runtime();
        }
    }


    /**
     * @param groupId
     * @param em
     * @param group
     * @param toAdd
     */
    private static void addNewNestedGroups ( EntityManager em, Group group, Set<UUID> toAdd ) {
        for ( UUID toAddId : toAdd ) {
            Group groupToAdd = em.find(Group.class, toAddId);

            if ( groupToAdd == null ) {
                throw new LDAPSynchronizationRuntimeException("Could not find nested group " + toAddId); //$NON-NLS-1$
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Adding group " + groupToAdd); //$NON-NLS-1$
            }

            groupToAdd.getMemberships().add(group);
            group.getMembers().add(groupToAdd);
            em.persist(groupToAdd);
        }
    }


    /**
     * @param newNestedGroups
     * @param em
     * @param group
     * @param toRemove
     * @param toAdd
     */
    private static void removeGoneNestedGroups ( EntityManager em, Group group, Set<UUID> toAdd ) {
        Set<Group> toRemove = new HashSet<>();
        for ( Subject member : group.getMembers() ) {
            if ( member.getType() != SubjectType.REMOTE || ! ( member instanceof Group ) ) {
                continue;
            }
            Group memberGroup = (Group) member;

            if ( !toAdd.contains(memberGroup.getId()) ) {
                log.debug("Removing nested group " + member); //$NON-NLS-1$
                toRemove.add(memberGroup);
                memberGroup.getMemberships().remove(group);
                em.persist(memberGroup);
            }

            toAdd.remove(member.getId());
        }
        group.getMembers().removeAll(toRemove);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#setForwardNestedGroups(java.util.UUID,
     *      java.util.Set)
     */
    @Override
    public void setForwardNestedGroups ( UUID groupId, Set<UUID> newForwardNestedGroups ) {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();
            Group group = em.find(Group.class, groupId);
            if ( group == null ) {
                throw new LDAPSynchronizationRuntimeException("Group not found " + groupId); //$NON-NLS-1$
            }

            Set<UUID> toAdd = new HashSet<>(newForwardNestedGroups);
            removeGoneForwardNestedGroups(tx, group, toAdd);
            addNewForwardNestedGroups(tx, group, toAdd);

            em.persist(group);
            em.flush();
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw e.runtime();
        }
    }


    /**
     * @param em
     * @param group
     * @param toAdd
     */
    private static void addNewForwardNestedGroups ( EntityTransactionContext tx, Group group, Set<UUID> toAdd ) {
        for ( UUID toAddId : toAdd ) {
            Group groupToAdd = tx.getEntityManager().find(Group.class, toAddId);

            if ( groupToAdd == null ) {
                throw new LDAPSynchronizationRuntimeException("Could not find membership group " + toAddId); //$NON-NLS-1$
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Adding group membership " + groupToAdd); //$NON-NLS-1$
            }

            groupToAdd.getMembers().add(group);
            group.getMemberships().add(groupToAdd);
            tx.getEntityManager().persist(groupToAdd);
        }
    }


    /**
     * @param tx
     * @param group
     * @param toAdd
     */
    private static void removeGoneForwardNestedGroups ( EntityTransactionContext tx, Group group, Set<UUID> toAdd ) {
        Set<Group> toRemove = new HashSet<>();
        for ( Group memberOf : group.getMemberships() ) {
            if ( memberOf.getType() != SubjectType.REMOTE ) {
                continue;
            }

            if ( !toAdd.contains(memberOf.getId()) ) {
                log.debug("Removing group membership " + memberOf); //$NON-NLS-1$
                toRemove.add(memberOf);
                memberOf.getMembers().remove(group);
                tx.getEntityManager().persist(memberOf);
            }

            toAdd.remove(memberOf.getId());
        }
        group.getMemberships().removeAll(toRemove);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPSynchronizationHandler#setForwardMembership(java.util.UUID,
     *      java.util.Set)
     */
    @Override
    public void setForwardMembership ( UUID userId, Set<UUID> newMembershipUUIDs ) {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();

            User user = em.find(User.class, userId);
            if ( user == null ) {
                throw new LDAPSynchronizationRuntimeException("User not found " + userId); //$NON-NLS-1$
            }

            Set<UUID> toAdd = new HashSet<>(newMembershipUUIDs);
            removeGoneMemberships(em, user, toAdd);
            addNewMemberships(em, user, toAdd);
            em.persist(user);
            em.flush();
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw e.runtime();
        }
    }


    /**
     * @param em
     * @param user
     * @param toAdd
     */
    private static void addNewMemberships ( EntityManager em, User user, Set<UUID> toAdd ) {
        for ( UUID toAddId : toAdd ) {
            Group groupToAdd = em.find(Group.class, toAddId);

            if ( groupToAdd == null ) {
                throw new LDAPSynchronizationRuntimeException("Could not find membership group " + toAddId); //$NON-NLS-1$
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Adding group membership " + groupToAdd); //$NON-NLS-1$
            }

            groupToAdd.getMembers().add(user);
            user.getMemberships().add(groupToAdd);
            em.persist(groupToAdd);
        }
    }


    /**
     * @param em
     * @param user
     * @param toAdd
     */
    private static void removeGoneMemberships ( EntityManager em, User user, Set<UUID> toAdd ) {
        Set<Group> toRemove = new HashSet<>();
        for ( Group memberOf : user.getMemberships() ) {
            if ( memberOf.getType() != SubjectType.REMOTE ) {
                continue;
            }

            if ( !toAdd.contains(memberOf.getId()) ) {
                log.debug("Removing group membership " + memberOf); //$NON-NLS-1$
                toRemove.add(memberOf);
                memberOf.getMembers().remove(user);
                em.persist(memberOf);
            }

            toAdd.remove(memberOf.getId());
        }
        user.getMemberships().removeAll(toRemove);
    }

}
