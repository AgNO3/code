/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2016 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.Status;
import org.apache.jackrabbit.webdav.security.report.PrincipalMatchReport;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.EntityType;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.PolicyViolation;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.vfs.VFSChange;
import eu.agno3.fileshare.vfs.VFSContainerChange;
import eu.agno3.fileshare.vfs.VFSContext;
import eu.agno3.fileshare.vfs.VFSEntityChange;
import eu.agno3.fileshare.webdav.sync.SyncTokenData;
import eu.agno3.runtime.util.iter.ClosableIterator;
import eu.agno3.runtime.webdav.server.DAVTreeNode;
import eu.agno3.runtime.webdav.server.PathEscapeUtil;
import eu.agno3.runtime.webdav.server.acl.FixedPrincipalSearchReport;
import eu.agno3.runtime.webdav.server.colsync.ColSyncReport;


/**
 * @author mbechler
 *
 */
public class GroupsRootSubtreeProvider implements SubtreeProvider {

    private static final Logger log = Logger.getLogger(GroupsRootSubtreeProvider.class);


    @Override
    public boolean isApplicable ( String repositoryPath, DAVLayout layout ) {
        return repositoryPath.startsWith(GroupsRootDAVNode.GROUPS_PATH);
    }


    @Override
    public boolean handlesChildren ( DAVTreeNode<EntityKey> node, DAVLayout layout ) {
        return node instanceof GroupsRootDAVNode;
    }


    @Override
    public Collection<? extends DAVTreeNode<EntityKey>> getRootChildren ( FileshareDAVTreeProviderInternal dt, DAVLayout layout )
            throws FileshareException {
        List<VFSContainerEntity> groupRoots = new ArrayList<>();
        Set<PolicyViolation> violations = new HashSet<>();
        dt.getBrowseService().getVisibleGroupsInternal(groupRoots, violations, false);
        if ( !groupRoots.isEmpty() ) {
            return Collections.singleton(
                new GroupsRootDAVNode(
                    Util.getRootId(dt, layout),
                    layout == DAVLayout.OWNCLOUD ? getGroupsRecursiveLastModified(dt) : getGroupsLastModified(dt),
                    layout));
        }
        log.trace("No groups found"); //$NON-NLS-1$
        return Collections.EMPTY_LIST;
    }


    @Override
    public Collection<DAVTreeNode<EntityKey>> getChildren ( FileshareDAVTreeProviderInternal dt, DAVTreeNode<EntityKey> node, DAVLayout layout )
            throws FileshareException {
        List<VFSContainerEntity> groupRoots = new ArrayList<>();
        Set<PolicyViolation> violations = new HashSet<>();
        dt.getBrowseService().getVisibleGroupsInternal(groupRoots, violations, false);
        List<DAVTreeNode<EntityKey>> res = new ArrayList<>();
        for ( VFSContainerEntity groupRoot : groupRoots ) {
            GroupRootDAVNode e = new GroupRootDAVNode(groupRoot, layout);
            if ( layout == DAVLayout.OWNCLOUD ) {
                e.setModificationTime(dt.getRecursiveEntityLastModified(groupRoot));
            }
            e.setPermissions(EnumSet.allOf(GrantPermission.class));
            res.add(e);
        }
        return res;
    }


    @Override
    public boolean contributeChanges ( FileshareDAVTreeProviderInternal dt, DAVTreeNode<EntityKey> rootNode, SyncTokenData inputToken,
            SyncTokenData outputToken, ColSyncReport<EntityKey> cs, DAVLayout layout, boolean rootModified )
            throws FileshareException, IOException, DavException {

        if ( rootNode instanceof GroupRootDAVNode ) {
            contributeGroupChanges(dt, rootNode, inputToken, outputToken, cs, layout, new HashSet<>(), ( (GroupRootDAVNode) rootNode ).getEntity());
            return true;
        }

        if ( ! ( rootNode instanceof RootDAVNode ) && ! ( rootNode instanceof GroupsRootDAVNode ) ) {
            return false;
        }

        List<VFSContainerEntity> groupRoots = new ArrayList<>();
        Set<PolicyViolation> violations = new HashSet<>();
        dt.getBrowseService().getVisibleGroupsInternal(groupRoots, violations, false);

        if ( groupRoots.isEmpty() && inputToken != null && inputToken.isHaveGroups() ) {
            // removal
            GroupsRootDAVNode node = new GroupsRootDAVNode(Util.getRootId(dt, layout), null, layout);
            cs.emit404(node, dt.getAbsolutePath(node));
            return true;
        }
        else if ( !groupRoots.isEmpty() && ( inputToken == null || rootModified || !inputToken.isHaveGroups() ) ) {
            // addition
            cs.emit200(new GroupsRootDAVNode(Util.getRootId(dt, layout), getGroupsLastModified(dt), layout));
        }

        Set<UUID> removedGroups = new HashSet<>();
        if ( inputToken != null ) {
            removedGroups.addAll(inputToken.getVisibleGroups());
        }
        if ( !groupRoots.isEmpty() ) {
            outputToken.setHaveGroups(true);
            for ( VFSContainerEntity groupRoot : groupRoots ) {
                contributeGroupChanges(dt, rootNode, inputToken, outputToken, cs, layout, removedGroups, groupRoot);
            }
        }

        for ( UUID groupId : removedGroups ) {
            GroupRootDAVNode node = new GroupRootDAVNode(groupId, layout);
            cs.emit404(node, dt.getAbsolutePath(node));
        }

        return true;
    }


