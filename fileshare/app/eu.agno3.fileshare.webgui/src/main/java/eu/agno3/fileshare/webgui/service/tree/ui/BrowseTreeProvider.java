/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.11.2013 by mbechler
 */
package eu.agno3.fileshare.webgui.service.tree.ui;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.model.TreeNode;

import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.SubjectGrant;
import eu.agno3.fileshare.model.SubjectInfo;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.query.CollectionResult;
import eu.agno3.fileshare.model.query.MailPeerInfo;
import eu.agno3.fileshare.model.query.PeerInfo;
import eu.agno3.fileshare.model.query.SearchResult;
import eu.agno3.fileshare.model.query.SubjectPeerInfo;
import eu.agno3.fileshare.model.query.SubjectQueryResult;
import eu.agno3.fileshare.model.query.TokenPeerInfo;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.policy.PolicyBean;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.file.FilePermissionBean;
import eu.agno3.fileshare.webgui.service.file.compare.ContainerGroupComparator;
import eu.agno3.fileshare.webgui.service.file.compare.SubjectGrantComparator;
import eu.agno3.fileshare.webgui.service.file.compare.VFSEntityComparator;
import eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode;
import eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode;
import eu.agno3.fileshare.webgui.service.tree.EntityTreeNode;
import eu.agno3.fileshare.webgui.service.tree.ErrorEntityTreeNode;
import eu.agno3.fileshare.webgui.service.tree.FavoritesTreeNode;
import eu.agno3.fileshare.webgui.service.tree.FileTreeConstants;
import eu.agno3.fileshare.webgui.service.tree.GroupTreeNode;
import eu.agno3.fileshare.webgui.service.tree.PeerSharedByTreeNode;
import eu.agno3.fileshare.webgui.service.tree.PeerSharedToTreeNode;
import eu.agno3.fileshare.webgui.service.tree.PeerSharesTreeNode;
import eu.agno3.fileshare.webgui.service.tree.PeersTreeNode;
import eu.agno3.fileshare.webgui.service.tree.SearchResultTreeNode;
import eu.agno3.fileshare.webgui.service.tree.ShareRootEntityTreeNode;
import eu.agno3.fileshare.webgui.service.tree.ShareSubjectTreeNode;
import eu.agno3.fileshare.webgui.service.tree.SharedTreeNode;
import eu.agno3.fileshare.webgui.service.tree.TreeFilterBean;
import eu.agno3.fileshare.webgui.subject.SubjectComparator;


/**
 * @author mbechler
 * 
 */
@ApplicationScoped
public class BrowseTreeProvider implements Serializable {

    private static final Logger log = Logger.getLogger(BrowseTreeProvider.class);

    /**
     * 
     */
    private static final long serialVersionUID = 8733468450625377874L;

    @Inject
    private FileshareServiceProvider fsp;

    @Inject
    private PolicyBean policy;

    @Inject
    private TreeFilterBean filterBean;

    @Inject
    private FilePermissionBean permBean;


    /**
     * @param parent
     * @param obj
     * @return A TreeNode adapter for the given object
     * @throws EntityNotFoundException
     */
    public EntityTreeNode getEntityAdapter ( BrowseTreeNode parent, VFSEntity obj ) throws EntityNotFoundException {
        if ( obj == null ) {
            return new ErrorEntityTreeNode(parent, this);
        }
        return new EntityTreeNode(obj.cloneShallow(), parent, this);
    }


    /**
     * @param parent
     * @param obj
     * @param grant
     * @param rootType
     * @param perms
     * @return A TreeNode adapter for the given object
     * @throws EntityNotFoundException
     */
    public EntityTreeNode getEntityAdapter ( BrowseTreeNode parent, VFSContainerEntity obj, Grant grant, String rootType, Set<GrantPermission> perms )
            throws EntityNotFoundException {
        EntityTreeNode n = getEntityAdapter(parent, obj);
        n.setGrant(grant);
        n.setOverrideRootType(rootType);
        n.setOverridePerms(perms);
        return n;
    }


