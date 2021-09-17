/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.09.2015 by mbechler
 */
package eu.agno3.runtime.util.iter;


import java.util.Iterator;


/**
 * @author mbechler
 * @param <T>
 *
 */
public interface ClosableIterator <T> extends Iterator<T>, AutoCloseable {

    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close ();
}
