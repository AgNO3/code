/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.05.2015 by mbechler
 */
package eu.agno3.runtime.eventlog;


import java.io.IOException;
import java.util.Iterator;


/**
 * @author mbechler
 *
 */
public interface EventIterator extends Iterator<EventWithProperties>, AutoCloseable {

    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close () throws IOException;
}
