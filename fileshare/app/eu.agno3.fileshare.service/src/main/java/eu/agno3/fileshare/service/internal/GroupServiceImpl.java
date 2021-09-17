/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.exceptions.AccessDeniedException;
import eu.agno3.fileshare.exceptions.AuthenticationException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.GroupCyclicException;
import eu.agno3.fileshare.exceptions.GroupNameConflictException;
import eu.agno3.fileshare.exceptions.GroupNameInvalidException;
import eu.agno3.fileshare.exceptions.GroupNotFoundException;
import eu.agno3.fileshare.exceptions.SubjectNotFoundException;
import eu.agno3.fileshare.exceptions.UserNotFoundException;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.VirtualGroup;
import eu.agno3.fileshare.model.query.GroupQueryResult;
import eu.agno3.fileshare.security.AccessControlService;
import eu.agno3.fileshare.service.admin.GroupService;
import eu.agno3.fileshare.service.admin.GroupServiceMBean;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.GroupServiceInternal;
import eu.agno3.fileshare.service.api.internal.QuotaServiceInternal;
import eu.agno3.fileshare.service.api.internal.RecursiveModificationTimeTracker;
import eu.agno3.fileshare.service.api.internal.VFSServiceInternal;
import eu.agno3.fileshare.service.audit.MultiSubjectFileshareAuditBuilder;
import eu.agno3.fileshare.service.audit.SubjectFileshareAuditBuilder;
import eu.agno3.fileshare.service.util.ServiceUtil;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.eventlog.AuditContext;
import eu.agno3.runtime.eventlog.AuditStatus;
import eu.agno3.runtime.jmx.JMXSecurityUtil;
import eu.agno3.runtime.jmx.MBean;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    GroupServiceInternal.class, GroupServiceMBean.class, MBean.class
}, property = {
    "objectName=eu.agno3.fileshare:type=GroupService"
} )
public class GroupServiceImpl extends GroupService implements GroupServiceMBean, GroupServiceInternal {

    /**
     * 
     */
    private static final String LIST_PERM = "manage:subjects:list"; //$NON-NLS-1$
    private static final String CREATE_PERM = "manage:groups:create"; //$NON-NLS-1$
    private static final String DELETE_PERM = "manage:groups:delete"; //$NON-NLS-1$
    private static final String ADD_MEMBER_PERM = "manage:groups:addMember"; //$NON-NLS-1$
    private static final String REMOVE_MEMBER_PERM = "manage:groups:removeMember"; //$NON-NLS-1$
    private static final String MANAGE_GROUP_NOTIFY_SETTINGS = "manage:groups:changeNotifySettings"; //$NON-NLS-1$
    private static final String QUERY_PERM = "groups:query"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(GroupServiceImpl.class);

    private DefaultServiceContext ctx;

    private AccessControlService accessControl;
    private QuotaServiceInternal quotaService;
    private VFSServiceInternal vfs;
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
    protected synchronized void setAccessControlService ( AccessControlService acs ) {
        this.accessControl = acs;
    }


