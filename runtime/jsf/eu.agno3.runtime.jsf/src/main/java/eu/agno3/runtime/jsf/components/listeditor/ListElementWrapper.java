/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.04.2014 by mbechler
 */
package eu.agno3.runtime.jsf.components.listeditor;


import org.eclipse.jdt.annotation.Nullable;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public class ListElementWrapper <@Nullable T> {

    private MutableListWrapper<T> wrapper;
    private int index;


    /**
     * @param wrapper
     * @param index
     */
    public ListElementWrapper ( MutableListWrapper<T> wrapper, int index ) {
        this.wrapper = wrapper;
        this.index = index;
    }


    /**
     * @param index
     *            the index to set
     */
    void setIndex ( int index ) {
        this.index = index;
    }


    /**
     * @return the value
     */
    public T getValue () {
        return this.wrapper.getDelegate().get(this.index);
    }


    /**
     * @param value
     *            the value to set
     */
    public void setValue ( T value ) {
        this.wrapper.getDelegate().set(this.index, value);
    }


    /**
     * @return the mapped list index
     */
    int getIndex () {
        return this.index;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return "wrapper" + getIndex() + //$NON-NLS-1$
                ":" + getValue(); //$NON-NLS-1$
    }
}
