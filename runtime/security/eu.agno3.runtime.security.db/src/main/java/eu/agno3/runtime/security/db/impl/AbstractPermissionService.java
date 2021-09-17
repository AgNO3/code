/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.runtime.security.db.impl;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.runtime.security.SecurityManagementException;
import eu.agno3.runtime.security.db.BasePermission;
import eu.agno3.runtime.security.db.BasePermissionService;


/**
 * @author mbechler
 *
 */
public class AbstractPermissionService implements BasePermissionService {

    private static final Logger log = Logger.getLogger(AbstractPermissionService.class);

    private static final String PERMISSION_ATTR = "permission"; //$NON-NLS-1$
    private static final String ROLE_ATTR = "roleName"; //$NON-NLS-1$


    /**
     * 
     */
    public AbstractPermissionService () {
        super();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.db.BasePermissionService#getDefinedRoles(javax.persistence.EntityManager)
     */
    @Override
    public Set<String> getDefinedRoles ( EntityManager em ) throws SecurityManagementException {

        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();

            CriteriaQuery<String> q = cb.createQuery(String.class);
            Root<BasePermission> from = q.from(BasePermission.class);
            EntityType<BasePermission> model = from.getModel();
            q.select(from.get(model.getSingularAttribute(ROLE_ATTR, String.class)));
            q.distinct(true);

            return new HashSet<>(em.createQuery(q).getResultList());
        }
        catch ( PersistenceException e ) {
            throw new SecurityManagementException("Failed to fetch defined roles", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.db.BasePermissionService#getRolePermissions(javax.persistence.EntityManager,
     *      java.lang.String)
     */
    @Override
    public Set<String> getRolePermissions ( EntityManager em, String role ) throws SecurityManagementException {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();

            CriteriaQuery<String> q = cb.createQuery(String.class);
            Root<BasePermission> from = q.from(BasePermission.class);
            EntityType<BasePermission> model = from.getModel();
            q.select(from.get(model.getSingularAttribute(PERMISSION_ATTR, String.class)));
            q.where(cb.equal(from.get(from.getModel().getSingularAttribute(ROLE_ATTR, String.class)), role));

            return new HashSet<>(em.createQuery(q).getResultList());
        }
        catch ( PersistenceException e ) {
            throw new SecurityManagementException("Failed to get permissions", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.db.BasePermissionService#setRolePermissions(javax.persistence.EntityManager,
     *      java.lang.String, java.util.Set)
     */
    @Override
    public void setRolePermissions ( EntityManager em, String role, Set<String> permissions ) throws SecurityManagementException {

        Set<String> currentPermissions = this.getRolePermissions(em, role);

        Set<String> missing = new HashSet<>(permissions);
        Set<String> excessive = new HashSet<>(currentPermissions);
        missing.removeAll(currentPermissions);
        excessive.removeAll(permissions);

        if ( log.isDebugEnabled() ) {
            log.debug("Removing permissions " + StringUtils.join(excessive, ',')); //$NON-NLS-1$
            log.debug("Adding permissions " + StringUtils.join(missing, ',')); //$NON-NLS-1$
        }

        if ( !excessive.isEmpty() && this.removeRolePermissions(em, role, excessive) != excessive.size() ) {
            throw new SecurityManagementException("Failed to remove some role permissions"); //$NON-NLS-1$
        }

        if ( !missing.isEmpty() ) {
            this.addRolePermissions(em, role, missing);
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.db.BasePermissionService#addRolePermissions(javax.persistence.EntityManager,
     *      java.lang.String, java.util.Set)
     */
    @Override
    public void addRolePermissions ( EntityManager em, String role, Set<String> permissions ) {
        for ( String permission : permissions ) {
            BasePermission p = new BasePermission();
            p.setRoleName(role);
            p.setPermission(permission);
            em.persist(p);
        }
        // em.flush();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.db.BasePermissionService#removeRolePermissions(javax.persistence.EntityManager,
     *      java.lang.String, java.util.Set)
     */
    @Override
    public int removeRolePermissions ( EntityManager em, String role, Set<String> permissions ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaDelete<BasePermission> q = cb.createCriteriaDelete(BasePermission.class);
        Root<BasePermission> from = q.from(BasePermission.class);

        EntityType<BasePermission> model = from.getModel();
        q.where(
            cb.and(
                cb.equal(from.get(model.getSingularAttribute(ROLE_ATTR, String.class)), role),
                from.get(from.getModel().getSingularAttribute(PERMISSION_ATTR, String.class)).in(permissions)));

        Query delete = em.createQuery(q);

        return delete.executeUpdate();
    }

}