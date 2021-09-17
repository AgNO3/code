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
public class EmptyGroupsTreeNode extends AbstractVirtualEmptyTreeNode {

    /**
     * 
     */
    private static final long serialVersionUID = -7112676811025446506L;


    /**
     * 
     */
    public EmptyGroupsTreeNode () {
        super();
    }


    /**
     * @param parent
     * @param treeProvider
     */
    public EmptyGroupsTreeNode ( BrowseTreeNode parent, BrowseTreeProvider treeProvider ) {
        super(parent, treeProvider);
    }


    /**
     * @param treeProvder
     */
    public EmptyGroupsTreeNode ( BrowseTreeProvider treeProvder ) {
        super(treeProvder);
    }


    /**
     * @param obj
     * @param parent
     * @param treeProvider
     */
    public EmptyGroupsTreeNode ( VFSEntity obj, BrowseTreeNode parent, BrowseTreeProvider treeProvider ) {
        super(obj, parent, treeProvider);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.DefaultTreeNode#getType()
     */
    @Override
    public String getType () {
        return FileTreeConstants.EMPTY_GROUPS_TYPE;
    }

}
