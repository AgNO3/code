/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2015 by mbechler
 */
package eu.agno3.fileshare.service;


import java.io.IOException;
import java.nio.channels.SeekableByteChannel;


/**
 * @author mbechler
 *
 */
public interface InputBuffer extends AutoCloseable {

    /**
     * 
     * @return the buffer size
     */
    public long getSize ();


    /**
     * @return an input stream
     * @throws IOException
     */
    public SeekableByteChannel getStream () throws IOException;


    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close () throws IOException;

}