    /**
     * @param parent
     * @param c
     * @return container entity children
     */
    public List<BrowseTreeNode> getEntityChildren ( BrowseTreeNode parent, VFSEntity c ) {
        List<BrowseTreeNode> adapters = new ArrayList<>();
        if ( ! ( c instanceof VFSContainerEntity ) ) {
            return adapters;
        }

        try {
            CollectionResult<VFSEntity> objs = this.fsp.getBrowseService().getChildren(c.getEntityKey());
            List<VFSEntity> sorted = new ArrayList<>(objs.getCollection());
            Collections.sort(sorted, new VFSEntityComparator());

            int hiddenFlagged = 0;
            int hiddenFilter = 0;
            for ( VFSEntity o : sorted ) {

                if ( this.filterBean.isHidden(o) ) {
                    hiddenFlagged++;
                    continue;
                }

                if ( this.filterBean.isFiltered(o) ) {
                    hiddenFilter++;
                    continue;
                }

                adapters.add(this.getEntityAdapter(parent, o));
            }

            if ( parent instanceof AbstractBrowseTreeNode ) {
                AbstractBrowseTreeNode bn = (AbstractBrowseTreeNode) parent;
                bn.setHiddenFlagged(hiddenFlagged);
                bn.setHiddenPolicy(objs.getNumHiddenPolicy());
                bn.setPolicyViolations(objs.getViolations());
                bn.setHiddenFilter(hiddenFilter);
            }
            else {
                log.debug("Not a browse tree node"); //$NON-NLS-1$
            }
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
        return adapters;
    }


    /**
     * @param parent
     * @return share roots
     */
    public List<BrowseTreeNode> getSharedChildren ( SharedTreeNode parent ) {
        List<BrowseTreeNode> adapters = new ArrayList<>();

        int hiddenPolicy = 0;
        int hiddenFlagged = 0;
        int hiddenFiltered = 0;
        try {
            List<SubjectQueryResult> objs = new ArrayList<>(this.fsp.getBrowseService().getSharingSubjects());
            Collections.sort(objs, new SubjectComparator());
            for ( SubjectQueryResult s : objs ) {
                if ( this.filterBean.isHidden(s) ) {
                    hiddenFlagged++;
                    continue;
                }

                if ( this.filterBean.isFiltered(s) ) {
                    hiddenFiltered++;
                    continue;
                }

                ShareSubjectTreeNode contentEntityAdapter = new ShareSubjectTreeNode(parent, s, this);
                contentEntityAdapter.setType(FileTreeConstants.SHARE_SUBJECT_ROOT_TYPE);
                adapters.add(contentEntityAdapter);
            }

            parent.setHiddenFlagged(hiddenFlagged);
            parent.setHiddenPolicy(hiddenPolicy);
            parent.setHiddenFilter(hiddenFiltered);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
        return adapters;
    }


    /**
     * @param parent
     * @return peer subjects
     */
    public List<BrowseTreeNode> getPeersChildren ( PeersTreeNode parent ) {
        List<BrowseTreeNode> adapters = new ArrayList<>();

        int hiddenPolicy = 0;
        int hiddenFlagged = 0;
        int hiddenFilter = 0;
        try {
            List<PeerInfo> objs = new ArrayList<>(this.fsp.getBrowseService().getPeers());
            Collections.sort(objs, new PeerComparator());
            for ( PeerInfo s : objs ) {

                if ( this.filterBean.isHidden(s) ) {
                    hiddenFlagged++;
                    continue;
                }

                if ( this.filterBean.isFiltered(s) ) {
                    hiddenFilter++;
                    continue;
                }

                PeerSharesTreeNode contentEntityAdapter = new PeerSharesTreeNode(parent, s, this);
                contentEntityAdapter.setType(FileTreeConstants.PEER_ROOT_TYPE);
                adapters.add(contentEntityAdapter);
            }
            parent.setHiddenFlagged(hiddenFlagged);
            parent.setHiddenPolicy(hiddenPolicy);
            parent.setHiddenFilter(hiddenFilter);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
        return adapters;
    }


    /**
     * @param parent
     * @return group roots
     */
    public List<BrowseTreeNode> getGroupChildren ( GroupTreeNode parent ) {
        List<BrowseTreeNode> adapters = new ArrayList<>();
        try {
            log.debug("Getting group children"); //$NON-NLS-1$
            CollectionResult<VFSContainerEntity> objs = this.fsp.getBrowseService().getVisibleGroupRoots();
            List<VFSContainerEntity> sorted = new ArrayList<>(objs.getCollection());
            Collections.sort(sorted, new ContainerGroupComparator());

            int hiddenFlagged = 0;
            int hiddenFilter = 0;
            for ( VFSContainerEntity o : sorted ) {

                if ( this.filterBean.isHiddenGroupMembership(o.getOwner()) ) {
                    hiddenFlagged++;
                    continue;
                }

                if ( this.filterBean.isFilteredGroupMembership(o.getOwner()) ) {
                    hiddenFilter++;
                    continue;
                }

                EntityTreeNode contentEntityAdapter = this.getEntityAdapter(parent, o);
                contentEntityAdapter.setType(FileTreeConstants.GROUP_ROOT_TYPE);
                adapters.add(contentEntityAdapter);
            }

            parent.setHiddenFlagged(hiddenFlagged);
            parent.setHiddenPolicy(objs.getNumHiddenPolicy());
            parent.setHiddenFilter(hiddenFilter);
            parent.setPolicyViolations(objs.getViolations());
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
        return adapters;
    }


    /**
     * @param parent
     * @return group roots
     */
    public List<BrowseTreeNode> getFavoritesChildren ( FavoritesTreeNode parent ) {
        List<BrowseTreeNode> adapters = new ArrayList<>();
        try {
            List<VFSEntity> objs = new ArrayList<>(this.fsp.getFlaggingService().getFavoriteEntities());
            Collections.sort(objs, new VFSEntityComparator());
            int hiddenFlagged = 0;
            int hiddenPolicy = 0;
            int hiddenFilter = 0;
            for ( VFSEntity o : objs ) {

                if ( !this.policy.isPolicyFulfilled(o.getSecurityLabel()) ) {
                    hiddenPolicy++;
                    continue;
                }

                if ( this.filterBean.isHidden(o) ) {
                    hiddenFlagged++;
                    continue;
                }

                if ( this.filterBean.isFiltered(o) ) {
                    hiddenFilter++;
                    continue;
                }

                EntityTreeNode contentEntityAdapter = getEntityAdapter(parent, o);
                Grant g = this.fsp.getEntityService().getGrant(o.getEntityKey());
                if ( g != null ) {
                    contentEntityAdapter.setGrant(g);
                    contentEntityAdapter.setOverridePerms(g.getPermissions());
                }
                else if ( this.permBean.isOwner(o) ) {
                    contentEntityAdapter.setOverridePerms(EnumSet.allOf(GrantPermission.class));
                }
                adapters.add(contentEntityAdapter);
            }

            parent.setHiddenFlagged(hiddenFlagged);
            parent.setHiddenPolicy(hiddenPolicy);
            parent.setHiddenFilter(hiddenFilter);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
        return adapters;
    }


    /**
     * @param subject
     * @param parent
     * @return the share roots
     */
    public List<? extends TreeNode> getSubjectShareChildren ( SubjectInfo subject, AbstractBrowseTreeNode parent ) {
        List<EntityTreeNode> adapters = new ArrayList<>();

        if ( subject == null ) {
            return adapters;
        }

        try {
            CollectionResult<SubjectGrant> grantsFromSubject = this.fsp.getBrowseService().getSubjectShareGrants(subject.getId());
            List<SubjectGrant> sorted = new ArrayList<>(grantsFromSubject.getCollection());
            Collections.sort(sorted, new SubjectGrantComparator());
            int hiddenFlagged = 0;
            int hiddenFilter = 0;

            for ( SubjectGrant o : sorted ) {
                if ( this.filterBean.isHidden(o.getEntity()) ) {
                    hiddenFlagged++;
                    continue;
                }

                if ( this.filterBean.isFiltered(o.getEntity()) ) {
                    hiddenFilter++;
                    continue;
                }

                EntityTreeNode contentEntityAdapter = new ShareRootEntityTreeNode(o, parent, this);
                contentEntityAdapter.setType(FileTreeConstants.SHARE_ROOT_TYPE);
                adapters.add(contentEntityAdapter);
            }

            parent.setHiddenFlagged(hiddenFlagged);
            parent.setHiddenPolicy(grantsFromSubject.getNumHiddenPolicy());
            parent.setPolicyViolations(grantsFromSubject.getViolations());
            parent.setHiddenFilter(hiddenFilter);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
        return adapters;
    }


    /**
     * @param parent
     * @return
     */
    private List<? extends TreeNode> getAllSharedToUserChildren ( PeerSharedByTreeNode parent ) {
        List<EntityTreeNode> adapters = new ArrayList<>();

        if ( parent == null ) {
            return adapters;
        }

        try {
            CollectionResult<SubjectGrant> grantsFromSubject = this.fsp.getBrowseService().getSharedToUserGrants();
            List<SubjectGrant> sorted = new ArrayList<>(grantsFromSubject.getCollection());
            Collections.sort(sorted, new SubjectGrantComparator());
            int hiddenFlagged = 0;
            int hiddenFilter = 0;

            for ( SubjectGrant o : sorted ) {
                if ( this.filterBean.isHidden(o.getEntity()) ) {
                    hiddenFlagged++;
                    continue;
                }

                if ( this.filterBean.isFiltered(o.getEntity()) ) {
                    hiddenFilter++;
                    continue;
                }

                EntityTreeNode contentEntityAdapter = new ShareRootEntityTreeNode(o, parent, this);
                contentEntityAdapter.setType(FileTreeConstants.SHARE_ROOT_TYPE);
                contentEntityAdapter.setOverrideRootType(FileTreeConstants.SHARED_ROOT_TYPE);
                adapters.add(contentEntityAdapter);
            }

            parent.setHiddenFlagged(hiddenFlagged);
            parent.setHiddenPolicy(grantsFromSubject.getNumHiddenPolicy());
            parent.setPolicyViolations(grantsFromSubject.getViolations());
            parent.setHiddenFilter(hiddenFilter);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
        return adapters;
    }


    /**
     * @param parent
     * @return
     */
    private List<? extends TreeNode> getAllSharedByUserChildren ( PeerSharedToTreeNode parent ) {
        List<EntityTreeNode> adapters = new ArrayList<>();

        if ( parent == null ) {
            return adapters;
        }

        try {
            CollectionResult<VFSEntity> sharedEntities = this.fsp.getBrowseService().getSharedByUserGrants();
            addUserEntities(parent, adapters, sharedEntities);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
        return adapters;
    }


    /**
     * @param subject
     * @param parent
     * @return
     */
    private List<? extends TreeNode> getSubjectSharedToChildren ( SubjectInfo subject, PeerSharedToTreeNode parent ) {
        List<EntityTreeNode> adapters = new ArrayList<>();

        if ( subject == null ) {
            return adapters;
        }

        try {
            CollectionResult<VFSEntity> sharedEntities = this.fsp.getBrowseService().getSubjectSharedToEntities(subject.getId());
            addUserEntities(parent, adapters, sharedEntities);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
        return adapters;
    }


    /**
     * @param subject
     * @param parent
     * @return
     */
    private List<? extends TreeNode> getMailSharedToChildren ( String mailAddr, PeerSharedToTreeNode parent ) {
        List<EntityTreeNode> adapters = new ArrayList<>();

        if ( StringUtils.isBlank(mailAddr) ) {
            return adapters;
        }

        try {
            CollectionResult<VFSEntity> sharedEntities = this.fsp.getBrowseService().getMailSharedToEntities(mailAddr);
            addUserEntities(parent, adapters, sharedEntities);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
        return adapters;
    }


    /**
     * @param subject
     * @param parent
     * @return
     */
    private List<? extends TreeNode> getLinkSharedToChildren ( PeerSharedToTreeNode parent ) {
        List<EntityTreeNode> adapters = new ArrayList<>();

        try {
            CollectionResult<VFSEntity> sharedEntities = this.fsp.getBrowseService().getTokenSharedToEntities();
            addUserEntities(parent, adapters, sharedEntities);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
        return adapters;
    }


    /**
     * @param parent
     * @param adapters
     * @param sharedEntities
     * @throws EntityNotFoundException
     */
    private void addUserEntities ( AbstractBrowseTreeNode parent, List<EntityTreeNode> adapters, CollectionResult<VFSEntity> entities )
            throws EntityNotFoundException {

        List<VFSEntity> sorted = new ArrayList<>(entities.getCollection());

        int hiddenFlagged = 0;
        int hiddenFilter = 0;
        for ( VFSEntity o : sorted ) {
            if ( this.filterBean.isHidden(o) ) {
                hiddenFlagged++;
                continue;
            }

            if ( this.filterBean.isFiltered(o) ) {
                hiddenFilter++;
                continue;
            }

            EntityTreeNode contentEntityAdapter = getEntityAdapter(parent, o);
            contentEntityAdapter.setOverridePerms(EnumSet.allOf(GrantPermission.class));
            if ( o.getOwner() instanceof Group ) {
                contentEntityAdapter.setOverrideRootType(FileTreeConstants.GROUP_ROOT_TYPE);
            }
            else {
                contentEntityAdapter.setOverrideRootType(FileTreeConstants.USER_ROOT_TYPE);
            }
            adapters.add(contentEntityAdapter);
        }

        parent.setHiddenFlagged(hiddenFlagged);
        parent.setHiddenPolicy(entities.getNumHiddenPolicy());
        parent.setPolicyViolations(entities.getViolations());
        parent.setHiddenFilter(hiddenFilter);
    }


    /**
     * @param parent
     * @return the roots shared by the specified target to the current user
     */
    public List<? extends TreeNode> getSharedByChildren ( PeerSharedByTreeNode parent ) {
        if ( parent == null ) {
            return new ArrayList<>();
        }

        PeerInfo pi = parent.getAttachedObject();

        if ( pi == null ) {
            return getAllSharedToUserChildren(parent);
        }
        else if ( pi instanceof SubjectPeerInfo ) {
            return getSubjectShareChildren( ( (SubjectPeerInfo) pi ).getSubject(), parent);
        }

        return new ArrayList<>();
    }


    /**
     * 
     * @param parent
     * @return the roots shared by the specified target to the current user
     */
    public List<? extends TreeNode> getSharedToChildren ( PeerSharedToTreeNode parent ) {
        if ( parent == null ) {
            return new ArrayList<>();
        }

        PeerInfo pi = parent.getAttachedObject();

        if ( pi == null ) {
            return getAllSharedByUserChildren(parent);
        }
        else if ( pi instanceof SubjectPeerInfo ) {
            return getSubjectSharedToChildren( ( (SubjectPeerInfo) pi ).getSubject(), parent);
        }
        else if ( pi instanceof MailPeerInfo ) {
            return getMailSharedToChildren( ( (MailPeerInfo) pi ).getMailAddress(), parent);
        }
        else if ( pi instanceof TokenPeerInfo ) {
            return getLinkSharedToChildren(parent);
        }

        return new ArrayList<>();
    }


    /**
     * @param parent
     * @param query
     * @return the found entires
     */
    public List<BrowseTreeNode> getSearchChildren ( SearchResultTreeNode parent, String query ) {
        List<BrowseTreeNode> adapters = new ArrayList<>();

        if ( parent == null ) {
            return adapters;
        }

        try {
            SearchResult results = this.fsp.getBrowseService().searchEntities(query, parent.getLimit(), parent.getOffset());
            int hiddenFlagged = 0;
            int hiddenPolicy = 0;
            int hiddenFilter = 0;

            parent.setHaveMoreResults(results.isHaveMoreElements());

            for ( VFSEntity o : results.getEntities() ) {

                if ( !this.policy.isPolicyFulfilled(o.getSecurityLabel()) ) {
                    hiddenPolicy++;
                    continue;
                }

                if ( this.filterBean.isHidden(o) ) {
                    hiddenFlagged++;
                    continue;
                }

                if ( this.filterBean.isFiltered(o) ) {
                    hiddenFilter++;
                    continue;
                }

                EntityTreeNode contentEntityAdapter = getEntityAdapter(parent, o);
                contentEntityAdapter.setOverrideRootType(FileTreeConstants.SEARCH_RESULT_TYPE);
                adapters.add(contentEntityAdapter);
            }

            parent.setHiddenFlagged(hiddenFlagged);
            parent.setHiddenPolicy(hiddenPolicy);
            parent.setHiddenFilter(hiddenFilter);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
        return adapters;
    }
}
