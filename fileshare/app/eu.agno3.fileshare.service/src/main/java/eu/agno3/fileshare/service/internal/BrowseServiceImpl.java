/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.util.WebUtils;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.exceptions.AccessDeniedException;
import eu.agno3.fileshare.exceptions.AuthenticationException;
import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.GrantAuthenticationRequiredException;
import eu.agno3.fileshare.exceptions.InvalidQueryException;
import eu.agno3.fileshare.exceptions.PolicyNotFulfilledException;
import eu.agno3.fileshare.exceptions.SubjectNotFoundException;
import eu.agno3.fileshare.exceptions.UserNotFoundException;
import eu.agno3.fileshare.model.ContainerEntity;
import eu.agno3.fileshare.model.ContentEntity;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.FileEntity;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.MailGrant;
import eu.agno3.fileshare.model.NativeEntityKey;
import eu.agno3.fileshare.model.PolicyViolation;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.SubjectGrant;
import eu.agno3.fileshare.model.TokenGrant;
import eu.agno3.fileshare.model.TrustLevel;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.model.VirtualGroup;
import eu.agno3.fileshare.model.query.CollectionResult;
import eu.agno3.fileshare.model.query.MailPeerInfo;
import eu.agno3.fileshare.model.query.PeerInfo;
import eu.agno3.fileshare.model.query.SearchResult;
import eu.agno3.fileshare.model.query.SubjectPeerInfo;
import eu.agno3.fileshare.model.query.SubjectQueryResult;
import eu.agno3.fileshare.model.query.TokenPeerInfo;
import eu.agno3.fileshare.security.AccessControlService;
import eu.agno3.fileshare.service.BrowseService;
import eu.agno3.fileshare.service.api.internal.BrowseServiceInternal;
import eu.agno3.fileshare.service.api.internal.DefaultServiceContext;
import eu.agno3.fileshare.service.api.internal.PolicyEvaluator;
import eu.agno3.fileshare.service.api.internal.RecursiveModificationTimeTracker;
import eu.agno3.fileshare.service.api.internal.UserServiceInternal;
import eu.agno3.fileshare.service.api.internal.VFSServiceInternal;
import eu.agno3.fileshare.service.audit.SingleEntityFileshareAuditBuilder;
import eu.agno3.fileshare.service.util.ServiceUtil;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.eventlog.AuditContext;
import eu.agno3.runtime.util.matching.PatternUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    BrowseServiceInternal.class, BrowseService.class
} )
public class BrowseServiceImpl implements BrowseServiceInternal {

    private static final Logger log = Logger.getLogger(BrowseServiceImpl.class);

    private static final int MAX_SEARCH_LIMIT = 60;

    private DefaultServiceContext ctx;
    private UserServiceInternal userService;

    private AccessControlService accessControl;
    private PolicyEvaluator policyEvaluator;

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
    protected synchronized void setUserService ( UserServiceInternal us ) {
        this.userService = us;
    }


