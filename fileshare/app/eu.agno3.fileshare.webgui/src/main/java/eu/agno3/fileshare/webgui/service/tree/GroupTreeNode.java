/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.tree;


import java.util.List;

import org.primefaces.model.TreeNode;

import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.webgui.service.tree.ui.BrowseTreeProvider;


/**
 * @author mbechler
 *
 */
public class GroupTreeNode extends AbstractBrowseTreeNode implements BrowseTreeNode {

    /**
     * 
     */
    private static final long serialVersionUID = 6673945459972025299L;


    /**
     * @param parent
     * @param browseTreeProvider
     */
    public GroupTreeNode ( RootTreeNode parent, BrowseTreeProvider browseTreeProvider ) {
        super(parent, browseTreeProvider);
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
        return FileTreeConstants.GROUPS_ROOT_TYPE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode#getIcon()
     */
    @Override
    public String getIcon () {
        return "ui-icon-person"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode#fetchChildren()
     */
    @Override
    protected List<? extends TreeNode> fetchChildren () {
        List<BrowseTreeNode> groupChildren = this.getTreeProvider().getGroupChildren(this);
        if ( groupChildren.isEmpty() ) {
            groupChildren.add(new EmptyGroupsTreeNode());
        }
        return groupChildren;
    }

}
