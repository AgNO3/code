/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.tree;


import java.util.List;

import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.webgui.service.tree.ui.BrowseTreeProvider;


/**
 * @author mbechler
 *
 */
public class SharedTreeNode extends AbstractBrowseTreeNode implements BrowseTreeNode {

    /**
     * 
     */
    private static final long serialVersionUID = -3862089246503380000L;


    /**
     * @param parent
     * @param treeProvider
     */
    public SharedTreeNode ( RootTreeNode parent, BrowseTreeProvider treeProvider ) {
        super(parent, treeProvider);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode#hasPermission(eu.agno3.fileshare.model.GrantPermission)
     */
    @Override
    public boolean hasPermission ( GrantPermission perm ) {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.DefaultTreeNode#getType()
     */
    @Override
    public String getType () {
        return FileTreeConstants.SHARED_ROOT_TYPE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode#getIcon()
     */
    @Override
    public String getIcon () {
        return "ui-icon-share"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode#fetchChildren()
     */
    @Override
    protected List<BrowseTreeNode> fetchChildren () {
        List<BrowseTreeNode> sharedChildren = this.getTreeProvider().getSharedChildren(this);
        if ( sharedChildren.isEmpty() ) {
            sharedChildren.add(new EmptySharesTreeNode());
        }
        return sharedChildren;
    }

}
