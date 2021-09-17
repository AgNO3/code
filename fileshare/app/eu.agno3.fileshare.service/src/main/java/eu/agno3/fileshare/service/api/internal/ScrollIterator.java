/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.api.internal;


import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.Nullable;
import org.hibernate.query.Query;

import eu.agno3.runtime.db.orm.EntityTransactionContext;
import eu.agno3.runtime.db.orm.EntityTransactionException;
import eu.agno3.runtime.util.iter.ClosableIterator;


/**
 * @author mbechler
 * @param <T>
 *
 */
public class ScrollIterator <T> implements ClosableIterator<@Nullable T> {

    private static final Logger log = Logger.getLogger(ScrollIterator.class);

    private final Stream<T> scroll;
    private final Class<T> clz;
    private final EntityTransactionContext tx;
    private final Query<T> query;

    private @Nullable T nextObject;
    private boolean closed;

    private Iterator<T> it;


    /**
     * @param clz
     * @param q
     * @param tx
     */
    public ScrollIterator ( Class<T> clz, Query<T> q, EntityTransactionContext tx ) {
        this.clz = clz;
        this.query = q;
        this.tx = tx;
        this.scroll = q.stream();
        this.it = this.scroll.iterator();
        this.nextObject = getNext();
    }


    /**
     * @return the query
     */
    public Query<T> getQuery () {
        return this.query;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext () {
        if ( this.closed ) {
            return true;
        }
        return this.nextObject != null;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close () {
        this.scroll.close();
        this.closed = true;
        try {
            if ( this.tx != null ) {
                this.tx.close();
            }
        }
        catch ( EntityTransactionException e ) {
            throw e.runtime();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#next()
     */

    @Override
    public @Nullable T next () {
        @Nullable
        T current = this.nextObject;
        this.nextObject = getNext();
        return current;
    }


    @SuppressWarnings ( "unchecked" )
    private @Nullable T getNext () {
        if ( this.closed ) {
            throw new IllegalStateException("ScrollIterator already closed"); //$NON-NLS-1$
        }

        try {
            if ( !this.it.hasNext() ) {
                return null;
            }
        }
        catch ( Exception e ) {
            log.warn("Could not get next object", e); //$NON-NLS-1$
            return null;
        }

        Object o = this.it.next();
        if ( o == null || !this.clz.isAssignableFrom(o.getClass()) ) {
            throw new IllegalArgumentException();
        }
        return (@Nullable T) o;
    }
}