    protected synchronized void unsetUserService ( UserServiceInternal us ) {
        if ( this.userService == us ) {
            this.userService = null;
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
    protected synchronized void setPolicyEvaluator ( PolicyEvaluator pe ) {
        this.policyEvaluator = pe;
    }


    protected synchronized void unsetPolicyEvaluator ( PolicyEvaluator pe ) {
        if ( this.policyEvaluator == pe ) {
            this.policyEvaluator = null;
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


    @Override
    public ContainerEntity getUserRoot () throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            User u = this.userService.getCurrentUser(tx);
            if ( u != null ) {
                if ( u.getNoSubjectRoot() ) {
                    log.debug("User root usage is disabled"); //$NON-NLS-1$
                    return null;
                }

                try ( VFSContext v = this.vfs.getNative().begin(tx) ) {
                    this.policyEvaluator.checkPolicy(v, u.getSubjectRoot(), WebUtils.getHttpRequest(SecurityUtils.getSubject()));
                    return u.getSubjectRoot().cloneShallow(true);
                }
            }
            return null;
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get user root", e); //$NON-NLS-1$
        }
    }


    @Override
    public CollectionResult<VFSEntity> getChildren ( EntityKey entityId ) throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            return getChildrenInternal(tx, entityId, true);
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get children", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.BrowseServiceInternal#getChildrenInternal(eu.agno3.fileshare.model.EntityKey,
     *      boolean)
     */
    @Override
    public CollectionResult<VFSEntity> getChildrenInternal ( EntityKey entityId, boolean clone )
            throws FileshareException, AccessDeniedException, GrantAuthenticationRequiredException, PolicyNotFulfilledException,
            AuthenticationException, UserNotFoundException, EntityNotFoundException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            return getChildrenInternal(tx, entityId, clone);
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get children", e); //$NON-NLS-1$
        }
    }


    private CollectionResult<VFSEntity> getChildrenInternal ( EntityTransactionContext tx, EntityKey entityId, boolean clone )
            throws FileshareException, AccessDeniedException, GrantAuthenticationRequiredException, PolicyNotFulfilledException,
            AuthenticationException, UserNotFoundException, EntityNotFoundException {
        try ( VFSContext v = this.vfs.getVFS(entityId).begin(tx) ) {
            VFSContainerEntity persistent = v.load(entityId, ContainerEntity.class);
            this.accessControl.checkAnyAccess(v, persistent, GrantPermission.values());
            HttpServletRequest httpRequest = WebUtils.getHttpRequest(SecurityUtils.getSubject());
            this.policyEvaluator.checkPolicy(v, persistent, httpRequest);
            User currentUser = this.accessControl.getCurrentUserCachable();
            Grant g = this.accessControl.getTokenAuthGrant(v, persistent);

            if ( this.accessControl.hasAccess(v, persistent, GrantPermission.BROWSE) ) {
                return filterPolicy(v, v.getChildren(persistent), httpRequest, clone);
            }

            // if the BROWSE permission is not present only the entries that were created by the user are visible
            List<VFSEntity> res = new ArrayList<>();

            for ( VFSEntity elem : v.getChildren(persistent) ) {
                if ( this.accessControl.isUserCreatorWithPerm(v, elem, currentUser, g) ) {
                    res.add(elem);
                }
            }
            return filterPolicy(v, res, httpRequest, clone);
        }
    }


    /**
     * @param elements
     * @return
     */
    private CollectionResult<VFSEntity> filterPolicy ( VFSContext v, Collection<? extends VFSEntity> elements, HttpServletRequest httpRequest,
            boolean clone ) {
        List<VFSEntity> res = new ArrayList<>();
        Set<PolicyViolation> violations = new HashSet<>();
        int numHiddenPolicy = 0;
        for ( VFSEntity e : elements ) {
            PolicyViolation violation = this.policyEvaluator.isPolicyFulfilled(v, e, httpRequest);
            if ( violation != null ) {
                numHiddenPolicy++;
                violations.add(violation);
                continue;
            }
            if ( clone ) {
                res.add(e.cloneShallow());
            }
            else {
                res.add(e);
            }
        }
        return new CollectionResult<>(res, numHiddenPolicy, violations);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.BrowseService#getRelativePath(eu.agno3.fileshare.model.EntityKey,
     *      java.lang.String[])
     */
    @Override
    public VFSEntity getRelativePath ( EntityKey entityId, String[] relativeSegments ) throws FileshareException {
        if ( relativeSegments == null ) {
            return null;
        }
        try ( VFSContext v = this.vfs.getVFS(entityId).begin(true) ) {
            VFSContainerEntity persistent = v.load(entityId, VFSContainerEntity.class);
            this.accessControl.checkAnyAccess(v, persistent, GrantPermission.values());
            this.policyEvaluator.checkPolicy(v, persistent, WebUtils.getHttpRequest(SecurityUtils.getSubject()));
            User currentUser = this.accessControl.getCurrentUserCachable();
            Grant g = this.accessControl.getTokenAuthGrant(v, persistent);
            boolean browseAccess = this.accessControl.hasAccess(v, persistent, GrantPermission.BROWSE);

            if ( log.isDebugEnabled() ) {
                log.debug("Resolving " + Arrays.toString(relativeSegments)); //$NON-NLS-1$
            }

            if ( v.canResolveByName() ) {
                return v.resolveRelative(persistent, relativeSegments);
            }

            return getRelativePathRecursive(v, persistent, currentUser, g, relativeSegments, 0, browseAccess);
        }
    }


    /**
     * @param persistent
     * @param segments
     * @param i
     * @param browseAccess
     * @return
     * @throws FileshareException
     * @throws UnsupportedEncodingException
     */
    private VFSEntity getRelativePathRecursive ( VFSContext v, VFSContainerEntity persistent, User currentUser, Grant g, String[] segments, int i,
            boolean browseAccess ) throws FileshareException {

        if ( persistent == null || i >= segments.length ) {
            if ( log.isDebugEnabled() ) {
                log.debug("No match at " + persistent); //$NON-NLS-1$
            }
            return null;
        }

        String segment = segments[ i ];

        for ( VFSEntity e : v.getChildren(persistent) ) {
            if ( this.policyEvaluator.isPolicyFulfilled(v, e, WebUtils.getHttpRequest(SecurityUtils.getSubject())) != null ) {
                continue;
            }

            if ( !this.accessControl.isOwner(v, e) ) {
                if ( !browseAccess && !this.accessControl.isUserCreatorWithPerm(null, e, currentUser, g) ) {
                    continue;
                }
            }

            if ( segment.equals(e.getLocalName()) ) {
                if ( e instanceof VFSFileEntity && i == segments.length - 1 ) {
                    return e;
                }
                else if ( e instanceof VFSContainerEntity && i == segments.length - 1 ) {
                    return e;
                }
                else if ( e instanceof VFSContainerEntity && i < segment.length() - 1 ) {
                    return getRelativePathRecursive(v, (VFSContainerEntity) e, currentUser, g, segments, i + 1, browseAccess);
                }
            }
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Did not find %s at %s", Arrays.asList(segments), persistent)); //$NON-NLS-1$
        }

        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.BrowseService#getVisibleGroupRoots()
     */
    @Override
    public CollectionResult<VFSContainerEntity> getVisibleGroupRoots () throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            List<VFSContainerEntity> roots = new ArrayList<>();
            Set<PolicyViolation> violations = new HashSet<>();
            int hiddenPolicy = getVisibleGroupsInternal(tx, roots, violations, true);
            return new CollectionResult<>(roots, hiddenPolicy, violations);
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get group roots", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.api.internal.BrowseServiceInternal#getVisibleGroupsInternal(java.util.List,
     *      java.util.Set, boolean)
     */
    @Override
    public int getVisibleGroupsInternal ( List<VFSContainerEntity> roots, Set<PolicyViolation> violations, boolean clone ) throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            return getVisibleGroupsInternal(tx, roots, violations, clone);
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get group roots", e); //$NON-NLS-1$
        }
    }


    private int getVisibleGroupsInternal ( EntityTransactionContext tx, List<VFSContainerEntity> roots, Set<PolicyViolation> violations,
            boolean clone ) throws FileshareException {
        Set<Group> groups = this.userService.getCurrentUserGroupClosure(tx);
        int hiddenPolicy = 0;
        for ( Group g : groups ) {
            if ( g instanceof VirtualGroup ) {
                VirtualGroup vg = (VirtualGroup) g;
                try ( VFSContext v = this.vfs.getVFS(vg.getVfs()).begin(tx) ) {
                    VFSContainerEntity root = v.getRoot();
                    PolicyViolation violation = this.policyEvaluator.isPolicyFulfilled(v, root, WebUtils.getHttpRequest(SecurityUtils.getSubject()));
                    if ( violation != null ) {
                        hiddenPolicy++;
                        violations.add(violation);
                        continue;
                    }

                    if ( clone ) {
                        roots.add(root.cloneShallow(true));
                    }
                    else {
                        roots.add(root);
                    }
                }
                catch ( FileshareException e ) {
                    log.warn("Error loading virtual group " + vg.getVfs(), e); //$NON-NLS-1$
                }
            }
            else {
                if ( !isHidden(g.getSubjectRoot()) ) {
                    PolicyViolation violation = this.policyEvaluator
                            .isPolicyFulfilled(null, g.getSubjectRoot(), WebUtils.getHttpRequest(SecurityUtils.getSubject()));
                    if ( violation != null ) {
                        hiddenPolicy++;
                        violations.add(violation);
                        continue;
                    }
                    if ( clone ) {
                        roots.add(g.getSubjectRoot().cloneShallow(true));
                    }
                    else {
                        roots.add(g.getSubjectRoot());
                    }
                }
            }
        }
        return hiddenPolicy;
    }


    /**
     * @return the subjects that shared something to this user
     * @throws FileshareException
     */
    @Override
    public Set<SubjectQueryResult> getSharingSubjects () throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            Set<SubjectQueryResult> subjects = new HashSet<>();
            User user = this.userService.getCurrentUser(tx);

            if ( user == null ) {
                return Collections.EMPTY_SET;
            }

            Set<Group> groups = this.userService.getCurrentUserGroupClosure(tx);

            addGrantSubjects(user.getGrants(), subjects, user);

            for ( Group g : groups ) {
                addGrantSubjects(g.getGrants(), subjects, user);
            }

            return subjects;
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get sharing subjects", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.BrowseService#getRecursiveLastModified(eu.agno3.fileshare.model.EntityKey)
     */
    @Override
    public DateTime getRecursiveLastModified ( EntityKey entityKey ) {
        try ( VFSContext v = this.vfs.getVFS(entityKey).begin(true) ) {
            VFSContainerEntity persistent = v.load(entityKey, VFSContainerEntity.class);
            this.accessControl.checkAnyAccess(v, persistent, GrantPermission.values());
            HttpServletRequest httpRequest = WebUtils.getHttpRequest(SecurityUtils.getSubject());
            this.policyEvaluator.checkPolicy(v, persistent, httpRequest);
            return new DateTime(this.modTracker.getRecursiveLastModifiedTime(v, persistent));
        }
        catch ( FileshareException e ) {
            log.warn("Failed to get entity recursive last modified time", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.BrowseService#getSharesLastModified()
     */
    @Override
    public DateTime getSharesLastModified () {
        return getSubjectSharesLastModified(null);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.BrowseService#getSharesRecursiveLastModified()
     */
    @Override
    public DateTime getSharesRecursiveLastModified () {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            return getSubjectSharesLastModifiedInternal(tx, null, true);
        }
        catch ( EntityTransactionException e ) {
            log.error("Failed to get last modified", e); //$NON-NLS-1$
            return DateTime.now().minusDays(1);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.BrowseService#getSubjectSharesLastModified(java.util.UUID)
     */
    @Override
    public DateTime getSubjectSharesLastModified ( UUID subjectId ) {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            return getSubjectSharesLastModifiedInternal(tx, subjectId, false);
        }
        catch ( EntityTransactionException e ) {
            log.error("Failed to get last modified", e); //$NON-NLS-1$
            return DateTime.now().minusDays(1);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.BrowseService#getSubjectSharesRecursiveLastModified(java.util.UUID)
     */
    @Override
    public DateTime getSubjectSharesRecursiveLastModified ( UUID subjectId ) {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            return getSubjectSharesLastModifiedInternal(tx, subjectId, true);
        }
        catch ( EntityTransactionException e ) {
            log.error("Failed to get last modified", e); //$NON-NLS-1$
            return DateTime.now().minusDays(1);
        }
    }


    /**
     * @param subjectId
     * @return
     */
    private DateTime getSubjectSharesLastModifiedInternal ( EntityTransactionContext tx, UUID subjectId, boolean withContents ) {
        DateTime defaultLastMod = DateTime.now().minusDays(1);
        User user;
        try {
            user = this.userService.getCurrentUser(tx);
        }
        catch (
            UserNotFoundException |
            AuthenticationException e ) {
            log.warn("User not found", e); //$NON-NLS-1$
            return defaultLastMod;
        }

        if ( user == null ) {
            return defaultLastMod;
        }

        DateTime max = null;
        Set<Group> groups;
        try {
            groups = this.userService.getCurrentUserGroupClosure(tx);
        }
        catch ( FileshareException e ) {
            log.warn("Failed to get user groups", e); //$NON-NLS-1$
            return defaultLastMod;
        }

        try {
            max = findMaxShareChangeSubjects(tx, user.getGrants(), subjectId, user, withContents);
            for ( Group g : groups ) {
                DateTime groupMax = findMaxShareChangeSubjects(tx, g.getGrants(), subjectId, user, withContents);
                if ( groupMax != null ) {
                    if ( max == null || max.isBefore(groupMax) ) {
                        max = groupMax;
                    }
                }
            }
        }
        catch ( FileshareException e ) {
            log.warn("Failed to determine share last modified time", e); //$NON-NLS-1$
        }

        if ( max == null ) {
            return defaultLastMod;
        }

        return max;
    }


    /**
     * @param grants
     * @param subjectId
     * @param user
     * @return
     * @throws FileshareException
     */
    private DateTime findMaxShareChangeSubjects ( EntityTransactionContext tx, Set<SubjectGrant> grants, UUID subjectId, User user,
            boolean withContents ) throws FileshareException {
        DateTime max = null;

        for ( Grant g : grants ) {
            Subject owner = g.getEntity().getOwner();
            if ( subjectId != null && !owner.getId().equals(subjectId) ) {
                continue;
            }

            if ( g.getExpires() != null && g.getExpires().isBeforeNow() ) {
                if ( max == null || max.isBefore(g.getExpires()) ) {
                    max = g.getExpires();
                }
            }

            if ( max == null || max.isBefore(g.getCreated()) ) {
                max = g.getCreated();
            }

            if ( withContents ) {
                // TODO: shares from virtual groups
                max = checkContentsLastModified(tx, max, g);
            }
        }
        return max;
    }


    /**
     * @param max
     * @param g
     * @return
     * @throws FileshareException
     */
    private DateTime checkContentsLastModified ( EntityTransactionContext tx, DateTime max, Grant g ) throws FileshareException {
        ContentEntity e = g.getEntity();
        Long lastMod = null;
        if ( e instanceof FileEntity ) {
            if ( max == null || ( e.getLastModified() != null && max.isBefore(e.getLastModified()) ) ) {
                return e.getLastModified();
            }

            return max;
        }
        else if ( e instanceof ContainerEntity ) {
            try ( VFSContext v = this.vfs.getNative().begin(tx) ) {
                lastMod = this.modTracker.getRecursiveLastModifiedTime(v, (VFSContainerEntity) e);
            }
        }

        if ( max == null || ( lastMod != null && max.isBefore(lastMod) ) ) {
            return new DateTime(lastMod);
        }
        return max;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.BrowseService#getPeers()
     */
    @Override
    public Set<PeerInfo> getPeers () throws FileshareException {

        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {

            Set<PeerInfo> peers = new HashSet<>();
            User user = this.userService.getCurrentUser(tx);

            if ( user == null ) {
                return Collections.EMPTY_SET;
            }

            Set<Group> groups = this.userService.getCurrentUserGroupClosure(tx);
            Set<UUID> ids = new HashSet<>();
            ids.add(user.getId());

            for ( Group g : groups ) {
                ids.add(g.getId());
            }

            EntityManager em = tx.getEntityManager();

            TypedQuery<Subject> incomingShares = em.createQuery(
                "SELECT DISTINCT s FROM SubjectGrant sg LEFT JOIN sg.entity.owner s " + //$NON-NLS-1$
                        "WHERE sg.target.id IN :targetIds AND NOT s IN :targetIds AND (sg.expires IS NULL OR sg.expires >= CURRENT_TIMESTAMP)", //$NON-NLS-1$
                Subject.class);

            incomingShares.setParameter("targetIds", ids); //$NON-NLS-1$

            Set<Subject> incomingSubjects = new HashSet<>(incomingShares.getResultList());

            TypedQuery<Grant> outgoingShares = em.createQuery(
                "SELECT g FROM Grant g WHERE g.entity.owner.id IN :sourceIds " //$NON-NLS-1$
                        + "AND (g.expires IS NULL OR g.expires >= CURRENT_TIMESTAMP)", //$NON-NLS-1$
                Grant.class);
            outgoingShares.setParameter("sourceIds", ids); //$NON-NLS-1$

            Set<Subject> incomingOnlySubjects = new HashSet<>();
            Set<Subject> outgoingOnlySubjects = new HashSet<>();
            Set<Subject> bothSubjects = new HashSet<>();
            Set<String> mailGrantsByAddress = new HashSet<>();
            boolean haveTokenGrant = false;

            for ( Grant g : outgoingShares.getResultList() ) {
                if ( g instanceof SubjectGrant ) {
                    SubjectGrant sg = (SubjectGrant) g;

                    if ( sg.getTarget() == null || ids.contains(sg.getTarget().getId()) ) {
                        continue;
                    }

                    if ( incomingSubjects.contains(sg.getTarget()) && !bothSubjects.contains(sg.getTarget()) ) {
                        bothSubjects.add(sg.getTarget());
                        incomingSubjects.remove(sg.getTarget());
                    }
                    else if ( !incomingSubjects.contains(sg.getTarget()) && !bothSubjects.contains(sg.getTarget()) ) {
                        outgoingOnlySubjects.add(sg.getTarget());
                    }
                }
                else if ( g instanceof MailGrant ) {
                    mailGrantsByAddress.add( ( (MailGrant) g ).getMailAddress());
                }
                else if ( g instanceof TokenGrant ) {
                    haveTokenGrant = true;
                }
            }

            incomingOnlySubjects.addAll(incomingSubjects);

            addSubjectPeers(peers, bothSubjects, true, true);
            addSubjectPeers(peers, incomingOnlySubjects, true, false);
            addSubjectPeers(peers, outgoingOnlySubjects, false, true);

            for ( String mailAddr : mailGrantsByAddress ) {
                MailPeerInfo mpi = new MailPeerInfo();
                mpi.setMailAddress(mailAddr);
                peers.add(mpi);
            }

            if ( haveTokenGrant ) {
                peers.add(new TokenPeerInfo());
            }

            return peers;
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to start entity transaction", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.BrowseService#getPeerMailAddresses(java.lang.String)
     */
    @Override
    public Set<String> getPeerMailAddresses ( String query ) throws FileshareException {

        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            User user = this.userService.getCurrentUser(tx);

            if ( user == null ) {
                return Collections.EMPTY_SET;
            }

            Set<Group> groups = this.userService.getCurrentUserGroupClosure(tx);
            Set<UUID> ids = new HashSet<>();
            ids.add(user.getId());

            for ( Group g : groups ) {
                ids.add(g.getId());
            }

            String likeQuery = PatternUtil.getLikePattern(PatternUtil.makeSubstringQuery(query));

            TypedQuery<String> mailShares = tx.getEntityManager().createQuery(
                "SELECT g.mailAddress FROM MailGrant g WHERE g.entity.owner.id IN :sourceIds AND g.mailAddress LIKE :query", //$NON-NLS-1$
                String.class);
            mailShares.setParameter("sourceIds", ids); //$NON-NLS-1$
            mailShares.setParameter("query", likeQuery); //$NON-NLS-1$

            mailShares.setMaxResults(20);
            return new HashSet<>(mailShares.getResultList());
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get peer mail addresses", e); //$NON-NLS-1$
        }
    }


    /**
     * @param subjects
     * @param haveSharedTo
     * @param haveSharedBy
     */
    private void addSubjectPeers ( Set<PeerInfo> peers, Set<Subject> subjects, boolean haveSharedBy, boolean haveSharedTo ) {
        for ( Subject s : subjects ) {
            SubjectPeerInfo spi = new SubjectPeerInfo();
            spi.setHaveSharedFrom(haveSharedBy);
            spi.setHaveSharedTo(haveSharedTo);
            SubjectQueryResult fromSubject = SubjectQueryResult.fromSubject(s);
            TrustLevel trustLevel = this.ctx.getConfigurationProvider().getTrustLevelConfiguration().getTrustLevel(s);
            if ( trustLevel != null ) {
                fromSubject.setTrustLevel(trustLevel.getId());
            }
            spi.setSubject(fromSubject);
            peers.add(spi);
        }
    }


    @Override
    public SubjectQueryResult getSharingSubject ( EntityKey subjId ) throws FileshareException {

        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();
            tx.commit();

            User user = this.userService.getCurrentUser(tx);

            if ( user == null ) {
                throw new AuthenticationException();
            }

            Set<Group> groups = this.userService.getCurrentUserGroupClosure(tx);

            if ( ! ( subjId instanceof NativeEntityKey ) ) {
                throw new SubjectNotFoundException();
            }
            UUID subjectId = ( (NativeEntityKey) subjId ).getId();

            if ( this.accessControl.hasPermission("subjects:query") ) { //$NON-NLS-1$
                Subject subject = em.find(Subject.class, subjectId);

                if ( subject == null ) {
                    throw new SubjectNotFoundException();
                }

                return SubjectQueryResult.fromSubject(subject);
            }

            for ( Grant g : user.getGrants() ) {
                if ( g.getEntity().getOwner().getId().equals(subjectId) ) {
                    return SubjectQueryResult.fromSubject(g.getEntity().getOwner());
                }
            }

            for ( Group group : groups ) {
                for ( Grant g : group.getGrants() ) {
                    if ( g.getEntity().getOwner().getId().equals(subjectId) ) {
                        return SubjectQueryResult.fromSubject(g.getEntity().getOwner());
                    }
                }
            }

            throw new AccessDeniedException();
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get sharing subject", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.BrowseService#getSharedToUserGrants()
     */
    @Override
    public CollectionResult<SubjectGrant> getSharedToUserGrants () throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            Set<SubjectGrant> grants = new HashSet<>();
            User user = this.userService.getCurrentUser(tx);
            if ( user == null ) {
                return new CollectionResult<>();
            }

            Set<Group> groups = this.accessControl.getCurrentUserGroupClosure(tx);
            for ( SubjectGrant g : user.getGrants() ) {
                if ( g.getExpires() != null && g.getExpires().isBeforeNow() ) {
                    continue;
                }

                if ( isOwned(g.getEntity().getOwner(), user, groups) ) {
                    continue;
                }

                ServiceUtil.enhanceVFSGrant(tx, this.vfs, g);
                grants.add(g);
            }

            for ( Group group : groups ) {
                for ( SubjectGrant g : group.getGrants() ) {
                    if ( g.getExpires() != null && g.getExpires().isBeforeNow() ) {
                        continue;
                    }

                    if ( isOwned(g.getEntity().getOwner(), user, groups) ) {
                        continue;
                    }

                    ServiceUtil.enhanceVFSGrant(tx, this.vfs, g);
                    grants.add(g);
                }
            }

            tx.commit();
            return uniquifyGrants(grants, WebUtils.getHttpRequest(SecurityUtils.getSubject()));
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get user shares", e); //$NON-NLS-1$
        }
    }


    /**
     * @param owner
     * @param user
     * @param groups
     * @return
     */
    private static boolean isOwned ( Subject owner, User user, Set<Group> groups ) {

        if ( owner == null || user == null ) {
            return false;
        }

        if ( user.equals(owner) ) {
            return true;
        }
        else if ( groups.contains(owner) ) {
            return true;
        }

        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.BrowseService#getSubjectSharedToEntities(java.util.UUID)
     */
    @Override
    public CollectionResult<VFSEntity> getSubjectSharedToEntities ( UUID subjectId ) throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            return getSubjectSharedToEntitiesInternal(tx, subjectId, true);
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get subject shares", e); //$NON-NLS-1$
        }
    }


    /**
     * @param em
     * @param subjectId
     * @param clone
     * @return
     * @throws AuthenticationException
     * @throws UserNotFoundException
     * @throws EntityNotFoundException
     * @throws FileshareException
     */
    CollectionResult<VFSEntity> getSubjectSharedToEntitiesInternal ( EntityTransactionContext tx, UUID subjectId, boolean clone )
            throws AuthenticationException, UserNotFoundException, EntityNotFoundException, FileshareException {
        User user = this.userService.getCurrentUser(tx);
        if ( user == null ) {
            return new CollectionResult<>();
        }

        Set<Group> groups = this.accessControl.getCurrentUserGroupClosure(tx);
        Set<UUID> ids = new HashSet<>();
        ids.add(user.getId());

        for ( Group g : groups ) {
            ids.add(g.getId());
        }

        TypedQuery<ContentEntity> outgoingShares = tx.getEntityManager().createQuery(
            "SELECT DISTINCT e FROM SubjectGrant g JOIN g.entity e WHERE g.entity.owner.id IN :sourceIds AND g.target.id = :subjectId " //$NON-NLS-1$
                    + "AND (g.expires IS NULL OR g.expires >= CURRENT_TIMESTAMP)", //$NON-NLS-1$
            ContentEntity.class);
        outgoingShares.setParameter("sourceIds", ids); //$NON-NLS-1$
        outgoingShares.setParameter("subjectId", subjectId); //$NON-NLS-1$

        return filterPolicy(null, enhanceEntityList(tx, outgoingShares.getResultList()), WebUtils.getHttpRequest(SecurityUtils.getSubject()), clone);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.BrowseService#getMailSharedToEntities(java.lang.String)
     */
    @Override
    public CollectionResult<VFSEntity> getMailSharedToEntities ( String mailAddr ) throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            return getMailSharedToEntitiesInternal(tx, mailAddr, true);
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get mail shares", e); //$NON-NLS-1$
        }
    }


    /**
     * @param mailAddr
     * @param em
     * @param clone
     * @return
     * @throws AuthenticationException
     * @throws UserNotFoundException
     * @throws EntityNotFoundException
     * @throws FileshareException
     */
    CollectionResult<VFSEntity> getMailSharedToEntitiesInternal ( EntityTransactionContext tx, String mailAddr, boolean clone )
            throws AuthenticationException, UserNotFoundException, EntityNotFoundException, FileshareException {
        User user = this.userService.getCurrentUser(tx);

        if ( user == null ) {
            return new CollectionResult<>();
        }

        Set<Group> groups = this.accessControl.getCurrentUserGroupClosure(tx);
        Set<UUID> ids = new HashSet<>();
        ids.add(user.getId());

        for ( Group g : groups ) {
            ids.add(g.getId());
        }

        TypedQuery<ContentEntity> outgoingShares = tx.getEntityManager().createQuery(
            "SELECT DISTINCT e FROM MailGrant g JOIN g.entity e WHERE g.entity.owner.id IN :sourceIds AND g.mailAddress = :mailAddress " //$NON-NLS-1$
                    + "AND (g.expires IS NULL OR g.expires >= CURRENT_TIMESTAMP)", //$NON-NLS-1$
            ContentEntity.class);
        outgoingShares.setParameter("sourceIds", ids); //$NON-NLS-1$
        outgoingShares.setParameter("mailAddress", mailAddr); //$NON-NLS-1$
        return filterPolicy(null, enhanceEntityList(tx, outgoingShares.getResultList()), WebUtils.getHttpRequest(SecurityUtils.getSubject()), clone);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.BrowseService#getTokenSharedToEntities()
     */
    @Override
    public CollectionResult<VFSEntity> getTokenSharedToEntities () throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            return getTokenSharedToEntitiesInternal(tx, true);
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get token shares", e); //$NON-NLS-1$
        }
    }


    /**
     * @param em
     * @param clone
     * @return
     * @throws AuthenticationException
     * @throws UserNotFoundException
     * @throws EntityNotFoundException
     * @throws FileshareException
     */
    CollectionResult<VFSEntity> getTokenSharedToEntitiesInternal ( EntityTransactionContext tx, boolean clone )
            throws AuthenticationException, UserNotFoundException, EntityNotFoundException, FileshareException {
        User user = this.userService.getCurrentUser(tx);

        if ( user == null ) {
            return new CollectionResult<>();
        }

        Set<Group> groups = this.accessControl.getCurrentUserGroupClosure(tx);
        Set<UUID> ids = new HashSet<>();
        ids.add(user.getId());

        for ( Group g : groups ) {
            ids.add(g.getId());
        }

        TypedQuery<ContentEntity> outgoingShares = tx.getEntityManager().createQuery(
            "SELECT DISTINCT e FROM TokenGrant g JOIN g.entity e WHERE g.entity.owner.id IN :sourceIds " //$NON-NLS-1$
                    + "AND (g.expires IS NULL OR g.expires >= CURRENT_TIMESTAMP)", //$NON-NLS-1$
            ContentEntity.class);
        outgoingShares.setParameter("sourceIds", ids); //$NON-NLS-1$
        return filterPolicy(null, enhanceEntityList(tx, outgoingShares.getResultList()), WebUtils.getHttpRequest(SecurityUtils.getSubject()), clone);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.BrowseService#getSharedByUserGrants()
     */
    @Override
    public CollectionResult<VFSEntity> getSharedByUserGrants () throws FileshareException {

        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            return getSharedByUserGrantsInternal(tx, true);
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get user shares", e); //$NON-NLS-1$
        }
    }


    /**
     * @param em
     * @return
     * @throws AuthenticationException
     * @throws UserNotFoundException
     * @throws EntityNotFoundException
     * @throws FileshareException
     */
    CollectionResult<VFSEntity> getSharedByUserGrantsInternal ( EntityTransactionContext tx, boolean clone )
            throws AuthenticationException, UserNotFoundException, EntityNotFoundException, FileshareException {
        User user = this.userService.getCurrentUser(tx);

        if ( user == null ) {
            return new CollectionResult<>();
        }

        Set<Group> groups = this.accessControl.getCurrentUserGroupClosure(tx);
        Set<UUID> ids = new HashSet<>();
        ids.add(user.getId());

        for ( Group g : groups ) {
            ids.add(g.getId());
        }

        TypedQuery<ContentEntity> outgoingShares = tx.getEntityManager().createQuery(
            "SELECT DISTINCT e FROM Grant g JOIN g.entity e WHERE g.entity.owner.id IN :sourceIds " //$NON-NLS-1$
                    + "AND (g.expires IS NULL OR g.expires >= CURRENT_TIMESTAMP)", //$NON-NLS-1$
            ContentEntity.class);
        outgoingShares.setParameter("sourceIds", ids); //$NON-NLS-1$
        return filterPolicy(null, enhanceEntityList(tx, outgoingShares.getResultList()), WebUtils.getHttpRequest(SecurityUtils.getSubject()), clone);
    }


    /**
     * @param resultList
     * @return
     * @throws EntityNotFoundException
     * @throws FileshareException
     */
    private List<VFSEntity> enhanceEntityList ( EntityTransactionContext tx, List<ContentEntity> resultList )
            throws EntityNotFoundException, FileshareException {
        List<VFSEntity> unwrapped = new LinkedList<>();
        for ( ContentEntity e : resultList ) {
            unwrapped.add(ServiceUtil.unwrapEntity(tx, this.vfs, e));
        }
        return unwrapped;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.BrowseService#getSubjectShareGrants(java.util.UUID)
     */
    @Override
    public CollectionResult<SubjectGrant> getSubjectShareGrants ( UUID subjectId ) throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();
            tx.commit();

            Set<SubjectGrant> grants = new HashSet<>();
            User user = this.userService.getCurrentUser(tx);
            if ( user == null ) {
                return new CollectionResult<>();
            }

            Set<Group> groups = this.userService.getCurrentUserGroupClosure(tx);
            Subject subject = em.find(Subject.class, subjectId);

            if ( subject == null ) {
                throw new SubjectNotFoundException();
            }

            addSubjectMatchingGrants(tx, user.getGrants(), grants, subject);

            for ( Group g : groups ) {
                addSubjectMatchingGrants(tx, g.getGrants(), grants, subject);
            }

            return uniquifyGrants(grants, WebUtils.getHttpRequest(SecurityUtils.getSubject()));
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get subject shares", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.BrowseService#getGrantWithName(java.util.UUID, java.lang.String)
     */
    @Override
    public Grant getGrantWithName ( UUID subjectId, String rootName ) throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();
            User user = this.userService.getCurrentUser(tx);
            tx.commit();
            if ( user == null ) {
                throw new AuthenticationException();
            }

            Set<Group> groups = this.userService.getCurrentUserGroupClosure(tx);
            Subject subject = em.find(Subject.class, subjectId);

            if ( subject == null ) {
                throw new SubjectNotFoundException();
            }

            Set<SubjectGrant> grants = new HashSet<>();
            addSubjectMatchingGrants(tx, user.getGrants(), grants, subject);

            for ( Group g : groups ) {
                addSubjectMatchingGrants(tx, g.getGrants(), grants, subject);
            }

            SubjectGrant found = null;
            for ( SubjectGrant g : grants ) {
                if ( g.getEntity().getLocalName().equals(rootName) ) {
                    if ( found != null ) {
                        log.debug("Duplicate resource"); //$NON-NLS-1$
                        return null;
                    }
                    found = g;
                }
            }
            return found;
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get grant", e); //$NON-NLS-1$
        }
    }


    /**
     * @param grants
     */
    private CollectionResult<SubjectGrant> uniquifyGrants ( Set<SubjectGrant> grants, HttpServletRequest req ) {
        // if there are multiple grants for a single resource we only need to return the one with the most privileges
        List<SubjectGrant> unique = new ArrayList<>(grants);
        Collections.sort(unique, new SubjectGrantPrivilegeComparator());
        int hiddenPolicy = 0;
        Set<PolicyViolation> violations = new HashSet<>();
        SubjectGrant last = null;
        Set<SubjectGrant> toRemove = new HashSet<>();

        for ( int i = 0; i < unique.size(); i++ ) {
            SubjectGrant cur = unique.get(i);
            ContentEntity entity = cur.getEntity();

            try ( VFSContext v = this.vfs.getVFS(entity.getEntityKey()).begin(true) ) {
                PolicyViolation violation = this.policyEvaluator.isPolicyFulfilled(v, entity, req);
                if ( violation != null ) {
                    hiddenPolicy++;
                    violations.add(violation);
                    toRemove.add(cur);
                    continue;
                }

                if ( last == null ) {
                    last = cur;
                    continue;
                }

                if ( last.getEntity().equals(entity) ) {
                    toRemove.add(cur);
                }

                last = cur;
            }
            catch ( FileshareException e ) {
                log.error("Failed to get VFS entity " + entity, e); //$NON-NLS-1$
            }
        }

        unique.removeAll(toRemove);
        return new CollectionResult<>(unique, hiddenPolicy, violations);
    }


    /**
     * @param grants
     * @param grants2
     * @param subject
     */
    private void addSubjectMatchingGrants ( EntityTransactionContext tx, Set<SubjectGrant> allAvailable, Set<SubjectGrant> subjectMatches,
            Subject subject ) {
        for ( SubjectGrant g : allAvailable ) {
            if ( g.getEntity().getOwner().equals(subject) && ( g.getExpires() == null || g.getExpires().isAfterNow() ) ) {
                try {
                    ServiceUtil.enhanceVFSGrant(tx, this.vfs, g);
                    subjectMatches.add(g);
                }
                catch ( FileshareException e ) {
                    log.error("Grant target is not available", e); //$NON-NLS-1$
                }

            }
        }
    }


    /**
     * @param grants
     * @param subjects
     * @param exclude
     */
    private static void addGrantSubjects ( Collection<SubjectGrant> grants, Set<SubjectQueryResult> subjects, User exclude ) {
        for ( Grant g : grants ) {
            Subject owner = g.getEntity().getOwner();
            if ( !owner.equals(exclude) ) {
                subjects.add(SubjectQueryResult.fromSubject(owner));
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     * 
     * @see eu.agno3.fileshare.service.BrowseService#getOrCreateGroupRoot(java.util.UUID)
     */
    @Override
    public VFSContainerEntity getOrCreateGroupRoot ( UUID groupId ) throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();
            Group g = em.find(Group.class, groupId);
            if ( g == null ) {
                throw new EntityNotFoundException();
            }

            this.accessControl.checkMember(g);

            if ( g instanceof VirtualGroup ) {
                VirtualGroup vg = (VirtualGroup) g;
                return getVirtualGroupRoot(tx, vg);
            }

            ContainerEntity subjectRoot = g.getSubjectRoot();
            if ( subjectRoot == null ) {
                try ( AuditContext<SingleEntityFileshareAuditBuilder> audit = this.ctx.getEventLogger()
                        .audit(SingleEntityFileshareAuditBuilder.class) ) {
                    audit.builder().access(this.accessControl).action("CREATE_GROUP_ROOT"); //$NON-NLS-1$

                    subjectRoot = ServiceUtil.createSubjectRoot(
                        tx,
                        this.modTracker,
                        g,
                        this.accessControl.getCurrentUser(),
                        this.ctx.getConfigurationProvider().getSecurityPolicyConfiguration());
                    g.setSubjectRoot(subjectRoot);
                    audit.builder().entity(subjectRoot);
                    em.persist(subjectRoot);
                    em.persist(g);
                    audit.builder().entity(subjectRoot);
                    em.flush();
                }
            }

            this.policyEvaluator.checkPolicy(null, subjectRoot, WebUtils.getHttpRequest(SecurityUtils.getSubject()));
            tx.commit();
            return subjectRoot;
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get group root", e); //$NON-NLS-1$
        }
    }


    /**
     * @param tx
     * @param vg
     * @return
     * @throws PolicyNotFulfilledException
     * @throws FileshareException
     */
    private VFSContainerEntity getVirtualGroupRoot ( EntityTransactionContext tx, VirtualGroup vg )
            throws PolicyNotFulfilledException, FileshareException {
        try ( VFSContext v = this.vfs.getVFS(vg.getVfs()).begin(tx) ) {
            VFSContainerEntity root = v.getRoot();
            PolicyViolation violation = this.policyEvaluator.isPolicyFulfilled(v, root, WebUtils.getHttpRequest(SecurityUtils.getSubject()));
            if ( violation != null ) {
                throw new PolicyNotFulfilledException(violation);
            }
            return root;
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.BrowseService#getGroupRootByName(java.lang.String, java.lang.String)
     */
    @Override
    public VFSContainerEntity getGroupRootByName ( String groupName, String realm ) throws FileshareException {
        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().start() ) {
            EntityManager em = tx.getEntityManager();
            TypedQuery<Group> q;
            if ( StringUtils.isBlank(realm) ) {
                q = em.createQuery("SELECT g FROM Group g WHERE g.name = :groupName AND g.realm IS NULL", Group.class); //$NON-NLS-1$
            }
            else {
                q = em.createQuery("SELECT g FROM Group g WHERE g.name = :groupName AND g.realm = :realm", Group.class); //$NON-NLS-1$
                q.setParameter("realm", realm); //$NON-NLS-1$ l
            }
            q.setParameter("groupName", groupName); //$NON-NLS-1$
            q.setMaxResults(1);
            List<Group> resultList = q.getResultList();
            Group g = resultList.size() > 0 ? resultList.get(0) : null;
            this.accessControl.checkMember(g);

            if ( g instanceof VirtualGroup ) {
                return getVirtualGroupRoot(tx, (VirtualGroup) g);
            }

            if ( g == null || g.getSubjectRoot() == null ) {
                return null;
            }
            ContainerEntity subjectRoot = g.getSubjectRoot();
            this.policyEvaluator.checkPolicy(null, subjectRoot, WebUtils.getHttpRequest(SecurityUtils.getSubject()));
            tx.commit();
            return subjectRoot;
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Failed to get group root", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws FileshareException
     *
     * @see eu.agno3.fileshare.service.BrowseService#searchEntities(java.lang.String, int, int)
     */
    @Override
    public SearchResult searchEntities ( String query, int limit, int offset ) throws FileshareException {

        if ( this.ctx.getConfigurationProvider().getSearchConfiguration().isSearchDisabled() ) {
            return new SearchResult();
        }

        if ( !this.accessControl.isUserAuthenticated() ) {
            return new SearchResult();
        }

        if ( !this.ctx.getConfigurationProvider().getSearchConfiguration().isAllowPaging() && offset != 0 ) {
            throw new InvalidQueryException("Paging is forbidden"); //$NON-NLS-1$
        }

        long start = System.currentTimeMillis();
        if ( limit <= 0 || limit > MAX_SEARCH_LIMIT ) {
            throw new InvalidQueryException("Limit not within allowed range"); //$NON-NLS-1$
        }

        SearchResult res = new SearchResult();

        String likeQuery = PatternUtil.getLikePattern(query);

        if ( log.isDebugEnabled() ) {
            log.debug("Query is " + likeQuery); //$NON-NLS-1$
        }

        Set<Group> groups = this.accessControl.getCurrentUserGroupClosure();
        Set<UUID> ownerSubjects = new HashSet<>();
        User currentUser = this.accessControl.getCurrentUser();
        ownerSubjects.add(currentUser.getId());
        for ( Group g : groups ) {
            ownerSubjects.add(g.getId());
        }

        try ( EntityTransactionContext tx = this.ctx.getFileshareEntityTS().startReadOnly() ) {
            EntityManager em = tx.getEntityManager();

            TypedQuery<UUID> incomingShares = em.createQuery(
                "SELECT DISTINCT s.id FROM SubjectGrant sg LEFT JOIN sg.entity.owner s WHERE sg.target.id IN :targetIds " //$NON-NLS-1$
                        + "AND (sg.expires IS NULL OR sg.expires >= CURRENT_TIMESTAMP)", //$NON-NLS-1$
                UUID.class);

            incomingShares.setParameter("targetIds", ownerSubjects); //$NON-NLS-1$

            Set<UUID> subjects = new HashSet<>(ownerSubjects);
            subjects.addAll(incomingShares.getResultList());

            Query q = em.createQuery("SELECT e, " + //$NON-NLS-1$
                    "CASE WHEN e.owner.id = :userId THEN 1 WHEN e.owner.id IN :userSubjects THEN 2 ELSE 3 END AS quality" //$NON-NLS-1$
                    + " FROM ContentEntity e WHERE e.owner.id IN :subjects AND localName LIKE :query ORDER BY quality, localName ASC"); //$NON-NLS-1$

            q.setParameter("userId", currentUser.getId()); //$NON-NLS-1$
            q.setParameter("userSubjects", ownerSubjects); //$NON-NLS-1$
            q.setParameter("subjects", subjects); //$NON-NLS-1$
            q.setParameter("query", likeQuery); //$NON-NLS-1$

            q.setFirstResult(offset);
            q.setMaxResults(limit + 1);

            int numFound = 0;
            for ( Object e : q.getResultList() ) {
                if ( ! ( e instanceof Object[] ) || ( (Object[]) e ).length != 2 || ! ( ( (Object[]) e )[ 0 ] instanceof ContentEntity ) ) {
                    continue;
                }
                Object[] o = (Object[]) e;
                ContentEntity entity = (ContentEntity) o[ 0 ];
                numFound++;
                if ( !ownerSubjects.contains(entity.getOwner().getId()) ) {
                    if ( !this.accessControl.hasAccess(null, entity, GrantPermission.BROWSE) ) {
                        continue;
                    }
                }

                if ( res.getEntities().size() < limit ) {
                    res.getEntities().add(entity.cloneShallow());
                }
            }

            res.setHaveMoreElements(numFound > limit);

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Query took %d ms", System.currentTimeMillis() - start)); //$NON-NLS-1$
            }

            return res;
        }
        catch ( EntityTransactionException e ) {
            throw new FileshareException("Search failed", e); //$NON-NLS-1$
        }
    }


    /**
     * @param subjectRoot
     * @return
     */
    protected boolean isHidden ( ContainerEntity subjectRoot ) {
        if ( subjectRoot == null ) {
            return true;
        }
        return false;
    }

}
