/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.05.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file.picker;


import java.util.function.Predicate;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.model.TreeNode;

import eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode;
import eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode;
import eu.agno3.fileshare.webgui.service.tree.EntityTreeNode;
import eu.agno3.fileshare.webgui.service.tree.TreeFilter;


/**
 * @author mbechler
 *
 */

@ViewScoped
@Named ( "entityPicker" )
public class EntityPicker extends AbstractFilePicker implements TreeFilter, Predicate<TreeNode> {

    /**
     * 
     */
    private static final long serialVersionUID = 8697259700009826572L;


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

        return !test(selected);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.TreeFilter#getPredicate(eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode)
     */
    @Override
    public Predicate<TreeNode> getPredicate ( AbstractBrowseTreeNode abstractBrowseTreeNode ) {
        return this;
    }


    @Override
    public boolean test ( TreeNode t ) {

        if ( ! ( t instanceof AbstractBrowseTreeNode ) ) {
            return true;
        }

        AbstractBrowseTreeNode bt = (AbstractBrowseTreeNode) t;

        if ( bt.isVirtualEmpty() ) {
            return true;
        }

        if ( ! ( bt instanceof EntityTreeNode ) ) {
            return false;
        }

        return false;
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
