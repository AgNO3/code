/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.01.2015 by mbechler
 */
package eu.agno3.fileshare.service.api.internal;


import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.service.ArchiveType;
import eu.agno3.fileshare.vfs.VFSContext;


/**
 * @author mbechler
 *
 */
public interface DeliveryService {

    /**
     * Deliver a file to an HTTP endpoint
     * 
     * Access control:
     * - the user must have READ access to the file
     * - the file's policy must be fulfilled
     * 
     * If the disposition is attachment, then we try to force the user agent into donwloading the resource.
     * 
     * If the disposition is inline, the user agent may render the resource. This poses a security risk as
     * a-priori these are considered to be from the same origin as the application. Therefore:
     * 
     * - files are only delivered inline if known to be embedded inside a HTML5 sandbox, generating a unique origin, and
     * - files are only delivered inline if the browser is known to support CSP, preventing them from loading external
     * resources
     * 
     * The second requirement may be lifted for certain content types when it is known that all browsers will safely
     * render them without loading external resources.
     * 
     * @param req
     * @param resp
     * @param id
     *            file id
     * @param fileName
     * @param disposition
     *            (inline|attachment)
     * @throws IOException
     */
    void doDeliverFile ( HttpServletRequest req, HttpServletResponse resp, EntityKey id, String fileName, String disposition ) throws IOException;


    /**
     * @param v
     * @param file
     * @param req
     * @param httpServletResponse
     * @param outputStream
     * @throws IOException
     * @throws FileshareException
     */
    void deliverDAV ( VFSContext v, VFSFileEntity file, HttpServletRequest req, HttpServletResponse httpServletResponse, OutputStream outputStream )
            throws IOException, FileshareException;


    /**
     * Delivers a zip file of all child entities
     * 
     * Access control:
     * - the user must have READ access to the container
     * - the directories' policy must be fulfilled
     * - only entries for which the policy is fulfilled will be returned
     * 
     * @param req
     * @param resp
     * @param id
     *            container id
     * @param fileName
     * @param type
     * @throws IOException
     */
    void doDeliverDirectory ( HttpServletRequest req, HttpServletResponse resp, EntityKey id, String fileName, ArchiveType type ) throws IOException;


    /**
     * Access control:
     * - only entries for which the policy is fulfilled will be returned
     * - only entries to which the user has read access will be returned
     * 
     * @param httpReq
     * @param httpResp
     * @param entities
     * @param type
     * @throws IOException
     */
    void doDeliverMultiple ( HttpServletRequest httpReq, HttpServletResponse httpResp, Set<EntityKey> entities, ArchiveType type ) throws IOException;

}
