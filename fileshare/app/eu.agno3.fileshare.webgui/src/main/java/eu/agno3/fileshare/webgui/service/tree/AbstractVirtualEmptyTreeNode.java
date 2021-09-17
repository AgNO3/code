/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.tree;


import java.util.Collections;
import java.util.List;

import org.primefaces.model.TreeNode;

import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.webgui.service.tree.ui.BrowseTreeProvider;


/**
 * @author mbechler
 *
 */
public abstract class AbstractVirtualEmptyTreeNode extends AbstractBrowseTreeNode {

    /**
     * 
     */
    private static final long serialVersionUID = -5452494793083622531L;


    /**
     * 
     */
    public AbstractVirtualEmptyTreeNode () {
        super();
    }


    /**
     * @param treeProvder
     */
    public AbstractVirtualEmptyTreeNode ( BrowseTreeProvider treeProvder ) {
        super(treeProvder);
    }


    /**
     * @param parent
     * @param treeProvider
     */
    public AbstractVirtualEmptyTreeNode ( BrowseTreeNode parent, BrowseTreeProvider treeProvider ) {
        super(parent, treeProvider);
    }


    /**
     * @param obj
     * @param parent
     * @param treeProvider
     */
    public AbstractVirtualEmptyTreeNode ( VFSEntity obj, BrowseTreeNode parent, BrowseTreeProvider treeProvider ) {
        super(obj, parent, treeProvider);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractLazyLoadTreeNode#getAttachedObject()
     */
    @Override
    public Object getAttachedObject () {
        return ( (AbstractBrowseTreeNode) getParent() ).getAttachedObject();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode#isVirtualEmpty()
     */
    @Override
    public boolean isVirtualEmpty () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode#hasPermission(eu.agno3.fileshare.model.GrantPermission)
     */
    @Override
    public boolean hasPermission ( GrantPermission perm ) {
        return this.getParent().hasPermission(perm);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode#getDepth()
     */
    @Override
    public int getDepth () {
        return ( (AbstractBrowseTreeNode) getParent() ).getDepth() + 1;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode#getIcon()
     */
    @Override
    public String getIcon () {
        return "ui-icon-plusthick"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode#fetchChildren()
     */
    @Override
    protected List<? extends TreeNode> fetchChildren () {
        return Collections.EMPTY_LIST;
    }

}