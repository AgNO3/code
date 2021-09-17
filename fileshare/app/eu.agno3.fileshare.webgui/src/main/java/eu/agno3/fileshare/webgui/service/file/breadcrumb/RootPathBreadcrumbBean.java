/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file.breadcrumb;


import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;

import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.query.SubjectPeerInfo;
import eu.agno3.fileshare.webgui.i18n.FileshareMessages;
import eu.agno3.fileshare.webgui.service.tree.FileTreeConstants;


/**
 * @author mbechler
 *
 */
@Named ( "rootPathBreadcrumbBean" )
@ViewScoped
public class RootPathBreadcrumbBean extends AbstractRootPathBreadcrumbBean {

    /**
     * 
     */
    private static final long serialVersionUID = 8048357379286197390L;


    @Override
    protected VFSEntity getEntity () {
        return this.fileRootSelection.getRootContainer();
    }


    @Override
    protected String getType () {
        return this.fileRootSelection.getSelectedType();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.file.breadcrumb.AbstractRootPathBreadcrumbBean#getRootType()
     */
    @Override
    protected String getRootType () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.file.breadcrumb.AbstractRootPathBreadcrumbBean#getGrant()
     */
    @Override
    protected Grant getGrant () {
        return this.fileRootSelection.getRootGrant();
    }


    /**
     * @param model
     */
    @Override
    protected boolean addStaticRootEntries ( DefaultMenuModel model ) {
        if ( this.currentUser.isAuthenticated() && !this.fileRootSelection.isSingleLevel() || this.fileRootSelection.getUserRoot() == null ) {
            DefaultMenuItem root = new DefaultMenuItem(
                FileshareMessages.get("tree.type.root"), //$NON-NLS-1$
                "ui-icon-home"); //$NON-NLS-1$
            root.setOutcome("/index.xhtml"); //$NON-NLS-1$
            model.addElement(root);
        }

        if ( StringUtils.isBlank(getType()) ) {
            return false;
        }

        switch ( getType() ) {
        case FileTreeConstants.GROUPS_ROOT_TYPE:
            addGroupsRoot(model);
            return false;
        case FileTreeConstants.SHARED_ROOT_TYPE:
            addSharedRoot(model);
            return false;
        case FileTreeConstants.SHARE_SUBJECT_ROOT_TYPE:
            addSharedRoot(model);
            addSubjectShareRoot(model, this.fileRootSelection.getSelectedSubjectInfo());
            return false;
        default:
            return true;
        }

    }


    /**
     * @param model
     */
    @Override
    protected void addDynamicIntermediaries ( DefaultMenuModel model ) {
        if ( FileTreeConstants.SHARE_ROOT_TYPE.equals(getType())
                || FileTreeConstants.SHARE_ROOT_TYPE.equals(this.fileRootSelection.getSelectedRootType()) ) {

            Grant rootGrant = this.fileRootSelection.getRootGrant();
            if ( this.fileRootSelection.isSingleLevel() ) {
                addPeersRoot(model);
                if ( rootGrant != null ) {
                    SubjectPeerInfo spi = new SubjectPeerInfo();
                    spi.setSubject(rootGrant.getEntity().getOwner());
                    addPeerRoot(model, spi);
                }
            }
            else {
                addSharedRoot(model);

                if ( rootGrant != null ) {
                    addSubjectShareRoot(model, rootGrant.getEntity().getOwner());
                }
            }
        }

        if ( FileTreeConstants.USER_ROOT_TYPE.equals(getType())
                || FileTreeConstants.USER_ROOT_TYPE.equals(this.fileRootSelection.getSelectedRootType()) ) {
            addMyDocuments(model);
        }

        if ( FileTreeConstants.GROUP_ROOT_TYPE.equals(getType())
                || FileTreeConstants.GROUP_ROOT_TYPE.equals(this.fileRootSelection.getSelectedRootType()) ) {
            addGroupsRoot(model);
            addGroupRoot(model, this.fileRootSelection.getSubjectRootContainer());
        }

        if ( FileTreeConstants.FAVORITES_ROOT_TYPE.equals(getType())
                || FileTreeConstants.FAVORITES_ROOT_TYPE.equals(this.fileRootSelection.getSelectedRootType()) ) {
            addFavoritesRoot(model);
        }

        if ( FileTreeConstants.ALL_SHARED_ROOT.equals(getType())
                || FileTreeConstants.ALL_SHARED_ROOT.equals(this.fileRootSelection.getSelectedRootType()) ) {
            addAllSharesRoot(model);
        }

        if ( FileTreeConstants.PEERS_ROOT_TYPE.equals(getType())
                || FileTreeConstants.PEERS_ROOT_TYPE.equals(this.fileRootSelection.getSelectedRootType()) ) {
            addPeersRoot(model);
        }

        if ( FileTreeConstants.PEER_ROOT_TYPE.equals(getType())
                || FileTreeConstants.PEER_ROOT_TYPE.equals(this.fileRootSelection.getSelectedRootType()) ) {
            addPeerRoot(model, this.fileRootSelection.getPeerInfo());
        }

        if ( FileTreeConstants.SEARCH_RESULT_TYPE.equals(getType())
                || FileTreeConstants.SEARCH_RESULT_TYPE.equals(this.fileRootSelection.getSelectedRootType()) ) {
            addSearchResultRoot(model, this.fileRootSelection.getQuery(), this.fileRootSelection.getOffset());
        }

    }
}
