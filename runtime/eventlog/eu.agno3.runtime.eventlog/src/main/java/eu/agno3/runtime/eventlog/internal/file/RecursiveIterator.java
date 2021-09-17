/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.05.2015 by mbechler
 */
package eu.agno3.runtime.eventlog.internal.file;


import java.util.Iterator;


/**
 * @author mbechler
 * @param <T>
 *
 */
public class RecursiveIterator <T> implements Iterator<T> {

    private Iterator<Iterator<T>> it;
    private Iterator<T> cur;


    /**
     * @param it
     * 
     */
    public RecursiveIterator ( Iterator<Iterator<T>> it ) {
        this.it = it;
        if ( it.hasNext() ) {
            this.cur = it.next();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext () {
        return this.cur != null && this.cur.hasNext();
    }


    /**
     * @return the cur
     */
    protected Iterator<T> getCur () {
        return this.cur;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#next()
     */
    @Override
    public T next () {
        T obj = this.cur.next();
        if ( this.cur.hasNext() ) {
            return obj;
        }

        if ( this.it.hasNext() ) {
            closeSubiterator(this.cur);
            this.cur = this.it.next();
        }
        else {
            closeSubiterator(this.cur);
            this.cur = null;
        }

        return obj;
    }


    /**
     * @param toClose
     */
    protected void closeSubiterator ( Iterator<T> toClose ) {

    }


    /**
     * 
     */
    public void close () {
        if ( this.cur != null ) {
            closeSubiterator(this.cur);
        }
    }
}
