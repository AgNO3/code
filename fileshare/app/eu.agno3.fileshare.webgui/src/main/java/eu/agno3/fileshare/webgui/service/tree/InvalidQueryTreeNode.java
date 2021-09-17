/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.05.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.tree;


/**
 * @author mbechler
 *
 */
public class InvalidQueryTreeNode extends AbstractVirtualEmptyTreeNode {

    /**
     * 
     */
    private static final long serialVersionUID = 5554409657657370339L;


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.DefaultTreeNode#getType()
     */
    @Override
    public String getType () {
        return FileTreeConstants.INVALID_QUERY_TREE_NODE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractVirtualEmptyTreeNode#getIcon()
     */
    @Override
    public String getIcon () {
        return "ui-icon-alert"; //$NON-NLS-1$
    }
}
