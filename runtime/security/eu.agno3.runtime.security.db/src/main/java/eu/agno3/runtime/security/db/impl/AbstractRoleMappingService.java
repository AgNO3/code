/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.01.2015 by mbechler
 */
package eu.agno3.runtime.security.db.impl;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.security.SecurityManagementException;
import eu.agno3.runtime.security.db.BaseRole;
import eu.agno3.runtime.security.db.BaseRoleMappingService;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public class AbstractRoleMappingService implements BaseRoleMappingService {

    private static final Logger log = Logger.getLogger(AbstractRoleMappingService.class);
    private static final String USER_ID_ATTR = "userId"; //$NON-NLS-1$
    private static final String ROLE_ATTR = "roleName"; //$NON-NLS-1$


    /**
     * 
     */
    public AbstractRoleMappingService () {
        super();
    }


    @Override
    public Set<String> getMappedRoles ( EntityTransactionContext tx, UserPrincipal user ) throws SecurityManagementException {
        try {
            EntityManager em = tx.getEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();

            CriteriaQuery<String> q = cb.createQuery(String.class);
            Root<BaseRole> from = q.from(BaseRole.class);
            EntityType<BaseRole> model = from.getModel();
            SingularAttribute<? super BaseRole, String> roleAttr = model.getSingularAttribute(ROLE_ATTR, String.class);
            SingularAttribute<? super BaseRole, UUID> userAttr = model.getSingularAttribute(USER_ID_ATTR, UUID.class);

            q.distinct(true);
            q.select(from.get(roleAttr));
            q.where(cb.equal(from.get(userAttr), user.getUserId()));

            List<String> result = em.createQuery(q).getResultList();
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Found %d roles for user %s", result.size(), user)); //$NON-NLS-1$
            }
            return new HashSet<>(result);
        }
        catch ( PersistenceException e ) {
            throw new SecurityManagementException("Failed to get mapped roles", e); //$NON-NLS-1$
        }
    }


    @Override
    public void setMappedRoles ( EntityTransactionContext tx, UserPrincipal user, Set<String> roles ) throws SecurityManagementException {
        Set<String> currentRoles = getMappedRoles(tx, user);

        Set<String> missing = new HashSet<>(roles);
        Set<String> excessive = new HashSet<>(currentRoles);
        missing.removeAll(currentRoles);
        excessive.removeAll(roles);

        if ( log.isDebugEnabled() ) {
            log.debug("Removing roles " + StringUtils.join(excessive, ',')); //$NON-NLS-1$
            log.debug("Adding roles " + StringUtils.join(excessive, ',')); //$NON-NLS-1$
        }

        if ( !excessive.isEmpty() && removeMappedRoles(tx, user, excessive) != excessive.size() ) {
            throw new SecurityManagementException("Failed to remove some user roles"); //$NON-NLS-1$
        }

        if ( !missing.isEmpty() ) {
            addMappedRoles(tx, user, missing);
        }

    }


    @Override
    public void addMappedRoles ( EntityTransactionContext tx, UserPrincipal user, Set<String> roles ) throws SecurityManagementException {

        try {
            EntityManager em = tx.getEntityManager();
            for ( String role : roles ) {
                BaseRole r = new BaseRole();
                r.setUserId(user.getUserId());
                r.setRoleName(role);
                em.persist(r);
            }
            em.flush();
        }
        catch ( PersistenceException e ) {
            throw new SecurityManagementException("Failed to add roles", e); //$NON-NLS-1$
        }
    }


    @Override
    public int removeMappedRoles ( EntityTransactionContext tx, UserPrincipal user, Set<String> roles ) throws SecurityManagementException {
        try {
            EntityManager em = tx.getEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();

            CriteriaDelete<BaseRole> q = cb.createCriteriaDelete(BaseRole.class);
            Root<BaseRole> from = q.from(BaseRole.class);
            EntityType<BaseRole> model = from.getModel();
            SingularAttribute<? super BaseRole, String> roleAttr = model.getSingularAttribute(ROLE_ATTR, String.class);
            SingularAttribute<? super BaseRole, UUID> userAttr = model.getSingularAttribute(USER_ID_ATTR, UUID.class);

            q.where(cb.and(cb.equal(from.get(userAttr), user.getUserId()), from.get(roleAttr).in(roles)));

            return em.createQuery(q).executeUpdate();
        }
        catch ( PersistenceException e ) {
            throw new SecurityManagementException("Failed to remove roles", e); //$NON-NLS-1$
        }
    }

}