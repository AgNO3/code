/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2014 by mbechler
 */
package eu.agno3.runtime.security.internal;


import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.security.RolePermissionContributor;


/**
 * @author mbechler
 *
 */
@Component ( service = RolePermissionResolver.class )
public class DelegatingRolePermissionsResolver implements RolePermissionResolver {

    private static final Logger log = Logger.getLogger(DelegatingRolePermissionsResolver.class);

    private Set<RolePermissionContributor> contributors = Collections.synchronizedSet(new HashSet<RolePermissionContributor>());


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindContributor ( RolePermissionContributor contrib ) {
        this.contributors.add(contrib);
    }


    protected synchronized void unbindContributor ( RolePermissionContributor contrib ) {
        this.contributors.remove(contrib);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.authz.permission.RolePermissionResolver#resolvePermissionsInRole(java.lang.String)
     */
    @Override
    public Collection<Permission> resolvePermissionsInRole ( String role ) {

        if ( this.contributors.isEmpty() ) {
            return Collections.EMPTY_SET;
        }

        if ( log.isTraceEnabled() ) {
            log.trace("Resolving permissions for role " + role); //$NON-NLS-1$
        }

        Collection<Permission> perms = new HashSet<>();

        for ( RolePermissionContributor contrib : this.contributors ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Adding permissions from contributor " + contrib.getClass().getName()); //$NON-NLS-1$
            }
            perms.addAll(contrib.resolvePermissionsInRole(role));
        }

        return perms;
    }
}
