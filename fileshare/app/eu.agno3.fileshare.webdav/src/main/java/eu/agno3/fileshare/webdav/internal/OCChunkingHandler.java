/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2016 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.log4j.Logger;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.PolicyNotFulfilledException;
import eu.agno3.fileshare.model.FileEntity;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.service.ChunkContext;
import eu.agno3.fileshare.vfs.VFSContext;


/**
 * @author mbechler
 *
 */
public class OCChunkingHandler {

    private static final Logger log = Logger.getLogger(OCChunkingHandler.class);
    private static final Pattern OC_CHUNKING_PATTERN = Pattern.compile("^((.*)/)?([^\\/]+)-chunking-([\\d]+)-([\\d]+)-([\\d]+)$"); //$NON-NLS-1$


    /**
     * @param root
     * @param relative
     * @param httpRequest
     * @param permissions
     * @param shared
     * @return
     * @throws FileshareException
     * @throws DavException
     */
    static OCChunkDAVNode handleOCChunked ( FileshareDAVTreeProviderImpl dt, VFSContext v, VFSContainerEntity root, String relative,
            HttpServletRequest httpRequest, Set<GrantPermission> permissions, UUID grantId, boolean shared ) throws FileshareException, DavException {
        String chunkSizeHeader = httpRequest.getHeader("OC-Chunk-Size"); //$NON-NLS-1$
        String totalLengthHeader = httpRequest.getHeader("OC-Total-Length"); //$NON-NLS-1$

        Matcher m = OC_CHUNKING_PATTERN.matcher(relative);
        if ( m.matches() ) {
            long chunkSize;
            if ( StringUtils.isBlank(chunkSizeHeader) ) {
                log.debug("Missing chunk size header"); //$NON-NLS-1$
                chunkSize = 1024000;
            }
            else {
                chunkSize = Long.parseLong(chunkSizeHeader);
            }
            Long totalSize = null;
            if ( !StringUtils.isBlank(totalLengthHeader) ) {
                totalSize = Long.parseLong(totalLengthHeader);
            }
            return getOCChunk(dt, v, root, httpRequest, chunkSize, totalSize, m, permissions, grantId, shared);
        }

        log.warn("Could not parse chunked filename " + relative); //$NON-NLS-1$
        return null;
    }


    /**
     * @param root
     * @param httpRequest
     * @param chunkSize
     * @param totalSize
     * @param m
     * @param permissions
     * @param shared
     * @return
     * @throws FileshareException
     * @throws DavException
     * @throws PolicyNotFulfilledException
     */
    private static OCChunkDAVNode getOCChunk ( FileshareDAVTreeProviderImpl dt, VFSContext v, VFSEntity root, HttpServletRequest httpRequest,
            long chunkSize, Long totalSize, Matcher m, Set<GrantPermission> permissions, UUID grantId, boolean shared )
            throws FileshareException, DavException, PolicyNotFulfilledException {
        String actualRootRelative = m.group(2);
        String actualName = m.group(3);

        VFSEntity target = dt.getUploadTarget(root, actualRootRelative, actualName);
        if ( target == null ) {
            log.debug("Chunk requested but parent not found"); //$NON-NLS-1$
            return null;
        }

        dt.getPolicyEvaluator().checkPolicy(v, target, httpRequest);

        long ts = Long.parseLong(m.group(4));
        int chunkNum = Integer.parseInt(m.group(5));
        int chunkIdx = Integer.parseInt(m.group(6));

        if ( log.isDebugEnabled() ) {
            log.debug(
                String.format("Chunk %d of %d of file %s request ( ts: %d parent: %s ) ", chunkIdx, chunkNum, actualName, ts, target.getEntityKey())); //$NON-NLS-1$
        }

        FileEntity f = new FileEntity();
        if ( totalSize != null ) {
            f.setFileSize(totalSize);
        }
        f.setLocalName(actualName);

        boolean targetIsFile = target instanceof VFSFileEntity;

        String token = target.getEntityKey() + String.valueOf(ts);

        ChunkContext chunkContext;
        try {
            if ( totalSize != null ) {
                chunkContext = dt.getContext().getChunkUploadService()
                        .getOrCreateChunkContext(target.getEntityKey(), targetIsFile, token, f, chunkSize, targetIsFile, true);
            }
            else {
                throw new DavException(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        catch ( IOException e ) {
            log.warn("Failed to get chunk context", e); //$NON-NLS-1$
            return null;
        }

        OCChunkDAVNode n = new OCChunkDAVNode(
            target,
            grantId,
            actualName,
            ts,
            chunkIdx,
            chunkNum,
            chunkSize,
            totalSize,
            chunkContext.getChunkState().haveChunk(chunkIdx));
        n.setPermissions(permissions);
        n.setShared(shared);
        return n;
    }
}
