/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.10.2014 by mbechler
 */
package eu.agno3.runtime.security.internal;


import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.permission.PermissionResolver;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.authz.permission.WildcardPermissionResolver;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.security.AuthorizationInfoProvider;
import eu.agno3.runtime.security.DynamicModularRealmAuthorizer;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    DynamicModularRealmAuthorizer.class, ModularRealmAuthorizer.class
} )
public class DynamicMultiRealmAuthorizer extends ModularRealmAuthorizer implements DynamicModularRealmAuthorizer {

    private static final Logger log = Logger.getLogger(DynamicMultiRealmAuthorizer.class);

    private final Set<Realm> realmsRegistered = Collections.synchronizedSet(new HashSet<Realm>());

    private PermissionResolver permissionsResolver;
    private RolePermissionResolver rolePermissionResolvers;


    @Reference
    protected synchronized void bindPermissionResolver ( PermissionResolver r ) {
        this.permissionsResolver = r;
        super.setPermissionResolver(r);
    }


    protected synchronized void unbindPermissionResolver ( PermissionResolver r ) {
        if ( this.permissionsResolver == r ) {
            this.permissionsResolver = null;
            this.setPermissionResolver(null);
        }
    }


    @Reference
    protected synchronized void bindRolePermissionsResolver ( RolePermissionResolver rpr ) {
        this.rolePermissionResolvers = rpr;
        this.setRolePermissionResolver(rpr);
    }


    protected synchronized void unbindRolePermissionsResolver ( RolePermissionResolver rpr ) {
        if ( this.rolePermissionResolvers == rpr ) {
            this.rolePermissionResolvers = null;
            this.setPermissionResolver(null);
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.DynamicModularRealmAuthorizer#getAuthorizationInfo(org.apache.shiro.subject.PrincipalCollection)
     */
    @Override
    public SimpleAuthorizationInfo getAuthorizationInfo ( PrincipalCollection col ) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        for ( Realm r : this.realms ) {
            if ( ! ( r instanceof AuthorizationInfoProvider ) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Not an authorization info provider " + r.getName()); //$NON-NLS-1$
                }
                continue;
            }

            AuthorizationInfoProvider aip = (AuthorizationInfoProvider) r;
            AuthorizationInfo authorizationInfo = aip.fetchAuthorizationInfo(col);
            if ( authorizationInfo != null ) {
                if ( authorizationInfo.getRoles() != null ) {
                    for ( String role : authorizationInfo.getRoles() ) {
                        Collection<Permission> permsForRole = this.rolePermissionResolvers.resolvePermissionsInRole(role);

                        if ( authorizationInfo instanceof SimpleAuthorizationInfo && permsForRole != null ) {
                            ( (SimpleAuthorizationInfo) authorizationInfo ).addObjectPermissions(permsForRole);
                        }
                        else if ( authorizationInfo.getObjectPermissions() != null && permsForRole != null ) {
                            authorizationInfo.getObjectPermissions().addAll(permsForRole);
                        }
                    }
                }
                else {
                    if ( log.isDebugEnabled() ) {
                        log.debug("Did not return any roles " + r.getName()); //$NON-NLS-1$
                    }
                }
                mergeAuthorizationInfo(info, authorizationInfo);
            }
            else if ( log.isDebugEnabled() ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Did not return authorization info " + r.getName()); //$NON-NLS-1$
                }
            }
        }
        return info;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.DynamicModularRealmAuthorizer#clearCaches(eu.agno3.runtime.security.principal.UserPrincipal)
     */
    @Override
    public void clearCaches ( UserPrincipal princs ) {
        for ( Realm r : this.realms ) {
            if ( r instanceof AuthorizationInfoProvider ) {
                ( (AuthorizationInfoProvider) r ).clearCaches(princs);
            }
        }
    }


    /**
     * @param info
     * @param authorizationInfo
     */
    protected void mergeAuthorizationInfo ( SimpleAuthorizationInfo info, AuthorizationInfo authorizationInfo ) {
        if ( authorizationInfo != null ) {
            if ( authorizationInfo.getRoles() != null ) {
                info.addRoles(authorizationInfo.getRoles());
            }

            if ( authorizationInfo.getObjectPermissions() != null ) {
                info.addObjectPermissions(authorizationInfo.getObjectPermissions());
            }

            if ( authorizationInfo.getStringPermissions() != null ) {
                info.addStringPermissions(authorizationInfo.getStringPermissions());
            }
        }
    }


    @Reference ( policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE )
    protected synchronized void bindRealm ( AuthorizingRealm r ) {

        if ( log.isDebugEnabled() ) {
            log.debug("Binding realm " + r); //$NON-NLS-1$
        }

        if ( this.permissionsResolver != null ) {
            r.setPermissionResolver(this.permissionsResolver);
        }

        if ( this.rolePermissionResolvers != null ) {
            r.setRolePermissionResolver(this.rolePermissionResolvers);
        }

        this.realmsRegistered.add(r);
        this.updateRealms();
    }


    protected synchronized void unbindRealm ( AuthorizingRealm r ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Unbinding realm " + r); //$NON-NLS-1$
        }
        this.realmsRegistered.remove(r);
        this.updateRealms();

        r.setPermissionResolver(new WildcardPermissionResolver());
        r.setRolePermissionResolver(null);
    }


    /**
     * 
     */
    private void updateRealms () {
        this.setRealms(this.realmsRegistered);
    }
}
