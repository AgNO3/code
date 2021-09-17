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
public class EmptySearchResultTreeNode extends AbstractVirtualEmptyTreeNode {

    /**
     * 
     */
    private static final long serialVersionUID = 2383046218344844429L;


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.DefaultTreeNode#getType()
     */
    @Override
    public String getType () {
        return FileTreeConstants.EMPTY_SEARCH_RESULT_TYPE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractVirtualEmptyTreeNode#getIcon()
     */
    @Override
    public String getIcon () {
        return "ui-icon-blank"; //$NON-NLS-1$
    }

}
