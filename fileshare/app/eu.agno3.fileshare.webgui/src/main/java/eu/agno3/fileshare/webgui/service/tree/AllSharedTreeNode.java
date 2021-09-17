/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.tree;


import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.primefaces.model.TreeNode;

import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.PolicyViolation;
import eu.agno3.fileshare.model.query.PeerInfo;
import eu.agno3.fileshare.webgui.service.tree.ui.BrowseTreeProvider;


/**
 * @author mbechler
 *
 */
public class AllSharedTreeNode extends AbstractBrowseTreeNode implements BrowseTreeNode {

    /**
     * 
     */
    private static final long serialVersionUID = 7250972730057489752L;


    /**
     * 
     */
    public AllSharedTreeNode () {}


    /**
     * @param parent
     * @param treeProvider
     */
    public AllSharedTreeNode ( RootTreeNode parent, BrowseTreeProvider treeProvider ) {
        super(parent, treeProvider);
    }


    /**
     * @param adapterFactory
     */
    public AllSharedTreeNode ( BrowseTreeProvider adapterFactory ) {
        super(adapterFactory);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode#hasPermission(eu.agno3.fileshare.model.GrantPermission)
     */
    @Override
    public boolean hasPermission ( GrantPermission perm ) {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode#getHiddenFilter()
     */
    @Override
    public int getHiddenFilter () {
        int hidden = 0;

        for ( TreeNode n : getChildren() ) {
            if ( n instanceof AbstractBrowseTreeNode ) {
                AbstractBrowseTreeNode bn = (AbstractBrowseTreeNode) n;
                hidden += bn.getHiddenFilter();
            }
        }
        return hidden;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode#getHiddenFlagged()
     */
    @Override
    public int getHiddenFlagged () {
        int hidden = 0;

        for ( TreeNode n : getChildren() ) {
            if ( n instanceof AbstractBrowseTreeNode ) {
                AbstractBrowseTreeNode bn = (AbstractBrowseTreeNode) n;
                hidden += bn.getHiddenFlagged();
            }
        }
        return hidden;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode#getHiddenPolicy()
     */
    @Override
    public int getHiddenPolicy () {
        int hidden = 0;

        for ( TreeNode n : getChildren() ) {
            if ( n instanceof AbstractBrowseTreeNode ) {
                AbstractBrowseTreeNode bn = (AbstractBrowseTreeNode) n;
                hidden += bn.getHiddenPolicy();
            }
        }
        return hidden;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode#getHiddenTotal()
     */
    @Override
    public int getHiddenTotal () {
        int hidden = 0;

        for ( TreeNode n : getChildren() ) {
            if ( n instanceof AbstractBrowseTreeNode ) {
                AbstractBrowseTreeNode bn = (AbstractBrowseTreeNode) n;
                hidden += bn.getHiddenTotal();
            }
        }
        return hidden;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode#getPolicyViolations()
     */
    @Override
    public Collection<PolicyViolation> getPolicyViolations () {
        List<PolicyViolation> violations = new LinkedList<>();

        for ( TreeNode n : getChildren() ) {
            if ( n instanceof AbstractBrowseTreeNode ) {
                AbstractBrowseTreeNode bn = (AbstractBrowseTreeNode) n;
                violations.addAll(bn.getPolicyViolations());
            }
        }

        return violations;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.DefaultTreeNode#getType()
     */
    @Override
    public String getType () {
        return FileTreeConstants.ALL_SHARED_ROOT;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode#getIcon()
     */
    @Override
    public String getIcon () {
        return "ui-icon-transferthick-e-w"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractLazyLoadTreeNode#getAttachedObject()
     */
    @Override
    public PeerInfo getAttachedObject () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode#loadChildren()
     */
    @Override
    public void loadChildren () {
        super.loadChildren();
        for ( TreeNode n : this.getChildren() ) {
            if ( n instanceof AbstractLazyLoadTreeNode ) {
                ( (AbstractLazyLoadTreeNode) n ).expandStatic();
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode#fetchChildren()
     */
    @Override
    protected List<? extends TreeNode> fetchChildren () {
        List<BrowseTreeNode> chld = new ArrayList<>();

        PeerSharedByTreeNode by = new PeerSharedByTreeNode(this, null, this.getTreeProvider());
        by.setSelectable(false);
        chld.add(by);

        PeerSharedToTreeNode to = new PeerSharedToTreeNode(this, null, this.getTreeProvider());
        to.setSelectable(false);
        chld.add(to);

        return chld;
    }

}
