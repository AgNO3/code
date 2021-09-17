/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 24, 2017 by mbechler
 */
package eu.agno3.runtime.db.orm;


import javax.persistence.EntityManager;


/**
 * @author mbechler
 *
 */
public interface EntityTransactionContext extends AutoCloseable {

    /**
     * 
     * @return whether this is a read only transaction
     */
    boolean isReadOnly ();


    /**
     * 
     * @return the entity manager
     */
    EntityManager getEntityManager ();


    /**
     * Mark transaction for commit
     * 
     * @throws EntityTransactionException
     */
    void commit () throws EntityTransactionException;


    /**
     * Mark transaction for rollback
     * 
     * @throws EntityTransactionException
     */
    void rollback () throws EntityTransactionException;


    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    void close () throws EntityTransactionException;
}
