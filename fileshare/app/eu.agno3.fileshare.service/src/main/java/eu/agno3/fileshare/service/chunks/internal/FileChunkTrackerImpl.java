/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 4, 2017 by mbechler
 */
package eu.agno3.fileshare.service.chunks.internal;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import eu.agno3.fileshare.model.query.ChunkInfo;
import eu.agno3.fileshare.service.UploadStateTracker;


/**
 * @author mbechler
 *
 */
public class FileChunkTrackerImpl extends BaseChunkTrackerImpl {

    private static final Logger log = Logger.getLogger(FileChunkTrackerImpl.class);

    private final Path chunkMap;


    /**
     * @param contextPath
     * @param ust
     * @param chunkSize
     * @param totalSize
     */
    public FileChunkTrackerImpl ( Path contextPath, UploadStateTracker ust, long chunkSize, long totalSize ) {
        super(ust, chunkSize, totalSize);
        this.chunkMap = contextPath.resolve("chunk.map"); //$NON-NLS-1$
    }


    private FileChannel getChunkMap () throws IOException {
        FileChannel channel = FileChannel.open(
            this.chunkMap, // $NON-NLS-1$
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.READ);
        channel.position(this.getNumChunks());
        channel.position(0);
        return channel;
    }


    @Override
    protected byte getChunkState ( int chunkIdx ) {
        if ( !this.getUploadState().isValid() ) {
            log.debug("Context is gone"); //$NON-NLS-1$
            return 0;
        }
        ByteBuffer buf = ByteBuffer.allocate(1);
        try ( FileChannel ch = getChunkMap() ) {
            if ( ch.size() < chunkIdx ) {
                return 0;
            }
            ch.position(chunkIdx).read(buf);
            return buf.get();
        }
        catch ( IOException e ) {
            log.warn("Failed to determine chunk status", e); //$NON-NLS-1$
            return 0;
        }
    }


    void setChunkState ( int chunkIdx, byte state ) {
        if ( !this.getUploadState().isValid() ) {
            log.debug("Context is gone"); //$NON-NLS-1$
            return;
        }
        ByteBuffer buf = ByteBuffer.allocate(1);
        buf.put(state);
        buf.flip();
        try ( FileChannel ch = getChunkMap() ) {
            ch.position(chunkIdx).write(buf);
            ch.force(true);
        }
        catch ( IOException e ) {
            log.warn("Failed to set chunk status", e); //$NON-NLS-1$
        }
    }


    /**
     * @param state
     * @return
     */
    @Override
    protected List<ChunkInfo> getChunksWithState ( byte state ) {
        if ( !this.getUploadState().isValid() ) {
            log.debug("Context is gone"); //$NON-NLS-1$
            return Collections.EMPTY_LIST;
        }
        int nc = this.getNumChunks();
        ByteBuffer buf = ByteBuffer.allocate(nc);
        try ( FileChannel ch = getChunkMap() ) {
            int read = 0;
            while ( read < nc ) {
                int r = ch.read(buf);
                if ( r < 0 ) {
                    break;
                }
                read += r;
            }
            buf.flip();

            List<ChunkInfo> info = new ArrayList<>();
            for ( int i = 0; i < nc; i++ ) {
                if ( i >= buf.limit() ) {
                    if ( state != 0 ) {
                        continue;
                    }
                }
                else if ( buf.get(i) != state ) {
                    continue;
                }
                info.add(makeChunkInfo(i));
            }
            return info;
        }
        catch ( IOException e ) {
            log.warn("Failed to determine chunk status", e); //$NON-NLS-1$
            return Collections.EMPTY_LIST;
        }
    }

}
