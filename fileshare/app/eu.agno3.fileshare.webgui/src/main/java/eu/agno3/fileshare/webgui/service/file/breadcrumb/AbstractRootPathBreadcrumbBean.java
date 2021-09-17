/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.07.2016 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file.breadcrumb;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuModel;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.SubjectInfo;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.model.query.PeerInfo;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.i18n.FileshareMessages;
import eu.agno3.fileshare.webgui.nav.NavigationController;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.file.FileDisplayBean;
import eu.agno3.fileshare.webgui.service.file.FileNavigationBean;
import eu.agno3.fileshare.webgui.service.tree.FileTreeConstants;
import eu.agno3.fileshare.webgui.service.tree.ui.FileRootSelectionBean;
import eu.agno3.fileshare.webgui.subject.CurrentUserBean;
import eu.agno3.fileshare.webgui.subject.SubjectDisplayBean;


/**
 * @author mbechler
 *
 */
public abstract class AbstractRootPathBreadcrumbBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -32965432688272491L;


    protected abstract VFSEntity getEntity ();


    protected abstract String getType ();


    protected abstract String getRootType ();


    protected abstract Grant getGrant ();

    @Inject
    protected FileshareServiceProvider fsp;
    @Inject
    protected CurrentUserBean currentUser;

    @Inject
    private FileNavigationBean fileNav;
    @Inject
    private NavigationController navCtrl;
    private List<VFSContainerEntity> parentsCache;

    @Inject
    FileRootSelectionBean fileRootSelection;

    @Inject
    private SubjectDisplayBean subjectDisplay;


    /**
     * @param model
     * @return
     */
    private static MenuModel truncate ( DefaultMenuModel model ) {
        List<MenuElement> elements = model.getElements();
        if ( elements.size() > 3 ) {
            DefaultMenuModel truncated = new DefaultMenuModel();
            truncated.addElement(elements.get(0));
            DefaultMenuItem placeholder = new DefaultMenuItem("..."); //$NON-NLS-1$
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for ( int i = 1; i < elements.size() - 2; i++ ) {
                if ( !first ) {
                    sb.append('/');
                }
                first = false;
                DefaultMenuItem mi = (DefaultMenuItem) elements.get(i);
                if ( !StringUtils.isBlank(mi.getTitle()) ) {
                    sb.append(mi.getTitle());
                }
                else {
                    sb.append(mi.getValue());
                }
            }
            placeholder.setTitle(sb.toString());
            placeholder.setHref(StringUtils.EMPTY);
            truncated.addElement(placeholder);
            truncated.addElement(elements.get(elements.size() - 2));
            truncated.addElement(elements.get(elements.size() - 1));
            return truncated;
        }

        return model;
    }


    private List<VFSContainerEntity> getParents () {

        VFSEntity rootContainer = getEntity();

        if ( rootContainer == null ) {
            return Collections.EMPTY_LIST;
        }

        if ( this.parentsCache == null ) {

            try {
                this.parentsCache = this.fsp.getEntityService().getParents(rootContainer.getEntityKey());
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                ExceptionHandler.handleException(e);
                return Collections.EMPTY_LIST;
            }
        }

        return this.parentsCache;

    }


    /**
     * @return breadcrumb menu model
     */
    public MenuModel getModel () {
        DefaultMenuModel model = new DefaultMenuModel();

        if ( !addRoots(model) ) {
            return removeLast(model);
        }

        for ( VFSContainerEntity parent : getParents() ) {
            addParentItem(model, parent, getGrant(), getRootType(), FileTreeConstants.DIR_TYPE, false);
        }

        VFSEntity curContainer = getEntity();
        String selectedType = getType();
        if ( curContainer != null && !StringUtils.isBlank(selectedType) ) {
            addParentItem(model, curContainer, getGrant(), getRootType(), selectedType, true);
        }

        return truncate(model);
    }


    /**
     * @return the current parent item
     */
    public MenuElement getParentItem () {
        MenuModel m = getModel();
        List<MenuElement> elements = m.getElements();
        if ( elements.size() < 2 ) {
            return null;
        }

        return elements.get(elements.size() - 2);
    }


    /**
     * @return the current parent item's outcome
     */
    public String getParentOutcome () {
        MenuElement el = getParentItem();
        if ( ! ( el instanceof DefaultMenuItem ) ) {
            return null;
        }

        DefaultMenuItem it = (DefaultMenuItem) el;
        return it.getOutcome();
    }


    /**
     * @param model
     * @return
     */
    private static MenuModel removeLast ( DefaultMenuModel model ) {
        if ( model.getElements().size() > 1 ) {
            model.getElements().remove(model.getElements().size() - 1);
        }
        return model;
    }


    /**
     * @param model
     */
    private boolean addRoots ( DefaultMenuModel model ) {
        if ( this.currentUser.getCurrentUser() == null ) {
            return true;
        }

        if ( !addStaticRootEntries(model) ) {
            return false;
        }

        addDynamicIntermediaries(model);
        return true;
    }


    protected void addDynamicIntermediaries ( DefaultMenuModel model ) {

    }


    protected boolean addStaticRootEntries ( DefaultMenuModel model ) {
        return true;
    }


    /**
     * @param model
     */
    protected void addSubjectShareRoot ( DefaultMenuModel model, SubjectInfo owner ) {
        DefaultMenuItem element = new DefaultMenuItem(makeSubjectDisplayName(owner), SubjectDisplayBean.getSubjectIconClass(owner));
        element.setOutcome(FileNavigationBean.getShareSubjectTarget(owner, FileTreeConstants.SHARE_SUBJECT_ROOT_TYPE));
        model.addElement(element);
    }


    /**
     * @param model
     */
    protected void addGroupRoot ( DefaultMenuModel model, VFSContainerEntity groupRoot ) {
        if ( groupRoot != null ) {
            DefaultMenuItem element = new DefaultMenuItem(
                makeSubjectDisplayName(groupRoot.getOwner()),
                FileDisplayBean.getTypeIconExpanded(groupRoot));
            element.setOutcome(
                this.fileNav.getDirectoryTarget(groupRoot, null, FileTreeConstants.GROUP_ROOT_TYPE, FileTreeConstants.GROUP_ROOT_TYPE));
            model.addElement(element);
        }
    }


    /**
     * @param model
     */
    protected void addFavoritesRoot ( DefaultMenuModel model ) {
        DefaultMenuItem element = new DefaultMenuItem(FileshareMessages.get(FileshareMessages.FAVORITES_ROOT_DISPLAY_NAME), "ui-icon-star"); //$NON-NLS-1$
        element.setOutcome(this.navCtrl.favorites());
        model.addElement(element);
    }


    /**
     * @param model
     */
    protected static void addMyDocuments ( DefaultMenuModel model ) {
        DefaultMenuItem element = new DefaultMenuItem(FileshareMessages.get(FileshareMessages.USER_ROOT_DISPLAY_NAME), "ui-icon-home"); //$NON-NLS-1$
        element.setOutcome(FileNavigationBean.getTypeTarget(FileTreeConstants.USER_ROOT_TYPE));
        model.addElement(element);
    }


    /**
     * @param model
     */
    protected static void addSharedRoot ( DefaultMenuModel model ) {
        DefaultMenuItem element = new DefaultMenuItem(FileshareMessages.get(FileshareMessages.SHARED_DISPLAY_NAME), "ui-icon-share"); //$NON-NLS-1$
        element.setOutcome(FileNavigationBean.getTypeTarget(FileTreeConstants.SHARED_ROOT_TYPE));
        model.addElement(element);
    }


    /**
     * @param model
     */
    protected static void addAllSharesRoot ( DefaultMenuModel model ) {
        DefaultMenuItem element = new DefaultMenuItem(FileshareMessages.get(FileshareMessages.ALL_SHARED_DISPLAY_NAME), "ui-icon-share"); //$NON-NLS-1$
        element.setOutcome(FileNavigationBean.getTypeTarget(FileTreeConstants.ALL_SHARED_ROOT));
        model.addElement(element);
    }


    /**
     * @param model
     */
    protected static void addPeersRoot ( DefaultMenuModel model ) {
        DefaultMenuItem element = new DefaultMenuItem(FileshareMessages.get(FileshareMessages.PEERS_ROOT_DISPLAY_NAME), "ui-icon-person"); //$NON-NLS-1$
        element.setOutcome(FileNavigationBean.getTypeTarget(FileTreeConstants.PEERS_ROOT_TYPE));
        model.addElement(element);
    }


    /**
     * @param model
     */
    protected void addPeerRoot ( DefaultMenuModel model, PeerInfo pi ) {
        DefaultMenuItem element = new DefaultMenuItem(this.subjectDisplay.getPeerDisplayName(pi), SubjectDisplayBean.getPeerIconClass(pi));
        element.setOutcome(FileNavigationBean.getPeerRootTarget(pi));
        model.addElement(element);
    }


    /**
     * @param model
     */
    protected static void addGroupsRoot ( DefaultMenuModel model ) {
        DefaultMenuItem element = new DefaultMenuItem(FileshareMessages.get(FileshareMessages.GROUPS_ROOT_DISPLAY_NAME), "ui-icon-person"); //$NON-NLS-1$
        element.setOutcome(FileNavigationBean.getTypeTarget(FileTreeConstants.GROUPS_ROOT_TYPE));
        model.addElement(element);
    }


    /**
     * @param model
     * @param query
     */
    protected static void addSearchResultRoot ( DefaultMenuModel model, String query, int offset ) {
        DefaultMenuItem element = new DefaultMenuItem(FileshareMessages.get(FileshareMessages.SEARCH_RESULT_DISPLAY_NAME), "ui-icon-search"); //$NON-NLS-1$
        element.setOutcome(FileNavigationBean.getSearchResultTarget(query, offset));
        model.addElement(element);
    }


    /**
     * @param parent
     * @return
     */
    private String makeSubjectDisplayName ( SubjectInfo subj ) {
        return this.subjectDisplay.getSubjectDisplayName(subj);
    }


    /**
     * @param parent
     * @param current
     * @param dirType
     */
    private void addParentItem ( DefaultMenuModel menu, VFSEntity parent, Grant g, String rootType, String type, boolean current ) {

        String localName = parent.getLocalName();
        if ( localName == null ) {
            // implicit nodes are handled by addParents
            return;
        }

        DefaultMenuItem parentItem = new DefaultMenuItem();
        parentItem.setTitle(localName);

        localName = FileDisplayBean.smartTruncateName(localName, current ? 25 : 18);

        parentItem.setValue(localName);
        parentItem.setIcon(getIcon(current)); // $NON-NLS-1$
        if ( parent instanceof VFSContainerEntity ) {
            parentItem.setOutcome(this.fileNav.getDirectoryTarget((VFSContainerEntity) parent, g, type, rootType));
        }
        else {
            parentItem.setOutcome(this.fileNav.getFileTarget((VFSFileEntity) parent, g, type, rootType));
        }
        menu.addElement(parentItem);
    }


    /**
     * @return the icon
     */
    protected String getIcon ( boolean current ) {
        return "ui-icon-folder-open"; //$NON-NLS-1$
    }


    /**
     * 
     */
    public AbstractRootPathBreadcrumbBean () {
        super();
    }

}