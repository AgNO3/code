/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.12.2014 by mbechler
 */
package eu.agno3.runtime.transaction.internal;


import eu.agno3.runtime.transaction.TransactionContext;


/**
 * @author mbechler
 *
 */
public class NOPTransactionContext implements TransactionContext {

    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close () {
        // nothing to do
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.transaction.TransactionContext#commit()
     */
    @Override
    public void commit () {
        // nothing to do
    }

}
