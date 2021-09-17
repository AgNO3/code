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
import eu.agno3.runtime.transaction.TransactionService;


/**
 * @author mbechler
 *
 */
@Component (
    service = EntityTransactionService.class,
    configurationPolicy = ConfigurationPolicy.REQUIRE,
    configurationPid = CMTTransactionServiceImpl.PID )
public class CMTTransactionServiceImpl implements EntityTransactionService {

    /**
     * 
     */
    public static final String PID = "orm.tx.cmt"; //$NON-NLS-1$

    private EntityManagerFactory entityManagerFactory;
    private TransactionService transactionService;


    @Reference ( )
    protected synchronized void bindEntityManagerFactory ( EntityManagerFactory emf ) {
        this.entityManagerFactory = emf;
    }


    protected synchronized void unbindEntityManagerFactory ( EntityManagerFactory emf ) {
        if ( this.entityManagerFactory == emf ) {
            this.entityManagerFactory = null;
        }
    }


    @Reference
    protected synchronized void bindTransactionService ( TransactionService ts ) {
        this.transactionService = ts;
    }


    protected synchronized void unbindTransactionService ( TransactionService ts ) {
        if ( this.transactionService == ts ) {
            this.transactionService = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.orm.EntityTransactionService#start()
     */
    @Override
    public EntityTransactionContext start () throws EntityTransactionException {
        return new CMTEntityTransactionContext(this.transactionService, this.entityManagerFactory, false);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.orm.EntityTransactionService#startReadOnly()
     */
    @Override
    public EntityTransactionContext startReadOnly () throws EntityTransactionException {
        return new CMTEntityTransactionContext(this.transactionService, this.entityManagerFactory, true);
    }

}
