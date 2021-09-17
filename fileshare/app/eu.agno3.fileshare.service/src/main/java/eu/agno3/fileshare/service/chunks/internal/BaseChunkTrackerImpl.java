/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 4, 2017 by mbechler
 */
package eu.agno3.fileshare.service.chunks.internal;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import eu.agno3.fileshare.model.query.ChunkInfo;
import eu.agno3.fileshare.service.ChunkStateTracker;
import eu.agno3.fileshare.service.UploadStateTracker;


/**
 * @author mbechler
 *
 */
public abstract class BaseChunkTrackerImpl implements ChunkStateTracker {

    private static final Logger log = Logger.getLogger(BaseChunkTrackerImpl.class);
    private final UploadStateTracker uploadState;
    private final int numChunks;
    private final long chunkSize;
    private final Long lastChunkSize;
    private final Long totalSize;


    /**
     * @param ust
     * @param chunkSize
     * @param totalSize
     * 
     */
    public BaseChunkTrackerImpl ( UploadStateTracker ust, long chunkSize, long totalSize ) {
        this.uploadState = ust;
        this.totalSize = totalSize;
        this.chunkSize = chunkSize;
        int nc = (int) ( totalSize / chunkSize );
        long lastSize = totalSize % chunkSize;
        if ( lastSize != 0 ) {
            nc++;
        }
        else {
            lastSize = chunkSize;
        }
        this.numChunks = nc;
        this.lastChunkSize = lastSize;
    }


    /**
     * @return the uploadState
     */
    protected UploadStateTracker getUploadState () {
        return this.uploadState;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkStateTracker#getChunkSize()
     */
    @Override
    public long getChunkSize () {
        return this.chunkSize;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkStateTracker#getLastChunkSize()
     */
    @Override
    public Long getLastChunkSize () {
        return this.lastChunkSize;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkStateTracker#getTotalSize()
     */
    @Override
    public Long getTotalSize () {
        return this.totalSize;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkStateTracker#getNumChunks()
     */
    @Override
    public int getNumChunks () {
        return this.numChunks;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkStateTracker#haveChunk(int)
     */
    @Override
    public boolean haveChunk ( int chunkIdx ) {
        if ( chunkIdx >= getNumChunks() ) {
            throw new IllegalArgumentException();
        }

        return getChunkState(chunkIdx) != 0;
    }


    /**
     * @param i
     * @return
     */
    protected abstract byte getChunkState ( int i );


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkStateTracker#getMissingChunks()
     */
    @Override
    public List<ChunkInfo> getMissingChunks () {
        return getChunksWithState((byte) 0);

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkStateTracker#getCompletedChunks()
     */
    @Override
    public List<ChunkInfo> getCompletedChunks () {
        return getChunksWithState((byte) 1);
    }


    /**
     * @param state
     * @return
     */
    protected List<ChunkInfo> getChunksWithState ( byte state ) {
        if ( !this.getUploadState().isValid() ) {
            log.debug("Context is gone"); //$NON-NLS-1$
            return Collections.EMPTY_LIST;
        }
        int nc = getNumChunks();

        List<ChunkInfo> info = new ArrayList<>();

        for ( int i = 0; i < nc; i++ ) {
            if ( getChunkState(i) == state ) {
                info.add(makeChunkInfo(i));
            }
        }

        return info;
    }


    /**
     * @param i
     * @return
     */
    protected ChunkInfo makeChunkInfo ( int i ) {
        long start = i * this.chunkSize;
        long end = ( i + 1 ) * this.chunkSize;

        if ( i == this.numChunks - 1 ) {
            end = start + ( this.lastChunkSize != null ? this.lastChunkSize : this.chunkSize );
        }
        return new ChunkInfo(i, start, end);
    }

}