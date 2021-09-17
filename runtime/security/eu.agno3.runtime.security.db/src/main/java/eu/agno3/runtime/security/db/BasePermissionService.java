/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.runtime.security.db;


import java.util.Set;

import javax.persistence.EntityManager;

import eu.agno3.runtime.security.SecurityManagementException;


/**
 * @author mbechler
 *
 */
public interface BasePermissionService {

    /**
     * @param em
     * @return the defined roles
     * @throws SecurityManagementException
     */
    Set<String> getDefinedRoles ( EntityManager em ) throws SecurityManagementException;


    /**
     * @param em
     * @param role
     * @return the permissions for role
     * @throws SecurityManagementException
     */
    Set<String> getRolePermissions ( EntityManager em, String role ) throws SecurityManagementException;


    /**
     * @param em
     * @param role
     * @param permissions
     * @throws SecurityManagementException
     */
    void setRolePermissions ( EntityManager em, String role, Set<String> permissions ) throws SecurityManagementException;


    /**
     * @param em
     * @param role
     * @param permissions
     */
    void addRolePermissions ( EntityManager em, String role, Set<String> permissions );


    /**
     * @param em
     * @param role
     * @param permissions
     * @return the number of removed permissions
     */
    int removeRolePermissions ( EntityManager em, String role, Set<String> permissions );

}
