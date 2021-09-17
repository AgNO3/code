/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.tree;


import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.webgui.service.tree.ui.BrowseTreeProvider;


/**
 * @author mbechler
 *
 */
public class ShareRootEntityTreeNode extends EntityTreeNode {

    /**
     * 
     */
    private static final long serialVersionUID = -8469951833111982590L;


    /**
     * 
     */
    public ShareRootEntityTreeNode () {
        super();
    }


    /**
     * @param treeProvider
     */
    public ShareRootEntityTreeNode ( BrowseTreeProvider treeProvider ) {
        super(treeProvider);
    }


    /**
     * @param obj
     * @param parent
     * @param treeProvider
     */
    public ShareRootEntityTreeNode ( Grant obj, BrowseTreeNode parent, BrowseTreeProvider treeProvider ) {
        super(obj.getEntity(), parent, treeProvider);
        super.setGrant(obj);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.EntityTreeNode#getType()
     */
    @Override
    public String getType () {

        if ( this.getAttachedObject() instanceof VFSContainerEntity ) {
            return super.getType();
        }

        return "share-root-file"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.EntityTreeNode#hasPermission(eu.agno3.fileshare.model.GrantPermission)
     */
    @Override
    public boolean hasPermission ( GrantPermission perm ) {
        if ( super.getGrant() == null || super.getGrant().getPermissions() == null ) {
            return false;
        }
        return super.getGrant().getPermissions().contains(perm);
    }
}
