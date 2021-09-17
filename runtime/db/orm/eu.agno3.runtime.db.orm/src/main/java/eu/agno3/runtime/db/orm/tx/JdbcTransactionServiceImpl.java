/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 24, 2017 by mbechler
 */
package eu.agno3.runtime.db.orm.tx;


import javax.persistence.EntityManagerFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.db.orm.EntityTransactionService;


/**
 * @author mbechler
 *
 */
@Component (
    service = EntityTransactionService.class,
    configurationPolicy = ConfigurationPolicy.REQUIRE,
    configurationPid = JdbcTransactionServiceImpl.PID )
public class JdbcTransactionServiceImpl implements EntityTransactionService {

    /**
     * 
     */
    public static final String PID = "orm.tx.jdbc"; //$NON-NLS-1$
    private EntityManagerFactory entityManagerFactory;


    @Reference ( )
    protected synchronized void bindEntityManagerFactory ( EntityManagerFactory emf ) {
        this.entityManagerFactory = emf;
    }


    protected synchronized void unbindEntityManagerFactory ( EntityManagerFactory emf ) {
        if ( this.entityManagerFactory == emf ) {
            this.entityManagerFactory = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.orm.EntityTransactionService#start()
     */
    @Override
    public EntityTransactionContext start () throws EntityTransactionException {
        return new JdbcEntityTransactionContext(this.entityManagerFactory, false);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.orm.EntityTransactionService#startReadOnly()
     */
    @Override
    public EntityTransactionContext startReadOnly () throws EntityTransactionException {
        return new JdbcEntityTransactionContext(this.entityManagerFactory, true);
    }
}
