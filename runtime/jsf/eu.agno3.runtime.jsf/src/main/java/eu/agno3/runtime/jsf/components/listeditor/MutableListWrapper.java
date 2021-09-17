/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.04.2014 by mbechler
 */
package eu.agno3.runtime.jsf.components.listeditor;


import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.Nullable;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public class MutableListWrapper <@Nullable T> extends AbstractList<ListElementWrapper<T>> implements List<ListElementWrapper<T>> {

    private static final Logger log = Logger.getLogger(MutableListWrapper.class);

    private List<T> delegate;
    private List<ListElementWrapper<T>> wrapped;


    /**
     * @param delegate
     */
    public MutableListWrapper ( List<T> delegate ) {
        if ( delegate == null ) {
            throw new IllegalArgumentException("Delgate must not be null"); //$NON-NLS-1$
        }

        this.delegate = delegate;
        this.wrapped = new ArrayList<>(this.delegate.size());

        for ( int i = 0; i < delegate.size(); i++ ) {
            this.wrapped.add(new ListElementWrapper<>(this, i));
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.util.AbstractList#get(int)
     */
    @Override
    public ListElementWrapper<T> get ( int index ) {
        ListElementWrapper<T> elem = this.wrapped.get(index);
        return elem;
    }


    @Override
    public int size () {
        return this.wrapped.size();
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.util.AbstractList#add(java.lang.Object)
     */
    @Override
    public boolean add ( ListElementWrapper<T> e ) {
        return this.add(e, null);
    }


    /**
     * 
     * @param e
     * @param item
     * @return
     */
    protected boolean add ( ListElementWrapper<T> e, T item ) {
        synchronized ( this.delegate ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Adding element " + item); //$NON-NLS-1$
            }
            int idx = this.delegate.size();

            e.setIndex(idx);
            this.delegate.add(idx, item);
            return this.wrapped.add(e);
        }
    }


    /**
     * @param from
     * @param to
     */
    public void moveTo ( int from, int to ) {
        synchronized ( this.delegate ) {

            if ( from == to ) {
                return;
            }

            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Moving element from %d to %d", from, to)); //$NON-NLS-1$
                log.trace("Wrappers before move " + this.wrapped); //$NON-NLS-1$
            }

            if ( from >= this.delegate.size() || to >= this.delegate.size() ) {
                return;
            }

            ListElementWrapper<T> wrapper = this.wrapped.remove(from);

            if ( log.isTraceEnabled() ) {
                log.trace("Source is " + wrapper.getValue()); //$NON-NLS-1$
            }

            if ( wrapper == null ) {
                return;
            }

            // int target = from < to ? ( to - 1 ) : to;
            int target = to;
            if ( log.isTraceEnabled() ) {
                log.trace("Target index is " + target); //$NON-NLS-1$

            }

            wrapper.setIndex(target);
            this.wrapped.add(target, wrapper);

            if ( log.isTraceEnabled() ) {
                log.trace("Wrappers after move " + this.wrapped); //$NON-NLS-1$
                log.trace("Delegate before move " + this.delegate); //$NON-NLS-1$
            }

            for ( int i = Math.min(from, to); i <= Math.max(from, to); i++ ) {
                this.wrapped.get(i).setIndex(i);
            }

            this.delegate.add(target, this.delegate.remove(from));

            if ( log.isTraceEnabled() ) {
                log.trace("Delegate after move " + this.delegate); //$NON-NLS-1$
            }
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.util.AbstractCollection#remove(java.lang.Object)
     */
    @Override
    public ListElementWrapper<T> remove ( int idx ) {
        synchronized ( this.delegate ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Removing element " + idx); //$NON-NLS-1$
            }
            ListElementWrapper<T> wrapper = this.wrapped.remove(idx);

            for ( int i = idx; i < this.wrapped.size(); i++ ) {
                this.wrapped.get(i).setIndex(i);
            }

            this.delegate.remove(idx);

            if ( log.isTraceEnabled() ) {
                log.trace("Delegate after remove " + this.delegate); //$NON-NLS-1$
            }
            return wrapper;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.util.AbstractCollection#remove(java.lang.Object)
     */
    @Override
    public boolean remove ( Object o ) {
        if ( o instanceof ListElementWrapper ) {
            this.remove( ( (ListElementWrapper<@Nullable ?>) o ).getIndex());
            return true;
        }
        return super.remove(o);
    }


    /**
     * @param item
     */
    public void addItem ( T item ) {
        this.add(new ListElementWrapper<>(this, -1), item);
    }


    /**
     * @return
     */
    List<T> getDelegate () {
        return this.delegate;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.util.AbstractList#hashCode()
     */
    @Override
    public int hashCode () {
        return this.delegate.hashCode();
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.util.AbstractList#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object o ) {

        if ( o instanceof MutableListWrapper ) {
            return this.delegate.equals( ( (MutableListWrapper<@Nullable ?>) o ).delegate);
        }

        return this.delegate.equals(o);
    }

}
