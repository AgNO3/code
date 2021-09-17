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

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.Status;
import org.apache.jackrabbit.webdav.security.report.PrincipalMatchReport;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.ContainerEntity;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.EntityType;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.NativeEntityKey;
import eu.agno3.fileshare.model.SubjectGrant;
import eu.agno3.fileshare.model.SubjectInfo;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.query.CollectionResult;
import eu.agno3.fileshare.model.query.SubjectQueryResult;
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
public class OthersRootSubtreeProvider implements SubtreeProvider {

    private static final Logger log = Logger.getLogger(OthersRootSubtreeProvider.class);


    @Override
    public boolean isApplicable ( String repositoryPath, DAVLayout layout ) {
        return repositoryPath.startsWith(OthersRootDAVNode.OTHERS_PATH);
    }


    @Override
    public boolean handlesChildren ( DAVTreeNode<EntityKey> node, DAVLayout layout ) {
        return node instanceof OthersRootDAVNode || node instanceof SharingSubjectDAVNode;
    }


    @Override
    public Collection<DAVTreeNode<EntityKey>> getChildren ( FileshareDAVTreeProviderInternal dt, DAVTreeNode<EntityKey> node, DAVLayout layout )
            throws FileshareException {
        if ( node instanceof OthersRootDAVNode ) {
            return getOtherRootChildren(dt, layout);
        }
        else if ( ! ( layout == DAVLayout.NATIVE ) && node instanceof SharingSubjectDAVNode ) {
            return getSujbectShareChildren(dt, node, layout);
        }

        return Collections.EMPTY_LIST;
    }


    @Override
    public Collection<? extends DAVTreeNode<EntityKey>> getRootChildren ( FileshareDAVTreeProviderInternal dt, DAVLayout layout )
            throws FileshareException {
        CollectionResult<SubjectGrant> grants = dt.getBrowseService().getSharedToUserGrants();
        if ( grants != null && !grants.getCollection().isEmpty() ) {
            return Collections.singleton(
                new OthersRootDAVNode(
                    Util.getRootId(dt, layout),
                    layout == DAVLayout.OWNCLOUD ? getSharesRecursiveLastModified(dt) : getSharesLastModified(dt),
                    layout));
        }
        log.trace("No shares found"); //$NON-NLS-1$
        return Collections.EMPTY_LIST;
    }


    /**
     * @param dt
     * @param node
     * @param ocCompat
     * @return
     * @throws FileshareException
     */
    Collection<DAVTreeNode<EntityKey>> getSujbectShareChildren ( FileshareDAVTreeProviderInternal dt, DAVTreeNode<EntityKey> node, DAVLayout layout )
            throws FileshareException {
        List<DAVTreeNode<EntityKey>> res = new ArrayList<>();
        UUID subjectId = node.getId() instanceof NativeEntityKey ? ( (NativeEntityKey) node.getId() ).getId() : null;
        CollectionResult<SubjectGrant> grants = dt.getBrowseService().getSubjectShareGrants(subjectId);

        for ( SubjectGrant g : grants.getCollection() ) {
            EntityDAVNode adapted = dt.adapt(g.getEntity(), g.getPermissions(), true, layout, g.getId(), g.getEntity().getParent().getEntityKey());
            adapted.setOverridePath(makeGrantRootName(g, adapted, layout)); // $NON-NLS-1$
            res.add(adapted);
        }

        return res;
    }


    /**
     * @param dt
     * @param ocCompat
     * @return
     * @throws FileshareException
     */
    Collection<DAVTreeNode<EntityKey>> getOtherRootChildren ( FileshareDAVTreeProviderInternal dt, DAVLayout layout ) throws FileshareException {
        List<DAVTreeNode<EntityKey>> children = new ArrayList<>();
        if ( layout == DAVLayout.NATIVE ) {
            CollectionResult<SubjectGrant> sharedToUserGrants = dt.getBrowseService().getSharedToUserGrants();
            for ( SubjectGrant g : sharedToUserGrants.getCollection() ) {
                EntityDAVNode adapted = dt
                        .adapt(g.getEntity(), g.getPermissions(), true, layout, g.getId(), g.getEntity().getParent().getEntityKey());
                adapted.setOverridePath(g.getId().toString());
                children.add(adapted);
            }
            return children;
        }

        Set<SubjectQueryResult> sharingSubjects = dt.getBrowseService().getSharingSubjects();
        if ( sharingSubjects == null ) {
            return Collections.EMPTY_LIST;
        }
        for ( SubjectQueryResult subj : sharingSubjects ) {
            children.add(
                new SharingSubjectDAVNode(
                    subj,
                    layout == DAVLayout.OWNCLOUD ? getSubjectSharesRecursiveLastModified(dt, subj) : getSubjectSharesLastModified(dt, subj),
                    layout));
        }
        return children;
    }


