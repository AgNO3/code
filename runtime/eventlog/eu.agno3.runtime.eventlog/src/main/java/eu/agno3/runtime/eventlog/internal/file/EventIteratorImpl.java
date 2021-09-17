/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.05.2015 by mbechler
 */
package eu.agno3.runtime.eventlog.internal.file;


import java.io.IOException;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParser;

import eu.agno3.runtime.eventlog.EventIterator;
import eu.agno3.runtime.eventlog.EventWithProperties;
import eu.agno3.runtime.eventlog.impl.MapEvent;


/**
 * @author mbechler
 *
 */
public class EventIteratorImpl implements EventIterator {

    private static final Logger log = Logger.getLogger(EventIteratorImpl.class);

    private FileChannel channel;
    private JsonParser parser;
    private EventWithProperties next;


    /**
     * @param channel
     * @param parser
     * @throws IOException
     */
    public EventIteratorImpl ( FileChannel channel, JsonParser parser ) throws IOException {
        this.channel = channel;
        this.parser = parser;

        this.next = readObject();
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close () throws IOException {
        log.debug("Closing log file"); //$NON-NLS-1$
        this.parser.close();
        this.channel.close();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext () {
        return this.next != null;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.Iterator#next()
     */
    @Override
    public EventWithProperties next () {
        EventWithProperties old = this.next;
        this.next = readObject();
        return old;
    }


    /**
     * @return
     * @throws IOException
     */
    private MapEvent readObject () {
        try {
            return this.parser.readValueAs(MapEvent.class);
        }
        catch ( IOException e ) {
            log.trace("IO exception", e); //$NON-NLS-1$
            return null;
        }
    }

}
