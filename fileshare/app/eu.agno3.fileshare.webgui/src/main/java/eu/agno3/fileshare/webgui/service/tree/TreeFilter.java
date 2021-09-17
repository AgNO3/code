/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.05.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.tree;


import java.util.function.Predicate;

import org.primefaces.model.TreeNode;


/**
 * @author mbechler
 *
 */
public interface TreeFilter {

    /**
     * @param abstractBrowseTreeNode
     * @return a predicate for the given parent
     */
    Predicate<TreeNode> getPredicate ( AbstractBrowseTreeNode abstractBrowseTreeNode );

}
