/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file;


import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.tree.FileTreeConstants;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "dragDropController" )
public class DragDropController {

    private static final Logger log = Logger.getLogger(DragDropController.class);

    private static final Set<String> MOVE_DRAG_SOURCES = new HashSet<>();

    private static final Set<String> MOVE_DROP_TARGETS = new HashSet<>();

    static {
        MOVE_DRAG_SOURCES.add(FileTreeConstants.FILE_TYPE);
        MOVE_DRAG_SOURCES.add(FileTreeConstants.DIR_TYPE);

        MOVE_DROP_TARGETS.add(FileTreeConstants.USER_ROOT_TYPE);
        MOVE_DROP_TARGETS.add(FileTreeConstants.GROUP_ROOT_TYPE);
        MOVE_DROP_TARGETS.add(FileTreeConstants.SHARE_ROOT_TYPE);
        MOVE_DROP_TARGETS.add(FileTreeConstants.FILE_TYPE);
        MOVE_DROP_TARGETS.add(FileTreeConstants.DIR_TYPE);
        MOVE_DROP_TARGETS.add(FileTreeConstants.EMPTY_DIR_TYPE);
    }

    @Inject
    private FileshareServiceProvider fsp;

    @Inject
    private FileSelectionBean selectionBean;

    @Inject
    private FileController fileController;


    /**
     * 
     */
    public void onDrop () {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String dragType = externalContext.getRequestParameterMap().get("dragType"); //$NON-NLS-1$
        String dropType = externalContext.getRequestParameterMap().get("dropType"); //$NON-NLS-1$
        String dropId = externalContext.getRequestParameterMap().get("dropId"); //$NON-NLS-1$
        String dragId = externalContext.getRequestParameterMap().get("dragId"); //$NON-NLS-1$
        String dragGrantId = externalContext.getRequestParameterMap().get("dragGrantId"); //$NON-NLS-1$
        String dropGrantId = externalContext.getRequestParameterMap().get("dropGrantId"); //$NON-NLS-1$

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Dragdrop: %s -> %s", dragType, dropType)); //$NON-NLS-1$
        }

        if ( MOVE_DRAG_SOURCES.contains(dragType) && MOVE_DROP_TARGETS.contains(dropType) ) {
            moveDragDrop(dropId, dragId, dragGrantId, dropGrantId);
        }

    }


    /**
     * @param dropId
     * @param dragId
     * @param dropGrantId
     * @param dragGrantId
     */
    private void moveDragDrop ( String dropId, String dragId, String dragGrantId, String dropGrantId ) {
        if ( StringUtils.isBlank(dragId) || StringUtils.isBlank(dropId) ) {
            return;
        }

        EntityKey draggedKey = this.fsp.getEntityService().parseEntityKey(dragId);
        EntityKey droppedKey = this.fsp.getEntityService().parseEntityKey(dropId);

        try {
            VFSEntity dragged = this.fsp.getEntityService().getEntity(draggedKey);
            VFSEntity dropped = this.fsp.getEntityService().getEntity(droppedKey);

            Grant dragGrant = null;
            Grant dropGrant = null;

            if ( !StringUtils.isBlank(dragGrantId) ) {
                dragGrant = this.fsp.getShareService().getGrant(UUID.fromString(dragGrantId));
            }

            if ( !StringUtils.isBlank(dropGrantId) ) {
                dropGrant = this.fsp.getShareService().getGrant(UUID.fromString(dropGrantId));
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Selected " + Arrays.toString(this.selectionBean.getSelection())); //$NON-NLS-1$
                log.debug("Dragged " + dragged); //$NON-NLS-1$
                log.debug("Drag grant " + dragGrant); //$NON-NLS-1$
                log.debug("Dropped to " + dropped); //$NON-NLS-1$
                log.debug("Drop grant " + dropGrant); //$NON-NLS-1$
            }

            if ( this.selectionBean.isMultiSelection() ) {
                this.fileController.multiMove(Arrays.asList(this.selectionBean.getSelection()), dropped, dropGrant);
            }
            else {
                this.fileController.move(dragged, dropped, dropGrant);
            }
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
    }

}
