/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 24, 2017 by mbechler
 */
package eu.agno3.runtime.db.orm.tx;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.apache.log4j.Logger;

import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;


/**
 * @author mbechler
 *
 */
public class JdbcEntityTransactionContext implements EntityTransactionContext {

    private static final Logger log = Logger.getLogger(JdbcEntityTransactionContext.class);
    private final EntityManager em;
    private final boolean readOnly;
    private EntityTransaction transaction;
    private boolean commit;


    /**
     * @param emf
     * @param readOnly
     */
    public JdbcEntityTransactionContext ( EntityManagerFactory emf, boolean readOnly ) {
        this.readOnly = readOnly;
        this.em = emf.createEntityManager();
        // if ( !readOnly ) {
        this.transaction = this.em.getTransaction();
        log.trace("Beginning transaction"); //$NON-NLS-1$
        this.transaction.begin();
        if ( readOnly ) {
            this.commit = true;
        }
        // }
        // else {
        // log.trace("Read only transaction"); //$NON-NLS-1$
        // this.transaction = null;
        // }
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
        return this.em;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.orm.EntityTransactionContext#commit()
     */
    @Override
    public void commit () throws EntityTransactionException {
        if ( this.transaction != null ) {
            this.em.flush();
            this.commit = true;
        }
        else {
            throw new EntityTransactionException("Trying to commit a read only transaction"); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.orm.EntityTransactionContext#rollback()
     */
    @Override
    public void rollback () throws EntityTransactionException {
        if ( this.transaction != null ) {
            this.transaction.setRollbackOnly();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.db.orm.EntityTransactionContext#close()
     */
    @Override
    public synchronized void close () throws EntityTransactionException {
        if ( this.transaction != null ) {
            if ( !this.commit ) {
                log.trace("Rollback transaction"); //$NON-NLS-1$
                this.transaction.rollback();
            }
            else {
                log.trace("Commit transaction"); //$NON-NLS-1$
                this.transaction.commit();
            }
            this.transaction = null;
        }
        else {
            log.trace("End transaction"); //$NON-NLS-1$
        }
    }

}
