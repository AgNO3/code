/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;

import eu.agno3.fileshare.exceptions.TokenReuseException;
import eu.agno3.fileshare.exceptions.TokenValidationException;
import eu.agno3.fileshare.model.UsedTokenTracker;
import eu.agno3.fileshare.model.tokens.SingleUseToken;
import eu.agno3.fileshare.service.api.internal.SingleUseTokenService;
import eu.agno3.runtime.db.orm.EntityTransactionContext;


/**
 * @author mbechler
 *
 */
@Component ( service = SingleUseTokenService.class )
public class SingleUseTokenServiceImpl implements SingleUseTokenService {

    @Override
    public void checkToken ( EntityTransactionContext tx, SingleUseToken tok ) throws TokenValidationException {
        UsedTokenTracker t = tx.getEntityManager().find(UsedTokenTracker.class, tok.getId());
        if ( t != null ) {
            throw new TokenReuseException("Token has already been used"); //$NON-NLS-1$
        }
    }


    @Override
    public void invalidateToken ( EntityTransactionContext tx, SingleUseToken tok, DateTime expires ) throws TokenValidationException {
        try {
            EntityManager em = tx.getEntityManager();
            UsedTokenTracker tracker = new UsedTokenTracker();
            tracker.setId(tok.getId());
            tracker.setExpires(expires);
            em.persist(tracker);
            em.flush();
        }
        catch ( PersistenceException e ) {
            throw new TokenValidationException("Could not invalidate token", e); //$NON-NLS-1$
        }
    }


    @Override
    public int cleanup ( EntityTransactionContext tx ) {
        EntityManager em = tx.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<UsedTokenTracker> query = cb.createCriteriaDelete(UsedTokenTracker.class);
        Root<UsedTokenTracker> from = query.from(UsedTokenTracker.class);
        SingularAttribute<? super UsedTokenTracker, DateTime> expires = em.getMetamodel().entity(UsedTokenTracker.class)
                .getSingularAttribute("expires", DateTime.class); //$NON-NLS-1$
        query.where(cb.lessThanOrEqualTo(from.get(expires), DateTime.now()));
        return em.createQuery(query).executeUpdate();
    }
}