    protected synchronized void unsetAccessControlService ( AccessControlService acs ) {
        if ( this.accessControl == acs ) {
            this.accessControl = null;
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
    protected synchronized void setVFSService ( VFSServiceInternal vs ) {
        this.vfs = vs;
    }


    protected synchronized void unsetVFSService ( VFSServiceInternal vs ) {
        if ( this.vfs == vs ) {
            this.vfs = null;
        }
    }


    @Reference
    protected synchronized void setRecursiveModTracker ( RecursiveModificationTimeTracker rmtt ) {
        this.modTracker = rmtt;
    }


    protected synchronized void unsetRecursiveModTracker ( RecursiveModificationTimeTracker rmtt ) {
        if ( this.modTracker == rmtt ) {
            this.modTracker = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#getGroup(java.util.UUID)
     */
    @Override
    public Group getGroup ( UUID id ) throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();

            Group g = em.find(Group.class, id);

            if ( !JMXSecurityUtil.isManagementCall() && !this.accessControl.hasPermission(LIST_PERM) && !this.accessControl.isMember(g) ) {
                throw new AccessDeniedException();
            }

            if ( g == null ) {
                throw new GroupNotFoundException("Could not find group with id " + id); //$NON-NLS-1$
            }

            return g.cloneShallow(true);
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get group", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#getGroupInfo(java.lang.String)
     */
    @Override
    public GroupQueryResult getGroupInfo ( String groupName ) throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();

            TypedQuery<Group> q = em.createQuery("SELECT g FROM Group g WHERE g.name = :groupName", Group.class); //$NON-NLS-1$
            q.setParameter("groupName", groupName); //$NON-NLS-1$
            q.setMaxResults(1);
            Group g = q.getResultList().size() > 0 ? q.getResultList().get(0) : null;

            if ( !JMXSecurityUtil.isManagementCall() && !this.accessControl.hasPermission(LIST_PERM) && !this.accessControl.isMember(g) ) {
                throw new AccessDeniedException();
            }

            if ( g == null ) {
                throw new GroupNotFoundException("Could not find group with name " + groupName); //$NON-NLS-1$
            }

            return GroupQueryResult.fromGroup(g);
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get group info", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#getGroupCount()
     */
    @Override
    public long getGroupCount () throws FileshareException {
        if ( !JMXSecurityUtil.isManagementCall() ) {
            this.accessControl.checkPermission(LIST_PERM);
        }
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Long> q = cb.createQuery(Long.class);
            Root<Group> group = q.from(Group.class);
            q.select(cb.count(group.get("id"))); //$NON-NLS-1$
            return em.createQuery(q).getSingleResult();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get group count", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#listGroups(int, int)
     */
    @Override
    public List<Group> listGroups ( int off, int limit ) throws FileshareException {
        if ( !JMXSecurityUtil.isManagementCall() ) {
            this.accessControl.checkPermission(LIST_PERM);
        }
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Group> q = cb.createQuery(Group.class);
            q.from(Group.class);
            TypedQuery<Group> cq = em.createQuery(q);
            if ( off >= 0 ) {
                cq.setFirstResult(off);
            }
            if ( limit >= 0 ) {
                cq.setMaxResults(limit);
            }

            List<Group> res = new ArrayList<>();
            for ( Group g : cq.getResultList() ) {
                res.add(g.cloneShallow(false));
            }
            return res;
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to list groups", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#queryGroups(java.lang.String, int)
     */
    @Override
    public List<GroupQueryResult> queryGroups ( String query, int limit ) throws FileshareException {
        String realQuery = QueryUtil.escapeQuery(query);

        if ( !JMXSecurityUtil.isManagementCall() && !this.accessControl.hasPermission(QUERY_PERM) ) {
            return queryUserGroups(query, limit);
        }

        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Group> q = cb.createQuery(Group.class);
            Root<Group> from = q.from(Group.class);
            q.where(cb.like(from.get(em.getMetamodel().entity(Group.class).getSingularAttribute("name", String.class)), realQuery)); //$NON-NLS-1$
            TypedQuery<Group> cq = em.createQuery(q);

            if ( limit >= 0 ) {
                cq.setMaxResults(limit);
            }

            return GroupQueryResult.fromGroupList(cq.getResultList());
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to query groups", e); //$NON-NLS-1$
        }
    }


    /**
     * @param query
     * @param limit
     * @return
     * @throws AuthenticationException
     */
    private List<GroupQueryResult> queryUserGroups ( String query, int limit ) throws AuthenticationException {
        List<GroupQueryResult> r = new ArrayList<>();
        for ( Group g : this.accessControl.getCurrentUserGroupClosure() ) {
            if ( r.size() >= limit ) {
                break;
            }

            if ( g.getName().contains(query) ) {
                r.add(GroupQueryResult.fromGroup(g));
            }
        }
        return r;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#queryGroupsExcludingUserGroups(java.lang.String,
     *      java.util.UUID, int)
     */
    @Override
    public List<GroupQueryResult> queryGroupsExcludingUserGroups ( String query, UUID userId, int limit ) throws FileshareException {
        String realQuery = QueryUtil.escapeQuery(query);
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();
            User u = em.find(User.class, userId);
            if ( u == null ) {
                throw new UserNotFoundException();
            }

            if ( !JMXSecurityUtil.isManagementCall() && !this.accessControl.hasPermission(LIST_PERM) ) {
                if ( !this.accessControl.hasPermission(QUERY_PERM) || !u.equals(this.accessControl.getCurrentUser()) ) {
                    throw new AccessDeniedException();
                }
            }

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Group> q = cb.createQuery(Group.class);
            Root<Group> from = q.from(Group.class);
            EntityType<Group> group = em.getMetamodel().entity(Group.class);

            List<Predicate> preds = new ArrayList<>();
            preds.add(cb.like(from.get(group.getSingularAttribute("name", String.class)), realQuery)); //$NON-NLS-1$

            if ( !u.getMemberships().isEmpty() ) {
                preds.add(cb.not(from.get(group.getSingularAttribute("id", UUID.class)).in(QueryUtil.toUUIDs(u.getMemberships())))); //$NON-NLS-1$
            }

            q.where(preds.toArray(new Predicate[] {}));

            TypedQuery<Group> cq = em.createQuery(q);
            if ( limit >= 0 ) {
                cq.setMaxResults(limit);
            }

            return GroupQueryResult.fromGroupList(cq.getResultList());
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to query groups", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#createGroup(eu.agno3.fileshare.model.Group, boolean)
     */
    @Override
    public Group createGroup ( Group g, boolean createRoot ) throws FileshareException {

        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("CREATE_GROUP"); //$NON-NLS-1$
            audit.builder().subject(g);
            try {
                if ( !JMXSecurityUtil.isManagementCall() ) {
                    this.accessControl.checkPermission(CREATE_PERM);
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

            try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                EntityManager em = tx.getEntityManager();

                checkGroupName(g);
                checkGroupNameConflict(g, em);

                g.setId(UUID.randomUUID());
                g.setCreator(this.accessControl.getCurrentUser());

                if ( createRoot ) {
                    g.setSubjectRoot(ServiceUtil.createSubjectRoot(
                        tx,
                        this.modTracker,
                        g,
                        this.accessControl.getCurrentUser(),
                        this.ctx.getConfigurationProvider().getSecurityPolicyConfiguration()));
                }

                DateTime now = DateTime.now();
                g.setCreated(now);
                g.setLastModified(now);
                em.persist(g);
                audit.builder().subject(g);
                em.flush();
                em.refresh(g);

                tx.commit();
                return g.cloneShallow(true);
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to create group", e); //$NON-NLS-1$
            }

        }
    }


    /**
     * @param g
     * @throws GroupNameInvalidException
     */
    private static void checkGroupName ( Group g ) throws GroupNameInvalidException {
        if ( StringUtils.isBlank(g.getName()) ) {
            throw new GroupNameInvalidException(g.getName(), "Group name may not be empty"); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#deleteGroups(java.util.List)
     */
    @Override
    public void deleteGroups ( List<UUID> ids ) throws FileshareException {

        try ( AuditContext<MultiSubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(MultiSubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("CREATE_GROUP_OUTER"); //$NON-NLS-1$

            try {
                if ( !JMXSecurityUtil.isManagementCall() ) {
                    this.accessControl.checkPermission(DELETE_PERM);
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

            try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                EntityManager em = tx.getEntityManager();

                for ( UUID groupId : ids ) {
                    Group g = em.find(Group.class, groupId);
                    audit.builder().subject(g);

                    if ( g == null ) {
                        throw new GroupNotFoundException("Could not find group with id " + groupId); //$NON-NLS-1$
                    }

                    deleteGroup(tx, g);
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
                throw new FileshareException("Failed to delete groups", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#getMembers(java.util.UUID)
     */
    @Override
    public List<Subject> getMembers ( UUID groupId ) throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();
            Group g = em.find(Group.class, groupId);

            if ( !JMXSecurityUtil.isManagementCall() && !this.accessControl.hasPermission(LIST_PERM) && !this.accessControl.isMember(g) ) {
                throw new AccessDeniedException();
            }

            if ( g == null ) {
                throw new GroupNotFoundException("Could not find group with id " + groupId); //$NON-NLS-1$
            }

            List<Subject> res = new ArrayList<>();
            for ( Subject member : g.getMembers() ) {
                res.add(member.cloneShallow());
            }
            tx.commit();
            return res;
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to enumerate members", e); //$NON-NLS-1$
        }
    }


    @Override
    public void deleteGroup ( EntityTransactionContext tx, Group g ) {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            EntityManager em = tx.getEntityManager();
            audit.builder().access(this.accessControl).action("DELETE_GROUP"); //$NON-NLS-1$
            try {
                audit.builder().subject(g);
                DateTime now = DateTime.now();
                for ( Group memberOf : g.getMemberships() ) {
                    memberOf.getMembers().remove(g);
                    memberOf.setLastModified(now);
                    em.persist(memberOf);
                }
                g.getMemberships().clear();
                em.remove(g);
            }
            catch ( RuntimeException e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw e;
            }
        }
    }


    /**
     * @param g
     * @param em
     * @throws GroupNameConflictException
     */
    private static void checkGroupNameConflict ( Group g, EntityManager em ) throws GroupNameConflictException {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Group> cq = cb.createQuery(Group.class);
        Root<Group> from = cq.from(Group.class);
        cq.where(cb.equal(from.get(em.getMetamodel().entity(Group.class).getSingularAttribute("name", String.class)), g.getName())); //$NON-NLS-1$
        if ( em.createQuery(cq).getResultList().size() > 0 ) {
            throw new GroupNameConflictException(g.getName(), "Group does already exist"); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#addToGroup(java.util.UUID, java.util.UUID)
     */
    @Override
    public void addToGroup ( UUID userId, UUID groupId ) throws FileshareException {
        this.addToGroups(userId, new HashSet<>(Arrays.asList(groupId)));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#addMembers(java.util.UUID, java.util.List)
     */
    @Override
    public void addMembers ( UUID groupId, List<UUID> subjectIds ) throws FileshareException {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("ADD_MEMBERS"); //$NON-NLS-1$
            try {
                if ( !JMXSecurityUtil.isManagementCall() ) {
                    this.accessControl.checkPermission(ADD_MEMBER_PERM);
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

            try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                EntityManager em = tx.getEntityManager();

                Group g = em.find(Group.class, groupId);
                Set<Group> currentGroupClosure = getGroupMembershipClosure(g);

                audit.builder().subject(g);
                audit.builder().property("memberIds", (Serializable) subjectIds); //$NON-NLS-1$
                LinkedList<String> memberTypes = new LinkedList<>();
                LinkedList<String> memberNames = new LinkedList<>();

                if ( g == null ) {
                    throw new GroupNotFoundException();
                }
                DateTime now = DateTime.now();

                for ( UUID subjectId : subjectIds ) {
                    addGroupMember(em, g, currentGroupClosure, now, subjectId, memberNames, memberTypes);
                }

                audit.builder().property("memberTypes", memberTypes); //$NON-NLS-1$
                audit.builder().property("memberNames", memberNames); //$NON-NLS-1$

                g.setLastModified(now);
                em.persist(g);
                em.flush();
                tx.commit();
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to add members", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param em
     * @param g
     * @param currentGroupClosure
     * @param now
     * @param subjectId
     * @param memberTypes
     * @param memberNames
     * @throws SubjectNotFoundException
     * @throws GroupCyclicException
     */
    private static void addGroupMember ( EntityManager em, Group g, Set<Group> currentGroupClosure, DateTime now, UUID subjectId,
            LinkedList<String> memberNames, LinkedList<String> memberTypes ) throws SubjectNotFoundException, GroupCyclicException {
        Subject s = em.find(Subject.class, subjectId);
        if ( s == null ) {
            throw new SubjectNotFoundException();
        }

        if ( s instanceof Group ) {
            Group addGroup = (Group) s;
            if ( currentGroupClosure.contains(addGroup) ) {
                throw new GroupCyclicException("Cyclic group structure"); //$NON-NLS-1$
            }
            memberTypes.add("group"); //$NON-NLS-1$
            memberNames.add(addGroup.getName());
        }
        else if ( s instanceof User ) {
            memberTypes.add("user"); //$NON-NLS-1$
            memberNames.add( ( (User) s ).getPrincipal().toString());
        }
        else {
            throw new IllegalArgumentException();
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Adding %s to %s", g, s)); //$NON-NLS-1$
        }

        s.setLastModified(now);
        g.getMembers().add(s);
        s.getMemberships().add(g);
        em.persist(s);
    }


    /**
     * @param g
     * @return
     */
    private Set<Group> getGroupMembershipClosure ( Group g ) {
        Set<Group> closure = new HashSet<>();
        closure.add(g);
        collectClosure(g.getMemberships(), closure);
        return closure;
    }


    /**
     * @param memberships
     * @param closure
     */
    private void collectClosure ( Set<Group> memberships, Set<Group> closure ) {
        if ( memberships == null || memberships.isEmpty() ) {
            return;
        }

        for ( Group memberOf : memberships ) {
            if ( closure.contains(memberOf) ) {
                continue;
            }
            closure.add(memberOf);
            collectClosure(memberOf.getMemberships(), closure);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#removeMembers(java.util.UUID, java.util.List)
     */
    @Override
    public void removeMembers ( UUID groupId, List<UUID> subjectIds ) throws FileshareException {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("REMOVE_MEMBERS"); //$NON-NLS-1$

            try {
                if ( !JMXSecurityUtil.isManagementCall() ) {
                    this.accessControl.checkPermission(REMOVE_MEMBER_PERM);
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

            try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                EntityManager em = tx.getEntityManager();

                Group g = em.find(Group.class, groupId);

                audit.builder().subject(g);
                audit.builder().property("memberIds", (Serializable) subjectIds); //$NON-NLS-1$
                LinkedList<String> memberTypes = new LinkedList<>();
                LinkedList<String> memberNames = new LinkedList<>();

                if ( g == null ) {
                    throw new GroupNotFoundException();
                }

                DateTime now = DateTime.now();
                for ( UUID subjectId : subjectIds ) {
                    removeGroupMember(em, g, now, subjectId, memberTypes, memberNames);
                }

                audit.builder().property("memberTypes", memberTypes); //$NON-NLS-1$
                audit.builder().property("memberNames", memberNames); //$NON-NLS-1$

                g.setLastModified(now);
                em.persist(g);
                em.flush();
                tx.commit();
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to remove members", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param em
     * @param g
     * @param now
     * @param subjectId
     * @param memberNames
     * @param memberTypes
     * @throws SubjectNotFoundException
     */
    private static void removeGroupMember ( EntityManager em, Group g, DateTime now, UUID subjectId, LinkedList<String> memberTypes,
            LinkedList<String> memberNames ) throws SubjectNotFoundException {
        Subject s = em.find(Subject.class, subjectId);
        if ( s == null ) {
            throw new SubjectNotFoundException();
        }

        if ( s instanceof Group ) {
            memberTypes.add("group"); //$NON-NLS-1$
            memberNames.add( ( (Group) s ).getName());
        }
        else if ( s instanceof User ) {
            memberTypes.add("user"); //$NON-NLS-1$
            memberNames.add( ( (User) s ).getPrincipal().toString());
        }
        else {
            throw new IllegalArgumentException();
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Removing %s from %s", g, s)); //$NON-NLS-1$
        }

        g.getMembers().remove(s);
        s.getMemberships().remove(g);
        s.setLastModified(now);
        em.persist(s);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#addToGroups(java.util.UUID, java.util.Set)
     */
    @Override
    public void addToGroups ( UUID userId, Set<UUID> groupIds ) throws FileshareException {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("ADD_TO_GROUPS"); //$NON-NLS-1$
            try {
                if ( !JMXSecurityUtil.isManagementCall() ) {
                    this.accessControl.checkPermission(ADD_MEMBER_PERM);
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

            try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                EntityManager em = tx.getEntityManager();

                User user = em.find(User.class, userId);

                audit.builder().subject(user);
                audit.builder().property("groupIds", new LinkedList<>(groupIds)); //$NON-NLS-1$
                LinkedList<String> groupNames = new LinkedList<>();

                if ( user == null ) {
                    throw new UserNotFoundException();
                }
                DateTime now = DateTime.now();
                for ( UUID groupId : groupIds ) {
                    Group group = em.find(Group.class, groupId);
                    if ( group == null ) {
                        throw new GroupNotFoundException();
                    }

                    groupNames.add(group.getName());

                    if ( log.isDebugEnabled() ) {
                        log.debug(String.format("Adding %s to %s", user, group)); //$NON-NLS-1$
                    }

                    user.getMemberships().add(group);
                    group.getMembers().add(user);
                    group.setLastModified(now);
                    em.persist(group);
                }

                audit.builder().property("groupNames", groupNames); //$NON-NLS-1$

                user.setLastModified(now);

                em.persist(user);
                em.flush();
                tx.commit();
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to add user membership", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#removeFromGroup(java.util.UUID, java.util.UUID)
     */
    @Override
    public void removeFromGroup ( UUID userId, UUID groupId ) throws FileshareException {
        this.removeFromGroups(userId, new HashSet<>(Arrays.asList(groupId)));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#removeFromGroups(java.util.UUID, java.util.Set)
     */
    @Override
    public void removeFromGroups ( UUID userId, Set<UUID> groupIds ) throws FileshareException {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("REMOVE_FROM_GROUPS"); //$NON-NLS-1$
            try {
                if ( !JMXSecurityUtil.isManagementCall() ) {
                    this.accessControl.checkPermission(REMOVE_MEMBER_PERM);
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

            try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                EntityManager em = tx.getEntityManager();

                User user = em.find(User.class, userId);

                audit.builder().subject(user);
                audit.builder().property("groupIds", new LinkedList<>(groupIds)); //$NON-NLS-1$
                LinkedList<String> groupNames = new LinkedList<>();

                if ( user == null ) {
                    throw new UserNotFoundException();
                }
                DateTime now = DateTime.now();

                for ( UUID groupId : groupIds ) {
                    Group group = em.find(Group.class, groupId);

                    if ( group == null ) {
                        throw new GroupNotFoundException();
                    }

                    groupNames.add(group.getName());

                    if ( log.isDebugEnabled() ) {
                        log.debug(String.format("Removing %s from %s", user, group)); //$NON-NLS-1$
                    }

                    user.getMemberships().remove(group);
                    group.setLastModified(now);
                    em.persist(group);
                    group.getMembers().remove(user);
                }

                audit.builder().property("groupNames", groupNames); //$NON-NLS-1$

                user.setLastModified(now);
                em.persist(user);
                em.flush();
                tx.commit();
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to remove user membership", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#setNotificationDisabled(java.util.UUID, boolean)
     */
    @Override
    public void setNotificationDisabled ( UUID id, boolean disableNotifications ) throws FileshareException {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action(disableNotifications ? "SET_NOTIFICATION_DISABLE" : //$NON-NLS-1$
                    "UNSET_NOTIFICATION_DISABLE"); //$NON-NLS-1$

            try {
                if ( !JMXSecurityUtil.isManagementCall() ) {
                    this.accessControl.checkPermission(MANAGE_GROUP_NOTIFY_SETTINGS);
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
            try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                EntityManager em = tx.getEntityManager();
                Group g = em.find(Group.class, id);

                audit.builder().subject(g);
                if ( g == null ) {
                    throw new GroupNotFoundException();
                }

                g.setDisableNotifications(disableNotifications);

                em.flush();
                tx.commit();
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to set notification status", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#setNotificationOverride(java.util.UUID, java.lang.String)
     */
    @Override
    public void setNotificationOverride ( UUID id, String overrideAddress ) throws FileshareException {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("SET_NOTIFICATION_OVERRIDE"); //$NON-NLS-1$
            audit.builder().property("overrideAddress", overrideAddress); //$NON-NLS-1$

            try {
                if ( !JMXSecurityUtil.isManagementCall() ) {
                    this.accessControl.checkPermission(MANAGE_GROUP_NOTIFY_SETTINGS);
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

            try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                EntityManager em = tx.getEntityManager();
                Group g = em.find(Group.class, id);

                audit.builder().subject(g);

                if ( g == null ) {
                    throw new GroupNotFoundException();
                }

                g.setNotificationOverrideAddress(overrideAddress);

                em.flush();
                tx.commit();
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to set notification override", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#setGroupLocale(java.util.UUID, java.util.Locale)
     */
    @Override
    public void setGroupLocale ( UUID id, Locale groupLocale ) throws FileshareException {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("SET_GROUP_LOCALE"); //$NON-NLS-1$
            audit.builder().property("groupLocale", groupLocale.toString()); //$NON-NLS-1$

            try {
                if ( !JMXSecurityUtil.isManagementCall() ) {
                    this.accessControl.checkPermission(MANAGE_GROUP_NOTIFY_SETTINGS);
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

            try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                EntityManager em = tx.getEntityManager();
                Group g = em.find(Group.class, id);

                audit.builder().subject(g);

                if ( g == null ) {
                    throw new GroupNotFoundException();
                }

                g.setGroupLocale(groupLocale);

                em.flush();
                tx.commit();
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to set group locale", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#updateGroupQuota(java.util.UUID, java.lang.Long)
     */
    @Override
    public void updateGroupQuota ( UUID groupId, Long quota ) throws FileshareException {
        try ( AuditContext<SubjectFileshareAuditBuilder> audit = this.ctx.getEventLogger().audit(SubjectFileshareAuditBuilder.class) ) {
            audit.builder().access(this.accessControl).action("SET_GROUP_QUOTA"); //$NON-NLS-1$

            try {
                if ( !JMXSecurityUtil.isManagementCall() ) {
                    this.accessControl.checkPermission("manage:groups:changeQuota"); //$NON-NLS-1$
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

            try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
                EntityManager em = tx.getEntityManager();

                Group g = em.find(Group.class, groupId);

                audit.builder().subject(g);
                if ( g == null ) {
                    throw new UserNotFoundException("Could not find group with id " + groupId); //$NON-NLS-1$
                }

                boolean previouslyDisabled = g.getQuota() == null && quota != null;

                g.setQuota(quota);
                em.persist(g);
                em.flush();

                if ( previouslyDisabled ) {
                    this.quotaService.updateDirectorySizes(em, g);
                }

                tx.commit();
            }
            catch ( FileshareException e ) {
                audit.builder().fail(e);
                throw e;
            }
            catch ( Exception e ) {
                audit.builder().fail(AuditStatus.INTERNAL);
                throw new FileshareException("Failed to set group quota", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#getGroupsLastModified()
     */
    @Override
    public DateTime getGroupsLastModified () {
        if ( !this.accessControl.isUserAuthenticated() ) {
            return null;
        }

        try {
            DateTime lastMod = this.accessControl.getCurrentUser().getLastModified();
            for ( Group g : this.accessControl.getCurrentUserGroupClosure() ) {

                if ( g.getLastModified() == null ) {
                    lastMod = g.getCreated();
                }

                if ( lastMod == null || lastMod.isBefore(g.getLastModified()) ) {
                    lastMod = g.getLastModified();
                }
            }
            return lastMod;
        }
        catch (
            AuthenticationException |
            UserNotFoundException e ) {
            log.warn("Failed to get group memberships", e); //$NON-NLS-1$
            return DateTime.now();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.admin.GroupServiceMBean#getGroupsRecursiveLastModified()
     */
    @Override
    public DateTime getGroupsRecursiveLastModified () {
        if ( !this.accessControl.isUserAuthenticated() ) {
            return null;
        }

        try {
            DateTime lastMod = this.accessControl.getCurrentUser().getLastModified();
            for ( Group g : this.accessControl.getCurrentUserGroupClosure() ) {
                if ( g.getLastModified() == null ) {
                    lastMod = g.getCreated();
                }

                if ( lastMod == null || ( g.getLastModified() != null && lastMod.isBefore(g.getLastModified()) ) ) {
                    lastMod = g.getLastModified();
                }

                Long contentLastMod = null;
                if ( g instanceof VirtualGroup ) {
                    try ( VFSContext v = this.vfs.getVFS( ( (VirtualGroup) g ).getVfs()).begin(true) ) {
                        contentLastMod = this.modTracker.getRecursiveLastModifiedTime(v, v.getRoot());
                    }
                }
                else if ( g.getSubjectRoot() != null ) {
                    try ( VFSContext v = this.vfs.getNative().begin(true) ) {
                        contentLastMod = this.modTracker.getRecursiveLastModifiedTime(v, g.getSubjectRoot());
                    }
                }

                if ( lastMod == null || ( contentLastMod != null && lastMod.isBefore(contentLastMod) ) ) {
                    lastMod = new DateTime(contentLastMod);
                }
            }
            return lastMod;
        }
        catch ( FileshareException e ) {
            log.warn("Failed to get group last modififed", e); //$NON-NLS-1$
            return DateTime.now();
        }
    }
}
