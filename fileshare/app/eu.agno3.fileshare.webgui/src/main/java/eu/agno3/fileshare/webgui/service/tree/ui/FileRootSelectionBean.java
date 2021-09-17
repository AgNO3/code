/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.tree.ui;


import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.model.TreeNode;

import eu.agno3.fileshare.exceptions.AccessDeniedException;
import eu.agno3.fileshare.exceptions.AuthenticationException;
import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.SubjectInfo;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.query.PeerInfo;
import eu.agno3.fileshare.model.query.SubjectQueryResult;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.prefs.UserPreferencesBean;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.file.EntityGrantInfo;
import eu.agno3.fileshare.webgui.service.share.ShareListBean;
import eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode;
import eu.agno3.fileshare.webgui.service.tree.AllSharedTreeNode;
import eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode;
import eu.agno3.fileshare.webgui.service.tree.EntityTreeNode;
import eu.agno3.fileshare.webgui.service.tree.ErrorEntityTreeNode;
import eu.agno3.fileshare.webgui.service.tree.FavoritesTreeNode;
import eu.agno3.fileshare.webgui.service.tree.FileTreeConstants;
import eu.agno3.fileshare.webgui.service.tree.GroupTreeNode;
import eu.agno3.fileshare.webgui.service.tree.PeerSharesTreeNode;
import eu.agno3.fileshare.webgui.service.tree.PeersTreeNode;
import eu.agno3.fileshare.webgui.service.tree.RootTreeNode;
import eu.agno3.fileshare.webgui.service.tree.SearchResultTreeNode;
import eu.agno3.fileshare.webgui.service.tree.ShareRootEntityTreeNode;
import eu.agno3.fileshare.webgui.service.tree.ShareSubjectTreeNode;
import eu.agno3.fileshare.webgui.service.tree.SharedTreeNode;
import eu.agno3.fileshare.webgui.subject.CurrentUserBean;
import eu.agno3.fileshare.webgui.subject.PeerUtil;
import eu.agno3.fileshare.webgui.users.CurrentUserMembershipBean;


/**
 * @author mbechler
 *
 */
@Named ( "fileRootSelectionBean" )
@ViewScoped
public class FileRootSelectionBean implements Serializable, EntityGrantInfo {

    /**
     * 
     */
    private static final long serialVersionUID = 5115792050508144472L;

    private static final Logger log = Logger.getLogger(FileRootSelectionBean.class);

    private static final Set<String> READ_ONLY_TYPES = new HashSet<>(
        Arrays.asList(
            FileTreeConstants.GROUPS_ROOT_TYPE,
            FileTreeConstants.PEERS_ROOT_TYPE,
            FileTreeConstants.SHARED_ROOT_TYPE,
            FileTreeConstants.FAVORITES_ROOT_TYPE,
            FileTreeConstants.PEER_ROOT_TYPE,
            FileTreeConstants.ALL_SHARED_ROOT,
            FileTreeConstants.ROOT_TYPE,
            FileTreeConstants.SEARCH_RESULT_TYPE,
            FileTreeConstants.SHARE_SUBJECT_ROOT_TYPE));

    @Inject
    private FileshareServiceProvider fsp;

    @Inject
    private BrowseTreeProvider treeProvider;

    @Inject
    private UserPreferencesBean userPrefs;

    @Inject
    private CurrentUserBean currentUser;

    @Inject
    private CurrentUserMembershipBean groupNembership;

    @Inject
    private PeerUtil peerUtil;

    @Inject
    private ShareListBean shareList;

    private EntityKey selectedRootId;
    private String selectedType;
    private String selectedRootType;
    private UUID selectedRootGrantId;

    private Grant cachedGrant;
    private VFSContainerEntity userRootCache;
    private VFSContainerEntity rootCache;
    private SubjectQueryResult subjectCache;

    private AbstractBrowseTreeNode cachedRoot;

    private boolean userRootLoaded;

    private VFSContainerEntity cachedGroupRoot;

    private String selectedPeer;

    private PeerInfo peerInfo;

    private boolean firstGrantsLoaded;
    private List<Grant> firstGrants;
    private boolean numGrantsLoaded;
    private int numGrants;

    private String query;
    private int offset;


    /**
     * @return the selectedRootId
     */
    public EntityKey getSelectedRootId () {
        return this.selectedRootId;
    }


