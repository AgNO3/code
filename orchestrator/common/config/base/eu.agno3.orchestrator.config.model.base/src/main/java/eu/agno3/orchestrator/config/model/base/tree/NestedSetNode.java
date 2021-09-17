/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.tree;


/**
 * @author mbechler
 * 
 */
public interface NestedSetNode {

    /**
     * 
     * @return the left value of the node
     */
    int getLeft ();


    /**
     * 
     * @return the right value of the node
     */
    int getRight ();


    /**
     * 
     * @return the depth value of the node
     */
    int getDepth ();


    /**
     * @param left
     */
    void setLeft ( int left );


    /**
     * @param right
     */
    void setRight ( int right );


    /**
     * @param depth
     */
    void setDepth ( int depth );
}
