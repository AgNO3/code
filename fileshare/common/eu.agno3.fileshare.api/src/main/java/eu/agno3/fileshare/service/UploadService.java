/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.02.2015 by mbechler
 */
package eu.agno3.fileshare.service;


import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.VFSFileEntity;


/**
 * @author mbechler
 *
 */
public interface UploadService {

    /**
     * Create a file
     * 
     * Access control:
     * - user must have UPLOAD access to the target container
     * 
     * Ownership is inherited from the target container
     * 
     * If a file with the same name exists, a new non-existant file name is generated.
     * 
     * @param targetId
     *            target container id
     * @param file
     * @param data
     * @param req
     * @param resp
     * @return the created file
     * @throws FileshareException
     */
    VFSFileEntity create ( EntityKey targetId, VFSFileEntity file, ReadableByteChannel data, ServletRequest req, ServletResponse resp )
            throws FileshareException;


    /**
     * 
     * Access control:
     * - user must have EDIT access to the target container
     * 
     * if a file with the same mapped filename exists and overwriting is allowed in the directory then
     * the file is replaced, otherwise a new file is created.
     * 
     * @param id
     * @param file
     * @param inputstream
     * @param req
     * @param resp
     * @return the created file
     * @throws FileshareException
     */
    VFSFileEntity createOrReplace ( EntityKey id, VFSFileEntity file, ReadableByteChannel inputstream, ServletRequest req, ServletResponse resp )
            throws FileshareException;


    /**
     * 
     * Access control:
     * - user must have EDIT access to the target file
     * - the container also must have allowOverwrite set
     * 
     * The file's size, content type, last modifier, last modified will be updated. All other properties remain the
     * same.
     * 
     * @param id
     * @param file
     * @param inputstream
     * @param req
     * @param resp
     * @return the updated file
     * @throws FileshareException
     */
    VFSFileEntity replaceFile ( EntityKey id, VFSFileEntity file, ReadableByteChannel inputstream, ServletRequest req, ServletResponse resp )
            throws FileshareException;


    /**
     * 
     * Access control:
     * - the access control of the respective upload operation
     * 
     * @param k
     * @param fe
     * @param chunkContext
     * @param request
     * @param response
     * @return the created entity
     * @throws IOException
     * @throws FileshareException
     */
    VFSFileEntity retry ( EntityKey k, VFSFileEntity fe, ChunkContext chunkContext, ServletRequest request, ServletResponse response )
            throws IOException, FileshareException;

}
