/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.tree.ui;


import org.primefaces.model.TreeNode;

import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.webgui.service.tree.EntityTreeNode;
import eu.agno3.fileshare.webgui.service.tree.FileTreeConstants;
import eu.agno3.fileshare.webgui.service.tree.RootTreeNode;
import eu.agno3.fileshare.webgui.service.tree.ShareSubjectTreeNode;
import eu.agno3.fileshare.webgui.service.tree.SharedTreeNode;


/**
 * @author mbechler
 *
 */
final class TreeExpansionUtil {

    /**
     * 
     */
    private TreeExpansionUtil () {}


    /**
     * @param model2
     * @param fileTreeBean
     * @param g
     * @return
     */
    static TreeNode expandGrant ( TreeNode root, FileTreeBean ft, Grant g ) {
        if ( ft.getSingleLevel() ) {
            return null;
        }
        if ( root instanceof RootTreeNode ) {
            return expandInSharedRoot(root, ft, g);
        }
        else if ( root instanceof SharedTreeNode ) {
            return expandInSharedTree(root, ft, g);
        }
        else if ( root instanceof ShareSubjectTreeNode ) {
            return expandInSubjectShares(root, ft, g);
        }
        else if ( FileTreeConstants.SHARE_ROOT_TYPE.equals(root.getType()) ) {
            ft.ensureNodeExpanded(root);
            return root;
        }
        return null;
    }


    /**
     * @param root
     * @param ft
     * @param g
     * @return
     */
    private static TreeNode expandInSubjectShares ( TreeNode root, FileTreeBean ft, Grant g ) {
        for ( TreeNode c : root.getChildren() ) {
            if ( c instanceof EntityTreeNode && ( (EntityTreeNode) c ).getAttachedObject().equals(g.getEntity()) ) {
                return expandGrant(c, ft, g);
            }
        }
        return null;
    }


    /**
     * @param root
     * @param ft
     * @param g
     * @return
     */
    private static TreeNode expandInSharedTree ( TreeNode root, FileTreeBean ft, Grant g ) {
        ft.ensureNodeExpanded(root);
        for ( TreeNode c : root.getChildren() ) {
            if ( c instanceof ShareSubjectTreeNode
                    && ( (ShareSubjectTreeNode) c ).getAttachedObject().getId().equals(g.getEntity().getOwner().getId()) ) {
                return expandGrant(c, ft, g);
            }
        }
        return null;
    }


    /**
     * @param root
     * @param ft
     * @param g
     * @return
     */
    private static TreeNode expandInSharedRoot ( TreeNode root, FileTreeBean ft, Grant g ) {
        ft.ensureNodeExpanded(root);
        for ( TreeNode n : root.getChildren() ) {
            if ( FileTreeConstants.SHARED_ROOT_TYPE.equals(n.getType()) ) {
                ft.ensureNodeExpanded(n);
                return expandGrant(n, ft, g);
            }
        }
        return null;
    }


    /**
     * @param owner
     * @return
     */
    static TreeNode expandGroupRoot ( Group g, TreeNode root, FileTreeBean ft ) {
        if ( ft.getSingleLevel() ) {
            return null;
        }
        if ( root instanceof RootTreeNode ) {
            return expandInGroupsRoot(g, root, ft);
        }
        else if ( FileTreeConstants.GROUPS_ROOT_TYPE.equals(root.getType()) ) {
            return expandInGroupRoot(g, root, ft);
        }
        else if ( FileTreeConstants.GROUP_ROOT_TYPE.equals(root.getType()) && ( (EntityTreeNode) root ).getAttachedObject().getOwner().equals(g) ) {
            ft.ensureNodeExpanded(root);
            return root;
        }
        return null;
    }


    /**
     * @param g
     * @param root
     * @param ft
     * @return
     */
    private static TreeNode expandInGroupRoot ( Group g, TreeNode root, FileTreeBean ft ) {
        ft.ensureNodeExpanded(root);
        for ( TreeNode c : root.getChildren() ) {
            if ( FileTreeConstants.GROUP_ROOT_TYPE.equals(c.getType()) && ( (EntityTreeNode) c ).getAttachedObject().getOwner().equals(g) ) {
                ft.ensureNodeExpanded(c);
                return c;
            }
        }
        return null;
    }


    /**
     * @param g
     * @param root
     * @param ft
     * @return
     */
    private static TreeNode expandInGroupsRoot ( Group g, TreeNode root, FileTreeBean ft ) {
        ft.ensureNodeExpanded(root);
        for ( TreeNode n : root.getChildren() ) {
            if ( FileTreeConstants.GROUPS_ROOT_TYPE.equals(n.getType()) ) {
                ft.ensureNodeExpanded(n);
                return expandGroupRoot(g, n, ft);
            }
        }
        return null;
    }


    /**
     * 
     */
    static TreeNode expandUserRoot ( TreeNode root, FileTreeBean ft ) {
        if ( ft.getSingleLevel() ) {
            return null;
        }
        if ( root instanceof RootTreeNode ) {
            ft.ensureNodeExpanded(root);
            for ( TreeNode n : root.getChildren() ) {
                if ( FileTreeConstants.USER_ROOT_TYPE.equals(n.getType()) ) {
                    ft.ensureNodeExpanded(n);
                    return n;
                }
            }
        }
        else if ( root != null && FileTreeConstants.USER_ROOT_TYPE.equals(root.getType()) ) {
            ft.ensureNodeExpanded(root);
            return root;
        }
        return null;
    }

}
