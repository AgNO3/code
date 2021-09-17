/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.tree;


import org.primefaces.model.TreeNode;

import eu.agno3.fileshare.model.GrantPermission;


/**
 * @author mbechler
 *
 */
public interface BrowseTreeNode extends TreeNode {

    /**
     * @return the depth
     */
    int getDepth ();


    /**
     * @param perm
     * @return whether the permission is present
     */
    boolean hasPermission ( GrantPermission perm );


    /**
     * 
     * @return the icon style class
     */
    String getIcon ();


    /**
     * 
     */
    void loadChildren ();


    /**
     * @return whether this is a virtual empty node
     */
    boolean isVirtualEmpty ();

}