    /**
     * @param selectedRootId
     *            the selectedRootId to set
     */
    public void setSelectedRootId ( EntityKey selectedRootId ) {
        this.selectedRootId = selectedRootId;
    }


    /**
     * 
     * @return the encoded root id
     */
    public String getSelectedRootIdEncoded () {
        if ( this.selectedRootId != null ) {
            return this.selectedRootId.toString();
        }
        return null;
    }


    /**
     * 
     * @param selectedRootId
     */
    public void setSelectedRootIdEncoded ( String selectedRootId ) {
        this.selectedRootId = this.fsp.getEntityService().parseEntityKey(selectedRootId);
    }


    /**
     * @return the selectedType
     */
    public String getSelectedType () {
        if ( this.selectedType == null && ( this.isSingleLevel() && this.getUserRoot() != null ) ) {
            return FileTreeConstants.USER_ROOT_TYPE;
        }
        else if ( this.selectedType == null && this.isSingleLevel() ) {
            return FileTreeConstants.ROOT_TYPE;
        }
        return this.selectedType;
    }


    /**
     * @param selectedType
     *            the selectedType to set
     */
    public void setSelectedType ( String selectedType ) {
        this.selectedType = selectedType;
    }


    /**
     * @return the selectedRootType
     */
    public String getSelectedRootType () {
        if ( StringUtils.isBlank(this.selectedRootType) ) {
            return this.selectedType;
        }
        return this.selectedRootType;
    }


    /**
     * @param selectedRootType
     *            the selectedRootType to set
     */
    public void setSelectedRootType ( String selectedRootType ) {
        this.selectedRootType = selectedRootType;
    }


    /**
     * @return the selectedRootGrant
     */
    public UUID getSelectedRootGrantId () {
        return this.selectedRootGrantId;
    }


    /**
     * @param selectedRootGrantId
     *            the selectedRootGrant to set
     */
    public void setSelectedRootGrantId ( UUID selectedRootGrantId ) {
        this.selectedRootGrantId = selectedRootGrantId;
    }


    /**
     * @return the singleLevel
     */
    public boolean isSingleLevel () {
        return !this.userPrefs.getTreeFileView();
    }


    /**
     * @return the selectedPeer
     */
    public String getSelectedPeer () {
        return this.selectedPeer;
    }


    /**
     * @param selectedPeer
     *            the selectedPeer to set
     */
    public void setSelectedPeer ( String selectedPeer ) {
        this.selectedPeer = selectedPeer;
    }


    /**
     * @return the query
     */
    public String getQuery () {
        return this.query;
    }


    /**
     * @param query
     *            the query to set
     */
    public void setQuery ( String query ) {
        try {
            this.query = URLDecoder.decode(query, "UTF-8");//$NON-NLS-1$
        }
        catch ( UnsupportedEncodingException e ) {
            log.warn("Failed to decode query", e); //$NON-NLS-1$
            this.query = query;
        }
    }


    /**
     * @return the offset
     */
    public int getOffset () {
        if ( !this.fsp.getConfigurationProvider().getSearchConfiguration().isAllowPaging() ) {
            return 0;
        }
        return this.offset;
    }


    /**
     * @param offset
     *            the offset to set
     */
    public void setOffset ( int offset ) {
        this.offset = offset;
    }


    /**
     * @return the selected root tree node
     */
    public AbstractBrowseTreeNode getRoot () {

        if ( this.cachedRoot != null ) {
            return this.cachedRoot;
        }

        if ( log.isTraceEnabled() ) {
            log.trace(String.format(
                "Selected root is %s, type %s, grant %s, perms %s", //$NON-NLS-1$
                this.selectedRootId,
                this.selectedType,
                this.selectedRootGrantId,
                this.getRootPermissionsString()));
        }

        AbstractBrowseTreeNode r;
        try {
            r = getRootInternal();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            log.warn("Failed to get tree root", e); //$NON-NLS-1$
            ExceptionHandler.handleException(e);
            return null;
        }

        if ( r == null ) {
            return null;
        }

        synchronized ( r ) {
            r.expandStatic();
        }

        this.cachedRoot = r;
        return r;
    }


