/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 4, 2017 by mbechler
 */
package eu.agno3.fileshare.service.smb.internal;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import org.apache.log4j.Logger;

import jcifs.smb.SmbRandomAccessFile;


/**
 * @author mbechler
 *
 */
public class SMBSeekableByteChannel implements SeekableByteChannel {

    private static final Logger log = Logger.getLogger(SMBSeekableByteChannel.class);
    private final SmbRandomAccessFile file;
    private boolean closed;


    /**
     * @param openRandomAccess
     */
    public SMBSeekableByteChannel ( SmbRandomAccessFile openRandomAccess ) {
        this.file = openRandomAccess;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.nio.channels.Channel#isOpen()
     */
    @Override
    public boolean isOpen () {
        return !this.closed;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.nio.channels.Channel#close()
     */
    @Override
    public void close () throws IOException {
        log.debug("Closing"); //$NON-NLS-1$
        this.closed = true;
        this.file.close();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.nio.channels.SeekableByteChannel#read(java.nio.ByteBuffer)
     */
    @Override
    public int read ( ByteBuffer dst ) throws IOException {
        byte[] buffer = new byte[dst.remaining()];
        int have = this.file.read(buffer);
        if ( have > 0 ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Have read " + have); //$NON-NLS-1$
            }
            dst.put(buffer, 0, have);
        }
        return have;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.nio.channels.SeekableByteChannel#write(java.nio.ByteBuffer)
     */
    @Override
    public int write ( ByteBuffer src ) throws IOException {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.nio.channels.SeekableByteChannel#position()
     */
    @Override
    public long position () throws IOException {
        return this.file.getFilePointer();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.nio.channels.SeekableByteChannel#position(long)
     */
    @Override
    public SeekableByteChannel position ( long newPosition ) throws IOException {
        if ( log.isDebugEnabled() ) {
            log.debug("Seeking " + newPosition); //$NON-NLS-1$
        }
        this.file.seek(newPosition);
        return this;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.nio.channels.SeekableByteChannel#size()
     */
    @Override
    public long size () throws IOException {
        return this.file.length();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.nio.channels.SeekableByteChannel#truncate(long)
     */
    @Override
    public SeekableByteChannel truncate ( long size ) throws IOException {
        throw new UnsupportedOperationException();
    }

}