    @Override
    public boolean contributeChanges ( FileshareDAVTreeProviderInternal dt, DAVTreeNode<EntityKey> rootNode, SyncTokenData inputToken,
            SyncTokenData outputToken, ColSyncReport<EntityKey> cs, DAVLayout layout, boolean rootModified )
                    throws FileshareException, IOException, DavException {

        if ( ! ( rootNode instanceof RootDAVNode ) && ! ( rootNode instanceof OthersRootDAVNode ) ) {
            return false;
        }

        CollectionResult<SubjectGrant> grants = dt.getBrowseService().getSharedToUserGrants();

        if ( grants.getCollection().isEmpty() && inputToken != null && inputToken.isHaveShares() ) {
            // removal
            OthersRootDAVNode node = new OthersRootDAVNode(Util.getRootId(dt, layout), null, layout);
            cs.emit404(node, dt.getAbsolutePath(node));
            return true;
        }
        else if ( !grants.getCollection().isEmpty() && ( inputToken == null || rootModified || !inputToken.isHaveShares() ) ) {
            // addition
            cs.emit200(new OthersRootDAVNode(Util.getRootId(dt, layout), getSharesLastModified(dt), layout));
        }

        Set<UUID> removedGrants = new HashSet<>();

        if ( inputToken != null ) {
            removedGrants.addAll(inputToken.getVisibleGrants());
        }

        if ( !grants.getCollection().isEmpty() ) {
            outputToken.setHaveShares(true);
            for ( Grant g : grants.getCollection() ) {
                outputToken.getVisibleGrants().add(g.getId());
                removedGrants.remove(g.getId());
                outputToken.updateLastModified(g.getLastModified());
                outputToken.updateLastModified(g.getEntity().getLastModified());

                boolean doFull = inputToken == null || !inputToken.getVisibleGrants().contains(g.getId());
                VFSEntity ent = g.getEntity();
                if ( ent instanceof VFSContainerEntity ) {
                    VFSContainerEntity ce = (VFSContainerEntity) ent;
                    // add modified/deleted entries
                    try ( VFSContext vc = dt.getVfs().getVFS(ce.getEntityKey()).begin(true);
                          ClosableIterator<VFSChange> modified = vc.findModifiedSince(
                              vc.load(ce.getEntityKey(), VFSContainerEntity.class),
                              !doFull && inputToken != null ? inputToken.getLastModified() : null) ) {
                        while ( modified.hasNext() ) {
                            VFSChange ch = modified.next();
                            handleChange(vc, dt, outputToken, cs, layout, g, ce, ch);
                        }
                    }
                    catch ( eu.agno3.fileshare.exceptions.UnsupportedOperationException e ) {
                        RemovedDAVNode node = new RemovedDAVNode(
                            new OthersRootDAVNode(Util.getRootId(dt, layout), null, layout),
                            g.getId().toString(),
                            true,
                            layout);
                        cs.emit403(node, dt.getAbsolutePath(node), false);
                        continue;
                    }
                }

                if ( doFull || ( inputToken != null && g.getLastModified() != null && g.getLastModified().isAfter(inputToken.getLastModified()) )
                        || ( inputToken != null && g.getEntity().getLastModified().isAfter(inputToken.getLastModified()) ) ) {
                    EntityDAVNode e = dt
                            .adapt(g.getEntity(), g.getPermissions(), true, layout, g.getId(), new NativeEntityKey(OthersRootDAVNode.OTHERS_ID));
                    e.setOverridePath(g.getId().toString());
                    cs.emit200(e);
                }

            }
        }

        for ( UUID grantId : removedGrants ) {
            RemovedDAVNode node = new RemovedDAVNode(
                new OthersRootDAVNode(Util.getRootId(dt, layout), null, layout),
                grantId.toString(),
                true,
                layout);
            cs.emit404(node, dt.getAbsolutePath(node));
        }

        return true;
    }


