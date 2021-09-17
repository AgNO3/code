/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.12.2014 by mbechler
 */
package eu.agno3.runtime.transaction.internal;


import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.apache.log4j.Logger;

import eu.agno3.runtime.transaction.InternalTransactionException;
import eu.agno3.runtime.transaction.TransactionContext;


/**
 * @author mbechler
 *
 */
public class TransactionContextImpl implements TransactionContext {

    private static final Logger log = Logger.getLogger(TransactionContextImpl.class);

    private boolean commit;
    private TransactionManager transactionManager;


    /**
     * @param transactionManager
     */
    public TransactionContextImpl ( TransactionManager transactionManager ) {
        this.transactionManager = transactionManager;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close () {

        try {
            int status = this.transactionManager.getStatus();
            if ( status == Status.STATUS_MARKED_ROLLBACK ) {
                log.trace("Rolling back transaction"); //$NON-NLS-1$
                doRollback();
                return;
            }
            else if ( status != Status.STATUS_ACTIVE ) {
                return;
            }
        }
        catch ( SystemException e ) {
            throw new InternalTransactionException("Failed to determine transaction status", e); //$NON-NLS-1$
        }

        if ( this.commit ) {
            log.trace("Commiting transaction"); //$NON-NLS-1$
            doCommit();
        }
        else {
            log.trace("Rolling back transaction"); //$NON-NLS-1$
            doRollback();
        }
    }


    /**
     * @param started
     */
    private void doCommit () {
        try {
            this.transactionManager.commit();
        }
        catch (
            IllegalStateException |
            SecurityException |
            HeuristicMixedException |
            HeuristicRollbackException |
            RollbackException |
            SystemException e ) {
            throw new InternalTransactionException("Failed to commit transaction", e); //$NON-NLS-1$
        }
    }


    private void doRollback () {
        try {
            this.transactionManager.rollback();
        }
        catch (
            IllegalStateException |
            SecurityException |
            SystemException e ) {
            throw new InternalTransactionException("Failed to rollback transaction", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.transaction.TransactionContext#commit()
     */
    @Override
    public void commit () {
        this.commit = true;
    }

}
