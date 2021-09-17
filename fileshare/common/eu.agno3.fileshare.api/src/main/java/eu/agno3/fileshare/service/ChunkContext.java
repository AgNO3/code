/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.06.2015 by mbechler
 */
package eu.agno3.fileshare.service;


import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.ChunkUploadCanceledException;


/**
 * @author mbechler
 *
 */
public interface ChunkContext {

    /**
     * 
     * @return upload metadata
     */
    ChunkUploadMeta getMetadata ();


    /**
     * @return chunk state tracker
     */
    ChunkStateTracker getChunkState ();


    /**
     * 
     * @return the context state
     */
    UploadStateTracker getState ();


    /**
     * @return the size of the stored data
     * @throws IOException
     */
    long getStoredSize () throws IOException;


    /**
     * 
     * @return whether all chunks are complete
     */
    boolean isComplete ();


    /**
     * @return the last time this context was written to
     * @throws IOException
     */
    DateTime getLastModified () throws IOException;


    /**
     * @return the input buffer for this context
     * @throws IOException
     *             if data is incomplete or inaccessible
     */
    InputBuffer getInputBuffer () throws IOException;


    /**
     * 
     * @param idx
     * @param data
     * @param length
     * @return whether input EOF was reached
     * @throws IOException
     * @throws ChunkUploadCanceledException
     */
    boolean storeChunk ( int idx, ReadableByteChannel data, long length ) throws IOException, ChunkUploadCanceledException;


    /**
     * @param data
     * @param inputLength
     * @throws IOException
     * @throws ChunkUploadCanceledException
     */
    void storeInput ( ReadableByteChannel data, long inputLength ) throws IOException, ChunkUploadCanceledException;

}
