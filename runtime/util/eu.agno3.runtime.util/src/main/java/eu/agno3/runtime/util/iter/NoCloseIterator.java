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
public class NoCloseIterator <T> implements ClosableIterator<T> {

    private Iterator<T> delegate;


    /**
     * @param it
     * 
     */
    public NoCloseIterator ( Iterator<T> it ) {
        this.delegate = it;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext () {
        return this.delegate.hasNext();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#next()
     */
    @Override
    public T next () {
        return this.delegate.next();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close () {}

}
