/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.01.2015 by mbechler
 */
package eu.agno3.runtime.security;


import java.util.Collection;
import java.util.Set;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.RolePermissionResolver;


/**
 * @author mbechler
 *
 */
public interface PermissionMapper extends RolePermissionResolver {

    /**
     * 
     * @param role
     * @return the permissions that are assigned to these roles
     */
    Set<Permission> getPermissionsForRoles ( Collection<String> role );


    /**
     * @return the roles that are defined by this mapper
     */
    Collection<String> getDefinedRoles ();

}