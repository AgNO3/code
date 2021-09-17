/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.tree;


import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.primefaces.model.TreeNode;

import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.webgui.service.tree.ui.BrowseTreeProvider;


/**
 * @author mbechler
 *
 */
public class EntityTreeNode extends AbstractBrowseTreeNode implements BrowseTreeNode {

    static final Logger log = Logger.getLogger(EntityTreeNode.class);

    /**
     * 
     */
    private static final long serialVersionUID = -2617324705606132973L;

    private Grant grant;
    private String overrideRootType;
    private Set<GrantPermission> overridePerms;


    /**
     * 
     */
    public EntityTreeNode () {}


    /**
     * @param treeProvider
     * 
     */
    public EntityTreeNode ( BrowseTreeProvider treeProvider ) {
        super(treeProvider);
    }


    /**
     * @param obj
     * @param parent
     * @param treeProvider
     */
    public EntityTreeNode ( VFSEntity obj, BrowseTreeNode parent, BrowseTreeProvider treeProvider ) {
        super(obj, parent, treeProvider);
    }


    /**
     * @return the overrideRootType
     */
    public String getOverrideRootType () {
        return this.overrideRootType;
    }


    /**
     * @param overrideRootType
     *            the overrideRootType to set
     */
    public void setOverrideRootType ( String overrideRootType ) {
        this.overrideRootType = overrideRootType;
    }


    /**
     * @param overridePerms
     *            the overridePerms to set
     */
    public void setOverridePerms ( Set<GrantPermission> overridePerms ) {
        this.overridePerms = overridePerms;
    }


    @Override
    public boolean hasPermission ( GrantPermission perm ) {
        if ( this.overridePerms != null ) {
            return this.overridePerms.contains(perm);
        }

        if ( this.getParent() == null ) {
            return false;
        }

        return this.getParent().hasPermission(perm);
    }


    /**
     * @return an applicable grant
     */
    public Grant getGrant () {
        if ( this.grant != null ) {
            return this.grant;
        }

        if ( this.getParent() instanceof EntityTreeNode ) {
            return ( (EntityTreeNode) this.getParent() ).getGrant();
        }

        return null;
    }


    /**
     * @param obj
     */
    public void setGrant ( Grant obj ) {
        this.grant = obj;
    }


    /**
     * @return the root container type
     */
    public String getRootType () {

        if ( this.overrideRootType != null ) {
            return this.overrideRootType;
        }

        String type = super.getType();

        if ( type != null ) {
            return type;
        }

        if ( this.getParent() instanceof EntityTreeNode ) {
            return ( (EntityTreeNode) this.getParent() ).getRootType();
        }

        if ( this.getParent() != null ) {
            return this.getParent().getType();
        }

        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.DefaultTreeNode#getType()
     */
    @Override
    public String getType () {

        String type = super.getType();

        if ( type != null ) {
            return type;
        }

        if ( this.getAttachedObject() instanceof VFSFileEntity ) {
            return "file"; //$NON-NLS-1$
        }
        else if ( this.getAttachedObject() instanceof VFSContainerEntity ) {
            return "dir"; //$NON-NLS-1$
        }

        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode#getIcon()
     */
    @Override
    public String getIcon () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractLazyLoadTreeNode#getAttachedObject()
     */
    @Override
    public VFSEntity getAttachedObject () {
        return (VFSEntity) super.getAttachedObject();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode#loadChildren()
     */
    @Override
    public void loadChildren () {
        if ( ! ( this.getAttachedObject() instanceof VFSContainerEntity ) ) {
            return;
        }
        super.loadChildren();
    }


    /**
     * @return
     */
    @Override
    protected List<? extends TreeNode> fetchChildren () {
        List<BrowseTreeNode> contentEntityChildren = this.getTreeProvider().getEntityChildren(this, this.getAttachedObject());
        if ( contentEntityChildren.isEmpty() ) {
            contentEntityChildren.add(new EmptyDirectoryTreeNode(this, getTreeProvider()));
        }
        return contentEntityChildren;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.primefaces.model.DefaultTreeNode#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof EntityTreeNode ) {
            EntityTreeNode otherObj = (EntityTreeNode) obj;

            if ( this.getAttachedObject() == null || otherObj.getAttachedObject() == null ) {
                return false;
            }

            return otherObj.getAttachedObject().equals(this.getAttachedObject());
        }

        return super.equals(obj);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.primefaces.model.DefaultTreeNode#hashCode()
     */
    @Override
    public int hashCode () {
        if ( this.getAttachedObject() != null ) {
            return this.getAttachedObject().hashCode();
        }
        return super.hashCode();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.DefaultTreeNode#toString()
     */
    @Override
    public String toString () {
        return String.format("%s: %s", this.getType(), this.getAttachedObject()); //$NON-NLS-1$
    }
}
