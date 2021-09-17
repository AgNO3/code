/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.05.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file.picker;


import java.util.function.Predicate;

import org.primefaces.model.TreeNode;

import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.webgui.service.file.FilePermissionBean;
import eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode;
import eu.agno3.fileshare.webgui.service.tree.EntityTreeNode;


/**
 * @author mbechler
 *
 */
public class DirectoryFilterPredicate implements Predicate<TreeNode> {

    private FilePermissionBean filePerms;
    private AbstractBrowseTreeNode parent;


    /**
     * @param filePerms
     * @param parent
     */
    public DirectoryFilterPredicate ( FilePermissionBean filePerms, AbstractBrowseTreeNode parent ) {
        this.filePerms = filePerms;
        this.parent = parent;
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

        EntityTreeNode et = (EntityTreeNode) bt;

        if ( ! ( et.getAttachedObject() instanceof VFSContainerEntity ) ) {
            return true;
        }

        if ( !this.filePerms.isOwner(et.getAttachedObject()) && !this.filePerms.canUpload(et) && !this.filePerms.canUpload(this.parent) ) {
            return true;
        }

        return false;
    }
}