    private void contributeGroupChanges ( FileshareDAVTreeProviderInternal dt, DAVTreeNode<EntityKey> rootNode, SyncTokenData inputToken,
            SyncTokenData outputToken, ColSyncReport<EntityKey> cs, DAVLayout layout, Set<UUID> removedGroups, VFSContainerEntity groupRoot )
            throws FileshareException, IOException, DavException, EntityNotFoundException {
        UUID gid = groupRoot.getOwner().getId();
        outputToken.getVisibleGroups().add(gid);
        outputToken.updateLastModified(groupRoot.getLastModified());
        removedGroups.remove(gid);

        // add modified/deleted entries
        try ( VFSContext vc = dt.getVfs().getVFS(groupRoot.getEntityKey()).begin(true) ) {

            Long lm = inputToken != null ? inputToken.getLastModified() : null;

            if ( !vc.findModifiedSinceSupported(groupRoot, lm) ) {
                if ( rootNode instanceof GroupsRootDAVNode || rootNode instanceof RootDAVNode ) {
                    GroupRootDAVNode node = new GroupRootDAVNode(groupRoot, layout);
                    cs.emit403(node, dt.getAbsolutePath(node), true);
                    return;
                }

                throw new DavException(
                    403,
                    "Need full sync", //$NON-NLS-1$
                    null,
                    FileshareDAVTreeProviderImpl.createError(DavConstants.NAMESPACE, "valid-sync-token")); //$NON-NLS-1$
            }

            try ( ClosableIterator<VFSChange> changes = vc.findModifiedSince(groupRoot, lm) ) {
                while ( changes.hasNext() ) {
                    VFSChange ch = changes.next();
                    handleChange(vc, dt, outputToken, cs, layout, ch);
                }
            }
        }
        catch ( eu.agno3.fileshare.exceptions.UnsupportedOperationException e ) {
            log.warn("Sync unsupported for " + groupRoot, e); //$NON-NLS-1$
            if ( rootNode instanceof GroupsRootDAVNode || rootNode instanceof RootDAVNode ) {
                GroupRootDAVNode node = new GroupRootDAVNode(groupRoot, layout);
                cs.emit403(node, dt.getAbsolutePath(node), false);
                return;
            }
            throw new DavException(403);
        }

        if ( inputToken == null || !inputToken.getVisibleGroups().contains(gid) || groupRoot.getLastModified() == null
                || groupRoot.getLastModified().isAfter(inputToken.getLastModified()) ) {
            cs.emit200(new GroupRootDAVNode(groupRoot, layout));
        }
    }


