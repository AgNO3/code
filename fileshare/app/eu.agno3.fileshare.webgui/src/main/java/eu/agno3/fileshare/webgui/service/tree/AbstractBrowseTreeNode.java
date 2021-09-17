/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.tree;


import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.primefaces.model.TreeNode;
import org.primefaces.model.TreeNodeChildren;

import eu.agno3.fileshare.model.PolicyViolation;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.webgui.service.tree.ui.BrowseTreeProvider;


/**
 * @author mbechler
 *
 */
public abstract class AbstractBrowseTreeNode extends AbstractLazyLoadTreeNode implements BrowseTreeNode {

    /**
     * 
     */
    private static final long serialVersionUID = -6498544164978446250L;
    private static final Logger log = Logger.getLogger(AbstractBrowseTreeNode.class);

    private BrowseTreeProvider treeProvider;
    private int depth;

    private int hiddenFlagged;
    private int hiddenPolicy;
    private int hiddenFilter;

    private TreeFilter filter;
    private Collection<PolicyViolation> policyViolations;


    /**
     * 
     */
    public AbstractBrowseTreeNode () {
        super();
    }


    /**
     * @param treeProvder
     */
    public AbstractBrowseTreeNode ( BrowseTreeProvider treeProvder ) {
        super();
        this.treeProvider = treeProvder;
    }


    /**
     * @param parent
     * @param treeProvider
     */
    public AbstractBrowseTreeNode ( BrowseTreeNode parent, BrowseTreeProvider treeProvider ) {
        super(null, null, null);
        this.treeProvider = treeProvider;
        if ( parent != null ) {
            this.depth = parent.getDepth() + 1;
        }
    }


    /**
     * @param obj
     * @param parent
     * @param treeProvider
     */
    public AbstractBrowseTreeNode ( VFSEntity obj, BrowseTreeNode parent, BrowseTreeProvider treeProvider ) {
        super(null, obj, null);
        this.treeProvider = treeProvider;
        if ( parent != null ) {
            this.depth = parent.getDepth() + 1;
        }
    }


    /**
     * @return the filter
     */
    public TreeFilter getFilter () {
        if ( this.filter != null ) {
            return this.filter;
        }
        else if ( this.getParent() instanceof AbstractBrowseTreeNode ) {
            return ( (AbstractBrowseTreeNode) this.getParent() ).getFilter();
        }

        return null;
    }


    /**
     * @param filter
     *            the filter to set
     */
    public void setFilter ( TreeFilter filter ) {
        this.filter = filter;
    }


    /**
     * 
     * @return whether this is a label node
     */
    public boolean isLabelNode () {
        return false;
    }


    /**
     * @return the hiddenFlagged
     */
    public int getHiddenFlagged () {
        return this.hiddenFlagged;
    }


    /**
     * @param hiddenFlagged
     *            the hiddenFlagged to set
     */
    public void setHiddenFlagged ( int hiddenFlagged ) {
        this.hiddenFlagged = hiddenFlagged;
    }


    /**
     * @return the hiddenPolicy
     */
    public int getHiddenPolicy () {
        return this.hiddenPolicy;
    }


    /**
     * @param hiddenPolicy
     *            the hiddenPolicy to set
     */
    public void setHiddenPolicy ( int hiddenPolicy ) {
        this.hiddenPolicy = hiddenPolicy;
    }


    /**
     * @return the hiddenFilter
     */
    public int getHiddenFilter () {
        return this.hiddenFilter;
    }


    /**
     * @param hiddenFilter
     *            the hiddenFilter to set
     */
    public void setHiddenFilter ( int hiddenFilter ) {
        this.hiddenFilter = hiddenFilter;
    }


    /**
     * 
     * @return total number of hidden children
     */
    public int getHiddenTotal () {
        return this.getHiddenFilter() + this.getHiddenFlagged() + this.getHiddenPolicy();
    }


    /**
     * @param violations
     */
    public void setPolicyViolations ( Collection<PolicyViolation> violations ) {
        this.policyViolations = violations;
    }


    /**
     * @return the policyViolations
     */
    public Collection<PolicyViolation> getPolicyViolations () {
        return this.policyViolations;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.DefaultTreeNode#getParent()
     */
    @Override
    public BrowseTreeNode getParent () {
        return (BrowseTreeNode) super.getParent();
    }


    /**
     * @return the treeProvider
     */
    protected BrowseTreeProvider getTreeProvider () {
        return this.treeProvider;
    }


    protected abstract List<? extends TreeNode> fetchChildren ();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode#getDepth()
     */
    @Override
    public int getDepth () {
        return this.depth;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode#isVirtualEmpty()
     */
    @Override
    public boolean isVirtualEmpty () {
        return false;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractLazyLoadTreeNode#loadChildren()
     */
    @Override
    public void loadChildren () {
        if ( log.isTraceEnabled() ) {
            log.trace("Loading children of " + this); //$NON-NLS-1$
        }

        @SuppressWarnings ( "unchecked" )
        List<TreeNode> children = (List<TreeNode>) this.fetchChildren();
        filterChildren(children);
        TreeNodeChildren childData = new TreeNodeChildren(this);
        childData.addAll(children);

        updateRowKeys(childData);

        if ( children.isEmpty() ) {
            this.noChildrenFound();
        }

        this.setChildren(childData);
    }


    /**
     * @param children
     */
    protected void filterChildren ( List<TreeNode> children ) {
        TreeFilter f = this.getFilter();

        if ( f != null && children != null ) {
            children.removeIf(f.getPredicate(this));
        }
    }


    /**
     * @param childData
     */
    private void updateRowKeys ( List<TreeNode> childData ) {
        int childCount = childData.size();
        if ( childCount > 0 ) {
            for ( int i = 0; i < childCount; i++ ) {
                TreeNode childNode = childData.get(i);
                childNode.setRowKey(makeRowKey(this, i));
                updateRowKeys(childNode.getChildren());
            }
        }
    }


    /**
     * @param node
     * @param i
     * @return
     */
    private static String makeRowKey ( TreeNode node, int i ) {
        return ( node.getParent() == null ) ? String.valueOf(i) : node.getRowKey() + '_' + i;
    }

}