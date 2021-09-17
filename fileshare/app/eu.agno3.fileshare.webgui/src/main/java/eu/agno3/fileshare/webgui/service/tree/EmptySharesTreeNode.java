/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.tree;


import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.webgui.service.tree.ui.BrowseTreeProvider;


/**
 * @author mbechler
 *
 */
public class EmptySharesTreeNode extends AbstractVirtualEmptyTreeNode {

    /**
     * 
     */
    private static final long serialVersionUID = 9128250941560284945L;


    /**
     * 
     */
    public EmptySharesTreeNode () {
        super();
    }


    /**
     * @param parent
     * @param treeProvider
     */
    public EmptySharesTreeNode ( BrowseTreeNode parent, BrowseTreeProvider treeProvider ) {
        super(parent, treeProvider);
    }


    /**
     * @param treeProvder
     */
    public EmptySharesTreeNode ( BrowseTreeProvider treeProvder ) {
        super(treeProvder);
    }


    /**
     * @param obj
     * @param parent
     * @param treeProvider
     */
    public EmptySharesTreeNode ( VFSEntity obj, BrowseTreeNode parent, BrowseTreeProvider treeProvider ) {
        super(obj, parent, treeProvider);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.DefaultTreeNode#getType()
     */
    @Override
    public String getType () {
        return FileTreeConstants.EMPTY_SHARES_TYPE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractVirtualEmptyTreeNode#getIcon()
     */
    @Override
    public String getIcon () {
        return "ui-icon-plusthick"; //$NON-NLS-1$
    }
}
