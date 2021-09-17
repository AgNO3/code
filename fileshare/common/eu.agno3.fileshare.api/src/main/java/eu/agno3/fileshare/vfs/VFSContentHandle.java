/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 27, 2017 by mbechler
 */
package eu.agno3.fileshare.vfs;


import java.io.IOException;
import java.io.OutputStream;

import eu.agno3.fileshare.exceptions.FileshareException;


/**
 * @author mbechler
 *
 */
public interface VFSContentHandle extends AutoCloseable {

    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    void close () throws FileshareException;


    /**
     * @return whether this handle provides a actual file size
     */
    boolean haveStoredSize ();


    /**
     * @return stored size (free of race conditions)
     * @throws FileshareException
     */
    long getStoredSize () throws FileshareException;


    /**
     * @param out
     * @param buffer
     * @return total transferred bytes
     * @throws IOException
     */
    long transferTo ( OutputStream out, byte[] buffer ) throws IOException;


    /**
     * @param out
     * @param outputStream
     * @param start
     * @param length
     * @param buffer
     * @param bs
     * @return total transferred bytes
     * @throws IOException
     */
    long transferTo ( OutputStream out, long start, long length, byte[] buffer ) throws IOException;

}
