/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 24, 2017 by mbechler
 */
package eu.agno3.runtime.db.orm.tx;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.SystemException;

import org.apache.log4j.Logger;

import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.transaction.TransactionContext;
import eu.agno3.runtime.transaction.TransactionService;


/**
 * @author mbechler
 *
 */
public class CMTEntityTransactionContext implements EntityTransactionContext {

    private static final Logger log = Logger.getLogger(CMTEntityTransactionContext.class);

    private final EntityManagerFactory emf;
    private final TransactionService ts;

    private final TransactionContext transContext;
    private final EntityManager entityManager;

    private final boolean readOnly;


    /**
     * @param ts
     * @param emf
     * @param readOnly
     * 
     */
    public CMTEntityTransactionContext ( TransactionService ts, EntityManagerFactory emf, boolean readOnly ) {
        this.ts = ts;
        this.emf = emf;
        this.readOnly = readOnly;
        if ( !readOnly ) {
            this.transContext = this.ts.ensureTransacted();
            try {
                this.entityManager = this.emf.createEntityManager();
            }
            catch ( Exception e ) {
                this.transContext.close();
                throw e;
            }
        }
        else {
            this.transContext = null;
            this.entityManager = this.emf.createEntityManager();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.orm.EntityTransactionContext#isReadOnly()
     */
    @Override
    public boolean isReadOnly () {
        return this.readOnly;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.orm.EntityTransactionContext#getEntityManager()
     */
    @Override
    public EntityManager getEntityManager () {
        return this.entityManager;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.orm.EntityTransactionContext#commit()
     */
    @Override
    public void commit () {
        log.debug("Flushing and marking for commit"); //$NON-NLS-1$
        this.entityManager.flush();
        if ( this.transContext != null ) {
            this.transContext.commit();
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws EntityTransactionException
     *
     * @see eu.agno3.runtime.db.orm.EntityTransactionContext#rollback()
     */
    @Override
    public void rollback () throws EntityTransactionException {
        if ( this.transContext == null ) {
            return;
        }
        log.debug("Marking for rollback"); //$NON-NLS-1$
        try {
            this.ts.createUserTransaction().setRollbackOnly();
        }
        catch (
            IllegalStateException |
            SystemException e ) {

            throw new EntityTransactionException("Failed to set transaction to rollback only", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.orm.EntityTransactionContext#close()
     */
    @Override
    public synchronized void close () throws EntityTransactionException {
        log.debug("Reached end of scope, finishing"); //$NON-NLS-1$
        TransactionContext tc = this.transContext;
        if ( tc != null ) {
            tc.close();
        }
    }

}
