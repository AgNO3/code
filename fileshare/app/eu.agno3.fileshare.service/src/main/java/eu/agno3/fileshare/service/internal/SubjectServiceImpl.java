/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.01.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;
import org.hibernate.metamodel.spi.MetamodelImplementor;
import org.hibernate.persister.entity.JoinedSubclassEntityPersister;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import eu.agno3.fileshare.exceptions.AccessDeniedException;
import eu.agno3.fileshare.exceptions.AuthenticationException;
import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.SubjectNotFoundException;
import eu.agno3.fileshare.model.ContainerEntity;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.SecurityLabel;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.SubjectGrant;
import eu.agno3.fileshare.model.TrustLevel;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.query.SubjectQueryResult;
import eu.agno3.fileshare.model.query.UserQueryResult;
import eu.agno3.fileshare.security.AccessControlService;
import eu.agno3.fileshare.service.EntityService;
import eu.agno3.fileshare.service.admin.SubjectService;
import eu.agno3.fileshare.service.admin.SubjectServiceMBean;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.audit.SubjectFileshareAuditBuilder;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.eventlog.AuditContext;
import eu.agno3.runtime.eventlog.AuditStatus;
import eu.agno3.runtime.jmx.JMXSecurityUtil;
import eu.agno3.runtime.jmx.MBean;
import eu.agno3.runtime.security.PermissionMapper;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    SubjectServiceMBean.class, MBean.class
}, property = {
    "objectName=eu.agno3.fileshare:type=SubjectService"
} )
public class SubjectServiceImpl extends SubjectService implements SubjectServiceMBean {

    private static final String MANAGE_ROLES_LIST = "manage:roles:list"; //$NON-NLS-1$
    private static final String MANAGE_ROLES_MODIFY = "manage:roles:modify"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(SubjectServiceImpl.class);

    private DefaultServiceContext ctx;

    private AccessControlService accessControl;
    private Set<PermissionMapper> permissionMappers = new LinkedHashSet<>();

    private EntityService entityService;


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
    protected synchronized void setEntityService ( EntityService es ) {
        this.entityService = es;
    }


