/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2016 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.Status;
import org.apache.jackrabbit.webdav.security.report.PrincipalMatchReport;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.util.WebUtils;
import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.AccessDeniedException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.SyncException;
import eu.agno3.fileshare.model.ContainerEntity;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.EntityType;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.NativeEntityKey;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.vfs.VFSChange;
import eu.agno3.fileshare.vfs.VFSContainerChange;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.fileshare.vfs.VFSEntityChange;
import eu.agno3.fileshare.webdav.sync.SyncTokenData;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.util.iter.ClosableIterator;
import eu.agno3.runtime.webdav.server.DAVTreeNode;
import eu.agno3.runtime.webdav.server.PathEscapeUtil;
import eu.agno3.runtime.webdav.server.acl.FixedPrincipalSearchReport;
import eu.agno3.runtime.webdav.server.colsync.ColSyncReport;


/**
 * @author mbechler
 *
 */
public class UserRootSubtreeProvider implements SubtreeProvider {

    private static final Logger log = Logger.getLogger(UserRootSubtreeProvider.class);


    /**
     * 
     * @param dt
     * @return the user root node
     * @throws DavException
     */
    public DAVTreeNode<EntityKey> getRootNode ( FileshareDAVTreeProviderImpl dt ) throws DavException {
        ContainerEntity userRoot = null;
        try {
            DAVLayout layout = FileshareDAVTreeProviderImpl.getLayout(WebUtils.getHttpRequest(SecurityUtils.getSubject()));
            if ( SecurityUtils.getSubject().isAuthenticated() ) {
                if ( layout != DAVLayout.OWNCLOUD ) {
                    return new RootDAVNode(null, dt.getAccessControl().getCurrentUser().getId(), layout);
                }

                userRoot = dt.getBrowseService().getUserRoot();
                if ( userRoot != null ) {
                    return makeUserRootNode(dt, userRoot, layout);
                }
                log.debug("No user root available for " + SecurityUtils.getSubject().getPrincipals().oneByType(UserPrincipal.class)); //$NON-NLS-1$
            }
            else {
                log.debug("Not authenticated"); //$NON-NLS-1$
                throw new AccessDeniedException();
            }

            DateTime lastModified = layout == DAVLayout.OWNCLOUD ? getRootLastModifiedRecursive(dt, userRoot)
                    : getRootVirtualLastModified(dt, userRoot);
            return new RootDAVNode(lastModified, dt.getAccessControl().getCurrentUser().getId(), layout);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            FileshareDAVTreeProviderImpl.wrapException(e);
            return null;
        }
    }


    /**
     * @param userRoot
     * @return
     * @throws FileshareException
     */
    private static DAVTreeNode<EntityKey> makeUserRootNode ( FileshareDAVTreeProviderInternal dt, VFSContainerEntity userRoot, DAVLayout layout )
            throws FileshareException {
        UserRootDAVNode root = new UserRootDAVNode(Util.getRootId(dt, layout), userRoot, layout);
        DateTime lastMod = layout == DAVLayout.OWNCLOUD ? getRootLastModifiedRecursive(dt, userRoot) : getRootVirtualLastModified(dt, userRoot);
        if ( log.isDebugEnabled() ) {
            log.debug("Root last modified is " + lastMod); //$NON-NLS-1$
        }

        root.setModificationTime(lastMod);

        if ( userRoot.getOwner().getQuota() != null ) {
            long usedSize = dt.getQuotaService().getUsedSize(userRoot);
            root.setQuotaAvailable(userRoot.getOwner().getQuota() - usedSize);
            root.setQuotaUsed(usedSize);
        }
        root.setPermissions(EnumSet.allOf(GrantPermission.class));
        return root;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.SubtreeProvider#handlesChildren(eu.agno3.runtime.webdav.server.DAVTreeNode,
     *      eu.agno3.fileshare.webdav.internal.DAVLayout)
     */
    @Override
    public boolean handlesChildren ( DAVTreeNode<EntityKey> node, DAVLayout layout ) {
        return node instanceof UserRootDAVNode || node instanceof RootDAVNode;
    }


    @Override
    public Collection<DAVTreeNode<EntityKey>> getChildren ( FileshareDAVTreeProviderInternal dt, DAVTreeNode<EntityKey> node, DAVLayout layout )
            throws FileshareException {
        Collection<DAVTreeNode<EntityKey>> children;
        if ( node instanceof UserRootDAVNode ) {
            children = dt.getEntityChildren(node, EnumSet.allOf(GrantPermission.class), false, layout);

            if ( layout == DAVLayout.OWNCLOUD ) {
                Iterator<DAVTreeNode<EntityKey>> child = children.iterator();

                while ( child.hasNext() ) {
                    DAVTreeNode<EntityKey> n = child.next();
                    if ( FileshareDAVTreeProviderImpl.ROOT_BLACKLIST.contains(n.getPathName()) ) {
                        child.remove();
                    }
                }
            }
            else {
                return children;
            }
        }
        else if ( node instanceof RootDAVNode ) {
            children = new ArrayList<>();
            if ( SecurityUtils.getSubject().isAuthenticated() ) {
                VFSContainerEntity userRoot = dt.getBrowseService().getUserRoot();
                if ( userRoot != null ) {
                    children.add(makeUserRootNode(dt, userRoot, layout));
                }
                else {
                    log.debug("No user root available for " + SecurityUtils.getSubject().getPrincipals().oneByType(UserPrincipal.class)); //$NON-NLS-1$
                }
            }
        }
        else {
            return Collections.EMPTY_LIST;
        }

        if ( !SecurityUtils.getSubject().isAuthenticated() ) {
            log.debug("Not authenticated"); //$NON-NLS-1$
            return children;
        }

        for ( SubtreeProvider subtreeProvider : dt.getProviders() ) {
            children.addAll(subtreeProvider.getRootChildren(dt, layout));
        }

        return children;
    }


