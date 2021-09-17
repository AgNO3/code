/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 4, 2017 by mbechler
 */
package eu.agno3.fileshare.service.smb.internal;


import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

import org.apache.log4j.Logger;

import eu.agno3.fileshare.service.InputBuffer;

import jcifs.CIFSException;
import jcifs.SmbResource;
import jcifs.smb.SmbRandomAccessFile;


/**
 * @author mbechler
 *
 */
public class SMBInputBuffer implements InputBuffer {

    private static final Logger log = Logger.getLogger(SMBInputBuffer.class);
    private SmbResource dataFile;


    /**
     * @param dataFile
     */
    public SMBInputBuffer ( SmbResource dataFile ) {
        this.dataFile = dataFile;
    }


    /**
     * @return the dataFile
     */
    public SmbResource getDataFile () {
        return this.dataFile;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.InputBuffer#getSize()
     */
    @Override
    public long getSize () {
        try {
            return this.dataFile.length();
        }
        catch ( CIFSException e ) {
            log.error("Failed to get length", e); //$NON-NLS-1$
            return -1;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.InputBuffer#getStream()
     */
    @Override
    public SeekableByteChannel getStream () throws IOException {
        log.debug("Get stream"); //$NON-NLS-1$
        return new SMBSeekableByteChannel((SmbRandomAccessFile) this.dataFile.openRandomAccess("r")); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.InputBuffer#close()
     */
    @Override
    public void close () throws IOException {

    }

}
