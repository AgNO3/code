/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.06.2016 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.Status;
import org.apache.jackrabbit.webdav.security.report.PrincipalMatchReport;
import org.apache.log4j.Logger;

import eu.agno3.fileshare.exceptions.ChunkUploadCanceledException;
import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.query.ChunkedUploadInfo;
import eu.agno3.fileshare.webdav.sync.SyncTokenData;
import eu.agno3.runtime.webdav.server.DAVTreeNode;
import eu.agno3.runtime.webdav.server.acl.FixedPrincipalSearchReport;
import eu.agno3.runtime.webdav.server.colsync.ColSyncReport;


/**
 * @author mbechler
 *
 */
public class TransferSubtreeProvider implements SubtreeProvider {

    private static final Logger log = Logger.getLogger(TransferSubtreeProvider.class);


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.SubtreeProvider#isApplicable(java.lang.String,
     *      eu.agno3.fileshare.webdav.internal.DAVLayout)
     */
    @Override
    public boolean isApplicable ( String repositoryPath, DAVLayout layout ) {
        return layout != DAVLayout.OWNCLOUD && repositoryPath.startsWith(TransfersDAVNode.TRANSFERS_PATH);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.SubtreeProvider#resolve(eu.agno3.fileshare.webdav.internal.FileshareDAVTreeProviderInternal,
     *      java.lang.String, eu.agno3.fileshare.webdav.internal.DAVLayout)
     */
    @Override
    public DAVTreeNode<EntityKey> resolve ( FileshareDAVTreeProviderInternal dt, String repositoryPath, DAVLayout layout )
            throws FileshareException, DavException {

        if ( log.isDebugEnabled() ) {
            log.debug("Resolving " + repositoryPath); //$NON-NLS-1$
        }

        if ( !repositoryPath.startsWith(TransfersDAVNode.TRANSFERS_PATH) ) {
            return null;
        }

        if ( TransfersDAVNode.TRANSFERS_PATH.equals(repositoryPath) ) {
            return new TransfersDAVNode(layout);
        }

        int nextSep = repositoryPath.indexOf('/', TransfersDAVNode.TRANSFERS_PATH.length() + 1);

        String idstr;
        String relative;
        if ( nextSep >= 0 ) {
            idstr = repositoryPath.substring(TransfersDAVNode.TRANSFERS_PATH.length() + 1, nextSep);
            relative = repositoryPath.substring(nextSep + 1);
        }
        else {
            idstr = repositoryPath.substring(TransfersDAVNode.TRANSFERS_PATH.length() + 1);
            relative = null;
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Resolving context %s subpath %s", idstr, relative)); //$NON-NLS-1$
        }

        if ( relative == null ) {
            try {
                ChunkedUploadInfo chunkInfo = dt.getContext().getChunkUploadService().getChunkInfo(idstr);
                return new TransferDAVNode(chunkInfo, layout);
            }
            catch (
                ChunkUploadCanceledException |
                IOException e ) {
                throw new EntityNotFoundException("Chunk context not available", e); //$NON-NLS-1$
            }
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.SubtreeProvider#handlesChildren(eu.agno3.runtime.webdav.server.DAVTreeNode,
     *      eu.agno3.fileshare.webdav.internal.DAVLayout)
     */
    @Override
    public boolean handlesChildren ( DAVTreeNode<EntityKey> node, DAVLayout layout ) {
        return ( layout != DAVLayout.OWNCLOUD ) && node instanceof TransfersDAVNode;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.SubtreeProvider#getChildren(eu.agno3.fileshare.webdav.internal.FileshareDAVTreeProviderInternal,
     *      eu.agno3.runtime.webdav.server.DAVTreeNode, eu.agno3.fileshare.webdav.internal.DAVLayout)
     */
    @Override
    public Collection<DAVTreeNode<EntityKey>> getChildren ( FileshareDAVTreeProviderInternal dt, DAVTreeNode<EntityKey> node, DAVLayout layout )
            throws FileshareException {
        List<DAVTreeNode<EntityKey>> children = new ArrayList<>();
        if ( node instanceof TransfersDAVNode ) {
            log.debug("Listing incomplete chunk contexts"); //$NON-NLS-1$
            for ( ChunkedUploadInfo ci : dt.getContext().getChunkUploadService().getIncompleteChunkInfo() ) {
                children.add(new TransferDAVNode(ci, layout));
            }
        }
        return children;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.SubtreeProvider#getRootChildren(eu.agno3.fileshare.webdav.internal.FileshareDAVTreeProviderInternal,
     *      eu.agno3.fileshare.webdav.internal.DAVLayout)
     */
    @Override
    public Collection<? extends DAVTreeNode<EntityKey>> getRootChildren ( FileshareDAVTreeProviderInternal dt, DAVLayout layout )
            throws FileshareException {
        return Collections.EMPTY_SET;
    }


    @Override
    public boolean contributeChanges ( FileshareDAVTreeProviderInternal dt, DAVTreeNode<EntityKey> rootNode, SyncTokenData inputToken,
            SyncTokenData outputToken, ColSyncReport<EntityKey> cs, DAVLayout layout, boolean rootModified ) throws FileshareException {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.SubtreeProvider#contributePath(eu.agno3.fileshare.webdav.internal.FileshareDAVTreeProviderInternal,
     *      eu.agno3.runtime.webdav.server.DAVTreeNode, eu.agno3.fileshare.webdav.internal.DAVLayout,
     *      java.lang.StringBuilder)
     */
    @Override
    public boolean contributePath ( FileshareDAVTreeProviderInternal dt, DAVTreeNode<EntityKey> node, DAVLayout l, StringBuilder path )
            throws FileshareException {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.SubtreeProvider#findPrincipalMatch(eu.agno3.fileshare.webdav.internal.FileshareDAVTreeProviderInternal,
     *      eu.agno3.runtime.webdav.server.DAVTreeNode, eu.agno3.fileshare.webdav.internal.DAVLayout,
     *      org.apache.jackrabbit.webdav.security.report.PrincipalMatchReport)
     */
    @Override
    public Map<DAVTreeNode<EntityKey>, Status> findPrincipalMatch ( FileshareDAVTreeProviderInternal dt, DAVTreeNode<EntityKey> wrapped,
            DAVLayout layout, PrincipalMatchReport pm ) throws FileshareException {
        return Collections.EMPTY_MAP;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.SubtreeProvider#searchPrincipal(eu.agno3.fileshare.webdav.internal.FileshareDAVTreeProviderInternal,
     *      eu.agno3.runtime.webdav.server.DAVTreeNode, eu.agno3.fileshare.webdav.internal.DAVLayout,
     *      eu.agno3.runtime.webdav.server.acl.FixedPrincipalSearchReport)
     */
    @Override
    public Map<DAVTreeNode<EntityKey>, Status> searchPrincipal ( FileshareDAVTreeProviderInternal dt, DAVTreeNode<EntityKey> wrapped,
            DAVLayout layout, FixedPrincipalSearchReport ps ) throws FileshareException {
        return Collections.EMPTY_MAP;
    }

}
