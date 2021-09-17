/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 4, 2017 by mbechler
 */
package eu.agno3.fileshare.service.smb.internal;


import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.ChunkUploadCanceledException;
import eu.agno3.fileshare.service.ChunkStateTracker;
import eu.agno3.fileshare.service.ChunkUploadMeta;
import eu.agno3.fileshare.service.InputBuffer;
import eu.agno3.fileshare.service.UploadState;
import eu.agno3.fileshare.service.chunks.internal.BaseChunkContext;

import jcifs.CIFSException;
import jcifs.SmbResource;
import jcifs.smb.SmbRandomAccessFile;


/**
 * @author mbechler
 *
 */
public class SMBChunkContextImpl extends BaseChunkContext {

    private static final Logger log = Logger.getLogger(SMBChunkContextImpl.class);
    private static final int BUFFER_SIZE = 16 * 1024;
    private final SmbResource dataFile;
    private final SMBChunkStateTracker chunkState;
    private SmbResource contextPath;


    /**
     * @param meta
     * @param ctxp
     * @throws CIFSException
     */
    public SMBChunkContextImpl ( ChunkUploadMeta meta, SmbResource ctxp ) throws CIFSException {
        super(meta, new SMBUploadStateTracker(ctxp));
        this.contextPath = ctxp;
        this.dataFile = ctxp.resolve("data"); //$NON-NLS-1$
        this.chunkState = new SMBChunkStateTracker(ctxp, getState(), meta.getChunkSize(), meta.getTotalSize());
    }


    /**
     * @return the contextPath
     */
    public SmbResource getContextPath () {
        return this.contextPath;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.chunks.internal.BaseChunkContext#getChunkState()
     */
    @Override
    public ChunkStateTracker getChunkState () {
        return this.chunkState;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkContext#getStoredSize()
     */
    @Override
    public long getStoredSize () throws IOException {
        return this.dataFile.length();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkContext#getLastModified()
     */
    @Override
    public DateTime getLastModified () throws IOException {
        if ( this.dataFile.exists() ) {
            return new DateTime(this.dataFile.lastModified());
        }
        return DateTime.now();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkContext#getInputBuffer()
     */
    @Override
    public InputBuffer getInputBuffer () throws IOException {
        return new SMBInputBuffer(this.dataFile);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkContext#storeChunk(int, java.nio.channels.ReadableByteChannel, long)
     */
    @Override
    public boolean storeChunk ( int idx, ReadableByteChannel data, long length ) throws IOException, ChunkUploadCanceledException {
        if ( !getState().isValid() ) {
            throw new ChunkUploadCanceledException(); // $NON-NLS-1$
        }
        if ( log.isDebugEnabled() ) {
            log.debug("Store chunk " + idx); //$NON-NLS-1$
        }

        byte buffer[] = new byte[BUFFER_SIZE];

        try ( InputStream is = Channels.newInputStream(data);
              SmbRandomAccessFile os = (SmbRandomAccessFile) this.dataFile.openRandomAccess("rw") ) { //$NON-NLS-1$
            getState().setState(UploadState.UPLOADING);

            long written = 0;
            long read = 0;
            long readTotal = 0;
            int maxReadSize = (int) Math.min(length, BUFFER_SIZE);

            os.seek(getChunkState().getChunkSize() * idx);
            while ( written < length && ( read = is.read(buffer, 0, maxReadSize) ) != -1 ) {
                readTotal += read;
                os.write(buffer, 0, maxReadSize);
                written += maxReadSize;

                maxReadSize = (int) Math.min( ( length - written ), BUFFER_SIZE);
                if ( log.isTraceEnabled() ) {
                    log.trace(String.format(
                        "Read size %d read %d written %d total %d", //$NON-NLS-1$
                        maxReadSize,
                        read,
                        written,
                        length));
                }
            }
            if ( log.isDebugEnabled() ) {
                log.debug(String.format(
                    "Read total %d wrote total %d indicated length %d (chunk size %d, idx %d)", //$NON-NLS-1$
                    readTotal,
                    written,
                    length,
                    getChunkState().getChunkSize(),
                    idx));
            }

            if ( !getState().isValid() ) {
                throw new ChunkUploadCanceledException(); // $NON-NLS-1$
            }
            this.chunkState.setChunkState(idx, (byte) 1);
            return read == -1;
        }
        catch ( IOException e ) {
            getState().setState(UploadState.FAILED);
            log.debug("Upload interrupted " + idx, e); //$NON-NLS-1$
            throw e;
        }
    }

}