    /**
     * 
     * @return the permissions for the selected (top-level) root
     */
    public Set<GrantPermission> getRootPermissions () {
        if ( READ_ONLY_TYPES.contains(this.selectedType) ) {
            return EnumSet.of(GrantPermission.READ);
        }

        VFSContainerEntity root = this.getSelectedRootContainer();
        if ( root != null && root.isStaticReadOnly() ) {
            return EnumSet.of(GrantPermission.READ);
        }

        if ( this.getSelectedRootGrantId() != null ) {
            if ( this.getRootGrant() == null ) {
                log.warn("No root grant found"); //$NON-NLS-1$
                return EnumSet.noneOf(GrantPermission.class);
            }
            return this.getRootGrant().getPermissions();
        }

        String type = this.getSelectedType();

        if ( type == null ) {
            type = this.getSelectedRootType();
        }

        if ( FileTreeConstants.SHARE_ROOT_TYPE.equals(type) ) {
            Grant g = this.getRootGrant();
            if ( g == null ) {
                return EnumSet.noneOf(GrantPermission.class);
            }
            return g.getPermissions();
        }
        else if ( READ_ONLY_TYPES.contains(type) ) {
            return EnumSet.of(GrantPermission.READ);
        }

        if ( !this.currentUser.isAuthenticated() && this.getSelectedRootGrantId() == null ) {
            return EnumSet.noneOf(GrantPermission.class);
        }

        return EnumSet.allOf(GrantPermission.class);
    }


    /**
     * 
     * @return a string spec of the root permissions
     */
    public String getRootPermissionsString () {
        return StringUtils.join(getRootPermissions(), ',');
    }


    /**
     * 
     * @param perm
     * @return whether the root permission is present
     */
    public boolean haveRootPermission ( String perm ) {
        return this.getRootPermissions().contains(GrantPermission.valueOf(perm));
    }


    private AbstractBrowseTreeNode getRootInternal () throws EntityNotFoundException, AuthenticationException {

        if ( StringUtils.isBlank(this.selectedType) ) {
            return getDefaultRoot();
        }

        switch ( this.selectedType ) {

        case FileTreeConstants.DIR_TYPE:
            return getDirectoryRoot();
        case FileTreeConstants.GROUPS_ROOT_TYPE:
            return getGroupsRoot();
        case FileTreeConstants.GROUP_ROOT_TYPE:
            return getGroupRoot();
        case FileTreeConstants.SHARED_ROOT_TYPE:
            return getSharedRoot();
        case FileTreeConstants.SHARE_SUBJECT_ROOT_TYPE:
            return getSubjectSharesRoot();
        case FileTreeConstants.SHARE_ROOT_TYPE:
            return getShareRoot();
        case FileTreeConstants.FILE_TYPE:
        case FileTreeConstants.SHARE_ROOT_FILE_TYPE:
            // file types cannot be roots
            return null;
        case FileTreeConstants.USER_ROOT_TYPE:
            return getUserRootNode();
        case FileTreeConstants.FAVORITES_ROOT_TYPE:
            return getFavoritesRootNode();
        case FileTreeConstants.PEERS_ROOT_TYPE:
            return getPeersRootNode();
        case FileTreeConstants.ALL_SHARED_ROOT:
            return getAllSharedRootNode();
        case FileTreeConstants.PEER_ROOT_TYPE:
            return getPeerRoot();
        case FileTreeConstants.SEARCH_RESULT_TYPE:
            return getSearchResultRoot();
        default:
            return getDefaultRoot();
        }

    }


    /**
     * @param bn
     */
    public void updateRoot ( BrowseTreeNode bn ) {
        if ( bn == null || bn.getType() == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Node is NULL " + bn); //$NON-NLS-1$
            }
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Updating root with type " + bn.getType()); //$NON-NLS-1$
        }

