/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file.breadcrumb;


import java.lang.reflect.UndeclaredThrowableException;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.jdt.annotation.Nullable;
import org.primefaces.model.menu.DefaultMenuModel;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.model.query.SubjectPeerInfo;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.file.FileDisplayBean;
import eu.agno3.fileshare.webgui.service.file.URLFileSelectionBean;
import eu.agno3.fileshare.webgui.service.share.GrantSelectionBean;
import eu.agno3.fileshare.webgui.service.tree.FileTreeConstants;


/**
 * @author mbechler
 *
 */
@Named ( "filePathBreadcrumbBean" )
@ViewScoped
public class FilePathBreadcrumbBean extends AbstractRootPathBreadcrumbBean {

    /**
     * 
     */
    private static final long serialVersionUID = 8048357379286197390L;

    @Inject
    private URLFileSelectionBean fileSelection;

    @Inject
    private GrantSelectionBean grantSelection;

    private VFSContainerEntity cachedGroupRoot;


    @Override
    protected VFSEntity getEntity () {
        return this.fileSelection.getSingleSelection();
    }


    @Override
    protected String getType () {
        return "file"; //$NON-NLS-1$
    }


    @Override
    protected String getRootType () {
        VFSEntity selection = this.fileSelection.getSingleSelection();
        if ( selection == null ) {
            return null;
        }

        if ( this.currentUser.isAuthenticated() ) {
            Subject owner = selection.getOwner();
            if ( this.currentUser.getCurrentUser().equals(owner) ) {
                return FileTreeConstants.USER_ROOT_TYPE;
            }
            else if ( ( owner instanceof Group ) && this.currentUser.isMemberOf((Group) owner) ) {
                return FileTreeConstants.GROUP_ROOT_TYPE;
            }
        }

        Grant g = getGrant();
        if ( g == null ) {
            return null;
        }

        if ( g.getEntity() instanceof VFSFileEntity ) {
            return FileTreeConstants.SHARE_ROOT_FILE_TYPE;
        }
        return FileTreeConstants.SHARE_ROOT_TYPE;
    }


    @Override
    protected Grant getGrant () {
        return this.grantSelection.getSingleSelection();
    }


    private VFSContainerEntity getGroupRoot ( Group g ) {
        if ( g == null ) {
            return null;
        }
        if ( this.cachedGroupRoot == null || !g.equals(this.cachedGroupRoot.getOwner()) ) {
            try {
                this.cachedGroupRoot = this.fsp.getBrowseService().getOrCreateGroupRoot(g.getId());
            }
            catch (
                FileshareException |
                UndeclaredThrowableException ex ) {
                ExceptionHandler.handleException(ex);
                return null;
            }
        }

        return this.cachedGroupRoot;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.service.file.breadcrumb.AbstractRootPathBreadcrumbBean#getIcon(boolean)
     */
    @Override
    protected String getIcon ( boolean current ) {
        if ( current ) {
            return FileDisplayBean.getTypeIconExpanded(this.fileSelection.getSingleSelection());
        }
        return super.getIcon(current);
    }


    /**
     * @param model
     */
    @Override
    protected boolean addStaticRootEntries ( DefaultMenuModel model ) {
        @Nullable
        VFSEntity selection = this.fileSelection.getSingleSelection();
        if ( selection == null ) {
            return true;
        }

        if ( this.currentUser.isAuthenticated() ) {
            Subject owner = selection.getOwner();
            if ( this.currentUser.getCurrentUser().equals(owner) ) {
                addMyDocuments(model);
                return true;
            }
            else if ( ( owner instanceof Group ) && this.currentUser.isMemberOf((Group) owner) ) {
                addGroupsRoot(model);
                addGroupRoot(model, getGroupRoot((Group) owner));
                return true;
            }

            Grant rootGrant = this.grantSelection.getSingleSelection();
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
        return true;
    }
}
