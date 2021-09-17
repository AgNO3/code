/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.05.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file.picker;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.model.TreeNode;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.share.GrantSelectionBean;
import eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode;
import eu.agno3.fileshare.webgui.service.tree.AbstractLazyLoadTreeNode;
import eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode;
import eu.agno3.fileshare.webgui.service.tree.EmptyDirectoryTreeNode;
import eu.agno3.fileshare.webgui.service.tree.RootTreeNode;
import eu.agno3.fileshare.webgui.service.tree.ShareRootEntityTreeNode;
import eu.agno3.fileshare.webgui.service.tree.TreeFilter;
import eu.agno3.fileshare.webgui.service.tree.ui.BrowseTreeProvider;
import eu.agno3.fileshare.webgui.service.tree.ui.FileRootSelectionBean;
import eu.agno3.fileshare.webgui.subject.CurrentUserBean;
import eu.agno3.fileshare.webgui.users.CurrentUserMembershipBean;


/**
 * @author mbechler
 *
 */
public abstract class AbstractFilePicker implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4473097176762446231L;

    private static final Logger log = Logger.getLogger(AbstractFilePicker.class);

    private BrowseTreeNode model;

    private Set<TreeNode> expandedNodes = new HashSet<>();

    @Inject
    private BrowseTreeProvider adapterFactory;

    @Inject
    private FileshareServiceProvider fsp;

    @Inject
    private GrantSelectionBean grantSelection;

    @Inject
    private CurrentUserBean currentUser;

    @Inject
    private CurrentUserMembershipBean currentUserMembership;

    private BrowseTreeNode selection;


    /**
     * @return the selection
     */
    public BrowseTreeNode getSelection () {
        return this.selection;
    }


    /**
     * @param selection
     *            the selection to set
     */
    public void setSelection ( BrowseTreeNode selection ) {
        if ( this.acceptSelection(selection) ) {
            this.selection = selection;
        }
        else {
            this.selection = null;
        }
    }


    /**
     * 
     */
    public void clear () {
        this.selection = null;
    }


    /**
     * @param selected
     * @return
     */
    protected boolean acceptSelection ( BrowseTreeNode selected ) {
        return true;
    }


    /**
     * 
     * @return the tree model
     */
    public BrowseTreeNode getModel () {
        if ( this.model == null ) {
            this.model = getRoot();

        }
        return this.model;
    }


    /**
     * @return
     */
    private BrowseTreeNode getRoot () {
        try {
            if ( !this.currentUser.isAuthenticated() ) {
                Grant rootGrant = this.grantSelection.getSingleSelection();
                if ( rootGrant == null ) {
                    log.warn("No grant found"); //$NON-NLS-1$
                    return new EmptyDirectoryTreeNode();
                }

                RootTreeNode root = new RootTreeNode();

                ShareRootEntityTreeNode r = new ShareRootEntityTreeNode(rootGrant, root, this.adapterFactory);
                root.getChildren().add(r);
                r.setOverridePerms(rootGrant.getPermissions());
                r.setFilter(this.getFilter());
                r.expandStatic();
                return root;
            }

            AbstractBrowseTreeNode root = FileRootSelectionBean.makeRootNode(
                this.adapterFactory,
                this.fsp.getBrowseService().getUserRoot(),
                this.addShared(),
                this.addGroups());
            root.setFilter(this.getFilter());
            return root;
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
            return new RootTreeNode();
        }
    }


    /**
     * @return whether to add the groups node
     */
    private boolean addGroups () {
        return this.currentUserMembership.haveAnyGroups();
    }


    /**
     * @return
     */
    protected boolean addShared () {
        return true;
    }


    /**
     * @return
     */
    protected TreeFilter getFilter () {
        return null;
    }


    /**
     * @return
     */
    private Set<TreeNode> getExpandedNodes () {
        return this.expandedNodes;
    }


    /**
     * @param ev
     */
    public void onNodeExpand ( NodeExpandEvent ev ) {
        synchronized ( getExpandedNodes() ) {
            if ( getExpandedNodes().contains(ev.getTreeNode()) ) {
                return;
            }

            getExpandedNodes().add(ev.getTreeNode());
        }

        if ( ev.getTreeNode() == null ) {
            return;
        }

        if ( log.isTraceEnabled() ) {
            log.trace("State: expanded " + ev.getTreeNode().getData()); //$NON-NLS-1$
        }

        TreeNode t = ev.getTreeNode();
        synchronized ( this.getModel() ) {
            if ( t instanceof AbstractLazyLoadTreeNode ) {
                AbstractLazyLoadTreeNode n = (AbstractLazyLoadTreeNode) t;
                n.loadChildren();
            }
        }
    }


    /**
     * @param ev
     */
    public void onNodeCollapse ( NodeCollapseEvent ev ) {
        synchronized ( getExpandedNodes() ) {
            getExpandedNodes().remove(ev.getTreeNode());
        }
        if ( log.isTraceEnabled() ) {
            log.trace("State: collapsed " + ev.getTreeNode().getData()); //$NON-NLS-1$
        }
    }


    /**
     * @param ev
     */
    public void onNodeSelect ( NodeSelectEvent ev ) {}


    /**
     * @param ev
     */
    public void onNodeUnselect ( NodeUnselectEvent ev ) {}
}
