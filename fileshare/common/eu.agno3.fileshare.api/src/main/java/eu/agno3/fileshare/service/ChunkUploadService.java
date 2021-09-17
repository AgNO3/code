/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.06.2015 by mbechler
 */
package eu.agno3.fileshare.service;


import java.io.IOException;
import java.util.List;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.model.query.ChunkedUploadInfo;


/**
 * @author mbechler
 *
 */
public interface ChunkUploadService {

    /**
     * @param target
     * @param targetIsFile
     * @param reference
     * @param f
     * @param chunkSize
     * @param replacing
     * @param externalSource
     * @return a chunk context
     * @throws IOException
     * @throws FileshareException
     */
    ChunkContext getOrCreateChunkContext ( EntityKey target, boolean targetIsFile, String reference, VFSFileEntity f, Long chunkSize,
            boolean replacing, boolean externalSource ) throws IOException, FileshareException;


    /**
     * @param targetId
     * @param reference
     * @return the chunk context
     * @throws IOException
     * @throws FileshareException
     */
    ChunkContext getChunkContext ( String reference ) throws FileshareException, IOException;


    /**
     * 
     * @param target
     * @param userId
     * @param parent
     * @param reference
     * @throws FileshareException
     * @throws IOException
     */
    void removeChunkContext ( String reference ) throws FileshareException, IOException;


    /**
     * Reowns the context
     * 
     * Reowning means that the context is modified in a way that prevents other existing readers and writers to access
     * it.
     * 
     * @param target
     * @param ref
     * @return the new context
     * @throws IOException
     * @throws FileshareException
     */
    ChunkContext reownContext ( String ref ) throws IOException, FileshareException;


    /**
     * 
     * @param userId
     * @return incomplete chunks
     * @throws FileshareException
     * @throws IOException
     */
    List<ChunkContext> getIncompleteContexts () throws FileshareException, IOException;


    /**
     * @return the current users incomplete chunk info
     * @throws FileshareException
     */
    List<ChunkedUploadInfo> getIncompleteChunkInfo () throws FileshareException;


    /**
     * @param targetId
     * @param reference
     * @return the chunk info
     * @throws FileshareException
     * @throws IOException
     */
    ChunkedUploadInfo getChunkInfo ( String reference ) throws FileshareException, IOException;


    /**
     * @return the number of incomplete contexts for the current user
     * @throws FileshareException
     */
    int getNumIncompleteContexts () throws FileshareException;


    /**
     * @return the default chunk size
     */
    long getDefaultChunkSize ();


    /**
     * @return the maximum chunk size
     */
    long getMaximumChunkSize ();


    /**
     * @return the optimal number of chunks a file should be divided into
     */
    int getOptimalChunkCount ();

}
