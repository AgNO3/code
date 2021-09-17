/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.03.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file.info;


import java.lang.reflect.UndeclaredThrowableException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.file.URLFileSelectionBean;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "entityInfoController" )
public class EntityInfoController {

    private static final Logger log = Logger.getLogger(EntityInfoController.class);

    @Inject
    private FileshareServiceProvider fsp;

    @Inject
    private URLFileSelectionBean fileSelection;


    /**
     * @param entity
     * @return outcome
     */
    public String updateMimeType ( VFSFileEntity entity ) {

        try {
            this.fsp.getEntityService().setMimeType(entity.getEntityKey(), entity.getContentType());
            this.fileSelection.refreshSelection();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
        return null;
    }


    /**
     * @param fileId
     * @param newName
     */
    public void renameEntity ( VFSEntity fileId, String newName ) {

        try {
            this.fsp.getEntityService().rename(fileId.getEntityKey(), newName);
            this.fileSelection.refreshSelection();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
    }


    /**
     * @param entity
     * @param expires
     * @return null
     */
    public String updateExpirationDate ( VFSFileEntity entity, DateTime expires ) {
        try {
            log.debug("Updating expiration date " + expires); //$NON-NLS-1$
            this.fsp.getEntityService().setExpirationDate(entity.getEntityKey(), expires);
            this.fileSelection.refreshSelection();
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
        return null;
    }
}
