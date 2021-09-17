/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.06.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.chunkupload;


import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.i18n.FileshareMessages;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;


/**
 * @author mbechler
 *
 */
@SessionScoped
@Named ( "chunkUploadBean" )
public class ChunkUploadBean implements Serializable {

    /**
     * 
     */
    private static final Logger log = Logger.getLogger(ChunkUploadBean.class);

    private static final long serialVersionUID = -4418640783361726158L;
    private static final int MIN_UPDATE_INTERVAL = 10;
    private static final int HEURISTIC_UPDATE_INTERVAL = 1;

    private transient DateTime nextUpdate;
    private transient int numIncompleteUploads;
    private transient int oldIncompleteUploadNum;

    @Inject
    private FileshareServiceProvider fsp;


    /**
     * 
     * @return the users number of incomplete uploads
     */
    public int getNumIncompleteUploads () {
        loadIncompleteUploads();
        return this.numIncompleteUploads;
    }


    /**
     * @return whether the user has resumable uploads
     * 
     */
    public boolean getHaveIncompleteUploads () {
        loadIncompleteUploads();
        return this.numIncompleteUploads > 0;
    }


    /**
     * 
     */
    private synchronized void loadIncompleteUploads () {

        if ( this.nextUpdate != null && this.nextUpdate.isAfterNow() ) {
            return;
        }

        try {
            this.oldIncompleteUploadNum = this.numIncompleteUploads;
            this.numIncompleteUploads = this.fsp.getChunkUploadService().getNumIncompleteContexts();
            if ( log.isTraceEnabled() ) {
                log.trace("Have incomplete uploads " + this.numIncompleteUploads); //$NON-NLS-1$
            }

            if ( this.numIncompleteUploads > 0 && this.oldIncompleteUploadNum != this.numIncompleteUploads ) {
                FacesContext currentInstance = FacesContext.getCurrentInstance();
                log.debug("New incomplete uploads"); //$NON-NLS-1$
                if ( currentInstance.getCurrentPhaseId().getOrdinal() < PhaseId.RENDER_RESPONSE.getOrdinal() ) {
                    currentInstance.addMessage(
                        null,
                        new FacesMessage(
                            FacesMessage.SEVERITY_INFO,
                            FileshareMessages.format("incomplete-uploads-fmt", this.numIncompleteUploads), //$NON-NLS-1$
                            StringUtils.EMPTY));
                }
            }

            this.nextUpdate = DateTime.now().plusSeconds(MIN_UPDATE_INTERVAL);
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }
    }


    /**
     * 
     */
    public void refresh () {
        this.nextUpdate = DateTime.now().plusSeconds(HEURISTIC_UPDATE_INTERVAL);
    }


    /**
     * 
     */
    public void refreshNow () {
        this.nextUpdate = null;
        this.loadIncompleteUploads();
    }
}