    protected synchronized void unsetEntityService ( EntityService es ) {
        if ( this.entityService == es ) {
            this.entityService = null;
        }
    }


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE )
    protected synchronized void setPermissionMapper ( PermissionMapper pm ) {
        this.permissionMappers.add(pm);
    }


    protected synchronized void unsetPermissionMapper ( PermissionMapper pm ) {
        this.permissionMappers.remove(pm);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.SubjectServiceMBean#getSubject(java.util.UUID)
     */
    @Override
    public Subject getSubject ( UUID id ) throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();
            Subject subj = em.find(Subject.class, id);
            if ( JMXSecurityUtil.isManagementCall() || this.accessControl.hasPermission("manage:subjects:list") ) { //$NON-NLS-1$
                if ( subj == null ) {
                    throw new SubjectNotFoundException("Could not find subject with id " + id); //$NON-NLS-1$
                }

                return subj.cloneShallow(true);
            }

            if ( subj instanceof Group && this.accessControl.isMember((Group) subj) ) {
                return subj.cloneShallow(true);
            }
            else if ( subj instanceof User && this.accessControl.getCurrentUser().equals(subj) ) {
                return subj.cloneShallow(true);
            }

            throw new AccessDeniedException();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get subject", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.SubjectServiceMBean#getSubjectInfo(java.util.UUID)
     */
    @Override
    public SubjectQueryResult getSubjectInfo ( UUID id ) throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();
            User currentUser = this.accessControl.getCurrentUser();
            Subject subj = em.find(Subject.class, id);
            if ( JMXSecurityUtil.isManagementCall() || this.accessControl.hasPermission("manage:subjects:list") //$NON-NLS-1$
                    || this.accessControl.hasPermission("subjects:query") || isKnownSubject(subj, currentUser) ) { //$NON-NLS-1$
                if ( subj == null ) {
                    throw new SubjectNotFoundException("Could not find subject with id " + id); //$NON-NLS-1$
                }
                SubjectQueryResult r = SubjectQueryResult.fromSubject(subj);
                TrustLevel trustLevel = this.ctx.getConfigurationProvider().getTrustLevelConfiguration().getTrustLevel(subj);
                if ( trustLevel != null ) {
                    r.setTrustLevel(trustLevel.getId());
                }
                return r;
            }

            throw new AccessDeniedException();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get subject info", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.SubjectServiceMBean#getUserInfo(eu.agno3.runtime.security.principal.UserPrincipal)
     */
    @Override
    public UserQueryResult getUserInfo ( UserPrincipal principal ) throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();
            User currentUser = this.accessControl.getCurrentUser(tx);
            TypedQuery<User> q = em.createQuery(
                "SELECT u FROM User u WHERE u.principal.userName = :principalName AND u.principal.realmName = :realmName", //$NON-NLS-1$
                User.class);
            q.setParameter("principalName", principal.getUserName()); //$NON-NLS-1$
            q.setParameter("realmName", principal.getRealmName()); //$NON-NLS-1$
            q.setMaxResults(1);

            User u = q.getResultList().size() > 0 ? q.getResultList().get(0) : null;

            if ( JMXSecurityUtil.isManagementCall() || this.accessControl.hasPermission("manage:subjects:list") //$NON-NLS-1$
                    || this.accessControl.hasPermission("subjects:query") || isKnownSubject(u, currentUser) ) { //$NON-NLS-1$
                if ( u == null ) {
                    throw new SubjectNotFoundException("Could not find subject with principal " + principal); //$NON-NLS-1$
                }
                UserQueryResult r = UserQueryResult.fromUser(u);
                TrustLevel trustLevel = this.ctx.getConfigurationProvider().getTrustLevelConfiguration().getTrustLevel(u);
                if ( trustLevel != null ) {
                    r.setTrustLevel(trustLevel.getId());
                }
                return r;
            }

            throw new AccessDeniedException();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get user info", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.SubjectServiceMBean#getSubjectRootLabel(java.util.UUID)
     */
    @Override
    public SecurityLabel getSubjectRootLabel ( UUID subjectId ) throws FileshareException {
        if ( !JMXSecurityUtil.isManagementCall() ) {
            this.accessControl.checkPermission("manage:subjects:list"); //$NON-NLS-1$
        }
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();
            Subject persistent = em.find(Subject.class, subjectId);

            if ( persistent == null ) {
                throw new SubjectNotFoundException();
            }

            ContainerEntity root = persistent.getSubjectRoot();

            if ( root == null ) {
                return null;
            }

            return root.getSecurityLabel();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get subject root label", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.admin.SubjectServiceMBean#addRole(java.util.UUID, java.lang.String)
     */
    @Override
    public void addRole ( UUID id, String role ) throws FileshareException {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("ADD_ROLE"); //$NON-NLS-1$
            audit.builder().property("role", role); //$NON-NLS-1$
            try {
                if ( !JMXSecurityUtil.isManagementCall() ) {
                    this.accessControl.checkPermission(MANAGE_ROLES_MODIFY);
                }
                try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                    EntityManager em = tx.getEntityManager();

                    Subject subj = em.find(Subject.class, id);

                    audit.builder().subject(subj);

                    if ( subj == null ) {
                        throw new SubjectNotFoundException("Could not find subject with id " + id); //$NON-NLS-1$
                    }

                    if ( log.isDebugEnabled() ) {
                        log.debug(String.format("Adding role %s to %s", role, subj)); //$NON-NLS-1$
                    }

                    subj.getRoles().add(role);
                    em.persist(subj);
                    em.flush();
                    tx.commit();

                    refreshSubjectRoles(subj);
                }
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to add role", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.SubjectServiceMBean#setRoles(java.util.UUID, java.util.Set)
     */
    @Override
    public void setRoles ( UUID id, Set<String> roles ) throws FileshareException {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("SET_ROLES"); //$NON-NLS-1$
            audit.builder().property("roles", (Serializable) roles); //$NON-NLS-1$
            try {
                if ( !JMXSecurityUtil.isManagementCall() ) {
                    this.accessControl.checkPermission(MANAGE_ROLES_MODIFY);
                }
                try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                    EntityManager em = tx.getEntityManager();

                    Subject subj = em.find(Subject.class, id);
                    audit.builder().subject(subj);

                    if ( subj == null ) {
                        throw new SubjectNotFoundException("Could not find subject with id " + id); //$NON-NLS-1$
                    }

                    if ( log.isDebugEnabled() ) {
                        log.debug(String.format("Setting roles %s to %s", roles, subj)); //$NON-NLS-1$
                    }

                    subj.setRoles(roles);
                    em.persist(subj);
                    em.flush();
                    tx.commit();

                    refreshSubjectRoles(subj);
                }
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to set roles", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.admin.SubjectServiceMBean#removeRole(java.util.UUID, java.lang.String)
     */
    @Override
    public void removeRole ( UUID id, String role ) throws FileshareException {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("REMOVE_ROLE"); //$NON-NLS-1$
            audit.builder().property("role", role); //$NON-NLS-1$
            try {
                if ( !JMXSecurityUtil.isManagementCall() ) {
                    this.accessControl.checkPermission(MANAGE_ROLES_MODIFY);
                }
                try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                    EntityManager em = tx.getEntityManager();
                    Subject subj = em.find(Subject.class, id);

                    audit.builder().subject(subj);

                    if ( subj == null ) {
                        throw new SubjectNotFoundException("Could not find subject with id " + id); //$NON-NLS-1$
                    }

                    if ( log.isDebugEnabled() ) {
                        log.debug(String.format("Removing role %s from %s", role, subj)); //$NON-NLS-1$
                    }

                    subj.getRoles().remove(role);
                    em.persist(subj);
                    em.flush();
                    tx.commit();

                    refreshSubjectRoles(subj);
                }
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to remove role", e); //$NON-NLS-1$
            }
        }

    }


    /**
     * @param subj
     */
    private void refreshSubjectRoles ( Subject subj ) {
        if ( subj instanceof User ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Clear authorization cache of user " + subj); //$NON-NLS-1$
            }
            this.accessControl.clearAuthorizationCaches( ( (User) subj ).getPrincipal());
        }
        else if ( subj instanceof Group ) {
            Set<Subject> refreshedSubjects = new HashSet<>();
            refreshMembersRecursive( ( (Group) subj ).getMembers(), refreshedSubjects);
        }
    }


    /**
     * @param subj
     * @param refreshedSubjects
     */
    private void refreshMembersRecursive ( Collection<Subject> subjs, Set<Subject> refreshedSubjects ) {

        for ( Subject s : subjs ) {

            if ( refreshedSubjects.contains(s) ) {
                continue;
            }

            if ( s instanceof User ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Clear authorization cache of user " + s); //$NON-NLS-1$
                }
                this.accessControl.clearAuthorizationCaches( ( (User) s ).getPrincipal());
                refreshedSubjects.add(s);
            }
            else if ( s instanceof Group ) {
                refreshedSubjects.add(s);
                refreshMembersRecursive( ( (Group) s ).getMembers(), refreshedSubjects);
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.SubjectServiceMBean#setSubjectRootSecurityLabel(java.util.UUID,
     *      java.lang.String)
     */
    @Override
    public void setSubjectRootSecurityLabel ( UUID id, String label ) throws FileshareException {

        if ( !JMXSecurityUtil.isManagementCall() ) {
            this.accessControl.checkPermission("manage:subjects:subjectRootSecurityLabel"); //$NON-NLS-1$
        }

        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();

            Subject persistent = em.find(Subject.class, id);
            if ( persistent == null ) {
                throw new SubjectNotFoundException();
            }

            if ( persistent.getSubjectRoot() == null ) {
                throw new EntityNotFoundException();
            }

            this.entityService.setSecurityLabel(persistent.getSubjectRoot().getEntityKey(), label, true);
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to set subject root label", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.SubjectServiceMBean#setSubjectRootSecurityLabelRecursive(java.util.UUID,
     *      java.lang.String)
     */
    @Override
    public void setSubjectRootSecurityLabelRecursive ( UUID id, String label ) throws FileshareException {
        if ( !JMXSecurityUtil.isManagementCall() ) {
            this.accessControl.checkPermission("manage:subjects:subjectRootSecurityLabel"); //$NON-NLS-1$
        }

        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();

            Subject persistent = em.find(Subject.class, id);
            if ( persistent == null ) {
                throw new SubjectNotFoundException();
            }

            if ( persistent.getSubjectRoot() == null ) {
                throw new EntityNotFoundException();
            }

            this.entityService.setSecurityLabelRecursive(persistent.getSubjectRoot().getEntityKey(), label, true, Collections.EMPTY_SET);
            tx.commit();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to recurisvely set owned security label", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.SubjectServiceMBean#getAvailableRoles()
     */
    @Override
    public Collection<String> getAvailableRoles () throws AccessDeniedException {
        if ( !JMXSecurityUtil.isManagementCall() ) {
            this.accessControl.checkPermission(MANAGE_ROLES_LIST);
        }
        Set<String> roles = new TreeSet<>();
        for ( PermissionMapper mapper : this.permissionMappers ) {
            roles.addAll(mapper.getDefinedRoles());
        }
        return roles;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.admin.SubjectServiceMBean#getEffectiveRoles(java.util.UUID)
     */
    @Override
    public Set<String> getEffectiveRoles ( UUID id ) throws FileshareException {
        if ( !JMXSecurityUtil.isManagementCall() ) {
            this.accessControl.checkPermission(MANAGE_ROLES_LIST);
        }

        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();
            Subject subj = em.find(Subject.class, id);

            if ( subj == null ) {
                throw new SubjectNotFoundException("Could not find subject with id " + id); //$NON-NLS-1$
            }

            Set<String> roles = new HashSet<>();
            Set<Group> handled = new HashSet<>();
            roles.addAll(getMappedRoles(subj));
            roles.addAll(subj.getRoles());
            recursiveAddRoles(subj.getMemberships(), roles, handled);
            return roles;
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get effective roles", e); //$NON-NLS-1$
        }
    }


    private Collection<? extends String> getMappedRoles ( Subject subj ) throws AccessDeniedException {
        if ( !JMXSecurityUtil.isManagementCall() ) {
            this.accessControl.checkPermission(MANAGE_ROLES_LIST);
        }
        if ( ! ( subj instanceof User ) ) {
            return Collections.EMPTY_LIST;
        }
        return this.accessControl.getMappedRoles( ( (User) subj ).getPrincipal());

    }


    /**
     * @param memberships
     * @param roles
     * @param handled
     */
    private void recursiveAddRoles ( Set<Group> groups, Set<String> roles, Set<Group> handled ) {
        for ( Group g : groups ) {
            if ( handled.contains(g) ) {
                continue;
            }
            handled.add(g);
            roles.addAll(g.getRoles());
            recursiveAddRoles(g.getMemberships(), roles, handled);
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.admin.SubjectServiceMBean#querySubjectsExcludingMembers(java.lang.String,
     *      java.util.UUID, int)
     */
    @Override
    public List<SubjectQueryResult> querySubjectsExcludingMembers ( String query, UUID groupId, int limit ) throws FileshareException {
        String realQuery = QueryUtil.escapeQuery(query);

        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();
            Group u = em.find(Group.class, groupId);

            if ( !JMXSecurityUtil.isManagementCall() && !this.accessControl.hasPermission("manage:subjects:list") ) { //$NON-NLS-1$
                if ( !this.accessControl.hasPermission("subjects:query") || !this.accessControl.isMember(u) ) { //$NON-NLS-1$
                    throw new AccessDeniedException();
                }
            }

            String q = "SELECT DISTINCT S.*, U.*, G.*, " //$NON-NLS-1$
                    + "CASE WHEN U.ID IS NOT NULL THEN ?1 WHEN G.ID IS NOT NULL THEN ?2 ELSE -1 END AS clazz_ " //$NON-NLS-1$
                    + "FROM \"subjects\" AS S " //$NON-NLS-1$
                    + "LEFT JOIN \"users\" AS U ON U.ID = S.ID " //$NON-NLS-1$
                    + "LEFT JOIN \"users_details\" AS UD ON UD.USER_ID = U.ID " //$NON-NLS-1$
                    + "LEFT JOIN \"full_name_index\" AS FI ON FI.USER_ID = UD.ID  " //$NON-NLS-1$
                    + "LEFT JOIN \"pref_name_index\" AS PI ON PI.USER_ID = UD.ID  " //$NON-NLS-1$
                    + "LEFT JOIN \"group_members\" AS GM ON GM.MEMBERS_ID = S.ID AND GM.MEMBERSHIPS_ID = ?4 " //$NON-NLS-1$
                    + "LEFT JOIN \"groups\" AS G ON G.ID = S.ID AND NOT G.ID = ?4 " //$NON-NLS-1$
                    + "WHERE ( G.NAME LIKE ?3 OR U.USERNAME LIKE ?3 OR FI.NAME_PART LIKE ?3 OR PI.NAME_PART LIKE ?3 ) AND GM.MEMBERS_ID IS NULL "; //$NON-NLS-1$

            Query cq = em.createNativeQuery(q, Subject.class);
            setSubjectQueryDiscriminators(em, cq);
            cq.setParameter(3, realQuery);
            cq.setParameter(4, u.getId());

            if ( limit >= 0 ) {
                cq.setMaxResults(limit);
            }

            return fromList(cq.getResultList());
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to query subjects", e); //$NON-NLS-1$
        }
    }


    /**
     * Get a list of query results from a list of groups
     * 
     * @param list
     * @return a list of query results from a list of groups
     */
    private List<SubjectQueryResult> fromList ( List<Subject> list ) {
        List<SubjectQueryResult> res = new ArrayList<>();

        for ( Subject g : list ) {
            SubjectQueryResult fromSubject = SubjectQueryResult.fromSubject(g);
            TrustLevel trustLevel = this.ctx.getConfigurationProvider().getTrustLevelConfiguration().getTrustLevel(g);
            if ( trustLevel != null ) {
                fromSubject.setTrustLevel(trustLevel.getId());
            }
            res.add(fromSubject);
        }

        Collections.sort(res, new SubjectQueryResultComparator(this.ctx.getConfigurationProvider().getTrustLevelConfiguration()));
        return res;
    }


    /**
     * @param em
     * @param cq
     */
    private static void setSubjectQueryDiscriminators ( EntityManager em, Query cq ) {
        // this is not pretty and heavily relies on hibernate internals, but there seems no other way to get the
        // proper discriminator value
        MetamodelImplementor mi = (MetamodelImplementor) em.getMetamodel();
        cq.setParameter(1, ( (JoinedSubclassEntityPersister) mi.entityPersister(User.class) ).getDiscriminatorValue());
        cq.setParameter(2, ( (JoinedSubclassEntityPersister) mi.entityPersister(Group.class) ).getDiscriminatorValue());
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws AccessDeniedException
     *
     * @see eu.agno3.fileshare.service.admin.SubjectServiceMBean#querySubjects(java.lang.String, int)
     */
    @Override
    public List<SubjectQueryResult> querySubjects ( String query, int limit ) throws FileshareException {
        String realQuery = QueryUtil.escapeQuery(query);
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();

            if ( !JMXSecurityUtil.isManagementCall() && !this.accessControl.hasPermission("manage:subjects:list") && //$NON-NLS-1$
                    !this.accessControl.hasPermission("subjects:query") ) { //$NON-NLS-1$
                return queryKnownSubjects(query, limit);
            }

            String q = "SELECT DISTINCT S.*, U.*, G.*, " //$NON-NLS-1$
                    + "CASE WHEN U.ID IS NOT NULL THEN ?1 WHEN G.ID IS NOT NULL THEN ?2 ELSE -1 END AS clazz_ " //$NON-NLS-1$
                    + "FROM \"subjects\" AS S " //$NON-NLS-1$
                    + "LEFT JOIN \"users\" AS U ON U.ID = S.ID " //$NON-NLS-1$
                    + "LEFT JOIN \"users_details\" AS UD ON UD.USER_ID = U.ID " //$NON-NLS-1$
                    + "LEFT JOIN \"full_name_index\" AS FI ON FI.USER_ID = UD.ID  " //$NON-NLS-1$
                    + "LEFT JOIN \"pref_name_index\" AS PI ON PI.USER_ID = UD.ID  " //$NON-NLS-1$
                    + "LEFT JOIN \"groups\" AS G ON G.ID = S.ID " //$NON-NLS-1$
                    + "WHERE G.NAME LIKE ?3 OR U.USERNAME LIKE ?3 OR FI.NAME_PART LIKE ?3 OR PI.NAME_PART LIKE ?3 "; //$NON-NLS-1$

            Query cq = em.createNativeQuery(q, Subject.class);
            setSubjectQueryDiscriminators(em, cq);
            cq.setParameter(3, realQuery);

            if ( limit >= 0 ) {
                cq.setMaxResults(limit);
            }

            List<Subject> resultList = cq.getResultList();
            if ( log.isDebugEnabled() ) {
                log.debug(realQuery + " -> found " + resultList); //$NON-NLS-1$
            }
            return fromList(resultList);
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to query subjects", e); //$NON-NLS-1$
        }
    }


    /**
     * @param subj
     * @return
     */
    private static boolean isKnownSubject ( Subject subj, User currentUser ) {

        if ( subj == null ) {
            return false;
        }

        if ( subj.equals(currentUser) ) {
            return true;
        }

        for ( Grant g : currentUser.getGrants() ) {
            if ( g.getEntity().getOwner().equals(subj) ) {
                return true;
            }
        }

        return false;
    }


    /**
     * @param query
     * @param limit
     * @return
     * @throws AuthenticationException
     */
    private List<SubjectQueryResult> queryKnownSubjects ( String query, int limit ) throws FileshareException {
        User currentUser = this.accessControl.getCurrentUser();
        Set<SubjectQueryResult> res = new HashSet<>();
        addGrantSubjects(res, currentUser.getGrants(), query, limit);

        if ( currentUser.getCreator() != null ) {
            TrustLevel trustLevel = this.ctx.getConfigurationProvider().getTrustLevelConfiguration().getTrustLevel(currentUser.getCreator());
            SubjectQueryResult fromSubject = SubjectQueryResult.fromSubject(currentUser.getCreator());
            if ( trustLevel != null ) {
                fromSubject.setTrustLevel(trustLevel.getId());
            }
            res.add(fromSubject);
        }

        // TODO: possibly add other sources, user's group members, addressbooks?
        return new ArrayList<>(res);
    }


    /**
     * Adds subjects to the result that match the query and are either the owner of a grant or have created the grant
     * 
     * @param res
     * @param grants
     * @param query
     * @param limit
     */
    private void addGrantSubjects ( Set<SubjectQueryResult> res, Set<SubjectGrant> grants, String query, int limit ) {
        for ( SubjectGrant g : grants ) {
            if ( res.size() >= limit ) {
                return;
            }

            Subject s = g.getEntity().getOwner();
            TrustLevel trustLevel = this.ctx.getConfigurationProvider().getTrustLevelConfiguration().getTrustLevel(s);
            if ( subjectMatches(s, query) ) {
                SubjectQueryResult fromSubject = SubjectQueryResult.fromSubject(s);
                if ( trustLevel != null ) {
                    fromSubject.setTrustLevel(trustLevel.getId());
                }
                res.add(fromSubject);
            }

            Subject creator = g.getCreator();
            if ( subjectMatches(creator, query) ) {
                SubjectQueryResult fromSubject = SubjectQueryResult.fromSubject(creator);
                if ( trustLevel != null ) {
                    fromSubject.setTrustLevel(trustLevel.getId());
                }
                res.add(fromSubject);
            }
        }
    }


    /**
     * @param s
     * @param query
     * @return
     */
    private static boolean subjectMatches ( Subject s, String query ) {
        if ( s instanceof User ) {
            return userMatches(query, (User) s);
        }
        else if ( s instanceof Group ) {
            Group g = (Group) s;
            return g.getName().contains(query);
        }

        return false;
    }


    /**
     * @param query
     * @param u
     * @return
     */
    private static boolean userMatches ( String query, User u ) {
        if ( u.getUserDetails() != null ) {
            if ( u.getUserDetails().getPreferredName() != null && u.getUserDetails().getPreferredName().contains(query) ) {
                return true;
            }
            if ( u.getUserDetails().getFullName() != null && u.getUserDetails().getFullName().contains(query) ) {
                return true;
            }
        }
        return u.getPrincipal().getUserName().contains(query);
    }

}