    @Override
    public Map<DAVTreeNode<EntityKey>, Status> findPrincipalMatch ( FileshareDAVTreeProviderInternal dt, DAVTreeNode<EntityKey> wrapped,
            DAVLayout layout, PrincipalMatchReport pm ) {
        return Collections.EMPTY_MAP;
    }


    @Override
    public Map<DAVTreeNode<EntityKey>, Status> searchPrincipal ( FileshareDAVTreeProviderInternal dt, DAVTreeNode<EntityKey> wrapped,
            DAVLayout layout, FixedPrincipalSearchReport ps ) {
        return null;
    }


    @Override
    public boolean contributeChanges ( FileshareDAVTreeProviderInternal dt, DAVTreeNode<EntityKey> rootNode, SyncTokenData inputToken,
            SyncTokenData outputToken, ColSyncReport<EntityKey> cs, DAVLayout layout, boolean rootModified )
            throws FileshareException, IOException, DavException {

        if ( ! ( rootNode instanceof RootDAVNode ) && ! ( rootNode instanceof UserRootDAVNode ) ) {
            return false;
        }

        VFSContainerEntity userRoot = dt.getBrowseService().getUserRoot();
        if ( userRoot == null && inputToken != null && inputToken.isHaveUserRoot() ) {
            // removal
            UserRootDAVNode node = new UserRootDAVNode(new NativeEntityKey(dt.getAccessControl().getCurrentUser().getId()), null, layout);
            cs.emit404(node, dt.getAbsolutePath(node));
            return true;
        }
        else if ( userRoot != null && ( ( inputToken == null || rootModified || !inputToken.isHaveUserRoot() )
                || userRoot.getLastModified().isAfter(inputToken.getLastModified()) ) ) {
            // addition or modification
            cs.emit200(makeUserRootNode(dt, userRoot, layout));
        }

        if ( userRoot != null ) {
            outputToken.setHaveUserRoot(true);
            outputToken.updateLastModified(userRoot.getLastModified());

            // add modified/deleted entries
            try ( VFSContext vc = dt.getVfs().getVFS(userRoot.getEntityKey()).begin(true);
                  ClosableIterator<VFSChange> modified = vc.findModifiedSince(userRoot, inputToken != null ? inputToken.getLastModified() : null) ) {
                while ( modified.hasNext() ) {
                    VFSChange ch = modified.next();
                    handleChange(dt, outputToken, cs, layout, ch, vc);
                }
            }
        }

        return true;
    }


    /**
     * @param dt
     * @param outputToken
     * @param res
     * @param layout
     * @param ch
     * @throws FileshareException
     * @throws IOException
     * @throws DavException
     */
    void handleChange ( FileshareDAVTreeProviderInternal dt, SyncTokenData outputToken, ColSyncReport<EntityKey> cs, DAVLayout layout, VFSChange ch,
            VFSContext vc ) throws FileshareException, IOException, DavException {
        if ( ch instanceof VFSEntityChange ) {
            VFSEntity e = ( (VFSEntityChange) ch ).getEntity();
            outputToken.updateLastModified(e.getLastModified());
            cs.emit200(dt.adapt(vc, e, EnumSet.allOf(GrantPermission.class), false, layout, null));
        }
        else if ( ch instanceof VFSContainerChange ) {
            VFSContainerChange vch = (VFSContainerChange) ch;
            outputToken.updateLastModified(vch.getChangeTime());
            VFSContainerEntity container = vch.getContainer();
            boolean replaced = Util.wasReplacedLater(vc, vch, container);
            if ( !replaced ) {
                RemovedDAVNode node = new RemovedDAVNode(
                    dt.adapt(vc, container, EnumSet.allOf(GrantPermission.class), false, layout, null),
                    vch.getEntityName(),
                    vch.getEntityType() == EntityType.DIRECTORY,
                    layout);
                cs.emit404(node, dt.getAbsolutePath(node));
            }
        }
        else {
            throw new SyncException();
        }
    }


