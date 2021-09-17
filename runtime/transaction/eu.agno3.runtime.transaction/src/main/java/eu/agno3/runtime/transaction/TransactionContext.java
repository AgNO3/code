/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.12.2014 by mbechler
 */
package eu.agno3.runtime.transaction;


/**
 * @author mbechler
 *
 */
public interface TransactionContext extends AutoCloseable {

    /**
     * Signal that the transaction should be committed
     */
    void commit ();


    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    void close ();

}
