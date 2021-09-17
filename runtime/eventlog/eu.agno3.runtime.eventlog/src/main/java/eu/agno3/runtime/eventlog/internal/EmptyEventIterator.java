/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.05.2015 by mbechler
 */
package eu.agno3.runtime.eventlog.internal;


import java.io.IOException;

import eu.agno3.runtime.eventlog.EventIterator;
import eu.agno3.runtime.eventlog.EventWithProperties;


/**
 * @author mbechler
 *
 */
public class EmptyEventIterator implements EventIterator {

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
    public EventWithProperties next () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventIterator#close()
     */
    @Override
    public void close () throws IOException {
        // ignore
    }

}
