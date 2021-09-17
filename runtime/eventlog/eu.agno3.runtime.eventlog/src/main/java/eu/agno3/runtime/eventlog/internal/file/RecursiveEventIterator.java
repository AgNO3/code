/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.05.2015 by mbechler
 */
package eu.agno3.runtime.eventlog.internal.file;


import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger;

import eu.agno3.runtime.eventlog.EventIterator;
import eu.agno3.runtime.eventlog.EventWithProperties;


/**
 * @author mbechler
 *
 */
public class RecursiveEventIterator extends RecursiveIterator<EventWithProperties> implements EventIterator {

    private static final Logger log = Logger.getLogger(RecursiveEventIterator.class);


    /**
     * @param it
     */
    public RecursiveEventIterator ( Iterator<Iterator<EventWithProperties>> it ) {
        super(it);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.internal.file.RecursiveIterator#closeSubiterator(java.util.Iterator)
     */
    @Override
    protected void closeSubiterator ( Iterator<EventWithProperties> toClose ) {

        if ( toClose instanceof EventIterator ) {
            try {
                ( (EventIterator) toClose ).close();
            }
            catch ( IOException e ) {
                log.error("Failed to close iterator", e); //$NON-NLS-1$
            }
        }
    }
}
