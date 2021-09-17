/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.01.2015 by mbechler
 */
package eu.agno3.fileshare.security.internal;


import java.io.Serializable;
import java.security.AccessControlException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.exceptions.AccessDeniedException;
import eu.agno3.fileshare.exceptions.AuthenticationException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.GrantAuthenticationRequiredException;
import eu.agno3.fileshare.exceptions.UserNotFoundException;
import eu.agno3.fileshare.model.ContainerEntity;
import eu.agno3.fileshare.model.ContentEntity;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.MailGrant;
import eu.agno3.fileshare.model.SubjectGrant;
import eu.agno3.fileshare.model.TokenGrant;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.tokens.AccessToken;
import eu.agno3.fileshare.model.tokens.AnonymousGrantToken;
import eu.agno3.fileshare.model.tokens.SessionIntentToken;
import eu.agno3.fileshare.security.AccessControlService;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.db.orm.EntityTransactionService;
import eu.agno3.runtime.security.DynamicModularRealmAuthorizer;
import eu.agno3.runtime.security.password.PasswordCompareUtil;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.security.web.login.token.TokenPrincipal;


/**
 * @author mbechler
 *
 */
@Component ( service = AccessControlService.class )
public class AccessControlServiceImpl implements AccessControlService {

    /**
     * 
     */
    private static final String CRYPTO_TOKEN_ATTR = "crypto.token"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(AccessControlServiceImpl.class);
    private static final int USER_CACHE_SIZE = 1024;
    private EntityTransactionService fileshareEts;

    private DynamicModularRealmAuthorizer authorizer;
    private Map<UserPrincipal, User> userCache = Collections.synchronizedMap(new LRUMap<>(USER_CACHE_SIZE));


    @Reference ( target = "(persistenceUnit=fileshare)" )
    protected synchronized void bindEntityTransactionService ( EntityTransactionService ets ) {
        this.fileshareEts = ets;
    }


    protected synchronized void unbindEntityTransactionService ( EntityTransactionService ets ) {
        if ( this.fileshareEts == ets ) {
            this.fileshareEts = null;
        }
    }


    @Reference
    protected synchronized void setAuthorizer ( DynamicModularRealmAuthorizer authz ) {
        this.authorizer = authz;
    }


    protected synchronized void unsetAuthorizer ( DynamicModularRealmAuthorizer authz ) {
        if ( this.authorizer == authz ) {
            this.authorizer = null;
        }
    }


    @Override
    public void checkAccess ( VFSContext ctx, VFSEntity e, GrantPermission... perms ) throws AccessDeniedException {
        if ( !hasAccess(ctx, e, perms) ) {
            throw new AccessDeniedException(String.format("Access %s denied on %s", Arrays.toString(perms), e)); //$NON-NLS-1$
        }
    }


    @Override
    public void checkAnyAccess ( VFSContext ctx, VFSEntity entity, GrantPermission... perms )
            throws AccessDeniedException, GrantAuthenticationRequiredException {
        if ( !hasAnyAccess(ctx, entity, perms) ) {
            throw new AccessDeniedException(String.format("Access %s denied on %s", Arrays.toString(perms), entity)); //$NON-NLS-1$
        }
    }


    @Override
    public void checkUserIsCreatorWithPerm ( VFSContext ctx, VFSEntity entity, User currentUser, Grant grant, GrantPermission... perms )
            throws AccessDeniedException {
        if ( !this.isUserCreatorWithPerm(ctx, entity, currentUser, grant, perms) ) {
            throw new AccessDeniedException(String.format("Access %s denied or not creator on %s", Arrays.toString(perms), entity)); //$NON-NLS-1$
        }
    }


    @Override
    public void checkUserIsCreatorWithPermRecursive ( VFSContext ctx, VFSEntity entity, User currentUser, Grant grant, GrantPermission... perms )
            throws AccessDeniedException, UserNotFoundException, AuthenticationException {
        if ( !this.isUserCreatorWithPermRecursive(ctx, entity, currentUser, grant, perms) ) {
            throw new AccessDeniedException(String.format("Access %s denied or not creator on %s (recursive)", Arrays.toString(perms), entity)); //$NON-NLS-1$
        }
    }


