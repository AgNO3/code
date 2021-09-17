/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.07.2016 by mbechler
 */
package eu.agno3.fileshare.service.vfs.smb;


import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.runtime.util.iter.ClosableIterator;


/**
 * @author mbechler
 * @param <T>
 *
 */
public class EmptyIterator <@Nullable T> implements ClosableIterator<T> {

    /**
     * {@inheritDoc}
     *
     * @see java.io.Closeable#close()
     */
    @Override
    public void close () {}


    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext () {

        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#next()
     */
    @Override
    public T next () {
        return null;
    }

}