        switch ( bn.getType() ) {

        case FileTreeConstants.DIR_TYPE:
            updateDirectoryRoot((EntityTreeNode) bn);
            return;
        case FileTreeConstants.SHARE_SUBJECT_ROOT_TYPE:
            updateSubjectSharesRoot((ShareSubjectTreeNode) bn);
            return;
        case FileTreeConstants.GROUPS_ROOT_TYPE:
            updateGroupsRoot((GroupTreeNode) bn);
            return;
        case FileTreeConstants.SHARE_ROOT_TYPE:
            updateShareRoot((ShareRootEntityTreeNode) bn);
            return;
        case FileTreeConstants.USER_ROOT_TYPE:
            updateUserRoot((AbstractBrowseTreeNode) bn);
            return;
        case FileTreeConstants.ROOT_TYPE:
            updateDefaultRoot((RootTreeNode) bn);
            return;
        case FileTreeConstants.FAVORITES_ROOT_TYPE:
            updateFavoritesRoot((FavoritesTreeNode) bn);
            return;
        case FileTreeConstants.PEERS_ROOT_TYPE:
            updatePeersRoot((PeersTreeNode) bn);
            return;
        case FileTreeConstants.ALL_SHARED_ROOT:
            updateAllSharedRoot((AllSharedTreeNode) bn);
            return;
        case FileTreeConstants.PEER_ROOT_TYPE:
            updatePeerRoot((PeerSharesTreeNode) bn);
            return;
        case FileTreeConstants.SEARCH_RESULT_TYPE:
            updateSearchResultRoot((SearchResultTreeNode) bn);
            return;
        }

    }


    /**
     * @return
     * @throws UnsupportedEncodingException
     */
    private AbstractBrowseTreeNode getSearchResultRoot () {
        SearchResultTreeNode res = new SearchResultTreeNode(this.query, this.treeProvider);
        res.setOffset(this.getOffset());
        res.setLimit(this.fsp.getConfigurationProvider().getSearchConfiguration().getPageSize());
        res.setPagingAllowed(this.fsp.getConfigurationProvider().getSearchConfiguration().isAllowPaging());
        return res;
    }


    /**
     * @param bn
     */
    protected void updateSearchResultRoot ( SearchResultTreeNode bn ) {
        // do nothing
    }


    /**
     * @return
     */
    private AbstractBrowseTreeNode getPeerRoot () {
        return new PeerSharesTreeNode(getPeerInfo(), this.treeProvider);
    }


    /**
     * @param bn
     */
    protected void updatePeerRoot ( PeerSharesTreeNode bn ) {
        bn.loadChildren();
    }


    /**
     * @return
     */
    private AbstractBrowseTreeNode getAllSharedRootNode () {
        return new AllSharedTreeNode(this.treeProvider);
    }


    /**
     * @param bn
     */
    protected void updateAllSharedRoot ( AllSharedTreeNode bn ) {
        bn.loadChildren();
    }


    /**
     * @return
     */
    private AbstractBrowseTreeNode getPeersRootNode () {
        return new PeersTreeNode(this.treeProvider);
    }


    /**
     * @param bn
     */
    protected void updatePeersRoot ( PeersTreeNode bn ) {
        bn.loadChildren();
    }


    /**
     * @return
     */
    private FavoritesTreeNode getFavoritesRootNode () {
        return new FavoritesTreeNode(this.treeProvider);
    }


    /**
     * @param bn
     */
    protected void updateFavoritesRoot ( FavoritesTreeNode bn ) {
        bn.loadChildren();
    }


    /**
     * @param bn
     */
    protected void updateGroupsRoot ( GroupTreeNode bn ) {
        this.groupNembership.refresh();
        bn.loadChildren();
    }


    /**
     * @return
     * @throws EntityNotFoundException
     */
    private EntityTreeNode getGroupRoot () throws EntityNotFoundException {
        EntityTreeNode r = getDirectoryRoot();
        r.setType(FileTreeConstants.GROUP_ROOT_TYPE);
        return r;
    }


    /**
     * @return
     * @throws EntityNotFoundException
     */
    private EntityTreeNode getDirectoryRoot () throws EntityNotFoundException {
        return this.treeProvider
                .getEntityAdapter(null, this.getRootContainer(), this.getRootGrant(), this.getSelectedRootType(), this.getRootPermissions());
    }


    private void updateDirectoryRoot ( EntityTreeNode en ) {
        en.setGrant(this.getRootGrant());
        en.updateAttachedObject(this.getRootContainer());
        en.setOverridePerms(getRootPermissions());
    }


    /**
     * @return
     */
    private GroupTreeNode getGroupsRoot () {
        return new GroupTreeNode(null, this.treeProvider);
    }


    /**
     * @return
     */
    private SharedTreeNode getSharedRoot () {
        return new SharedTreeNode(null, this.treeProvider);
    }


    /**
     * @return
     */
    private ShareSubjectTreeNode getSubjectSharesRoot () {
        return new ShareSubjectTreeNode(null, getSelectedSubjectInfo(), this.treeProvider);
    }


    private void updateSubjectSharesRoot ( ShareSubjectTreeNode ss ) {
        ss.setSubject(getSelectedSubjectInfo());
    }


    /**
     * @return
     */
    private EntityTreeNode getShareRoot () {
        Grant rootGrant = this.getRootGrant();

        if ( rootGrant == null ) {
            log.warn("Grant is null"); //$NON-NLS-1$
            return new ErrorEntityTreeNode(null, this.treeProvider);
        }

        ShareRootEntityTreeNode r = new ShareRootEntityTreeNode(rootGrant, null, this.treeProvider);
        r.setType(FileTreeConstants.SHARE_ROOT_TYPE);

        return r;
    }


    private void updateShareRoot ( ShareRootEntityTreeNode sh ) {
        sh.setGrant(this.getRootGrant());
    }


    /**
     * @return
     * @throws EntityNotFoundException
     */
    private EntityTreeNode getUserRootNode () throws EntityNotFoundException {
        EntityTreeNode userRootEntityNode = this.treeProvider.getEntityAdapter(null, this.getUserRoot());
        userRootEntityNode.setType(FileTreeConstants.USER_ROOT_TYPE);
        userRootEntityNode.setOverridePerms(EnumSet.allOf(GrantPermission.class));
        return userRootEntityNode;
    }


    private void updateUserRoot ( AbstractBrowseTreeNode bn ) {
        bn.updateAttachedObject(this.getUserRoot());
        bn.loadChildren();
    }


    /**
     * @return
     * @throws EntityNotFoundException
     * @throws AuthenticationException
     */
    private AbstractBrowseTreeNode getDefaultRoot () throws EntityNotFoundException, AuthenticationException {

        if ( this.isSingleLevel() ) {
            if ( this.getUserRoot() != null ) {
                return getUserRootNode();
            }
        }

        if ( !this.currentUser.isAuthenticated() && this.getSelectedRootGrantId() == null ) {
            throw new AuthenticationException();
        }

        return makeRootNode(this.treeProvider, this.getUserRoot(), true, this.groupNembership.haveAnyGroups());
    }


    /**
     * @param factory
     * @param userRoot
     * @param addShared
     * @param addGroups
     * @return a virtual root node
     * @throws EntityNotFoundException
     */
    public static AbstractBrowseTreeNode makeRootNode ( BrowseTreeProvider factory, VFSContainerEntity userRoot, boolean addShared,
            boolean addGroups ) throws EntityNotFoundException {
        RootTreeNode r = new RootTreeNode();
        GroupTreeNode groupTreeNode = new GroupTreeNode(r, factory);
        SharedTreeNode sharedTreeNode = new SharedTreeNode(r, factory);

        List<AbstractBrowseTreeNode> children = new ArrayList<>();

        if ( userRoot != null ) {
            EntityTreeNode userRootEntityNode = factory.getEntityAdapter(r, userRoot);
            userRootEntityNode.setType(FileTreeConstants.USER_ROOT_TYPE);
            userRootEntityNode.setOverridePerms(EnumSet.allOf(GrantPermission.class));
            children.add(userRootEntityNode);
        }

        if ( addGroups ) {
            children.add(groupTreeNode);
        }

        if ( addShared ) {
            children.add(sharedTreeNode);
        }
        r.getChildren().addAll(children);

        return r;
    }


    private void updateDefaultRoot ( RootTreeNode node ) {
        for ( TreeNode n : node.getChildren() ) {
            if ( FileTreeConstants.USER_ROOT_TYPE.equals(n.getType()) ) {
                AbstractBrowseTreeNode bn = (AbstractBrowseTreeNode) n;
                bn.updateAttachedObject(this.getUserRoot());
            }
        }
    }


    /**
     * 
     * @return the root node type
     */
    public String getRootType () {
        BrowseTreeNode n = getRoot();

        if ( n != null ) {
            return n.getType();
        }
        return null;
    }


    /**
     * 
     */
    @Override
    public void refresh () {
        this.userRootCache = null;
        this.userRootLoaded = false;
        this.rootCache = null;
        this.subjectCache = null;
        this.cachedGrant = null;
        this.firstGrants = Collections.EMPTY_LIST;
        this.firstGrantsLoaded = false;
        this.numGrants = 0;
        this.numGrantsLoaded = false;
    }


    /**
     * @return the root entity
     */
    public VFSContainerEntity getUserRoot () {
        if ( !this.userRootLoaded ) {
            try {
                this.userRootLoaded = true;
                this.userRootCache = this.fsp.getBrowseService().getUserRoot();
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                ExceptionHandler.handleException(e);
            }
        }
        return this.userRootCache;
    }


    /**
     * @return the selected root container
     */
    public VFSContainerEntity getSelectedRootContainer () {
        if ( this.selectedRootId == null ) {
            return null;
        }

        if ( this.rootCache == null ) {
            try {
                this.rootCache = this.fsp.getDirectoryService().getById(this.selectedRootId);
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                ExceptionHandler.handleException(e);
                return null;
            }
        }
        return this.rootCache;
    }


    /**
     * @return the subject info for the selected subject share root
     */
    public SubjectInfo getSelectedSubjectInfo () {
        if ( this.selectedRootId == null ) {
            return null;
        }

        if ( this.subjectCache == null ) {
            try {
                this.subjectCache = this.fsp.getBrowseService().getSharingSubject(this.selectedRootId);
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                ExceptionHandler.handleException(e);
                return null;
            }
        }

        return this.subjectCache;
    }


    /**
     * @return the root container, if any
     */
    public VFSContainerEntity getRootContainer () {
        if ( StringUtils.isBlank(this.selectedType) ) {
            return getUserRoot();
        }

        switch ( this.selectedType ) {
        case FileTreeConstants.SHARE_ROOT_TYPE:
            return getShareRootContainer();

        case FileTreeConstants.DIR_TYPE:
        case FileTreeConstants.GROUP_ROOT_TYPE:
            return getSelectedRootContainer();

        case FileTreeConstants.USER_ROOT_TYPE:
            return getUserRoot();

        }

        return null;
    }


    /**
     * @return the root entities subject root
     */
    public VFSContainerEntity getSubjectRootContainer () {
        VFSContainerEntity e = getRootContainer();
        if ( e == null ) {
            return null;
        }

        if ( e.getOwner() instanceof User && e.getOwner().equals(this.currentUser.getCurrentUser()) ) {
            return this.getUserRoot();
        }
        else if ( e.getOwner() instanceof Group && this.currentUser.isMemberOf((Group) e.getOwner()) ) {
            if ( this.cachedGroupRoot == null ) {
                try {
                    this.cachedGroupRoot = this.fsp.getBrowseService().getOrCreateGroupRoot(e.getOwner().getId());
                }
                catch (
                    FileshareException |
                    UndeclaredThrowableException ex ) {
                    ExceptionHandler.handleException(ex);
                    return null;
                }
            }

            return this.cachedGroupRoot;
        }

        return null;
    }


    /**
     * @return
     */
    private VFSContainerEntity getShareRootContainer () {
        Grant g = getRootGrant();

        if ( g != null && g.getEntity() instanceof VFSContainerEntity ) {
            return (VFSContainerEntity) g.getEntity();
        }
        return null;
    }


    /**
     * @return the selected grant, if any
     */
    public Grant getRootGrant () {

        if ( this.selectedRootGrantId == null ) {
            return null;
        }

        if ( this.cachedGrant == null ) {
            try {
                this.cachedGrant = this.fsp.getShareService().getGrant(this.selectedRootGrantId);
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                if ( ExceptionHandler.unwrapException(e) instanceof AccessDeniedException ) {
                    this.selectedRootGrantId = null;
                }
                ExceptionHandler.handleException(e);
                return null;
            }
        }

        return this.cachedGrant;
    }


    /**
     * 
     * @return the selected peer info
     */
    public PeerInfo getPeerInfo () {
        if ( this.peerInfo == null ) {

            try {
                this.peerInfo = this.peerUtil.decodePeerInfo(this.getSelectedPeer());
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                ExceptionHandler.handleException(e);
                return null;
            }
        }
        return this.peerInfo;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.file.EntityGrantInfo#getFirstGrants()
     */
    @Override
    public List<Grant> getFirstGrants () {
        if ( !this.firstGrantsLoaded ) {
            this.firstGrantsLoaded = true;
            this.firstGrants = this.shareList.getFirstShares(this.getRootContainer());
        }
        return this.firstGrants;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.file.EntityGrantInfo#getNumGrants()
     */
    @Override
    public int getNumGrants () {
        if ( !this.numGrantsLoaded ) {
            this.numGrantsLoaded = true;
            this.numGrants = this.shareList.getGrantCount(this.getRootContainer());
        }
        return this.numGrants;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.file.EntityGrantInfo#getGrantsExceedingLimit()
     */
    @Override
    public int getGrantsExceedingLimit () {
        return Math.max(0, getNumGrants() - this.shareList.getGrantLimit());
    }

}
