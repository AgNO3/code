/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.tree;


import java.util.List;

import org.primefaces.model.TreeNode;

import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.query.PeerInfo;
import eu.agno3.fileshare.webgui.service.tree.ui.BrowseTreeProvider;


/**
 * @author mbechler
 *
 */
public class PeerSharedToTreeNode extends AbstractBrowseTreeNode implements LabelNode {

    /**
     * 
     */
    private static final long serialVersionUID = 7250972730057489752L;
    private PeerInfo peer;


    /**
     * 
     */
    public PeerSharedToTreeNode () {}


    /**
     * @param parent
     * @param s
     * @param browseTreeProvider
     */
    public PeerSharedToTreeNode ( AbstractBrowseTreeNode parent, PeerInfo s, BrowseTreeProvider browseTreeProvider ) {
        super(parent, browseTreeProvider);
        this.peer = s;
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
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode#isLabelNode()
     */
    @Override
    public boolean isLabelNode () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode#getIcon()
     */
    @Override
    public String getIcon () {
        return "ui-icon-arrowthick-1-e"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.DefaultTreeNode#getType()
     */
    @Override
    public String getType () {
        return FileTreeConstants.PEER_SHARED_TO_TYPE;
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
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode#fetchChildren()
     */
    @Override
    protected List<? extends TreeNode> fetchChildren () {
        return this.getTreeProvider().getSharedToChildren(this);
    }

}
