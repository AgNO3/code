/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.11.2013 by mbechler
 */
package eu.agno3.fileshare.webgui.service.tree;


import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractLazyLoadTreeNode extends DefaultTreeNode {

    /**
     * 
     */
    private static final long serialVersionUID = -4121377913615263656L;

    private boolean leaf = false;

    private Object attachedObject;


    /**
     * 
     */
    public AbstractLazyLoadTreeNode () {
        super();
    }


    /**
     * @param data
     * @param parent
     */
    public AbstractLazyLoadTreeNode ( Object data, TreeNode parent ) {
        super(null, parent);
        this.attachedObject = data;
    }


    /**
     * @param data
     */
    public AbstractLazyLoadTreeNode ( Object data ) {
        super(null);
        this.attachedObject = data;
    }


    /**
     * @param type
     * @param data
     * @param parent
     */
    public AbstractLazyLoadTreeNode ( String type, Object data, TreeNode parent ) {
        super(type, null, parent);
        this.attachedObject = data;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.primefaces.model.DefaultTreeNode#getData()
     */
    @Override
    public Object getData () {
        return this;
    }


    /**
     * @return the data object to this tree node
     */
    public Object getAttachedObject () {
        return this.attachedObject;
    }


    /**
     * @param o
     */
    public void updateAttachedObject ( Object o ) {
        this.attachedObject = o;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.primefaces.model.DefaultTreeNode#isLeaf()
     */
    @Override
    public boolean isLeaf () {
        return this.leaf;
    }


    protected void noChildrenFound () {
        this.leaf = true;
    }


    /**
     * Lazy loads this nodes children
     */
    public abstract void loadChildren ();


    /**
     * 
     */
    public void expandStatic () {
        if ( !this.isExpanded() ) {
            this.loadChildren();
            super.setExpanded(true);
        }
    }
}
