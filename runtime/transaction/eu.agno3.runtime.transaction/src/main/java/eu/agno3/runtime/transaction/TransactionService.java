/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.07.2013 by mbechler
 */
package eu.agno3.runtime.transaction;


import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import com.atomikos.datasource.TransactionalResource;


/**
 * @author mbechler
 * 
 */
public interface TransactionService {

    /**
     * @return the transaction manager
     */
    TransactionManager getTransactionManager ();


    /**
     * @return a new user transaction
     */
    UserTransaction createUserTransaction ();


    /**
     * @return whether a transaction was started
     */
    TransactionContext ensureTransacted ();


    /**
     * @param res
     */
    void registerResource ( TransactionalResource res );


    /**
     * @param res
     */
    void unregisterResource ( TransactionalResource res );
}
