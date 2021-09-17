/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 4, 2017 by mbechler
 */
package eu.agno3.fileshare.service.chunks.internal;


import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.model.query.ChunkInfo;
import eu.agno3.fileshare.model.query.ChunkedUploadInfo;
import eu.agno3.fileshare.service.ChunkContext;


/**
 * @author mbechler
 *
 */
public final class ChunkUploadUtil {

    private static final Logger log = Logger.getLogger(ChunkUploadUtil.class);
    static final long DEFAULT_MAXIMUM_CHUNK_SIZE = 8 * 1024 * 2048L;
    static final long DEFAULT_CHUNK_SIZE = 2 * 1024 * 1024L;
    static final int DEFAULT_OPTIMAL_CHUNK_COUNT = 32;
    static final int DEFAULT_CLEANUP_INTERVAL = 60;
    static final int DEFAULT_UPLOAD_TIMEOUT_MS = 60000;
    /**
     * 
     */
    public static final String DIR_TYPE = "dir"; //$NON-NLS-1$
    /**
     * 
     */
    public static final String FILE_TYPE = "file"; //$NON-NLS-1$


    /**
     * 
     */
    private ChunkUploadUtil () {}


    /**
     * @param ctx
     * @param persistentTarget
     * @return
     */
    static ChunkedUploadInfo createFromContext ( ChunkContext ctx, VFSEntity persistentTarget ) {
        ChunkedUploadInfo info = new ChunkedUploadInfo();
        long haveSize = 0;
        info.setCompleteChunks(ctx.getChunkState().getCompletedChunks());
        info.setMissingChunks(ctx.getChunkState().getMissingChunks());
        info.setChunkSize(ctx.getChunkState().getChunkSize());
        if ( ctx.getChunkState().getTotalSize() != null ) {
            info.setTotalSize(ctx.getChunkState().getTotalSize());
        }
        info.setState(ctx.getState().getState());
        info.setLocalName(ctx.getMetadata().getLocalName());
        info.setReference(ctx.getMetadata().getReference());
        info.setContentType(ctx.getMetadata().getContentType());
        info.setExternalSource(ctx.getMetadata().isExternalSource());

        try {
            info.setLastSeen(ctx.getLastModified());
        }
        catch ( IOException e ) {
            log.debug("Failed to get last modified time", e); //$NON-NLS-1$
        }

        info.setTarget(persistentTarget);

        for ( ChunkInfo ch : info.getCompleteChunks() ) {
            haveSize += ch.getLength();
        }
        info.setCompleteSize(haveSize);
        return info;
    }


    /**
     * @param target
     * @param token
     * @param f
     * @param chunkSize
     * @param replacing
     * @return
     */
    static Properties makeCommonProperties ( EntityKey targetId, boolean targetIsFile, String token, VFSFileEntity f, long chunkSize,
            boolean replacing, boolean externalSource ) {
        Properties props = new Properties();
        props.setProperty(
            "target-type", //$NON-NLS-1$
            targetIsFile ? ChunkUploadUtil.FILE_TYPE : ChunkUploadUtil.DIR_TYPE);
        props.setProperty("replacing", String.valueOf(replacing)); //$NON-NLS-1$
        props.setProperty("externalSource", String.valueOf(externalSource)); //$NON-NLS-1$
        props.setProperty("target-id", targetId.toString()); //$NON-NLS-1$
        props.setProperty("local-name", f.getLocalName()); //$NON-NLS-1$
        props.setProperty("chunk-size", String.valueOf(chunkSize)); //$NON-NLS-1$
        props.setProperty("reference", token); //$NON-NLS-1$

        if ( f.getContentType() != null ) {
            props.setProperty("content-type", f.getContentType()); //$NON-NLS-1$
        }

        props.setProperty("total-size", String.valueOf(f.getFileSize())); //$NON-NLS-1$

        return props;
    }


    /**
     * @param totalSize
     * @param contextDir
     * @return
     */
    static long getContextSize ( Path contextDir ) {
        if ( !Files.exists(contextDir) ) {
            return 0;
        }

        try ( Stream<Path> files = Files.list(contextDir) ) {
            return files.mapToLong(p -> {
                if ( Files.isDirectory(p) ) {
                    log.warn("Found directory where there should not be one " + p); //$NON-NLS-1$
                    return 0;
                }
                try {
                    return Files.size(p);
                }
                catch ( Exception e ) {
                    log.warn("Failed to get size of " + p, e); //$NON-NLS-1$
                    return 0;
                }
            }).sum();
        }
        catch ( IOException e ) {
            log.warn("Failed to enumerate chunks", e); //$NON-NLS-1$
            return 0;
        }
    }

    // using exception for flow control is quite nasty, but the streaming APIs are pretty much unusable without it
    /**
     * @author mbechler
     *
     */
    public static final class BreakException extends RuntimeException {

        /**
         * 
         */
        public BreakException () {}

        /**
         * 
         */
        private static final long serialVersionUID = 2546286997108614792L;

    }

    static final class FileDeleter extends SimpleFileVisitor<Path> {

        public static final FileDeleter INSTANCE = new FileDeleter();


        /**
         * {@inheritDoc}
         *
         * @see java.nio.file.SimpleFileVisitor#visitFile(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
         */
        @Override
        public FileVisitResult visitFile ( Path file, BasicFileAttributes attrs ) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }


        /**
         * {@inheritDoc}
         *
         * @see java.nio.file.SimpleFileVisitor#postVisitDirectory(java.lang.Object, java.io.IOException)
         */
        @Override
        public FileVisitResult postVisitDirectory ( Path dir, IOException exc ) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    }
}
