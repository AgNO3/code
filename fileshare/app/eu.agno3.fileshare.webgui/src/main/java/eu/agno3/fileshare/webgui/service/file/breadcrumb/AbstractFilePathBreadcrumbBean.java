/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file.breadcrumb;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.jdt.annotation.Nullable;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuModel;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.Group;
import eu.agno3.fileshare.model.Subject;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.i18n.FileshareMessages;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.file.FileDisplayBean;
import eu.agno3.fileshare.webgui.service.file.URLFileSelectionBean;
import eu.agno3.fileshare.webgui.service.share.GrantSelectionBean;
import eu.agno3.fileshare.webgui.subject.CurrentUserBean;
import eu.agno3.fileshare.webgui.subject.SubjectDisplayBean;


/**
 * @author mbechler
 *
 */
public abstract class AbstractFilePathBreadcrumbBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3172866924895450717L;

    @Inject
    private URLFileSelectionBean fileSelection;

    @Inject
    private GrantSelectionBean grantSelection;

    @Inject
    private FileshareServiceProvider fsp;

    @Inject
    private CurrentUserBean currentUser;

    @Inject
    private SubjectDisplayBean subjectDisplay;

    private List<VFSContainerEntity> parentsCache;


    private List<VFSContainerEntity> getParents () {

        VFSEntity selected = this.fileSelection.getSingleSelection();

        if ( selected == null ) {
            return Collections.EMPTY_LIST;
        }

        if ( this.parentsCache == null ) {

            try {
                List<VFSContainerEntity> parents = this.fsp.getEntityService().getParents(selected.getEntityKey());

                @Nullable
                Grant singleSelection = this.grantSelection.getSingleSelection();
                if ( singleSelection != null ) {
                    parents = filterParents(parents, singleSelection.getEntity());
                }

                this.parentsCache = parents;
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
     * @param parents
     * @return
     */
    private static List<VFSContainerEntity> filterParents ( List<VFSContainerEntity> parents, VFSEntity root ) {
        Iterator<VFSContainerEntity> parent = parents.iterator();

        while ( parent.hasNext() ) {
            VFSContainerEntity p = parent.next();
            if ( root.equals(p) ) {
                break;
            }
            parent.remove();
        }

        return parents;
    }


    /**
     * @return breadcrumb menu model
     */
    public MenuModel getModel () {
        DefaultMenuModel model = new DefaultMenuModel();

        Grant g = this.grantSelection.getSingleSelection();

        for ( VFSContainerEntity parent : getParents() ) {
            addParentItem(model, parent, g);
        }

        VFSEntity curContainer = this.fileSelection.getSingleSelection();

        if ( curContainer != null ) {
            addParentItem(model, curContainer, g);

        }

        return truncate(model);
    }


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
            placeholder.setDisabled(true);
            truncated.addElement(placeholder);
            truncated.addElement(elements.get(elements.size() - 2));
            truncated.addElement(elements.get(elements.size() - 1));
            return truncated;
        }

        return model;
    }


    /**
     * @param parent
     * @param g
     * @param dirType
     */
    private void addParentItem ( DefaultMenuModel menu, VFSEntity parent, Grant g ) {

        DefaultMenuItem parentItem = new DefaultMenuItem();

        if ( parent.getLocalName() != null ) {
            parentItem.setValue(parent.getLocalName());
            parentItem.setIcon(FileDisplayBean.getTypeIconExpanded(parent));
        }
        else {
            Subject owner = parent.getOwner();
            if ( owner.equals(this.currentUser.getCurrentUser()) ) {
                parentItem.setIcon("ui-icon-home"); //$NON-NLS-1$
                parentItem.setValue(FileshareMessages.get("tree.name.user-root")); //$NON-NLS-1$
            }
            else if ( owner instanceof Group && this.currentUser.isMemberOf((Group) owner) ) {
                parentItem.setIcon(SubjectDisplayBean.getGroupIconClass());
                parentItem.setValue(this.subjectDisplay.getSubjectDisplayName(owner));
            }
            else {
                parentItem.setIcon(SubjectDisplayBean.getSubjectIconClass(owner));
                parentItem.setValue(this.subjectDisplay.getSubjectDisplayName(owner));
            }
        }

        String outcome = makeOutcome(parent, g);
        if ( outcome != null ) {
            parentItem.setOutcome(outcome);
        }
        else {
            parentItem.setDisabled(true);
        }

        menu.addElement(parentItem);

    }


    /**
     * @param parent
     * @param g
     * @return
     */
    protected abstract String makeOutcome ( VFSEntity parent, Grant g );
}
