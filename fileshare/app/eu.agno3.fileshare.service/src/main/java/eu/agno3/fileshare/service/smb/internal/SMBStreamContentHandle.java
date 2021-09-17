/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 27, 2017 by mbechler
 */
package eu.agno3.fileshare.service.smb.internal;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.StorageException;
import eu.agno3.fileshare.vfs.VFSContentHandle;


/**
 * @author mbechler
 *
 */
public class SMBStreamContentHandle implements VFSContentHandle {

    private final InputStream stream;


    /**
     * @param is
     */
    public SMBStreamContentHandle ( InputStream is ) {
        this.stream = is;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContentHandle#close()
     */
    @Override
    public void close () throws FileshareException {
        try {
            this.stream.close();
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
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContentHandle#getStoredSize()
     */
    @Override
    public long getStoredSize () throws FileshareException {
        return -1;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContentHandle#transferTo(java.io.OutputStream, byte[])
     */
    @Override
    public long transferTo ( OutputStream out, byte[] buffer ) throws IOException {
        return IOUtils.copyLarge(this.stream, out, buffer);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContentHandle#transferTo(java.io.OutputStream, long, long, byte[])
     */
    @Override
    public long transferTo ( OutputStream out, long start, long length, byte[] buffer ) throws IOException {
        long skipped = this.stream.skip(start);
        return IOUtils.copyLarge(this.stream, out, start - skipped, length, buffer);
    }

}