    /**
     * @param dt
     * @param outputToken
     * @param res
     * @param layout
     * @param g
     * @param ce
     * @param ch
     * @throws FileshareException
     * @throws IOException
     * @throws DavException
     */
    void handleChange ( VFSContext vc, FileshareDAVTreeProviderInternal dt, SyncTokenData outputToken, ColSyncReport<EntityKey> cs, DAVLayout layout,
            Grant g, VFSContainerEntity ce, VFSChange ch ) throws FileshareException, IOException, DavException {
        if ( ch instanceof VFSEntityChange ) {
            VFSEntity e = ( (VFSEntityChange) ch ).getEntity();
            outputToken.updateLastModified(e.getLastModified());
            cs.emit200(dt.adapt(vc, e, EnumSet.allOf(GrantPermission.class), false, layout, g.getId()));
        }
        else if ( ch instanceof VFSContainerChange ) {
            VFSContainerChange vch = (VFSContainerChange) ch;
            outputToken.updateLastModified(vch.getChangeTime());
            VFSContainerEntity container = vch.getContainer();
            boolean replaced = Util.wasReplacedLater(vc, vch, container);
            if ( !replaced ) {
                RemovedDAVNode node = new RemovedDAVNode(
                    dt.adapt(vc, container, g.getPermissions(), true, layout, g.getId()),
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


    /**
     * {@inheritDoc}
     * 
     * @throws FileshareException
     *
     * @see eu.agno3.fileshare.webdav.internal.SubtreeProvider#contributePath(eu.agno3.fileshare.webdav.internal.FileshareDAVTreeProviderInternal,
     *      eu.agno3.runtime.webdav.server.DAVTreeNode, eu.agno3.fileshare.webdav.internal.DAVLayout,
     *      java.lang.StringBuilder)
     */
    @Override
    public boolean contributePath ( FileshareDAVTreeProviderInternal fd, DAVTreeNode<EntityKey> node, DAVLayout l, StringBuilder path )
            throws FileshareException {
        if ( node instanceof SharingSubjectDAVNode ) {
            path.append(OthersRootDAVNode.OTHERS_PATH).append('/');
            path.append(PathEscapeUtil.escapePathSegment(node.getPathName()));
            return true;
        }

        if ( node instanceof EntityDAVNode ) {
            EntityDAVNode en = (EntityDAVNode) node;
            if ( en.getGrantId() == null ) {
                return false;
            }

            path.append(OthersRootDAVNode.OTHERS_PATH).append('/');
            if ( en.getLayout() == DAVLayout.NATIVE ) {
                path.append(en.getGrantId());
                if ( en.getEntity() == null ) {
                    return true;
                }

                Grant grant = fd.getContext().getShareService().getGrant(en.getGrantId());
                if ( grant == null ) {
                    throw new EntityNotFoundException();
                }
                try ( VFSContext vc = fd.getVfs().getVFS(grant.getEntity().getEntityKey()).begin(true) ) {

                    VFSEntity ve = vc.load(grant.getEntity().getEntityKey());
                    VFSEntity te = vc.load(en.getEntity().getEntityKey());
                    List<String> rootPath = fd.getEntityService().getFullPath(vc, ve, true);
                    List<String> fullPath = fd.getEntityService().getFullPath(vc, te, true);
                    for ( int i = rootPath.size(); i < fullPath.size(); i++ ) {
                        path.append('/').append(PathEscapeUtil.escapePathSegment(fullPath.get(i)));
                    }
                    if ( log.isDebugEnabled() ) {
                        log.debug("Produced " + path); //$NON-NLS-1$
                    }
                    return true;
                }
            }

            Grant grant = fd.getContext().getShareService().getGrant(en.getGrantId());
            if ( grant == null ) {
                throw new EntityNotFoundException();
            }
            path.append(PathEscapeUtil.escapePathSegment(SharingSubjectDAVNode.getSubjectName(grant.getEntity().getOwner())));
            path.append('/');
            path.append(makeGrantRootName(grant, en, l));

            if ( !grant.isCollection() ) {
                // no more path to add
                return true;
            }

            try ( VFSContext vc = fd.getVfs().getVFS(en.getEntity().getEntityKey()).begin(true) ) {
                boolean stripFirst = true;
                List<String> fullPath = fd.getEntityService().getFullPath(vc, en.getEntity(), true);
                for ( String elem : fullPath ) {
                    if ( stripFirst ) {
                        stripFirst = false;
                        continue;
                    }
                    path.append('/').append(PathEscapeUtil.escapePathSegment(elem));
                }
                return true;
            }
        }
        return false;
    }


    private static String makeGrantRootName ( Grant grant, EntityDAVNode en, DAVLayout layout ) {
        if ( layout == DAVLayout.NATIVE ) {
            return grant.getId().toString();
        }
        String gname = grant.getDisplayName();
        String prefix;
        String suffix;
        int suffixSep = gname.indexOf('.', 1); // skip a dot at the first position
        if ( grant.isCollection() || suffixSep < 0 ) {
            // does not have a file extension, use whole name as prefix
            prefix = gname;
            suffix = StringUtils.EMPTY;
        }
        else {
            // have file extension, use name as prefix, extension as suffix
            prefix = gname.substring(0, suffixSep);
            suffix = gname.substring(suffixSep);
        }
        return prefix + "+" + grant.getId() + suffix; //$NON-NLS-1$
    }


    private static Grant findGrantFromName ( FileshareDAVTreeProviderInternal dt, SubjectQueryResult sqi, String rootName )
            throws FileshareException {

        if ( rootName.length() == 36 ) {
            // this extactly fits the UUID for native mode, everything else would be longer
            return dt.getContext().getShareService().getGrant(UUID.fromString(rootName));
        }

        int extsep = rootName.indexOf('.', 1);
        String id = null;
        if ( extsep > 0 ) {
            // if we build the name using the extension suffix, this denotes the end of ID
            // start is the last plus before it
            int idSep = rootName.lastIndexOf('+', extsep);
            id = rootName.substring(idSep + 1, extsep);
        }
        else {
            // if we don't have one id, no suffix, everything after the last plus is the ID
            int idSep = rootName.lastIndexOf('+');

            if ( idSep < 0 ) {
                // legacy: try fetching by name
                if ( log.isDebugEnabled() ) {
                    log.debug("Trying to fetch grant by name " + rootName); //$NON-NLS-1$
                }
                return dt.getBrowseService().getGrantWithName(sqi.getId(), rootName);
            }
            id = rootName.substring(idSep + 1);
        }

        if ( id != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Trying to fetch grant by id " + id); //$NON-NLS-1$
            }
            UUID uuid;
            try {
                uuid = UUID.fromString(id);
            }
            catch ( IllegalArgumentException e ) {
                log.debug("Failed to parse ID", e); //$NON-NLS-1$
                return null;
            }
            return dt.getContext().getShareService().getGrant(uuid);
        }
        return null;
    }


    @Override
    public DAVTreeNode<EntityKey> resolve ( FileshareDAVTreeProviderInternal dt, String repositoryPath, DAVLayout layout )
            throws FileshareException, DavException {
        if ( OthersRootDAVNode.OTHERS_PATH.equals(repositoryPath) ) {
            return new OthersRootDAVNode(
                Util.getRootId(dt, layout),
                layout == DAVLayout.OWNCLOUD ? getSharesRecursiveLastModified(dt) : getSharesLastModified(dt),
                layout);
        }

        int nextSep = repositoryPath.indexOf('/', OthersRootDAVNode.OTHERS_PATH.length() + 1);
        String identifier;
        String relativePath;
        if ( nextSep < 0 ) {
            identifier = PathEscapeUtil.unescapePathSegment(repositoryPath.substring(OthersRootDAVNode.OTHERS_PATH.length() + 1));
            relativePath = null;
        }
        else {
            identifier = PathEscapeUtil.unescapePathSegment(repositoryPath.substring(OthersRootDAVNode.OTHERS_PATH.length() + 1, nextSep));
            relativePath = repositoryPath.substring(nextSep + 1);
        }

        if ( layout == DAVLayout.NATIVE ) {
            Grant g = dt.getContext().getShareService().getGrant(UUID.fromString(identifier));
            DAVTreeNode<EntityKey> e = getForGrant(dt, layout, relativePath, g);
            if ( e instanceof EntityDAVNode ) {
                ( (EntityDAVNode) e ).setOverridePath(g.getId().toString());
            }
            return e;
        }

        SubjectQueryResult sqi = getSubjectFromName(dt, identifier);
        if ( sqi == null ) {
            return null;
        }

        if ( relativePath == null ) {
            return new SharingSubjectDAVNode(
                sqi,
                layout == DAVLayout.OWNCLOUD ? getSubjectSharesRecursiveLastModified(dt, sqi) : getSubjectSharesLastModified(dt, sqi),
                layout);
        }

        return getResourceFromShares(dt, relativePath, sqi, layout);
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
     * @param subjectName
     * @return
     * @throws FileshareException
     */
    private static SubjectQueryResult getSubjectFromName ( FileshareDAVTreeProviderInternal dt, String subjectName ) throws FileshareException {
        int realmSep = subjectName.lastIndexOf('@');
        SubjectQueryResult sqi;
        if ( realmSep < 0 ) {
            sqi = dt.getContext().getGroupService().getGroupInfo(subjectName);
        }
        else {
            String userName = subjectName.substring(0, realmSep);
            String realmName = subjectName.substring(realmSep + 1);
            UserPrincipal principal = new UserPrincipal(realmName, null, userName);

            if ( log.isDebugEnabled() ) {
                log.debug("Principal " + principal); //$NON-NLS-1$
            }

            sqi = dt.getContext().getSubjectService().getUserInfo(principal);
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Subject " + sqi); //$NON-NLS-1$
        }

        return sqi;
    }


    /**
     * @param sharePath
     * @param sqi
     * @return
     * @throws FileshareException
     * @throws DavException
     */
    DAVTreeNode<EntityKey> getResourceFromShares ( FileshareDAVTreeProviderInternal dt, String relativePath, SubjectQueryResult sqi,
            DAVLayout layout ) throws FileshareException, DavException {
        String sharePath;
        int rootSep = relativePath.indexOf('/');
        String rootName;
        if ( rootSep < 0 ) {
            rootName = relativePath;
            sharePath = null;
        }
        else {
            rootName = relativePath.substring(0, rootSep);
            sharePath = relativePath.substring(rootSep + 1);
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Root name " + rootName); //$NON-NLS-1$
            log.debug("Share path " + sharePath); //$NON-NLS-1$
        }

        return getForGrant(dt, layout, sharePath, findGrantFromName(dt, sqi, rootName));
    }


    /**
     * @param dt
     * @param layout
     * @param relativePath
     * @param grant
     * @return
     * @throws FileshareException
     * @throws DavException
     */
    DAVTreeNode<EntityKey> getForGrant ( FileshareDAVTreeProviderInternal dt, DAVLayout layout, String relativePath, Grant grant )
            throws FileshareException, DavException {
        if ( grant == null ) {
            log.debug("Grant not found"); //$NON-NLS-1$
            return null;
        }

        try ( VFSContext v = dt.getVfs().getNative().begin(true) ) {
            DAVTreeNode<EntityKey> r = null;
            if ( relativePath != null && grant.getEntity() instanceof ContainerEntity ) {
                r = dt.resolveRelative(v, (VFSContainerEntity) grant.getEntity(), relativePath, grant.getPermissions(), true, layout, grant.getId());
            }
            else if ( relativePath == null ) {
                r = dt.adapt(
                    v.load(grant.getEntity().getEntityKey()),
                    grant.getPermissions(),
                    true,
                    layout,
                    grant.getId(),
                    new NativeEntityKey(OthersRootDAVNode.OTHERS_ID));
            }

            if ( r instanceof EntityDAVNode ) {
                ( (EntityDAVNode) r ).setGrantId(grant.getId());
                ( (EntityDAVNode) r ).setPermissions(grant.getPermissions());
            }

            return r;
        }
    }


    /**
     * @param sqi
     * @return
     */
    static DateTime getSubjectSharesLastModified ( FileshareDAVTreeProviderInternal dt, SubjectInfo sqi ) {
        return dt.getBrowseService().getSubjectSharesLastModified(sqi.getId());
    }


    /**
     * @param sqi
     * @return
     */
    static DateTime getSubjectSharesRecursiveLastModified ( FileshareDAVTreeProviderInternal dt, SubjectInfo sqi ) {
        return dt.getBrowseService().getSubjectSharesRecursiveLastModified(sqi.getId());
    }


    /**
     * @return
     */
    static DateTime getSharesRecursiveLastModified ( FileshareDAVTreeProviderInternal dt ) {
        return dt.getBrowseService().getSharesRecursiveLastModified();
    }


    /**
     * @return
     */
    static DateTime getSharesLastModified ( FileshareDAVTreeProviderInternal dt ) {
        return dt.getBrowseService().getSharesLastModified();
    }

}
