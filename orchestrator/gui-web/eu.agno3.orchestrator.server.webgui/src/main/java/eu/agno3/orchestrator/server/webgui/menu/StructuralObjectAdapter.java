/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.01.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.menu;


import java.util.List;

import org.apache.log4j.Logger;
import org.primefaces.model.TreeNode;
import org.primefaces.model.TreeNodeChildren;

import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;


/**
 * @author mbechler
 * @param <T>
 *            object type
 * 
 */
public class StructuralObjectAdapter <T extends StructuralObject> extends AbstractLazyLoadTreeNode implements StructuralObjectTreeNode {

    /**
     * 
     */
    private static final long serialVersionUID = 2027624901218149317L;

    private static final Logger log = Logger.getLogger(StructuralObjectAdapter.class);

    protected MenuItemAdapterFactoryBean adapterFactory;


    /**
     * 
     */
    public StructuralObjectAdapter () {
        super();
    }


    /**
     * @param data
     * @param parent
     */
    public StructuralObjectAdapter ( T data, TreeNode parent ) {
        super(data, parent);
    }


    /**
     * @param data
     */
    public StructuralObjectAdapter ( T data ) {
        super(data);
    }


    /**
     * @param type
     * @param data
     * @param parent
     */
    public StructuralObjectAdapter ( String type, T data, TreeNode parent ) {
        super(type, data, parent);
    }


    /**
     * @param obj
     * @param parent
     * @param menuItemAdapterFactory
     */
    public StructuralObjectAdapter ( T obj, TreeNode parent, MenuItemAdapterFactoryBean menuItemAdapterFactory ) {
        super(obj, parent);
        this.adapterFactory = menuItemAdapterFactory;
    }


    /**
     * @return the adapterFactory
     */
    protected MenuItemAdapterFactoryBean getAdapterFactory () {
        return this.adapterFactory;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.primefaces.model.DefaultTreeNode#getType()
     */
    @Override
    public String getType () {
        return this.getAttachedObject().getType().name().toLowerCase();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.menu.AbstractLazyLoadTreeNode#getAttachedObject()
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public T getAttachedObject () {
        return (T) super.getAttachedObject();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.menu.AbstractLazyLoadTreeNode#getData()
     */
    @Override
    public StructuralObjectTreeNode getData () {
        return this;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.webgui.menu.AbstractLazyLoadTreeNode#isLeaf()
     */
    @Override
    public boolean isLeaf () {
        if ( this.getAttachedObject() instanceof ServiceStructuralObject || this.getAttachedObject() instanceof InstanceStructuralObject ) {
            return true;
        }

        return super.isLeaf();
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.menu.AbstractLazyLoadTreeNode#loadChildren()
     */
    @Override
    public void loadChildren () {
        if ( log.isDebugEnabled() ) {
            log.debug("Loading children of " + this); //$NON-NLS-1$
        }

        @SuppressWarnings ( "unchecked" )
        List<TreeNode> children = (List<TreeNode>) this.fetchChildren();
        TreeNodeChildren childData = new TreeNodeChildren(this);
        childData.addAll(children);

        updateRowKeys(childData);

        if ( children.isEmpty() ) {
            this.noChildrenFound();
        }

        this.setChildren(childData);
    }


    /**
     * @param childData
     */
    private void updateRowKeys ( List<TreeNode> childData ) {
        int childCount = childData.size();
        if ( childCount > 0 ) {
            for ( int i = 0; i < childCount; i++ ) {
                TreeNode childNode = childData.get(i);
                childNode.setRowKey(makeRowKey(this, i));
                updateRowKeys(childNode.getChildren());
            }
        }
    }


    /**
     * @param node
     * @param i
     * @return
     */
    private static String makeRowKey ( TreeNode node, int i ) {
        return ( node.getParent() == null ) ? String.valueOf(i) : node.getRowKey() + '_' + i;
    }


    /**
     * @return
     */
    protected List<? extends TreeNode> fetchChildren () {
        return this.adapterFactory.getChildren(this, this.getAttachedObject());
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.primefaces.model.DefaultTreeNode#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof StructuralObjectAdapter<?> ) {
            return ( (StructuralObjectAdapter<?>) obj ).getAttachedObject().equals(this.getAttachedObject());
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
        return this.getAttachedObject().hashCode();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.DefaultTreeNode#toString()
     */
    @Override
    public String toString () {
        return this.getAttachedObject().toString();
    }
}