    /**
     * @param entity
     * @param currentUser
     * @param grant
     * @param perms
     * @return whether the user is the creator of all entities and has the given permissions
     * @throws AccessDeniedException
     * @throws AuthenticationException
     * @throws UserNotFoundException
     */
    @Override
    public boolean isUserCreatorWithPermRecursive ( VFSContext ctx, VFSEntity entity, User currentUser, Grant grant, GrantPermission... perms )
            throws AccessDeniedException, UserNotFoundException, AuthenticationException {
        if ( !isUserCreatorWithPerm(ctx, entity, currentUser, grant, perms) ) {
            return false;
        }

        if ( entity instanceof ContainerEntity ) {
            ContainerEntity ce = (ContainerEntity) entity;
            for ( ContentEntity e : ce.getElements() ) {
                if ( !isUserCreatorWithPermRecursive(ctx, e, currentUser, grant, perms) ) {
                    return false;
                }
            }
        }

        return true;

    }


    /**
     * @param entity
     * @param currentUser
     * @param grant
     * @param perms
     * @return whether the user has the given permissions and is the creator of the entity
     * @throws AccessDeniedException
     */
    @Override
    public boolean isUserCreatorWithPerm ( VFSContext ctx, VFSEntity entity, User currentUser, Grant grant, GrantPermission... perms )
            throws AccessDeniedException {
        if ( perms != null && perms.length > 0 ) {
            if ( !this.hasAccess(ctx, entity, perms) ) {
                return false;
            }
        }

        if ( entity.getCreator() != null && entity.getCreator().equals(currentUser) ) {
            return true;
        }
        else if ( grant != null && ( grant instanceof TokenGrant && ! ( grant instanceof MailGrant ) ) ) {
            // this is a anonymous grant that might be shared by multiple users
            return false;
        }
        else if ( entity.getCreatorGrant() != null && entity.getCreatorGrant().equals(grant) ) {
            return true;
        }

        return false;
    }


    @Override
    public boolean hasAccess ( VFSContext ctx, VFSEntity e, GrantPermission... perms ) {
        if ( isOwner(ctx, e) ) {
            return true;
        }

        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Checking for perms %s on %s", Arrays.toString(perms), e)); //$NON-NLS-1$
        }

        try {
            Grant grant = getGrant(ctx, e, false, EnumSet.copyOf(Arrays.asList(perms)), true);
            if ( grant != null ) {
                log.trace("Found grant match"); //$NON-NLS-1$
                if ( grant instanceof TokenGrant ) {
                    checkGrantPassword((TokenGrant) grant);
                }
                return true;
            }
        }
        catch ( AuthenticationException ex ) {
            log.debug("Failed to check for grant", ex); //$NON-NLS-1$
        }

