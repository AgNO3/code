/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 4, 2017 by mbechler
 */
package eu.agno3.fileshare.service.smb.internal;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import eu.agno3.fileshare.model.query.ChunkInfo;
import eu.agno3.fileshare.service.UploadStateTracker;
import eu.agno3.fileshare.service.chunks.internal.BaseChunkTrackerImpl;

import jcifs.CIFSException;
import jcifs.SmbResource;
import jcifs.smb.SmbRandomAccessFile;


/**
 * @author mbechler
 *
 */
public class SMBChunkStateTracker extends BaseChunkTrackerImpl {

    private static final Logger log = Logger.getLogger(SMBChunkStateTracker.class);

    private final SmbResource chunkMap;


    /**
     * @param contextPath
     * @param ust
     * @param chunkSize
     * @param totalSize
     * @throws CIFSException
     */
    public SMBChunkStateTracker ( SmbResource contextPath, UploadStateTracker ust, long chunkSize, long totalSize ) throws CIFSException {
        super(ust, chunkSize, totalSize);
        if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "Total size is %d, chunk size %d, num chunks %d, last chunk %d", //$NON-NLS-1$
                totalSize,
                chunkSize,
                getNumChunks(),
                getLastChunkSize()));
        }
        this.chunkMap = contextPath.resolve("chunk.map"); //$NON-NLS-1$
    }


    /**
     * @return
     * @throws CIFSException
     */
    private SmbRandomAccessFile openChunkMap ( String mode ) throws CIFSException {
        return (SmbRandomAccessFile) this.chunkMap.openRandomAccess(mode); // $NON-NLS-1$
    }


    @Override
    protected byte getChunkState ( int chunkIdx ) {
        if ( !getUploadState().isValid() ) {
            log.debug("Context is gone"); //$NON-NLS-1$
            return 0;
        }
        try ( SmbRandomAccessFile ch = openChunkMap("r") ) { //$NON-NLS-1$
            if ( ch.length() <= chunkIdx ) {
                return 0;
            }
            ch.seek(chunkIdx);
            int r = ch.read();
            if ( r < 0 ) {
                return 0;
            }
            return (byte) r;
        }
        catch ( IOException e ) {
            log.warn("Failed to determine chunk status", e); //$NON-NLS-1$
            return 0;
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
        int nc = getNumChunks();
        try ( SmbRandomAccessFile ch = openChunkMap("r") ) { //$NON-NLS-1$
            List<ChunkInfo> info = new ArrayList<>();
            int i = 0;
            while ( i < nc ) {
                int r = ch.read();
                if ( r < 0 ) {
                    break;
                }

                if ( r == state ) {
                    info.add(makeChunkInfo(i));
                }
                i++;
            }
            if ( state == 0 ) {
                for ( ; i < nc; i++ ) {
                    info.add(makeChunkInfo(i));
                }
            }
            return info;
        }
        catch ( IOException e ) {
            log.warn("Failed to determine chunk status", e); //$NON-NLS-1$
            return Collections.EMPTY_LIST;
        }
    }


    void setChunkState ( int chunkIdx, byte state ) {
        if ( !getUploadState().isValid() ) {
            log.debug("Context is gone"); //$NON-NLS-1$
            return;
        }
        try ( SmbRandomAccessFile ch = openChunkMap("rw") ) { //$NON-NLS-1$
            ch.seek(chunkIdx);
            ch.write(state);
        }
        catch ( IOException e ) {
            log.warn("Failed to set chunk status", e); //$NON-NLS-1$
        }
    }
}
