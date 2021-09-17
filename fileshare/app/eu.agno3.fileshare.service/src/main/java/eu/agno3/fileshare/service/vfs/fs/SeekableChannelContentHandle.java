/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 27, 2017 by mbechler
 */
package eu.agno3.fileshare.service.vfs.fs;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;

import org.apache.commons.io.IOUtils;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.StorageException;
import eu.agno3.fileshare.vfs.VFSContentHandle;


/**
 * @author mbechler
 *
 */
public class SeekableChannelContentHandle implements VFSContentHandle {

    private final SeekableByteChannel ch;


    /**
     * @param sc
     */
    public SeekableChannelContentHandle ( SeekableByteChannel sc ) {
        this.ch = sc;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContentHandle#close()
     */
    @Override
    public void close () throws FileshareException {
        try {
            this.ch.close();
        }
        catch ( IOException e ) {
            throw new StorageException("Failed to close file", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContentHandle#haveStoredSize()
     */
    @Override
    public boolean haveStoredSize () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContentHandle#getStoredSize()
     */
    @Override
    public long getStoredSize () throws FileshareException {
        try {
            return this.ch.size();
        }
        catch ( IOException e ) {
            throw new StorageException("Failed to determine file size", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     *
     * @see eu.agno3.fileshare.vfs.VFSContentHandle#transferTo(java.io.OutputStream, byte[])
     */
    @Override
    public long transferTo ( OutputStream output, byte[] buffer ) throws IOException {
        try ( InputStream input = Channels.newInputStream(this.ch) ) {
            return IOUtils.copyLarge(input, output, buffer);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     *
     * @see eu.agno3.fileshare.vfs.VFSContentHandle#transferTo(java.io.OutputStream, long, long, byte[])
     */
    @Override
    public long transferTo ( OutputStream out, long start, long length, byte[] buffer ) throws IOException {
        try ( InputStream is = Channels.newInputStream(this.ch) ) {
            long skipped = is.skip(start);
            return IOUtils.copyLarge(is, out, start - skipped, length, buffer);
        }
    }
}