        return false;
    }


    /**
     * @param grant
     * @throws GrantAuthenticationRequiredException
     */
    @Override
    public void checkGrantPassword ( TokenGrant grant ) throws GrantAuthenticationRequiredException {
        if ( !grant.getPasswordProtected() ) {
            return;
        }

        log.trace("Need to check grant password"); //$NON-NLS-1$

        String savedPassword = getGrantPassword(grant);

        if ( StringUtils.isEmpty(savedPassword) ) {
            throw new GrantAuthenticationRequiredException(grant, "No password present for grant"); //$NON-NLS-1$
        }

        if ( !PasswordCompareUtil.comparePassword(grant.getPassword(), savedPassword) ) {
            throw new GrantAuthenticationRequiredException(grant, "Wrong grant password"); //$NON-NLS-1$
        }
    }


    /**
     * @param grant
     * @return
     * @throws GrantAuthenticationRequiredException
     */
    private static String getGrantPassword ( TokenGrant grant ) throws GrantAuthenticationRequiredException {
        Subject subject = SecurityUtils.getSubject();
        TokenPrincipal token = subject.getPrincipals().oneByType(TokenPrincipal.class);
        if ( token != null ) {
            Object data = token.getData();
            if ( data instanceof AnonymousGrantToken && !StringUtils.isBlank( ( (AnonymousGrantToken) data ).getPassword()) ) {
                return ( (AnonymousGrantToken) data ).getPassword();
            }
        }

        Session session = subject.getSession(false);
        if ( session == null ) {
            throw new GrantAuthenticationRequiredException(grant, "No active session"); //$NON-NLS-1$
        }

        return (String) session.getAttribute("grantpw_" + grant.getId()); //$NON-NLS-1$
    }


    /**
     * 
     * @param e
     * @param perms
     * @return whether the current user has any permission of the provided ones
     * @throws GrantAuthenticationRequiredException
     */
    @Override
    public boolean hasAnyAccess ( VFSContext ctx, VFSEntity e, GrantPermission... perms ) throws GrantAuthenticationRequiredException {
        if ( isOwner(ctx, e) ) {
            return true;
        }

        try {
            if ( getGrant(ctx, e, true, EnumSet.copyOf(Arrays.asList(perms)), true) != null ) {
                return true;
            }
        }
        catch ( GrantAuthenticationRequiredException ex ) {
            throw ex;
        }
        catch ( AuthenticationException ex ) {
            log.debug("Failed to check for grant", ex); //$NON-NLS-1$
            return false;
        }

        return false;
    }


    @Override
    public SubjectGrant getAnySubjectGrant ( VFSContext ctx, VFSEntity e ) throws AuthenticationException {
        Grant g = getGrant(ctx, e, true, EnumSet.allOf(GrantPermission.class), false);

        if ( g instanceof SubjectGrant ) {
            return (SubjectGrant) g;
        }

        return null;
    }


    /**
     * 
     * @param e
     * @param writeable
     * @return
     * @throws AuthenticationException
     */
    private Grant getGrant ( VFSContext ctx, VFSEntity e, boolean any, Set<GrantPermission> perms, boolean useToken ) throws AuthenticationException {

        if ( e == null ) {
            return null;
        }

        Set<Group> groupClosure = Collections.EMPTY_SET;
        Collection<UserPrincipal> userPrincipals = getCurrentUserPrincipals();

        String tokenValue = null;

        if ( useToken && this.isTokenAuth() ) {
            log.trace("Is token autentication"); //$NON-NLS-1$
            tokenValue = Base64.encodeBase64String( ( (AnonymousGrantToken) this.getTokenAuthValue() ).getNonce());
        }
        else {
            groupClosure = this.getCurrentUserGroupClosure();
        }

        Grant found = hasGrantInternal(ctx, e, any, perms, userPrincipals, groupClosure, tokenValue);
        if ( found != null ) {

            if ( found instanceof TokenGrant ) {
                checkGrantPassword((TokenGrant) found);
            }

            return found;
        }

        return null;
    }


    @Override
    public TokenGrant getTokenAuthGrant ( VFSContext ctx, VFSEntity e ) {
        if ( !this.isTokenAuth() || e == null ) {
            return null;
        }

        TokenGrant g = findGrantByTokenId(
            ctx,
            e,
            Base64.encodeBase64String( ( (AnonymousGrantToken) this.getTokenAuthValue() ).getNonce()),
            EnumSet.noneOf(GrantPermission.class));

        if ( g != null ) {
            try {
                checkGrantPassword(g);
            }
            catch ( GrantAuthenticationRequiredException e1 ) {
                log.warn("Need authentication for grant", e1); //$NON-NLS-1$
                return null;
            }
        }

        return g;
    }


    /**
     * @param e
     * @param encodeBase64String
     * @return
     */
    private TokenGrant findGrantByTokenId ( VFSContext ctx, VFSEntity e, String tokenValue, Set<GrantPermission> perms ) {

        if ( ctx == null || e == null ) {
            return null;
        }

        ContentEntity mapped = ctx.findMappedEntity(e);
        if ( mapped != null ) {
            for ( Grant g : mapped.getGrants() ) {

                if ( !GrantPermission.implies(g.getPermissions(), perms) ) {
                    continue;
                }

                if ( g.getExpires() != null && g.getExpires().isBeforeNow() ) {
                    continue;
                }

                if ( g instanceof TokenGrant && tokenValue != null ) {
                    TokenGrant tg = (TokenGrant) g;
                    if ( constantTimeTokenCompare(tokenValue, tg.getToken()) ) {
                        return tg;
                    }
                }
            }
        }

        VFSContainerEntity parent;
        try {
            parent = ctx.getParent(e);
            if ( parent != null ) {
                return findGrantByTokenId(ctx, parent, tokenValue, perms);
            }
        }
        catch ( FileshareException ex ) {
            log.error("Failed to get parent element", ex); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * 
     * @param e
     * @param any
     * @param writeable
     * @param userPrincipals
     * @param groupClosure
     * @param tokenValue
     * @return
     */
    private Grant hasGrantInternal ( VFSContext v, VFSEntity e, boolean any, Set<GrantPermission> perms, Collection<UserPrincipal> userPrincipals,
            Set<Group> groupClosure, String tokenValue ) {

        if ( e == null ) {
            return null;
        }

        ContentEntity mapped = v.findMappedEntity(e);
        if ( mapped != null ) {
            Set<Grant> grants = mapped.getGrants();

            if ( grants.isEmpty() && log.isTraceEnabled() ) {
                log.trace("No grants on element"); //$NON-NLS-1$
            }

            Grant found = findGrant(grants, any, perms, userPrincipals, groupClosure, tokenValue);
            if ( found != null ) {
                if ( log.isTraceEnabled() ) {
                    log.trace(String.format("Found grant %s on %s", found, e)); //$NON-NLS-1$
                }
                return found;
            }
        }

        if ( log.isTraceEnabled() ) {
            log.trace("Did not find grant on target element " + e); //$NON-NLS-1$
        }

        try {
            VFSContainerEntity parent = v.getParent(e);
            if ( parent != null ) {
                return hasGrantInternal(v, parent, any, perms, userPrincipals, groupClosure, tokenValue);
            }
        }
        catch ( FileshareException ex ) {
            log.error("Failed to get entity parent", ex); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * @param grants
     * @param any
     * @param userPrincipals
     * @param groupClosure
     * @param tokenValue
     * @return
     */
    private static Grant findGrant ( Set<Grant> grants, boolean any, Set<GrantPermission> perms, Collection<UserPrincipal> userPrincipals,
            Set<Group> groupClosure, String tokenValue ) {
        for ( Grant g : grants ) {
            // TODO: this does not take into account that there may be multiple grants which combined would yield the
            // required permissions
            if ( !any && !GrantPermission.implies(g.getPermissions(), perms) ) {
                if ( log.isTraceEnabled() ) {
                    log.trace(String.format("Permissions %s are not implied by %s", perms, g.getPermissions())); //$NON-NLS-1$
                }
                continue;
            }
            else if ( any ) {
                Set<GrantPermission> matches = EnumSet.copyOf(g.getPermissions());
                matches.retainAll(perms);
                if ( matches.isEmpty() ) {
                    log.trace("No match for any permission"); //$NON-NLS-1$
                    continue;
                }
            }

            if ( g.getExpires() != null && g.getExpires().isBeforeNow() ) {
                if ( log.isTraceEnabled() ) {
                    log.trace("Grant already expired" + g); //$NON-NLS-1$
                }
                continue;
            }

            if ( g instanceof TokenGrant && tokenValue != null ) {
                TokenGrant tg = (TokenGrant) g;
                if ( constantTimeTokenCompare(tokenValue, tg.getToken()) ) {
                    return g;
                }

                if ( log.isTraceEnabled() ) {
                    log.trace("Token does not match: " + tg.getToken()); //$NON-NLS-1$
                }
            }

            if ( ! ( g instanceof SubjectGrant ) ) {
                log.trace("Not a subject grant and no token match"); //$NON-NLS-1$
                continue;
            }

            eu.agno3.fileshare.model.Subject target = ( (SubjectGrant) g ).getTarget();
            if ( target instanceof User && userPrincipals.contains( ( (User) target ).getPrincipal()) ) {
                return g;
            }

            if ( target instanceof Group && groupClosure.contains(target) ) {
                return g;
            }

            log.trace("No subject match " + target); //$NON-NLS-1$
        }
        return null;
    }


    /**
     * Constant time string compare, to not leak timing for token comparisions
     * 
     * compares 64 chars
     * 
     * @param a
     * @param b
     * @return whether the two tokens match
     */
    private static boolean constantTimeTokenCompare ( String a, String b ) {
        boolean match = true;
        int lengthA = a.length();
        int lengthB = b.length();

        if ( lengthA != lengthB ) {
            match = false;
        }

        for ( int i = 0; i < 64; i++ ) {
            if ( a.charAt(i % lengthA) != b.charAt(i % lengthB) ) {
                match = false;
            }
        }

        return match;
    }


    @Override
    public void checkOwner ( VFSContext ctx, VFSEntity e ) throws AccessDeniedException {
        if ( !isOwner(ctx, e) ) {
            throw new AccessDeniedException("Ownership check failed " + e); //$NON-NLS-1$
        }
    }


    /**
     * @param e
     * @return whether the current user is owner of the entity, or member of the owning group
     */
    @Override
    public boolean isOwner ( VFSContext ctx, VFSEntity e ) {
        if ( e == null ) {
            return false;
        }
        if ( e.getOwner() instanceof User ) {
            return isCurrentUser((User) e.getOwner());
        }
        else if ( e.getOwner() instanceof Group ) {
            return isMember((Group) e.getOwner());
        }
        return false;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.security.AccessControlService#checkMember(eu.agno3.fileshare.model.Group)
     */
    @Override
    public void checkMember ( Group g ) throws AccessDeniedException {
        if ( !this.isMember(g) ) {
            throw new AccessDeniedException("Not a member of group"); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.security.AccessControlService#hasPermission(java.lang.String)
     */
    @Override
    public boolean hasPermission ( String perm ) {
        return SecurityUtils.getSubject().isPermitted(perm);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.security.AccessControlService#checkPermission(java.lang.String)
     */
    @Override
    public void checkPermission ( String perm ) throws AccessDeniedException {
        if ( !hasPermission(perm) ) {
            throw new AccessDeniedException("User does not have the required permission: " + perm); //$NON-NLS-1$
        }
    }


    /**
     * @param group
     * @return whether the current user is member of the group
     */
    @Override
    public boolean isMember ( Group group ) {
        Collection<UserPrincipal> currentUserPrincipals = getCurrentUserPrincipals();
        if ( currentUserPrincipals.isEmpty() ) {
            return false;
        }

        Set<eu.agno3.fileshare.model.Subject> checked = new HashSet<>();
        return isCurrentUserMemberInternal(group, currentUserPrincipals, checked);
    }


    /**
     * @param group
     * @param currentUserPrincipals
     * @param checked
     * @return
     */
    private boolean isCurrentUserMemberInternal ( Group group, Collection<UserPrincipal> currentUserPrincipals,
            Set<eu.agno3.fileshare.model.Subject> checked ) {
        if ( group == null ) {
            return false;
        }
        Set<eu.agno3.fileshare.model.Subject> members = group.getMembers();
        if ( members == null ) {
            return false;
        }
        for ( eu.agno3.fileshare.model.Subject member : members ) {
            if ( checked.contains(member) ) {
                continue;
            }
            checked.add(member);
            if ( member instanceof User ) {
                if ( currentUserPrincipals.contains( ( (User) member ).getPrincipal()) ) {
                    return true;
                }
            }
            else if ( member instanceof Group ) {
                if ( isCurrentUserMemberInternal((Group) member, currentUserPrincipals, checked) ) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.security.AccessControlService#isUserAuthenticated()
     */
    @Override
    public boolean isUserAuthenticated () {
        Collection<UserPrincipal> currentUserPrincipals = getCurrentUserPrincipals();
        if ( currentUserPrincipals.isEmpty() ) {
            return false;
        }
        return true;
    }


    /**
     * @param e
     * @return
     */
    private static boolean isCurrentUser ( User e ) {
        return getCurrentUserPrincipals().contains(e.getPrincipal());
    }


    /**
     * @return
     */
    private static Collection<UserPrincipal> getCurrentUserPrincipals () {
        Subject subject = SecurityUtils.getSubject();
        if ( !subject.isAuthenticated() ) {
            return Collections.EMPTY_LIST;
        }

        Collection<UserPrincipal> byType = subject.getPrincipals().byType(UserPrincipal.class);

        if ( byType.isEmpty() ) {
            return Collections.EMPTY_LIST;
        }

        return byType;
    }


    /**
     * @return the current user principal
     * @throws AuthenticationException
     */
    @Override
    public UserPrincipal getCurrentUserPrincipal () throws AuthenticationException {
        Subject subject = SecurityUtils.getSubject();
        PrincipalCollection col = subject.getPrincipals();

        if ( !subject.isAuthenticated() || col == null || col.isEmpty() ) {
            throw new AuthenticationException("Not authenticated"); //$NON-NLS-1$
        }

        Collection<UserPrincipal> userPrincs = col.byType(UserPrincipal.class);

        if ( userPrincs.size() != 1 ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Principals:" + userPrincs); //$NON-NLS-1$
            }
            throw new AuthenticationException("Not properly authenticated"); //$NON-NLS-1$
        }

        return userPrincs.iterator().next();
    }


    @Override
    public Set<Group> getCurrentUserGroupClosure () throws AuthenticationException {
        if ( !isUserAuthenticated() ) {
            return Collections.EMPTY_SET;
        }

        try ( EntityTransactionContext tx = this.fileshareEts.startReadOnly() ) {
            return getCurrentUserGroupClosure(tx);
        }
        catch ( EntityTransactionException e ) {
            throw new AuthenticationException("Internal error", e); //$NON-NLS-1$
        }
    }


    @Override
    public Set<Group> getCurrentUserGroupClosure ( EntityTransactionContext tx ) throws AuthenticationException {
        Set<Group> groups = new HashSet<>();
        User user = findByPrincipal(tx, getCurrentUserPrincipal());

        if ( user == null ) {
            throw new AuthenticationException("User not found"); //$NON-NLS-1$
        }

        addGroupsRecursive(groups, user.getMemberships());
        return groups;
    }


    /**
     * @param groups
     * @param memberships
     */
    private void addGroupsRecursive ( Collection<Group> groups, Set<Group> memberships ) {
        for ( Group g : memberships ) {
            if ( !groups.contains(g) ) {
                groups.add(g);
                addGroupsRecursive(groups, g.getMemberships());
            }
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws UserNotFoundException
     *
     * @see eu.agno3.fileshare.security.AccessControlService#getCurrentUser()
     */
    @Override
    public User getCurrentUser () throws AuthenticationException, UserNotFoundException {
        if ( !isUserAuthenticated() ) {
            return null;
        }

        try ( EntityTransactionContext tx = this.fileshareEts.startReadOnly() ) {
            User user = getCurrentUser(tx);
            this.userCache.put(user.getPrincipal(), user.cloneShallow(false));
            return user;
        }
        catch ( EntityTransactionException e ) {
            throw new AuthenticationException("Internal error", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.security.AccessControlService#getCurrentUserCachable()
     */
    @Override
    public User getCurrentUserCachable () throws AuthenticationException, UserNotFoundException {
        if ( !isUserAuthenticated() ) {
            return null;
        }
        User u = this.userCache.get(getCurrentUserPrincipal());

        if ( u != null ) {
            return u;
        }

        return getCurrentUser();
    }


    /**
     * @param tx
     * @return the current user
     * @throws AuthenticationException
     */
    @Override
    public User getCurrentUser ( EntityTransactionContext tx ) throws AuthenticationException, UserNotFoundException {
        if ( !this.isUserAuthenticated() ) {
            return null;
        }

        UserPrincipal princ = getCurrentUserPrincipal();
        User user = findByPrincipal(tx, princ);
        if ( user == null ) {
            throw new UserNotFoundException();
        }
        return user;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.security.AccessControlService#clearAuthorizationCaches(eu.agno3.runtime.security.principal.UserPrincipal)
     */
    @Override
    public void clearAuthorizationCaches ( UserPrincipal princ ) {
        this.authorizer.clearCaches(princ);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.security.AccessControlService#getMappedRoles(eu.agno3.runtime.security.principal.UserPrincipal)
     */
    @Override
    public Set<String> getMappedRoles ( UserPrincipal princ ) {
        SimpleAuthorizationInfo authorizationInfo = this.authorizer.getAuthorizationInfo(new SimplePrincipalCollection(princ, StringUtils.EMPTY));
        return authorizationInfo.getRoles();
    }


    /**
     * @param princ
     * @return the user for the principal or null if none exists
     */
    @Override
    public User findByPrincipal ( EntityTransactionContext tx, UserPrincipal princ ) {
        EntityManager em = tx.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> q = cb.createQuery(User.class);
        Root<User> from = q.from(User.class);
        EntityType<User> userMeta = em.getMetamodel().entity(User.class);
        q.where(cb.equal(from.get(userMeta.getSingularAttribute("principal", UserPrincipal.class)), princ)); //$NON-NLS-1$
        List<User> res = em.createQuery(q).getResultList();
        if ( res.isEmpty() ) {
            return null;
        }

        if ( res.size() > 1 ) {
            log.warn("Multiple users do exist for the principal " + princ); //$NON-NLS-1$
        }
        return res.get(0);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.security.AccessControlService#isTokenAuth()
     */
    @Override
    public boolean isTokenAuth () {
        Subject subject = SecurityUtils.getSubject();
        if ( subject == null || subject.getPrincipals() == null ) {
            return false;
        }
        TokenPrincipal tok = subject.getPrincipals().oneByType(TokenPrincipal.class);
        return tok != null && tok.getData() instanceof AnonymousGrantToken;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.security.AccessControlService#getTokenAuthValue()
     */
    @Override
    public AccessToken getTokenAuthValue () {

        HttpServletRequest httpRequest = WebUtils.getHttpRequest(SecurityUtils.getSubject());

        PrincipalCollection principals;
        if ( httpRequest != null && httpRequest.getAttribute(CRYPTO_TOKEN_ATTR) instanceof PrincipalCollection ) {
            log.trace("Fetching principals from request attribute"); //$NON-NLS-1$
            principals = (PrincipalCollection) httpRequest.getAttribute(CRYPTO_TOKEN_ATTR);
        }
        else {
            log.trace("Fetching principals from subject"); //$NON-NLS-1$
            principals = SecurityUtils.getSubject().getPrincipals();
        }

        if ( principals == null ) {
            return null;
        }

        TokenPrincipal tok = principals.oneByType(TokenPrincipal.class);

        if ( tok == null ) {
            return null;
        }

        Object o = tok.getData();

        if ( ! ( o instanceof AccessToken ) ) {
            log.warn("Not an access token " + o); //$NON-NLS-1$
            return null;
        }

        return (AccessToken) o;
    }


    @Override
    public boolean matchAuthTokenValue ( String tokenValue ) {

        if ( !this.isTokenAuth() ) {
            return false;
        }

        String encUserToken = Base64.encodeBase64String( ( (AnonymousGrantToken) this.getTokenAuthValue() ).getNonce());
        boolean res = constantTimeTokenCompare(encUserToken, tokenValue);

        if ( log.isDebugEnabled() && !res ) {
            log.debug(String.format("Token nonce did not match actual: %s given: %s", tokenValue, encUserToken)); //$NON-NLS-1$
        }

        return res;
    }


    @Override
    public boolean haveIntent () throws AccessControlException {
        AccessToken at = getTokenAuthValue();

        if ( at == null ) {
            return false;
        }

        if ( at instanceof SessionIntentToken ) {
            SessionIntentToken sit = (SessionIntentToken) at;
            Serializable expectSess = sit.getWithIntentSessionId();
            Serializable haveSess = getSessionId();
            if ( expectSess == null || !expectSess.equals(haveSess) ) {
                log.debug("Intent is from wrong session id"); //$NON-NLS-1$
                return false;
            }
            log.debug("Intent verified by session id"); //$NON-NLS-1$
        }

        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.security.AccessControlService#getSessionId()
     */
    @Override
    public Serializable getSessionId () {
        Session s = SecurityUtils.getSubject().getSession();

        if ( s == null ) {
            return null;
        }

        return s.getId();
    }
}
