/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.06.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.resourcelibraries;


import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibraryFileInfo;


/**
 * @author mbechler
 *
 */
public class FileNode extends DefaultTreeNode {

    /**
     * 
     */
    private static final long serialVersionUID = 8189481891287591809L;


    /**
     * 
     */
    public FileNode () {
        super();
    }


    /**
     * @param info
     * @param filename
     * @param parent
     */
    public FileNode ( ResourceLibraryFileInfo info, String filename, TreeNode parent ) {
        super(info, parent);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.DefaultTreeNode#getData()
     */
    @Override
    public ResourceLibraryFileInfo getData () {
        return (ResourceLibraryFileInfo) super.getData();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.DefaultTreeNode#getType()
     */
    @Override
    public String getType () {
        return "file"; //$NON-NLS-1$
    }


    /**
     * @return the inherited
     */
    public boolean getInherited () {
        return getData().getInherited();
    }


    /**
     * 
     * @return style class for the node
     */
    public String getStyleClass () {
        if ( getInherited() ) {
            return "inherited"; //$NON-NLS-1$
        }
        return StringUtils.EMPTY;
    }

}
