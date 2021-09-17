/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Apr 22, 2016 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.runtime.webdav.server.DAVTreeNode;


/**
 * @author mbechler
 *
 */
public class RemovedDAVNode extends AbstractVirtualDAVNode {

    DAVTreeNode<EntityKey> parent;
    private boolean collection;


    /**
     * @param parent
     * @param name
     * @param collection
     * @param layout
     * 
     */
    public RemovedDAVNode ( DAVTreeNode<EntityKey> parent, String name, boolean collection, DAVLayout layout ) {
        super(name, parent.getId(), null, layout);
        this.parent = parent;
        this.collection = collection;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#isCollection()
     */
    @Override
    public boolean isCollection () {
        return this.collection;
    }


    /**
     * @return the parent
     */
    public DAVTreeNode<EntityKey> getParent () {
        return this.parent;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.webdav.server.DAVTreeNode#getAbsolutePath()
     */
    @Override
    public String getAbsolutePath () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webdav.internal.AbstractVirtualDAVNode#getInode()
     */
    @Override
    protected byte[] getInode () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof RemovedDAVNode ) {
            return this.getPathName().equals( ( (RemovedDAVNode) obj ).getPathName())
                    && this.getParent().equals( ( (RemovedDAVNode) obj ).getParent());
        }
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return this.parent.hashCode() + 3 * this.getParent().hashCode();
    }

}
