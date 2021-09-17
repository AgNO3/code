/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.10.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.tree;


import java.util.Collections;
import java.util.List;

import org.primefaces.model.TreeNode;

import eu.agno3.fileshare.webgui.service.tree.ui.BrowseTreeProvider;


/**
 * @author mbechler
 *
 */
public class ErrorEntityTreeNode extends EntityTreeNode {

    /**
     * @param parent
     * @param btp
     */
    public ErrorEntityTreeNode ( BrowseTreeNode parent, BrowseTreeProvider btp ) {
        super(null, parent, btp);
    }

    /**
     * 
     */
    private static final long serialVersionUID = -2433760905116708089L;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.EntityTreeNode#getIcon()
     */
    @Override
    public String getIcon () {
        return "ui-icon-alert"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractLazyLoadTreeNode#isLeaf()
     */
    @Override
    public boolean isLeaf () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.EntityTreeNode#fetchChildren()
     */
    @Override
    protected List<? extends TreeNode> fetchChildren () {
        return Collections.EMPTY_LIST;
    }

}
