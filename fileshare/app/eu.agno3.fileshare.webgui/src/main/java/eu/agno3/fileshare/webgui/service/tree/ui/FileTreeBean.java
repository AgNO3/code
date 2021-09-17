/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.tree.ui;


import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.TreeNode;

import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.User;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.webgui.i18n.FileshareMessages;
import eu.agno3.fileshare.webgui.service.file.FileSelectionBean;
import eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode;
import eu.agno3.fileshare.webgui.service.tree.AbstractLazyLoadTreeNode;
import eu.agno3.fileshare.webgui.service.tree.AbstractVirtualEmptyTreeNode;
import eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode;
import eu.agno3.fileshare.webgui.service.tree.EntityTreeNode;
import eu.agno3.fileshare.webgui.service.tree.LabelNode;
import eu.agno3.fileshare.webgui.subject.CurrentUserBean;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "fileTreeBean" )
public class FileTreeBean implements Serializable {

    private static final Logger log = Logger.getLogger(FileTreeBean.class);

    /**
     * 
     */
    private static final long serialVersionUID = 2787713773568309760L;

    private static final String ROOT_TITLE_TREE = "root.title.tree"; //$NON-NLS-1$
    private static final String ROOT_TILE = "root.title.flat"; //$NON-NLS-1$
    private static final String ROOT_DESCRIPTION_TREE = "root.description.tree"; //$NON-NLS-1$
    private static final String ROOT_DESCRIPTION = "root.description.flat"; //$NON-NLS-1$

    @Inject
    private FileSelectionBean selectionBean;

    @Inject
    private FileRootSelectionBean fileRootSelectionBean;

    @Inject
    private FileTreeComponentBinding componentBinding;

    @Inject
    private CurrentUserBean currentUser;

    private BrowseTreeNode model;

    private Set<TreeNode> expandedNodes = new HashSet<>();


    /**
     * @return the singleLevel
     */
    public boolean getSingleLevel () {
        return this.fileRootSelectionBean.isSingleLevel();
    }


    /**
     * 
     * @return menu title for the root
     */
    public String getRootTitle () {
        if ( !this.getSingleLevel() ) {
            return FileshareMessages.get(ROOT_TITLE_TREE);
        }

        return FileshareMessages.get(ROOT_TILE);
    }


    /**
     * 
     * @return menu description for the root
     */
    public String getRootDescription () {
        if ( !this.getSingleLevel() ) {
            return FileshareMessages.get(ROOT_DESCRIPTION_TREE);
        }

        return FileshareMessages.get(ROOT_DESCRIPTION);
    }


    /**
     * 
     * @return extra style classes to apply to root container
     */
    public String getExtraStyleClass () {
        if ( this.getSingleLevel() ) {
            return "single-level"; //$NON-NLS-1$
        }

        return "tree-view"; //$NON-NLS-1$
    }


    /**
     * 
     * @return the tree model
     */
    public BrowseTreeNode getModel () {
        if ( this.model == null ) {
            this.model = this.fileRootSelectionBean.getRoot();
            TreeExpansionUtil.expandUserRoot(this.model, this);

        }
        return this.model;
    }


    /**
     * 
     * @param ev
     */
    public void refreshAndClearSelectionIfTrue ( SelectEvent ev ) {

        if ( ev == null ) {
            return;
        }

        if ( ev.getObject() != null && ev.getObject() instanceof Boolean && (boolean) ev.getObject() ) {
            refreshAndClearSelection();
        }
    }


    /**
     * 
     */
    public void refreshAndClearSelection () {
        this.selectionBean.setSelection(new TreeNode[0]);
        refresh();
    }


    /**
     * Refresh the complete model
     */
    public void refresh () {
        log.debug("Refreshing"); //$NON-NLS-1$
        BrowseTreeNode n = this.getModel();
        this.fileRootSelectionBean.refresh();
        this.fileRootSelectionBean.updateRoot(n);

        synchronized ( n ) {
            n.loadChildren();
            if ( log.isTraceEnabled() ) {
                synchronized ( getExpandedNodes() ) {
                    for ( TreeNode r : getExpandedNodes() ) {
                        log.trace("Trying to expand node " + r); //$NON-NLS-1$
                    }
                }
            }
            staticExpandedNodes(n);

            TreeNode[] selection = this.selectionBean.getSelection();

            if ( selection != null ) {
                log.trace("Selecting nodes"); //$NON-NLS-1$
                HashSet<TreeNode> newSelection = new HashSet<>();
                staticSelectNodes(n, new HashSet<>(Arrays.asList(selection)), newSelection);
                this.selectionBean.setSelection(newSelection.toArray(new TreeNode[] {}));
            }

            if ( this.componentBinding.getComponent() != null ) {
                this.componentBinding.getComponent().setValue(n);
                this.componentBinding.getComponent().setSelection(this.selectionBean.getSelection());
            }
        }

    }


