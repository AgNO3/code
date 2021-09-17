/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 24, 2017 by mbechler
 */
package eu.agno3.runtime.db.orm;


/**
 * @author mbechler
 *
 */
public interface EntityTransactionService {

    /**
     * Starts a read-write transaction
     * 
     * @return transaction context
     * @throws EntityTransactionException
     */
    EntityTransactionContext start () throws EntityTransactionException;


    /**
     * Starts a read only transaction
     * 
     * Read only transaction do not need to be commited
     * 
     * @return transaction context
     * @throws EntityTransactionException
     */
    EntityTransactionContext startReadOnly () throws EntityTransactionException;
}
