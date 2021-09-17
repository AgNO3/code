/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.tree;


import java.util.Collections;
import java.util.List;

import org.primefaces.model.TreeNode;

import eu.agno3.fileshare.model.GrantPermission;


/**
 * @author mbechler
 *
 */
public class RootTreeNode extends AbstractBrowseTreeNode implements BrowseTreeNode {

    /**
     * 
     */
    private static final long serialVersionUID = -2048894127126959913L;


    /**
     */
    public RootTreeNode () {}


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
        return FileTreeConstants.ROOT_TYPE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode#getIcon()
     */
    @Override
    public String getIcon () {
        return "ui-icon-home"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode#loadChildren()
     */
    @Override
    public void loadChildren () {

        for ( TreeNode n : this.getChildren() ) {

            if ( ! ( n instanceof BrowseTreeNode ) ) {
                continue;
            }
            ( (BrowseTreeNode) n ).loadChildren();
        }
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
