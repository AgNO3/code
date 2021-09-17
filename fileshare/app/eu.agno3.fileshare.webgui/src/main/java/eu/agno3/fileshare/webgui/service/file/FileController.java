/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file;


import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.model.TreeNode;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.i18n.FileshareMessages;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.file.info.FileRenameBean;
import eu.agno3.fileshare.webgui.service.tree.EntityTreeNode;
import eu.agno3.fileshare.webgui.service.tree.ui.FileTreeBean;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "fileController" )
public class FileController {

    private static final Logger log = Logger.getLogger(FileController.class);

    @Inject
    private FileshareServiceProvider fsp;

    @Inject
    private FileTreeBean fileTree;

    @Inject
    private FileSelectionBean fileSelection;


    /**
     * @param list
     * @param target
     * @param targetGrant
     */
    public void multiMove ( List<TreeNode> list, VFSEntity target, Grant targetGrant ) {

        if ( list.isEmpty() ) {
            return;
        }

        Set<EntityKey> ids = nodesToIds(list);

        if ( multiMoveEntityIds(ids, target, targetGrant) ) {
            this.fileTree.refresh();

            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(
                    FacesMessage.SEVERITY_INFO,
                    FileshareMessages.format("info.multiMoveFmt", ids.size(), FileDisplayBean.getLocalDisplayName(target)), //$NON-NLS-1$
                    StringUtils.EMPTY));

        }
    }


    /**
     * @param list
     * @param target
     * @param targetGrant
     */
    public void multiMoveEntities ( List<VFSEntity> list, VFSEntity target, Grant targetGrant ) {

        if ( list.isEmpty() ) {
            return;
        }

        Set<EntityKey> ids = entitiesToIds(list);
        if ( multiMoveEntityIds(ids, target, targetGrant) ) {
            this.fileTree.refresh();

            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(
                    FacesMessage.SEVERITY_INFO,
                    FileshareMessages.format("info.multiMoveFmt", ids.size(), FileDisplayBean.getLocalDisplayName(target)), //$NON-NLS-1$
                    StringUtils.EMPTY));

        }
    }


    /**
     * 
     * @param list
     * @param target
     * @param targetGrant
     * @return outcome
     */
    public String multiMoveEntitiesDialog ( List<VFSEntity> list, VFSContainerEntity target, Grant targetGrant ) {

        if ( list.isEmpty() || target == null ) {
            return null;
        }

        Set<EntityKey> ids = entitiesToIds(list);
        if ( multiMoveEntityIds(ids, target, targetGrant) ) {
            return DialogContext.closeDialog(true);
        }

        return null;
    }


    /**
     * @param ids
     * @param target
     * @param targetGrant
     */
    private boolean multiMoveEntityIds ( Set<EntityKey> ids, VFSEntity target, Grant targetGrant ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Multi moving %s to %s", ids, target)); //$NON-NLS-1$
        }

        try {
            VFSContainerEntity realTarget;
            if ( target instanceof VFSContainerEntity ) {
                realTarget = (VFSContainerEntity) target;
            }
            else {
                realTarget = this.fsp.getEntityService().getParent(target.getEntityKey());
            }

            this.fsp.getEntityService().move(new ArrayList<>(ids), Collections.EMPTY_MAP, realTarget.getEntityKey());
            this.fileTree.ensureExpanded(realTarget, this.fsp.getEntityService().getParents(realTarget.getEntityKey()), targetGrant);

            return true;
        }
        catch (
            UndeclaredThrowableException |
            FileshareException e ) {
            ExceptionHandler.handleException(e);
        }

        return false;
    }


    /**
     * @param entity
     * @param target
     * @param targetGrant
     */
    public void move ( VFSEntity entity, VFSEntity target, Grant targetGrant ) {

        if ( entity == null || !entity.hasParent() ) {
            return;
        }

        try {
            VFSContainerEntity realTarget;
            if ( target instanceof VFSContainerEntity ) {
                realTarget = (VFSContainerEntity) target;
            }
            else {
                realTarget = this.fsp.getEntityService().getParent(target.getEntityKey());
            }

            this.fsp.getEntityService().move(entity.getEntityKey(), realTarget.getEntityKey());
            this.fileTree.ensureExpanded(realTarget, this.fsp.getEntityService().getParents(target.getEntityKey()), targetGrant);
            this.fileTree.refresh();

            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(
                    FacesMessage.SEVERITY_INFO,
                    FileshareMessages
                            .format("info.singleMoveFmt", FileDisplayBean.getLocalDisplayName(entity), FileDisplayBean.getLocalDisplayName(target)), //$NON-NLS-1$
                    StringUtils.EMPTY));
        }
        catch (
            UndeclaredThrowableException |
            FileshareException e ) {
            ExceptionHandler.handleException(e);
        }
    }


    /**
     * @param entity
     * @return null
     */
    public String delete ( VFSEntity entity ) {
        try {
            this.fsp.getEntityService().delete(entity.getEntityKey());
            this.fileSelection.clear();
            this.fileTree.refresh();

            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(
                    FacesMessage.SEVERITY_INFO,
                    FileshareMessages.format("info.singleDeleteFmt", FileDisplayBean.getLocalDisplayName(entity)), //$NON-NLS-1$
                    StringUtils.EMPTY));
        }
        catch (
            UndeclaredThrowableException |
            FileshareException e ) {
            ExceptionHandler.handleException(e);
        }

        return null;
    }


    /**
     * 
     * @param entity
     * @param renameBean
     * @return null
     */
    public String rename ( VFSEntity entity, FileRenameBean renameBean ) {
        if ( StringUtils.isBlank(renameBean.getNewName()) ) {
            return null;
        }

        if ( renameBean.getNewName().equals(renameBean.getOrigName()) ) {
            return null;
        }

        try {
            this.fsp.getEntityService().rename(entity.getEntityKey(), renameBean.getNewName());
            renameBean.clear();
            this.fileTree.refresh();
        }
        catch (
            UndeclaredThrowableException |
            FileshareException e ) {
            ExceptionHandler.handleException(e);
        }

        return null;
    }


    /**
     * @param entities
     * @return null
     */
    public String multiDelete ( Collection<VFSEntity> entities ) {

        if ( entities.size() == 1 ) {
            return delete(entities.iterator().next());
        }

        try {
            this.fsp.getEntityService().delete(entitiesToIds(entities));
            this.fileSelection.clear();
            this.fileTree.refresh();

            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, FileshareMessages.format("info.multiDeleteFmt", entities.size()), StringUtils.EMPTY)); //$NON-NLS-1$

        }
        catch (
            UndeclaredThrowableException |
            FileshareException e ) {
            ExceptionHandler.handleException(e);
        }

        return null;
    }


    /**
     * @param entities
     * @return null
     */
    public String multiDeleteDialog ( Collection<VFSEntity> entities ) {

        try {
            Set<EntityKey> ids = entitiesToIds(entities);
            this.fsp.getEntityService().delete(ids);
            return DialogContext.closeDialog(true);
        }
        catch (
            UndeclaredThrowableException |
            FileshareException e ) {
            ExceptionHandler.handleException(e);
        }

        return null;
    }


    /**
     * @param entities
     * @return
     */
    private static Set<EntityKey> entitiesToIds ( Collection<VFSEntity> entities ) {
        Set<EntityKey> ids = new HashSet<>();

        if ( entities == null ) {
            return ids;
        }

        for ( VFSEntity e : entities ) {
            ids.add(e.getEntityKey());
        }

        return ids;
    }


    /**
     * @param list
     * @return
     */
    private static Set<EntityKey> nodesToIds ( List<TreeNode> nodes ) {
        Set<EntityKey> ids = new HashSet<>();

        if ( nodes == null ) {
            return ids;
        }

        for ( TreeNode e : nodes ) {
            if ( e instanceof EntityTreeNode ) {
                ids.add( ( (EntityTreeNode) e ).getAttachedObject().getEntityKey());
            }

        }

        return ids;
    }
}
