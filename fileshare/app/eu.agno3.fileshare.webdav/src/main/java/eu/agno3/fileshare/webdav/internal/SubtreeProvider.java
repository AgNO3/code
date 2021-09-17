/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2016 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.Status;
import org.apache.jackrabbit.webdav.security.report.PrincipalMatchReport;

import eu.agno3.fileshare.exceptions.AuthenticationException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.webdav.sync.SyncTokenData;
import eu.agno3.runtime.webdav.server.DAVTreeNode;
import eu.agno3.runtime.webdav.server.acl.FixedPrincipalSearchReport;
import eu.agno3.runtime.webdav.server.colsync.ColSyncReport;


/**
 * @author mbechler
 *
 */
public interface SubtreeProvider {

    /**
     * @param repositoryPath
     * @param layout
     * @return whether this provider can resolve the node
     */
    boolean isApplicable ( String repositoryPath, DAVLayout layout );


    /**
     * @param dt
     * @param repositoryPath
     * @param layout
     * @return the resolved node
     * @throws FileshareException
     * @throws DavException
     */
    DAVTreeNode<EntityKey> resolve ( FileshareDAVTreeProviderInternal dt, String repositoryPath, DAVLayout layout )
            throws FileshareException, DavException;


    /**
     * @param node
     * @param layout
     * @return whether this provider handles the given nodes children
     */
    boolean handlesChildren ( DAVTreeNode<EntityKey> node, DAVLayout layout );


    /**
     * @param dt
     * @param node
     * @param layout
     * @return the children of the node
     * @throws FileshareException
     */
    Collection<DAVTreeNode<EntityKey>> getChildren ( FileshareDAVTreeProviderInternal dt, DAVTreeNode<EntityKey> node, DAVLayout layout )
            throws FileshareException;


    /**
     * @param dt
     * @param layout
     * @return nodes to add to the root
     * @throws FileshareException
     */
    Collection<? extends DAVTreeNode<EntityKey>> getRootChildren ( FileshareDAVTreeProviderInternal dt, DAVLayout layout ) throws FileshareException;


    /**
     * @param dt
     * @param rootNode
     * @param inputToken
     * @param outputToken
     * @param cs
     * @param layout
     * @param rootModified
     * @return whether this provider handled the request
     * @throws FileshareException
     * @throws IOException
     * @throws DavException
     */
    boolean contributeChanges ( FileshareDAVTreeProviderInternal dt, DAVTreeNode<EntityKey> rootNode, SyncTokenData inputToken,
            SyncTokenData outputToken, ColSyncReport<EntityKey> cs, DAVLayout layout, boolean rootModified )
                    throws FileshareException, IOException, DavException;


    /**
     * @param dt
     * @param node
     * @param l
     * @param path
     * @return whether the path was handled
     * @throws FileshareException
     */
    boolean contributePath ( FileshareDAVTreeProviderInternal dt, DAVTreeNode<EntityKey> node, DAVLayout l, StringBuilder path )
            throws FileshareException;


    /**
     * @param dt
     * @param wrapped
     * @param layout
     * @param pm
     * @return the nodes matching the given principal
     * @throws FileshareException
     * @throws AuthenticationException
     */
    Map<DAVTreeNode<EntityKey>, Status> findPrincipalMatch ( FileshareDAVTreeProviderInternal dt, DAVTreeNode<EntityKey> wrapped, DAVLayout layout,
            PrincipalMatchReport pm ) throws FileshareException;


    /**
     * @param dt
     * @param wrapped
     * @param layout
     * @param ps
     * @return the principal nodes matching the given properties
     * @throws FileshareException
     */
    Map<DAVTreeNode<EntityKey>, Status> searchPrincipal ( FileshareDAVTreeProviderInternal dt, DAVTreeNode<EntityKey> wrapped, DAVLayout layout,
            FixedPrincipalSearchReport ps ) throws FileshareException;

}
