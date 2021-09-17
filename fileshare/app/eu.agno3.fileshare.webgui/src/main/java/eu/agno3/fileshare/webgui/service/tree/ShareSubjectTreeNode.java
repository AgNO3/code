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
import eu.agno3.fileshare.model.SubjectInfo;
import eu.agno3.fileshare.webgui.service.tree.ui.BrowseTreeProvider;
import eu.agno3.fileshare.webgui.subject.SubjectDisplayBean;


/**
 * @author mbechler
 *
 */
public class ShareSubjectTreeNode extends AbstractBrowseTreeNode implements BrowseTreeNode {

    /**
     * 
     */
    private static final long serialVersionUID = 7250972730057489752L;
    private SubjectInfo subject;


    /**
     * 
     */
    public ShareSubjectTreeNode () {}


    /**
     * @param parent
     * @param s
     * @param browseTreeProvider
     */
    public ShareSubjectTreeNode ( SharedTreeNode parent, SubjectInfo s, BrowseTreeProvider browseTreeProvider ) {
        super(parent, browseTreeProvider);
        this.subject = s;
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
     * @see org.primefaces.model.DefaultTreeNode#getType()
     */
    @Override
    public String getType () {
        return FileTreeConstants.SHARE_SUBJECT_ROOT_TYPE;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.BrowseTreeNode#getIcon()
     */
    @Override
    public String getIcon () {
        return SubjectDisplayBean.getSubjectIconClass(this.subject);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractLazyLoadTreeNode#getAttachedObject()
     */
    @Override
    public SubjectInfo getAttachedObject () {
        return this.subject;
    }


    /**
     * @param subject
     *            the subject to set
     */
    public void setSubject ( SubjectInfo subject ) {
        this.subject = subject;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.tree.AbstractBrowseTreeNode#fetchChildren()
     */
    @Override
    protected List<? extends TreeNode> fetchChildren () {
        return this.getTreeProvider().getSubjectShareChildren(this.subject, this);
    }

}
