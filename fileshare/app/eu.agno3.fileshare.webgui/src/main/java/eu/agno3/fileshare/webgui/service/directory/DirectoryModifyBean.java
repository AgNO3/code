/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.directory;


import java.lang.reflect.UndeclaredThrowableException;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.file.URLFileSelectionBean;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "directoryModifyBean" )
public class DirectoryModifyBean {

    @Inject
    private URLFileSelectionBean fileSelection;

    @Inject
    private FileshareServiceProvider fsp;


    /**
     * @param ev
     */
    public void updateSendNotifications ( AjaxBehaviorEvent ev ) {
        VFSEntity entity = this.fileSelection.getSingleSelection();
        if ( ! ( entity instanceof VFSContainerEntity ) ) {
            return;
        }
        VFSContainerEntity dir = (VFSContainerEntity) entity;
        try {
            this.fsp.getDirectoryService().setSendNotifications(dir.getEntityKey(), dir.getSendNotifications());
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }

        this.fileSelection.refreshSelection();
    }


    /**
     * @param ev
     * 
     */
    public void updateAllowFileOverwrite ( AjaxBehaviorEvent ev ) {
        VFSEntity entity = this.fileSelection.getSingleSelection();
        if ( ! ( entity instanceof VFSContainerEntity ) ) {
            return;
        }
        VFSContainerEntity dir = (VFSContainerEntity) entity;
        try {
            this.fsp.getDirectoryService().setAllowFileOverwrite(dir.getEntityKey(), dir.getAllowFileOverwrite());
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
        this.fileSelection.refreshSelection();
    }
}