    /**
     * 
     * @return null
     */
    public String clearSelection () {
        this.selectionBean.clear();
        this.refresh();
        return null;
    }


    /**
     * @return null
     */
    public String selectAll () {
        if ( !this.getSingleLevel() ) {
            return null;
        }
        TreeNode[] all = this.getModel().getChildren().stream().filter(x -> ! ( x instanceof AbstractVirtualEmptyTreeNode || x instanceof LabelNode ))
                .collect(Collectors.toList()).toArray(new TreeNode[] {});
        this.selectionBean.setSelection(all);
        this.refresh();
        return null;
    }


    /**
     * @return
     */
    private Set<TreeNode> getExpandedNodes () {
        return this.expandedNodes;
    }


    /**
     * @param n
     * @param hashSet
     */
    private void staticSelectNodes ( BrowseTreeNode n, HashSet<TreeNode> toSelect, HashSet<TreeNode> newSelected ) {

        if ( toSelect.contains(n) ) {
            n.setSelected(true);
            newSelected.add(n);
        }
        else {
            n.setSelected(false);
        }

        toSelect.remove(n);

        if ( toSelect.isEmpty() ) {
            return;
        }

        for ( TreeNode child : n.getChildren() ) {
            if ( child instanceof BrowseTreeNode ) {
                this.staticSelectNodes((BrowseTreeNode) child, toSelect, newSelected);
            }
        }
    }


    /**
     * @param e
     * @param parents
     * @param g
     */
    public void ensureExpanded ( VFSContainerEntity e, List<VFSContainerEntity> parents, Grant g ) {
        this.ensureExpandedInternal(e, new LinkedList<>(parents), g);
    }


    /**
     * @param e
     * @param root
     * @return the expanded tree node
     */
    private TreeNode ensureExpandedInternal ( VFSContainerEntity e, List<VFSContainerEntity> parents, Grant g ) {

        if ( this.getSingleLevel() ) {
            return null;
        }

        VFSContainerEntity parent = !parents.isEmpty() ? parents.remove(0) : null;

        if ( parent != null ) {
            TreeNode parentNode = ensureExpandedInternal(parent, parents, g);
            if ( parentNode == null ) {
                return null;
            }
            for ( TreeNode child : parentNode.getChildren() ) {
                if ( child instanceof EntityTreeNode && ( (EntityTreeNode) child ).getAttachedObject().equals(e) ) {
                    ensureNodeExpanded(child);
                    return child;
                }
            }
        }

        return expandVirtualRootsTo(e, g);
    }


    /**
     * @param e
     * @param g
     * @return
     */
    private TreeNode expandVirtualRootsTo ( VFSContainerEntity e, Grant g ) {

        BrowseTreeNode root = this.model;
        if ( root == null ) {
            root = getModel();
        }

        if ( g != null ) {
            // need to expand grant
            return TreeExpansionUtil.expandGrant(root, this, g);
        }
        else if ( e.getOwner() instanceof User && e.getOwner().equals(this.currentUser.getCurrentUser()) ) {
            // need to expand user root
            return TreeExpansionUtil.expandUserRoot(root, this);
        }
        else if ( e.getOwner() instanceof Group && this.currentUser.isMemberOf((Group) e.getOwner()) ) {
            // need to expand group root
            return TreeExpansionUtil.expandGroupRoot((Group) e.getOwner(), root, this);
        }

        return null;
    }


    /**
     * @param n
     */
    void ensureNodeExpanded ( TreeNode n ) {
        if ( !n.isExpanded() ) {
            expandNode(n);
        }
    }


    /**
     * @param r
     */
    private void staticExpandedNodes ( BrowseTreeNode r ) {
        synchronized ( getExpandedNodes() ) {
            if ( getExpandedNodes().contains(r) ) {
                if ( log.isTraceEnabled() ) {
                    log.trace("Expanding node " + r); //$NON-NLS-1$
                }
                getExpandedNodes().remove(r);
                this.expandNode(r);
            }

            if ( getExpandedNodes().isEmpty() ) {
                return;
            }
        }

        for ( TreeNode child : r.getChildren() ) {
            if ( child instanceof BrowseTreeNode ) {
                this.staticExpandedNodes((BrowseTreeNode) child);
            }
        }
    }


    private void expandNode ( TreeNode found ) {
        if ( log.isTraceEnabled() ) {
            log.trace("Expanding " + ( (AbstractBrowseTreeNode) found ).getAttachedObject()); //$NON-NLS-1$
        }
        synchronized ( this.getModel() ) {
            synchronized ( getExpandedNodes() ) {
                ( (AbstractLazyLoadTreeNode) found ).expandStatic();
                getExpandedNodes().add(found);
            }
        }
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
