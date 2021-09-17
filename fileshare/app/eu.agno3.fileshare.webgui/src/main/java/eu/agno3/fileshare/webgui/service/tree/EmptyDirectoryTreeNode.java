/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.tree;


import eu.agno3.fileshare.webgui.service.tree.ui.BrowseTreeProvider;


/**
 * @author mbechler
 *
 */
public class EmptyDirectoryTreeNode extends AbstractVirtualEmptyTreeNode implements BrowseTreeNode {

    /**
     * 
     */
    private static final long serialVersionUID = -8508162412486209989L;


    /**
     * 
     */
    public EmptyDirectoryTreeNode () {
        super();
    }


    /**
     * @param parent
     * @param treeProvider
     */
    public EmptyDirectoryTreeNode ( BrowseTreeNode parent, BrowseTreeProvider treeProvider ) {
        super(parent, treeProvider);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.DefaultTreeNode#getType()
     */
    @Override
    public String getType () {
        return FileTreeConstants.EMPTY_DIR_TYPE;
    }

}
