/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 28, 2016 by mbechler
 */
package eu.agno3.runtime.security.dict;


import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 * @param <T>
 *
 */
public abstract class BufferHolder <T extends Buffer> {

    /**
     * 
     */
    private static final int MIN_RESIZE = 4096;

    private static final Logger log = Logger.getLogger(BufferHolder.class);

    private ByteBuffer mapped;
    private T wrapped;
    private FileChannel ch;

    private Path tempDirectory;


    /**
     * @param size
     * @throws IOException
     * 
     */
    public BufferHolder ( long size ) throws IOException {
        init(size);
    }


    /**
     * @param size
     * @param tempDirectory
     * @throws IOException
     * 
     */
    public BufferHolder ( long size, Path tempDirectory ) throws IOException {
        this.tempDirectory = tempDirectory;
        init(size);
    }


    /**
     * @param p
     * @param size
     * @param offset
     * @throws IOException
     * 
     */
    public BufferHolder ( Path p, long offset, long size ) throws IOException {
        init(p, size, offset);
    }


    /**
     * @return the allocation size
     */
    protected int getAllocationUnit () {
        return 1;
    }


    /**
     * @return the capacity in bytes
     */
    public int bytes () {
        return this.mapped.capacity();
    }


    /**
     * @return buffer capacity
     */
    public int capacity () {
        if ( this.mapped == null ) {
            return 0;
        }
        return this.mapped.capacity() / getAllocationUnit();
    }


    private void init ( Path f, long size, long offset ) throws IOException {
        try {
            if ( ( size % getAllocationUnit() ) != 0 ) {
                throw new IllegalArgumentException("Non-aligned"); //$NON-NLS-1$
            }
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Loading buffer from %s: [%d,%d] len %d", f, offset, offset + size, size)); //$NON-NLS-1$
            }
            this.ch = FileChannel.open(f, StandardOpenOption.READ);
            this.mapped = this.ch.map(MapMode.READ_ONLY, offset, size);
            this.wrapped = createWrapped(this.mapped);
        }
        catch ( IOException e ) {
            if ( this.ch != null ) {
                try {
                    this.ch.close();
                }
                catch ( IOException e1 ) {
                    e.addSuppressed(e1);
                }
            }
            throw e;
        }
    }


    /**
     * @param buf
     * @return
     */
    protected abstract T createWrapped ( ByteBuffer buf );


    /**
     * @return the wrapped
     */
    public final T getWrapped () {
        return this.wrapped;
    }


    private void init ( long size ) throws IOException {
        Path tf = null;
        try {
            long s = size * getAllocationUnit();
            if ( this.tempDirectory != null ) {
                tf = Files.createTempFile(this.tempDirectory, "ahc-buffer", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            else {
                tf = Files.createTempFile("ahc-buffer", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if ( log.isDebugEnabled() ) {
                log.debug("Creating buffer " + s); //$NON-NLS-1$
            }

            this.ch = FileChannel
                    .open(tf, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.DELETE_ON_CLOSE);
            this.ch.position(s);
            this.mapped = this.ch.map(MapMode.READ_WRITE, 0, s);
            this.wrapped = createWrapped(this.mapped);
        }
        catch ( IOException e ) {

            if ( this.ch != null ) {
                try {
                    this.ch.close();
                }
                catch ( IOException e1 ) {
                    e.addSuppressed(e1);
                }
            }

            throw e;
        }
        finally {
            if ( tf != null ) {
                try {
                    Files.deleteIfExists(tf);
                }
                catch ( IOException e ) {
                    log.warn("Failed to delete file", e); //$NON-NLS-1$
                }
            }
        }
    }


    /**
     * Resize the buffer to the given size
     * 
     * @param size
     */
    public final void resize ( long size ) {
        try {
            long s = size * getAllocationUnit();
            long diff = s - this.ch.size();
            if ( diff <= 0 ) {
                return;
            }
            if ( diff < MIN_RESIZE ) {
                s += 4096 * getAllocationUnit();
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Resizing buffer to " + size); //$NON-NLS-1$
            }

            this.ch.position(s);
            this.mapped = this.ch.map(MapMode.READ_WRITE, 0, s);
            this.wrapped = createWrapped(this.mapped);
        }
        catch ( IOException e ) {
            throw new RuntimeException("Failed to resize buffer", e); //$NON-NLS-1$
        }
        finally {
            // force gc to unmap the old buffer
            System.gc();
        }
    }


    /**
     * @param fc
     * @param position
     * @return number of bytes written
     * @throws IOException
     */
    public long writeTo ( FileChannel fc, long position ) throws IOException {
        this.mapped.rewind();
        long wrote = 0;
        int w = 0;
        int size = this.mapped.capacity();
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Writing %d to %d", size, position)); //$NON-NLS-1$
        }

        fc.position(position);
        while ( ( w = fc.write(this.mapped) ) > 0 ) {
            wrote += w;
        }

        if ( wrote != size ) {
            throw new IOException("Not written completely"); //$NON-NLS-1$
        }
        return wrote;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize () throws Throwable {
        release();
        super.finalize();
    }


    /**
     * 
     */
    final void release () {
        if ( this.wrapped != null ) {
            this.wrapped = null;
        }

        if ( this.mapped != null ) {
            this.mapped = null;
        }

        if ( this.ch != null ) {
            try {
                this.ch.close();
                this.ch = null;
            }
            catch ( IOException e1 ) {
                log.warn("Failed to close file on error", e1); //$NON-NLS-1$
            }
        }
    }

}
