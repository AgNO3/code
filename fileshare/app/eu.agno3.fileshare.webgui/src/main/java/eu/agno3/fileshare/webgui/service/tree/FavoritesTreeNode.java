/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.05.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.tree;


import java.util.List;

import org.primefaces.model.TreeNode;

import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.webgui.service.tree.ui.BrowseTreeProvider;


/**
 * @author mbechler
 *
 */
public class FavoritesTreeNode extends AbstractBrowseTreeNode {

    /**
     * 
     */
    private static final long serialVersionUID = 4802878417077785682L;


    /**
     * 
     */
    public FavoritesTreeNode () {
        super();
    }


    /**
     * @param parent
     * @param treeProvider
     */
    public FavoritesTreeNode ( BrowseTreeNode parent, BrowseTreeProvider treeProvider ) {
        super(parent, treeProvider);
    }


    /**
     * @param treeProvder
     */
    public FavoritesTreeNode ( BrowseTreeProvider treeProvder ) {
        super(treeProvder);
    }


    /**
     * @param obj
     * @param parent
     * @param treeProvider
     */
    public FavoritesTreeNode ( VFSEntity obj, BrowseTreeNode parent, BrowseTreeProvider treeProvider ) {
        super(obj, parent, treeProvider);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode#hasPermission(eu.agno3.fileshare.model.GrantPermission)
     */
    @Override
    public boolean hasPermission ( GrantPermission perm ) {
        if ( perm == GrantPermission.READ ) {
            return true;
        }
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode#getIcon()
     */
    @Override
    public String getIcon () {
        return "ui-icon-star"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode#fetchChildren()
     */
    @Override
    protected List<? extends TreeNode> fetchChildren () {
        List<BrowseTreeNode> favoriteChildren = this.getTreeProvider().getFavoritesChildren(this);
        if ( favoriteChildren.isEmpty() ) {
            favoriteChildren.add(new EmptyFavoritesTreeNode(this, getTreeProvider()));
        }
        return favoriteChildren;
    }

}
