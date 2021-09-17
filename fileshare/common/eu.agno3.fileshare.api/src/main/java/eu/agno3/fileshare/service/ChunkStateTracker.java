/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 4, 2017 by mbechler
 */
package eu.agno3.fileshare.service;


import java.util.List;

import eu.agno3.fileshare.model.query.ChunkInfo;


/**
 * @author mbechler
 *
 */
public interface ChunkStateTracker {

    /**
     * 
     * @return the chunk size
     */
    long getChunkSize ();


    /**
     * @return the size of the last chunk
     */
    Long getLastChunkSize ();


    /**
     * 
     * @return the total upload size
     */
    Long getTotalSize ();


    /**
     * 
     * @return the total number of chunks for this upload
     */
    int getNumChunks ();


    /**
     * 
     * @param chunkIdx
     * @return whether the chunk is complete
     */
    boolean haveChunk ( int chunkIdx );


    /**
     * 
     * @return the chunks not completely uploaded
     */
    List<ChunkInfo> getMissingChunks ();


    /**
     * 
     * @return the chunks completely uploaded
     */
    List<ChunkInfo> getCompletedChunks ();

}