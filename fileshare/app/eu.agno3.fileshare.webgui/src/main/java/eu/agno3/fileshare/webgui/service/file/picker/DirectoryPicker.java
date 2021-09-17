/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.05.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file.picker;


import java.util.function.Predicate;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.TreeNode;

import eu.agno3.fileshare.webgui.service.file.FilePermissionBean;
import eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode;
import eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode;
import eu.agno3.fileshare.webgui.service.tree.EntityTreeNode;
import eu.agno3.fileshare.webgui.service.tree.TreeFilter;


/**
 * @author mbechler
 *
 */

@ViewScoped
@Named ( "directoryPicker" )
public class DirectoryPicker extends AbstractFilePicker implements TreeFilter {

    /**
     * 
     */
    private static final long serialVersionUID = 8697259700009826572L;

    @Inject
    private FilePermissionBean filePerms;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.file.picker.AbstractFilePicker#acceptSelection(eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode)
     */
    @Override
    protected boolean acceptSelection ( BrowseTreeNode selected ) {
        if ( ! ( selected instanceof EntityTreeNode ) ) {
            return false;
        }

        return !getPredicate((EntityTreeNode) selected).test(selected);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.TreeFilter#getPredicate(eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode)
     */
    @Override
    public Predicate<TreeNode> getPredicate ( AbstractBrowseTreeNode parent ) {
        return new DirectoryFilterPredicate(this.filePerms, parent);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.file.picker.AbstractFilePicker#getFilter()
     */
    @Override
    protected TreeFilter getFilter () {
        return this;
    }
}
