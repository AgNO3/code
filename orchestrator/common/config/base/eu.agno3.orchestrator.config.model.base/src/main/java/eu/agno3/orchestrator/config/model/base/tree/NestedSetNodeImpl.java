/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.tree;


import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;


/**
 * @author mbechler
 * 
 */
@Embeddable
public class NestedSetNodeImpl implements NestedSetNode, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1748389033440095783L;
    private int left;
    private int right;
    private int depth;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.base.tree.NestedSetNode#getLeft()
     */
    @Override
    @Column ( name = "l", nullable = false )
    @Basic ( optional = false )
    public int getLeft () {
        return this.left;
    }


    /**
     * @param left
     *            the left to set
     */
    @Override
    public void setLeft ( int left ) {
        this.left = left;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.base.tree.NestedSetNode#getRight()
     */
    @Override
    @Column ( name = "r", nullable = false )
    @Basic ( optional = false )
    public int getRight () {
        return this.right;
    }


    /**
     * @param right
     *            the right to set
     */
    @Override
    public void setRight ( int right ) {
        this.right = right;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.base.tree.NestedSetNode#getDepth()
     */
    @Override
    @Column ( name = "d", nullable = false )
    @Basic ( optional = false )
    public int getDepth () {
        return this.depth;
    }


    /**
     * @param depth
     *            the depth to set
     */
    @Override
    public void setDepth ( int depth ) {
        this.depth = depth;
    }

}
