/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.11.2013 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.menu;


import org.primefaces.model.TreeNode;


/**
 * @author mbechler
 * 
 */
public interface StructuralObjectTreeNode extends TreeNode {

    /**
     * {@inheritDoc}
     * 
     * @see org.primefaces.model.TreeNode#getData()
     */
    @Override
    StructuralObjectTreeNode getData ();
}
