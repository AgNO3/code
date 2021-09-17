/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.01.2015 by mbechler
 */
package eu.agno3.fileshare.security.internal;


import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.log4j.Logger;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.MapCache;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.User;
import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.db.orm.EntityTransactionService;
import eu.agno3.runtime.security.AuthorizationInfoProvider;
import eu.agno3.runtime.security.PermissionMapper;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.security.web.login.token.TokenPrincipal;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    Realm.class, FileshareAuthorizer.class, AuthorizingRealm.class
} )
public class FileshareAuthorizer extends AuthorizingRealm implements AuthorizationInfoProvider {

    private static final Logger log = Logger.getLogger(FileshareAuthorizer.class);

    private PermissionMapper permissionMapper;

    private static final int AUTHZ_CACHE_SIZE = 1024;
    private Map<Object, AuthorizationInfo> authzCache = new LRUMap<>(AUTHZ_CACHE_SIZE);

    private EntityTransactionService fileshareETS;


    @Reference ( service = EntityTransactionService.class, target = "(persistenceUnit=fileshare)" )
    protected synchronized void bindEntityTransactionService ( EntityTransactionService ets ) {
        this.fileshareETS = ets;
    }


    protected synchronized void unbindEntityTransactionService ( EntityTransactionService ets ) {
        if ( this.fileshareETS == ets ) {
            this.fileshareETS = null;
        }
    }


    @Reference
    protected synchronized void setPermissionMapper ( PermissionMapper pm ) {
        this.permissionMapper = pm;
    }


    protected synchronized void unsetPermissionMapper ( PermissionMapper pm ) {
        if ( this.permissionMapper == pm ) {
            this.permissionMapper = null;
        }
    }


    /**
     * 
     */
    public FileshareAuthorizer () {
        setAuthorizationCachingEnabled(true);
        setAuthorizationCache(new MapCache<>("authzCache", this.authzCache)); //$NON-NLS-1$

    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.realm.AuthenticatingRealm#supports(org.apache.shiro.authc.AuthenticationToken)
     */
    @Override
    public boolean supports ( AuthenticationToken token ) {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.realm.AuthorizingRealm#doGetAuthorizationInfo(org.apache.shiro.subject.PrincipalCollection)
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo ( PrincipalCollection princs ) {
        return this.fetchAuthorizationInfo(princs);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.realm.AuthenticatingRealm#doGetAuthenticationInfo(org.apache.shiro.authc.AuthenticationToken)
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo ( AuthenticationToken tok ) throws AuthenticationException {
        // no auth
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.AuthorizationInfoProvider#fetchAuthorizationInfo(org.apache.shiro.subject.PrincipalCollection)
     */
    @Override
    public AuthorizationInfo fetchAuthorizationInfo ( PrincipalCollection princs ) {
        if ( log.isDebugEnabled() ) {
            log.debug("fetchAuthorizationInfo for " + princs); //$NON-NLS-1$
        }

        Collection<UserPrincipal> up = princs.byType(UserPrincipal.class);

        if ( up == null || up.isEmpty() || up.size() != 1 ) {
            if ( princs.oneByType(TokenPrincipal.class) != null ) {
                return new SimpleAuthorizationInfo(getTokenAuthRoles());
            }

            log.debug("Failed to find user principal"); //$NON-NLS-1$
            return null;
        }

        try ( EntityTransactionContext tx = this.fileshareETS.startReadOnly() ) {
            EntityManager em = tx.getEntityManager();

            UserPrincipal princ = up.iterator().next();
            User u = findByPrincipal(em, princ);

            if ( u == null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Failed to find user by principal " + princ); //$NON-NLS-1$
                }
                return null;
            }

            Set<String> roles = new HashSet<>();
            Set<Group> handled = new HashSet<>();

            roles.addAll(u.getRoles());

            recursiveAddRoles(u.getMemberships(), roles, handled);

            SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roles);
            info.addObjectPermissions(this.permissionMapper.getPermissionsForRoles(roles));
            return info;
        }
        catch ( EntityTransactionException e ) {
            throw new AuthenticationException("Internal error", e); //$NON-NLS-1$
        }
    }


    private static HashSet<String> getTokenAuthRoles () {
        return new HashSet<>(Arrays.asList("TOKEN")); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.realm.AuthenticatingRealm#getAuthenticationCacheKey(org.apache.shiro.subject.PrincipalCollection)
     */

    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.realm.AuthorizingRealm#getAuthorizationCacheKey(org.apache.shiro.subject.PrincipalCollection)
     */
    @Override
    protected Object getAuthorizationCacheKey ( PrincipalCollection principals ) {
        return principals.oneByType(UserPrincipal.class);
    }


    /**
     * 
     * @param principals
     * @return
     */
    @Override
    protected Object getAuthenticationCacheKey ( PrincipalCollection principals ) {
        return principals.oneByType(UserPrincipal.class);
    }


    /**
     * 
     * @param princ
     */
    @Override
    public void clearCaches ( UserPrincipal princ ) {
        this.getAuthorizationCache().remove(princ);
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
     * @param princ
     * @return
     */
    private static User findByPrincipal ( EntityManager em, UserPrincipal princ ) {
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

}