    /**
     * @param dt
     * @param outputToken
     * @param res
     * @param layout
     * @param ch
     * @throws FileshareException
     * @throws DavException
     * @throws IOException
     */
    void handleChange ( VFSContext vc, FileshareDAVTreeProviderInternal dt, SyncTokenData outputToken, ColSyncReport<EntityKey> cs, DAVLayout layout,
            VFSChange ch ) throws FileshareException, IOException, DavException {
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
            throw new UnsupportedOperationException();
        }
    }


    @Override
    public boolean contributePath ( FileshareDAVTreeProviderInternal fd, DAVTreeNode<EntityKey> node, DAVLayout l, StringBuilder path )
            throws EntityNotFoundException, FileshareException {
        if ( node instanceof GroupRootDAVNode ) {
            path.append(GroupsRootDAVNode.GROUPS_PATH).append('/');
            GroupRootDAVNode gn = (GroupRootDAVNode) node;
            if ( gn.getLayout() == DAVLayout.NATIVE ) {
                path.append(gn.getGroupId());
            }
            else {
                path.append(GroupRootDAVNode.getGroupName(gn.getEntity()));
            }
            return true;
        }

        if ( node instanceof EntityDAVNode ) {
            EntityDAVNode en = (EntityDAVNode) node;
            if ( en.getGrantId() == null && en.getEntity().getOwner() instanceof Group ) {
                Group group = (Group) en.getEntity().getOwner();
                path.append(GroupsRootDAVNode.GROUPS_PATH).append('/');
                if ( en.getLayout() == DAVLayout.NATIVE ) {
                    path.append(group.getId().toString());
                }
                else {
                    path.append(group.getName());
                }

                try ( VFSContext vc = fd.getVfs().getVFS(en.getEntity().getEntityKey()).begin(true) ) {
                    for ( String elem : fd.getEntityService().getFullPath(vc, en.getEntity(), false) ) {
                        path.append('/').append(PathEscapeUtil.escapePathSegment(elem));
                    }
                }
                return true;
            }
        }

        return false;
    }


    @Override
    public DAVTreeNode<EntityKey> resolve ( FileshareDAVTreeProviderInternal dt, String repositoryPath, DAVLayout layout )
            throws FileshareException, DavException {

        if ( GroupsRootDAVNode.GROUPS_PATH.equals(repositoryPath) ) {
            return new GroupsRootDAVNode(
                Util.getRootId(dt, layout),
                ( layout == DAVLayout.OWNCLOUD ) ? getGroupsRecursiveLastModified(dt) : getGroupsLastModified(dt),
                layout);
        }

        if ( repositoryPath.charAt(GroupsRootDAVNode.GROUPS_PATH.length()) != '/' ) {
            return null;
        }

        int nextSep = repositoryPath.indexOf('/', GroupsRootDAVNode.GROUPS_PATH.length() + 1);
        String groupIdentifier;
        String relativePath;
        if ( nextSep < 0 ) {
            groupIdentifier = PathEscapeUtil.unescapePathSegment(repositoryPath.substring(GroupsRootDAVNode.GROUPS_PATH.length() + 1));
            relativePath = null;
        }
        else {
            groupIdentifier = PathEscapeUtil.unescapePathSegment(repositoryPath.substring(GroupsRootDAVNode.GROUPS_PATH.length() + 1, nextSep));
            relativePath = repositoryPath.substring(nextSep + 1);
        }

        VFSContainerEntity groupRoot;

        if ( layout == DAVLayout.NATIVE ) {
            groupRoot = dt.getBrowseService().getOrCreateGroupRoot(UUID.fromString(groupIdentifier));
        }
        else {
            int realmSepPos = groupIdentifier.lastIndexOf('@');
            String realm = null;
            if ( realmSepPos > 0 ) {
                realm = groupIdentifier.substring(realmSepPos + 1);
                groupIdentifier = groupIdentifier.substring(0, realmSepPos);
            }
            groupRoot = dt.getBrowseService().getGroupRootByName(groupIdentifier, realm);
        }

        if ( groupRoot == null ) {
            log.debug("Group not found " + groupIdentifier); //$NON-NLS-1$
            return null;
        }

        try ( VFSContext v = dt.getVfs().getVFS(groupRoot.getEntityKey()).begin(true) ) {
            DAVTreeNode<EntityKey> r;
            if ( relativePath == null ) {
                EntityDAVNode g = new GroupRootDAVNode(groupRoot, layout);
                if ( layout == DAVLayout.OWNCLOUD ) {
                    g.setModificationTime(dt.getRecursiveEntityLastModified(groupRoot));
                }
                r = g;
            }
            else {
                r = dt.resolveRelative(v, groupRoot, relativePath, EnumSet.allOf(GrantPermission.class), false, layout, null);
            }

            if ( r instanceof EntityDAVNode ) {
                EntityDAVNode er = (EntityDAVNode) r;
                er.setQuotaUsed(groupRoot.getChildrenSize());
                if ( groupRoot.getOwner().getQuota() != null ) {
                    er.setQuotaAvailable(groupRoot.getOwner().getQuota() - groupRoot.getChildrenSize());
                }

                if ( groupRoot.isStaticReadOnly() ) {
                    er.setPermissions(EnumSet.of(GrantPermission.READ, GrantPermission.BROWSE));
                }
                else {
                    er.setPermissions(EnumSet.allOf(GrantPermission.class));
                }
            }
            return r;
        }
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


    /**
     * @return
     */
    static DateTime getGroupsRecursiveLastModified ( FileshareDAVTreeProviderInternal dt ) {
        DateTime mod = dt.getContext().getGroupService().getGroupsRecursiveLastModified();
        if ( log.isDebugEnabled() ) {
            log.debug("Groups last modified " + mod); //$NON-NLS-1$
        }
        return mod;
    }


    /**
     * @return
     */
    static DateTime getGroupsLastModified ( FileshareDAVTreeProviderInternal dt ) {
        return dt.getContext().getGroupService().getGroupsLastModified();
    }
}