    @Override
    public boolean contributePath ( FileshareDAVTreeProviderInternal fd, DAVTreeNode<EntityKey> node, DAVLayout l, StringBuilder path )
            throws FileshareException {
        if ( node instanceof EntityDAVNode ) {
            EntityDAVNode en = (EntityDAVNode) node;
            VFSEntity e = en.getEntity();
            Subject owner = e.getOwner();
            if ( owner instanceof User && en.getLayout() != DAVLayout.OWNCLOUD && ( (EntityDAVNode) node ).getGrantId() == null ) {
                path.append(UserRootDAVNode.USER_FILES_PATH);
            }
            else if ( owner instanceof User && ( (EntityDAVNode) node ).getGrantId() == null ) {}
            else {
                return false;
            }
            try ( VFSContext vc = fd.getVfs().getVFS(e.getEntityKey()).begin(true) ) {
                for ( String elem : fd.getEntityService().getFullPath(vc, e, false) ) {
                    path.append('/').append(PathEscapeUtil.escapePathSegment(elem));
                }
            }
            return true;
        }
        return false;
    }


    @Override
    public boolean isApplicable ( String repositoryPath, DAVLayout layout ) {
        if ( layout == DAVLayout.OWNCLOUD ) {
            return true;
        }
        return repositoryPath.startsWith(UserRootDAVNode.USER_FILES_PATH);
    }


    @Override
    public DAVTreeNode<EntityKey> resolve ( FileshareDAVTreeProviderInternal dt, String repositoryPath, DAVLayout layout )
            throws FileshareException, DavException {
        ContainerEntity userRoot = dt.getBrowseService().getUserRoot();
        if ( userRoot == null ) {
            log.debug("No user root available for " + SecurityUtils.getSubject().getPrincipals().oneByType(UserPrincipal.class)); //$NON-NLS-1$
            return null;
        }

        DAVTreeNode<EntityKey> r = null;
        String relativePath = null;
        if ( layout == DAVLayout.OWNCLOUD && StringUtils.isBlank(repositoryPath) || "/".equals(repositoryPath) ) { //$NON-NLS-1$
            r = makeUserRootNode(dt, userRoot, layout);
        }
        else if ( layout != DAVLayout.OWNCLOUD && StringUtils.isBlank(repositoryPath) || "/".equals(repositoryPath) ) { //$NON-NLS-1$
            r = new RootDAVNode(null, dt.getAccessControl().getCurrentUser().getId(), layout);
        }
        else if ( layout != DAVLayout.OWNCLOUD && repositoryPath.startsWith(UserRootDAVNode.USER_FILES_PATH) ) {
            if ( UserRootDAVNode.USER_FILES_PATH.equals(repositoryPath) ) {
                r = makeUserRootNode(dt, userRoot, layout);
            }
            else {
                relativePath = repositoryPath.substring(UserRootDAVNode.USER_FILES_PATH.length() + 1);
            }
        }
        else {
            relativePath = repositoryPath.substring(1);
        }

        if ( r == null && relativePath != null ) {
            try ( VFSContext v = dt.getVfs().getNative().begin(true) ) {
                r = dt.resolveRelative(v, userRoot, relativePath, EnumSet.allOf(GrantPermission.class), false, layout, null);
            }
        }

        if ( r instanceof EntityDAVNode ) {
            EntityDAVNode er = (EntityDAVNode) r;
            er.setQuotaUsed(userRoot.getChildrenSize());
            if ( userRoot.getOwner().getQuota() != null ) {
                er.setQuotaAvailable(userRoot.getOwner().getQuota() - userRoot.getChildrenSize());
            }
            er.setPermissions(EnumSet.allOf(GrantPermission.class));
        }
        return r;
    }


    @Override
    public Collection<? extends DAVTreeNode<EntityKey>> getRootChildren ( FileshareDAVTreeProviderInternal dt, DAVLayout layout ) {
        return Collections.EMPTY_LIST;
    }


    /**
     * @param userRoot
     * @return
     */
    private static DateTime getRootVirtualLastModified ( FileshareDAVTreeProviderInternal dt, VFSContainerEntity userRoot ) {
        DateTime mod = userRoot != null ? userRoot.getLastModified() : null;
        DateTime groupLastMod = GroupsRootSubtreeProvider.getGroupsLastModified(dt);
        if ( mod == null || ( groupLastMod != null && groupLastMod.isAfter(groupLastMod) ) ) {
            mod = groupLastMod;
        }
        DateTime otherLastMod = OthersRootSubtreeProvider.getSharesLastModified(dt);
        if ( mod == null || ( otherLastMod != null && otherLastMod.isAfter(groupLastMod) ) ) {
            mod = otherLastMod;
        }
        return mod;
    }


    private static DateTime getRootLastModifiedRecursive ( FileshareDAVTreeProviderInternal dt, VFSContainerEntity userRoot ) {
        DateTime mod = dt.getRecursiveEntityLastModified(userRoot);

        DateTime groupLastMod = GroupsRootSubtreeProvider.getGroupsRecursiveLastModified(dt);

        if ( mod == null || ( groupLastMod != null && groupLastMod.isAfter(mod) ) ) {
            mod = groupLastMod;
        }

        DateTime shareLastMod = OthersRootSubtreeProvider.getSharesRecursiveLastModified(dt);
        if ( mod == null || ( shareLastMod != null && shareLastMod.isAfter(mod) ) ) {
            mod = shareLastMod;
        }

        return mod;
    }

}
