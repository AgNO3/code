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
import java.util.Objects;

import org.primefaces.model.TreeNode;

import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.PolicyViolation;
import eu.agno3.fileshare.model.query.PeerInfo;
import eu.agno3.fileshare.webgui.service.tree.ui.BrowseTreeProvider;
import eu.agno3.fileshare.webgui.subject.SubjectDisplayBean;


/**
 * @author mbechler
 *
 */
public class PeerSharesTreeNode extends AbstractBrowseTreeNode implements BrowseTreeNode {

    /**
     * 
     */
    private static final long serialVersionUID = 7250972730057489752L;
    private PeerInfo peer;


    /**
     * 
     */
    public PeerSharesTreeNode () {}


    /**
     * @param parent
     * @param peerInfo
     * @param browseTreeProvider
     */
    public PeerSharesTreeNode ( PeersTreeNode parent, PeerInfo peerInfo, BrowseTreeProvider browseTreeProvider ) {
        super(parent, browseTreeProvider);
        this.peer = peerInfo;
    }


    /**
     * @param peerInfo
     * @param browseTreeProvider
     */
    public PeerSharesTreeNode ( PeerInfo peerInfo, BrowseTreeProvider browseTreeProvider ) {
        super(browseTreeProvider);
        this.peer = peerInfo;
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
     * @see eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode#getIcon()
     */
    @Override
    public String getIcon () {
        return SubjectDisplayBean.getPeerIconClass(this.peer);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractLazyLoadTreeNode#getAttachedObject()
     */
    @Override
    public PeerInfo getAttachedObject () {
        return this.peer;
    }


    /**
     * @param subject
     *            the subject to set
     */
    public void setPeer ( PeerInfo subject ) {
        this.peer = subject;
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

        if ( this.peer == null ) {
            return chld;
        }

        if ( this.peer.haveSharedFrom() ) {
            PeerSharedByTreeNode e = new PeerSharedByTreeNode(this, this.peer, this.getTreeProvider());
            e.setSelectable(false);
            chld.add(e);
        }

        if ( this.peer.haveSharedTo() ) {
            PeerSharedToTreeNode e = new PeerSharedToTreeNode(this, this.peer, this.getTreeProvider());
            e.setSelectable(false);
            chld.add(e);
        }

        return chld;
    }


    @Override
    public int hashCode () {
        return Objects.hash(this.peer);
    }


    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof PeerSharesTreeNode ) {
            return Objects.equals(this.peer, ( (PeerSharesTreeNode) obj ).getAttachedObject());
        }
        return false;
    }

}
