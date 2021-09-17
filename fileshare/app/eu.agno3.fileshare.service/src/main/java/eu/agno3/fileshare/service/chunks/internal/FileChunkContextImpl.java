/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.06.2015 by mbechler
 */
package eu.agno3.fileshare.service.chunks.internal;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.ChunkUploadCanceledException;
import eu.agno3.fileshare.service.ChunkStateTracker;
import eu.agno3.fileshare.service.ChunkUploadMeta;
import eu.agno3.fileshare.service.InputBuffer;
import eu.agno3.fileshare.service.UploadState;
import eu.agno3.fileshare.service.api.internal.ChunkContextInternal;
import eu.agno3.fileshare.service.internal.FileInputBuffer;


/**
 * @author mbechler
 *
 */
public class FileChunkContextImpl extends BaseChunkContext implements ChunkContextInternal {

    private static final Logger log = Logger.getLogger(FileChunkContextImpl.class);
    private static final int BUFFER_SIZE = 16384;

    private Path contextPath;
    private GroupPrincipal storageGroup;
    private FileChunkTrackerImpl chunkTracker;


    /**
     * @param contextPath
     * @param meta
     * @param storageGroup
     */
    public FileChunkContextImpl ( ChunkUploadMeta meta, Path contextPath, GroupPrincipal storageGroup ) {
        super(meta, new FileUploadStateTrackerImpl(contextPath));
        this.contextPath = contextPath;
        this.storageGroup = storageGroup;
        this.chunkTracker = new FileChunkTrackerImpl(contextPath, getStateTracker(), meta.getChunkSize(), meta.getTotalSize());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.chunks.internal.BaseChunkContext#getChunkState()
     */
    @Override
    public ChunkStateTracker getChunkState () {
        return this.chunkTracker;
    }


    /**
     * @return the contextPath
     */
    protected Path getContextPath () {
        return this.contextPath;
    }


    private Path getDataFile () {
        return getContextPath().resolve("data"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     *
     * @see eu.agno3.fileshare.service.ChunkContext#getStoredSize()
     */
    @Override
    public long getStoredSize () throws IOException {
        if ( !this.isComplete() ) {
            throw new IOException("Incomplete upload"); //$NON-NLS-1$
        }
        return Files.size(getDataFile());
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     *
     * @see eu.agno3.fileshare.service.ChunkContext#getLastModified()
     */
    @Override
    public DateTime getLastModified () throws IOException {
        if ( !Files.exists(getDataFile()) ) {
            return new DateTime(Files.getLastModifiedTime(getContextPath().resolve("chunk.properties")).toMillis()); //$NON-NLS-1$
        }
        return new DateTime(Files.getLastModifiedTime(getDataFile()).toMillis());
    }


    /**
     * 
     * @param idx
     * @param data
     * @param length
     * @return whether input EOF was reached
     * @throws IOException
     * @throws ChunkUploadCanceledException
     */
    @Override
    public boolean storeChunk ( int idx, ReadableByteChannel data, long length ) throws IOException, ChunkUploadCanceledException {
        if ( !getState().isValid() ) {
            throw new ChunkUploadCanceledException(); // $NON-NLS-1$
        }
        ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
        try ( FileChannel ch = FileChannel.open(getDataFile(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.SPARSE) ) {
            setFilePermissions(getDataFile());
            getState().setState(UploadState.UPLOADING);

            long written = 0;
            long read = 0;
            long readTotal = 0;
            int maxReadSize = (int) Math.min(length, BUFFER_SIZE);
            buf.limit(maxReadSize);
            ch.position(getChunkState().getChunkSize() * idx);
            while ( written < length && ( read = data.read(buf) ) != -1 ) {
                readTotal += read;
                buf.flip();
                int wrote = 0;
                while ( buf.remaining() > 0 ) {
                    wrote += ch.write(buf);
                }
                buf.compact();
                written += wrote;
                maxReadSize = (int) Math.min( ( length - written ), BUFFER_SIZE);
                buf.limit(maxReadSize);
                if ( log.isTraceEnabled() ) {
                    log.trace(String.format(
                        "Read size %d read %d wrote %d written %d total %d", //$NON-NLS-1$
                        maxReadSize,
                        read,
                        wrote,
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
            ch.force(true);
            if ( !getState().isValid() ) {
                throw new ChunkUploadCanceledException(); // $NON-NLS-1$
            }
            this.chunkTracker.setChunkState(idx, (byte) 1);
            return read == -1;
        }
        catch ( IOException e ) {
            getState().setState(UploadState.FAILED);
            log.debug("Upload interrupted " + idx, e); //$NON-NLS-1$
            throw e;
        }
    }


    /**
     * @param path
     * @throws IOException
     */
    private void setFilePermissions ( Path path ) throws IOException {
        if ( this.storageGroup != null ) {
            PosixFileAttributeView attrs = Files.getFileAttributeView(path, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
            attrs.setGroup(this.storageGroup);
            attrs.setPermissions(PosixFilePermissions.fromString("rw-rw----")); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws IOException
     *
     * @see eu.agno3.fileshare.service.ChunkContext#getInputBuffer()
     */
    @Override
    public InputBuffer getInputBuffer () throws IOException {
        if ( !isComplete() ) {
            throw new IOException("Upload is incomplete"); //$NON-NLS-1$
        }
        long size;
        Long ts = getChunkState().getTotalSize();
        if ( ts != null ) {
            size = ts;
        }
        else {
            size = getStoredSize();
        }
        Path dataFile = this.getDataFile();
        if ( !Files.exists(dataFile) && size == 0 ) {
            Files.write(dataFile, new byte[0]);
        }

        long fsize = Files.size(dataFile);
        if ( fsize != size ) {
            log.warn("Upload data is truncated, expected " + size + //$NON-NLS-1$
                    " have " + fsize); //$NON-NLS-1$
            throw new IOException("Data is truncated"); //$NON-NLS-1$
        }
        return new FileInputBuffer(dataFile, size);
    }

}
