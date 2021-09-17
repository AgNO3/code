/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2016 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.apache.jackrabbit.webdav.DavException;
import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.query.CollectionResult;
import eu.agno3.fileshare.security.AccessControlService;
import eu.agno3.fileshare.service.QuotaService;
import eu.agno3.fileshare.service.api.internal.BrowseServiceInternal;
import eu.agno3.fileshare.service.api.internal.EntityServiceInternal;
import eu.agno3.fileshare.service.api.internal.PolicyEvaluator;
import eu.agno3.fileshare.service.api.internal.VFSServiceInternal;
import eu.agno3.fileshare.service.gui.GuiServiceContext;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.fileshare.webdav.FileshareDAVTreeProvider;
import eu.agno3.runtime.webdav.server.DAVTreeNode;


/**
 * @author mbechler
 *
 */
public interface FileshareDAVTreeProviderInternal extends FileshareDAVTreeProvider {

    /**
     * @return the service context
     */
    GuiServiceContext getContext ();


    /**
     * @return the vfs service
     */
    VFSServiceInternal getVfs ();


    /**
     * @return the quota service
     */
    QuotaService getQuotaService ();


    /**
     * @return the policy evauluator
     */
    PolicyEvaluator getPolicyEvaluator ();


    /**
     * @return the browse service
     */
    BrowseServiceInternal getBrowseService ();


    /**
     * @return the entity service
     */
    EntityServiceInternal getEntityService ();


    /**
     * @return the access control service
     */
    AccessControlService getAccessControl ();


    /**
     * @param e
     * @param permissions
     * @param shared
     * @param layout
     * @param grantId
     * @param parentKey
     * @return adapted node
     */
    EntityDAVNode adapt ( VFSEntity e, Set<GrantPermission> permissions, boolean shared, DAVLayout layout, UUID grantId, EntityKey parentKey );


    /**
     * @param ctx
     * @param e
     * @param parentPath
     * @param permissions
     * @param shared
     * @param layout
     * @param grantId
     * @return adapted node
     * @throws FileshareException
     */
    EntityDAVNode adapt ( VFSContext ctx, VFSEntity e, Set<GrantPermission> permissions, boolean shared, DAVLayout layout, UUID grantId )
            throws FileshareException;


    /**
     * @param res
     * @param permissions
     * @param shared
     * @param layout
     * @param grantId
     * @param parentKey
     * @return adapted node
     * @throws FileshareException
     */
    Collection<DAVTreeNode<EntityKey>> adapt ( CollectionResult<VFSEntity> res, Set<GrantPermission> permissions, boolean shared, DAVLayout layout,
            UUID grantId, EntityKey parentKey ) throws FileshareException;


    /**
     * 
     * All nodes must have the same parent
     * 
     * @param ctx
     * 
     * @param res
     * @param parentPath
     * @param permissions
     * @param shared
     * @param layout
     * @param grantId
     * @return adapted nodes
     * @throws FileshareException
     */
    Collection<DAVTreeNode<EntityKey>> adapt ( VFSContext ctx, CollectionResult<VFSEntity> res, Set<GrantPermission> permissions, boolean shared,
            DAVLayout layout, UUID grantId ) throws FileshareException;


    /**
     * @param v
     * @param root
     * @param rootPath
     * @param relative
     * @param permissions
     * @param shared
     * @param layout
     * @param grantId
     * @return the resolved node, null if not exists
     * @throws FileshareException
     * @throws DavException
     */
    DAVTreeNode<EntityKey> resolveRelative ( VFSContext v, VFSContainerEntity root, String relative, Set<GrantPermission> permissions, boolean shared,
            DAVLayout layout, UUID grantId ) throws FileshareException, DavException;


    /**
     * @param node
     * @param permissions
     * @param shared
     * @param layout
     * @return the entities children
     * @throws FileshareException
     */
    Collection<DAVTreeNode<EntityKey>> getEntityChildren ( DAVTreeNode<EntityKey> node, Set<GrantPermission> permissions, boolean shared,
            DAVLayout layout ) throws FileshareException;


    /**
     * @param e
     * @return the entities recursive modification time
     */
    DateTime getRecursiveEntityLastModified ( VFSContainerEntity e );


    /**
     * @return the registered subtree providers
     */
    Collection<SubtreeProvider> getProviders ();

}
