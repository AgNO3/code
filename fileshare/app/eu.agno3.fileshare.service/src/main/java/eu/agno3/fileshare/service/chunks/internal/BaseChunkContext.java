/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 2, 2017 by mbechler
 */
package eu.agno3.fileshare.service.chunks.internal;


import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

import org.apache.log4j.Logger;

import eu.agno3.fileshare.exceptions.ChunkUploadCanceledException;
import eu.agno3.fileshare.service.ChunkContext;
import eu.agno3.fileshare.service.ChunkStateTracker;
import eu.agno3.fileshare.service.ChunkUploadMeta;
import eu.agno3.fileshare.service.UploadState;
import eu.agno3.fileshare.service.UploadStateTracker;


/**
 * @author mbechler
 *
 */
public abstract class BaseChunkContext implements ChunkContext {

    private static final Logger log = Logger.getLogger(BaseChunkContext.class);

    private final UploadStateTracker stateTracker;
    private final ChunkUploadMeta metadata;


    /**
     * @param meta
     * @param ust
     */
    public BaseChunkContext ( ChunkUploadMeta meta, UploadStateTracker ust ) {
        this.metadata = meta;
        this.stateTracker = ust;
    }


    @Override
    public abstract ChunkStateTracker getChunkState ();


    /**
     * @return the stateTracker
     */
    public UploadStateTracker getStateTracker () {
        return this.stateTracker;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkContext#getMetadata()
     */
    @Override
    public ChunkUploadMeta getMetadata () {
        return this.metadata;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkContext#isComplete()
     */
    @Override
    public boolean isComplete () {
        if ( this.stateTracker.getState() == UploadState.COMPLETE || this.stateTracker.getState() == UploadState.PROCESSING ) {
            return true;
        }
        boolean res = getChunkState().getMissingChunks().isEmpty();
        if ( res ) {
            this.stateTracker.setState(UploadState.PROCESSING);
        }
        return res;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ChunkContext#getState()
     */
    @Override
    public UploadStateTracker getState () {
        return this.stateTracker;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws ChunkUploadCanceledException
     *
     * @see eu.agno3.fileshare.service.ChunkContext#storeInput(java.nio.channels.ReadableByteChannel, long)
     */
    @Override
    public void storeInput ( ReadableByteChannel input, long inputLength ) throws IOException, ChunkUploadCanceledException {
        int nChunks = -1;
        long cs = getChunkState().getChunkSize();
        int nc = getChunkState().getNumChunks();
        if ( inputLength > 0 ) {
            nChunks = (int) ( inputLength / cs ) + 1;
            if ( ( inputLength % cs ) == 0 ) {
                nChunks--;
            }
        }
        if ( log.isDebugEnabled() ) {
            log.debug(String.format(
                "Storing first %d of %d chunks (input length %d, chunk size %d)", //$NON-NLS-1$
                nChunks,
                nc,
                inputLength,
                cs));
        }
        for ( int i = 0; ( nChunks < 0 && i < nc ) || i < nChunks; i++ ) {
            if ( log.isDebugEnabled() && getChunkState().haveChunk(i) ) {
                log.debug("Overwriting chunk " + i); //$NON-NLS-1$
            }
            else if ( log.isDebugEnabled() ) {
                log.trace("Storing chunk " + i); //$NON-NLS-1$
            }

            long len = cs;
            if ( i == nChunks - 1 ) {
                Long lcs = getChunkState().getLastChunkSize();
                if ( lcs != null ) {
                    len = lcs;
                }
            }
            try {
                if ( storeChunk(i, input, len) ) {
                    break;
                }
            }
            catch ( IOException e ) {
                getState().setState(UploadState.FAILED);
                log.debug("Incomplete chunk " + i); //$NON-NLS-1$
                return;
            }
        }
        getState().setState(UploadState.COMPLETE);
    }

}