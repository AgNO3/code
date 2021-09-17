/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.11.2013 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.menu;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import eu.agno3.orchestrator.config.model.base.AbstractModelException;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.service.StructuralObjectService;
import eu.agno3.orchestrator.server.webgui.connector.ServerServiceProvider;
import eu.agno3.orchestrator.server.webgui.structure.StructureCacheBean;
import eu.agno3.orchestrator.server.webgui.structure.StructureViewContextBean;
import eu.agno3.runtime.jsf.windowscope.WindowScoped;


/**
 * @author mbechler
 * 
 */
@Named ( "treeMenuState" )
@WindowScoped
public class TreeMenuStateBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6743280948075136049L;

    private static final Logger log = Logger.getLogger(TreeMenuStateBean.class);

    private Set<TreeNode> expandedNodes = new HashSet<>();

    @Inject
    private MenuItemAdapterFactoryBean adapterFactory;

    @Inject
    private ServerServiceProvider ssp;

    @Inject
    private StructureViewContextBean structureContext;

    @Inject
    private StructureCacheBean structureCache;

    private TreeNode root;

    private TreeNode selectedNode;

    private UUID selectionForAnchor;


    protected void loadRoot () {
        if ( log.isDebugEnabled() ) {
            log.debug("Initialize new menu instance"); //$NON-NLS-1$
        }

        this.root = new DefaultTreeNode("root", null); //$NON-NLS-1$

        StructuralObject c;
        try {
            c = this.ssp.getService(StructuralObjectService.class).getStructureRoot();
        }
        catch ( AbstractModelException e ) {
            log.error("Failed to load root cluster:", e); //$NON-NLS-1$
            return;
        }
        catch ( Exception e ) {
            log.error("Failed to access StructuralObjectService", e); //$NON-NLS-1$
            return;
        }

        buildTree(c);
    }


    private void buildTree ( StructuralObject c ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Root cluster " + c); //$NON-NLS-1$
        }

        StructuralObjectAdapter<StructuralObject> r = this.adapterFactory.getMenuItemAdapter(this.root, c);
        r.setParent(this.root);
        this.root.getChildren().add(r);

        if ( this.expandedNodes.isEmpty() ) {
            this.expandNode(r);
        }
        else {
            this.staticExpandedNodes(r);
        }

        if ( this.selectedNode != null ) {
            if ( this.selectedNode instanceof StructuralObjectAdapter<?> ) {
                this.expandParents( ( (StructuralObjectAdapter<?>) this.selectedNode ).getAttachedObject());
            }
            for ( TreeNode obj : this.expandedNodes ) {
                if ( obj.equals(this.selectedNode) ) {
                    obj.setSelected(true);
                    this.selectedNode = obj;
                }
            }
        }
    }


    /**
     * @param r
     */
    private void staticExpandedNodes ( StructuralObjectAdapter<?> r ) {
        if ( this.expandedNodes.contains(r) ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Expanding node " + r); //$NON-NLS-1$
            }
            this.expandedNodes.remove(r);
            this.expandNode(r);
        }

        for ( TreeNode child : r.getChildren() ) {
            if ( child instanceof StructuralObjectAdapter<?> ) {
                this.staticExpandedNodes((StructuralObjectAdapter<?>) child);
            }
        }
    }


    public void reload () {
        this.loadRoot();
    }


    public void reload ( ActionEvent ev ) {
        this.reload();
    }


    /**
     * @return the expandedNodes
     */
    public Set<TreeNode> getExpandedNodes () {
        return Collections.unmodifiableSet(this.expandedNodes);
    }


    /**
     * @param ev
     */
    public void onNodeExpand ( NodeExpandEvent ev ) {
        this.expandedNodes.add(ev.getTreeNode());
        if ( log.isDebugEnabled() ) {
            log.debug("State: expanded " + ev.getTreeNode().getData()); //$NON-NLS-1$
        }

        TreeNode t = ev.getTreeNode();

        if ( t instanceof AbstractLazyLoadTreeNode ) {
            AbstractLazyLoadTreeNode n = (AbstractLazyLoadTreeNode) t;
            t.getChildren().clear();
            n.loadChildren();
        }
    }


    public void onNodeSelect ( NodeSelectEvent ev ) {
        if ( log.isDebugEnabled() ) {
            log.debug("select handler selected node " + System.identityHashCode(this.selectedNode)); //$NON-NLS-1$
        }

        this.expandedNodes.add(ev.getTreeNode());
        if ( ev.getTreeNode() instanceof AbstractLazyLoadTreeNode ) {
            ( (AbstractLazyLoadTreeNode) ev.getTreeNode() ).expandStatic();
        }
        else {
            ev.getTreeNode().setExpanded(true);
        }
    }


    /**
     * @param ev
     */
    public void onNodeCollapse ( NodeCollapseEvent ev ) {
        this.expandedNodes.remove(ev.getTreeNode());
        if ( log.isDebugEnabled() ) {
            log.debug("State: collapsed " + ev.getTreeNode().getData()); //$NON-NLS-1$
        }
    }


    /**
     * @return the root
     */
    public synchronized TreeNode getRoot () {
        if ( this.root == null ) {
            this.loadRoot();
        }

        if ( this.selectionForAnchor == null || !this.selectionForAnchor.equals(this.structureContext.getSelectedAnchorId()) ) {
            refreshSelectedNode();
            this.selectionForAnchor = this.structureContext.getSelectedAnchorId();
        }

        return this.root;
    }


    /**
     * @return the selectedNode
     */
    public TreeNode getSelectedNode () {
        if ( log.isDebugEnabled() ) {
            log.debug("selected node " + System.identityHashCode(this.selectedNode)); //$NON-NLS-1$
        }

        if ( this.selectedNode == null && this.structureContext.getSelectedAnchorId() != null ) {
            refreshSelectedNode();
        }

        return this.selectedNode;
    }


    /**
     * 
     */
    private void refreshSelectedNode () {
        TreeNode found = findNodeForSelectedObject();
        if ( found != this.selectedNode ) {
            this.selectedNode = found;
            setSelectedNode(this.selectedNode);
            if ( log.isDebugEnabled() ) {
                log.debug("Found selection from structure context: " + ( this.selectedNode != null ? this.selectedNode.getData() : null )); //$NON-NLS-1$
            }

            expandParents();
        }
    }


    /**
     * 
     */
    protected void expandParents () {
        TreeNode parent = this.selectedNode;

        while ( parent != null ) {

            if ( parent instanceof AbstractLazyLoadTreeNode ) {
                ( (AbstractLazyLoadTreeNode) parent ).expandStatic();
            }

            parent = parent.getParent();
        }
    }


    private TreeNode findNodeForSelectedObject () {
        try {
            StructuralObject obj = this.structureContext.getSelectedAnchor();
            this.expandParents(obj);
            return findObject(obj);
        }
        catch ( Exception e ) {
            log.debug("Failed to find selected object", e); //$NON-NLS-1$
        }
        return null;
    }


    private TreeNode findObject ( StructuralObject obj ) {
        if ( obj != null ) {
            for ( TreeNode node : this.expandedNodes ) {
                TreeNode found = checkNode(obj, node);
                if ( found != null ) {
                    return found;
                }
            }
        }
        return null;
    }


    /**
     * @param obj
     * @param node
     */
    protected TreeNode checkNode ( StructuralObject obj, TreeNode node ) {
        if ( node instanceof StructuralObjectAdapter<?> && ( (StructuralObjectAdapter<?>) node ).getAttachedObject().equals(obj) ) {
            return node;
        }

        for ( TreeNode child : node.getChildren() ) {
            if ( child instanceof StructuralObjectAdapter<?> && ( (StructuralObjectAdapter<?>) child ).getAttachedObject().equals(obj) ) {
                return child;
            }
        }

        return null;
    }


    /**
     * 
     * @param obj
     *            the structural object to select
     */
    public void setSelectedObject ( StructuralObject obj ) {
        TreeNode oldSelection = this.getSelectedNode();

        if ( oldSelection != null ) {
            oldSelection.setSelected(false);
        }

        this.expandParents(obj);

        TreeNode node = this.findObject(obj);
        if ( node instanceof AbstractLazyLoadTreeNode ) {
            this.expandNode(node);
            node.setSelected(true);
        }
        this.setSelectedNode(node);
    }


    private void expandParents ( StructuralObject obj ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Expanding parents of " + obj); //$NON-NLS-1$
        }

        List<StructuralObject> parents = getObjectParents(obj);

        for ( int i = parents.size() - 1; i >= 0; i-- ) {
            TreeNode found = this.findObject(parents.get(i));

            if ( found != null && found.isExpanded() ) {
                continue;
            }

            if ( found instanceof StructuralObjectAdapter<?> ) {
                expandNode(found);
            }
        }

    }


    private void expandNode ( TreeNode found ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Expanding " + ( (StructuralObjectAdapter<?>) found ).getAttachedObject()); //$NON-NLS-1$
        }
        ( (AbstractLazyLoadTreeNode) found ).expandStatic();
        this.expandedNodes.add(found);
    }


    private List<StructuralObject> getObjectParents ( StructuralObject obj ) {
        StructuralObject cur = obj;
        List<StructuralObject> parents = new ArrayList<>();

        while ( cur != null ) {
            parents.add(cur);

            try {
                cur = this.structureCache.getParentFor(cur);
            }
            catch ( Exception e ) {
                log.warn("Failed to get parent chain", e); //$NON-NLS-1$
                break;
            }
        }
        return parents;
    }


    /**
     * @param node
     *            the selectedNode to set
     */
    public void setSelectedNode ( TreeNode node ) {
        if ( this.selectedNode != null ) {
            this.selectedNode.setSelected(false);
        }

        deselectChildren(this.root);

        if ( node != null ) {
            node.setSelected(true);
        }
        this.selectedNode = node;
        if ( log.isDebugEnabled() ) {
            log.debug("set selected node " + System.identityHashCode(node)); //$NON-NLS-1$
        }
    }


    /**
     * @param root2
     */
    private void deselectChildren ( TreeNode n ) {

        if ( n == null ) {
            return;
        }

        n.setSelected(false);
        for ( TreeNode c : n.getChildren() ) {
            deselectChildren(c);
        }
    }


    /**
     * @param s
     * 
     */
    public void setSelectedNode ( StructuralObject s ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Setting selected node " + s); //$NON-NLS-1$
        }

        try {
            StructuralObject obj = this.structureContext.getSelectedAnchor();
            this.expandParents(obj);
            setSelectedNode(findObject(s));
        }
        catch ( Exception e ) {
            log.debug("Failed to find object to select", e); //$NON-NLS-1$
        }
    }
}
