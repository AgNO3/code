/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.06.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.chunkupload;


import java.io.IOException;
import java.io.Serializable;
import java.net.URLDecoder;
import java.nio.channels.Channels;
import java.util.Collections;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.Nullable;
import org.primefaces.event.FileUploadEvent;

import eu.agno3.fileshare.exceptions.ChunkUploadCanceledException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.UploadException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.FileEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.model.query.ChunkInfo;
import eu.agno3.fileshare.model.query.ChunkedUploadInfo;
import eu.agno3.fileshare.service.ChunkContext;
import eu.agno3.fileshare.service.UploadState;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.i18n.FileshareMessages;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.file.URLFileSelectionBean;
import eu.agno3.fileshare.webgui.service.file.UserTokenBean;
import eu.agno3.runtime.jsf.view.stacking.DialogContext;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "resumableUploadsBean" )
public class ResumableUploadsBean implements Serializable {

    /**
     * 
     */
    private static final Logger log = Logger.getLogger(ResumableUploadsBean.class);
    private static final long serialVersionUID = -5630966434149007257L;

    @Inject
    private FileshareServiceProvider fsp;

    @Inject
    private ChunkUploadBean cub;

    private List<ChunkedUploadInfo> model;

    @Inject
    private URLFileSelectionBean fileSelection;

    @Inject
    private UserTokenBean userToken;

    private String selectedRef;

    private ChunkedUploadInfo selectedInfo;


    /**
     * @return the selectedRef
     */
    public String getSelectedRef () {
        return this.selectedRef;
    }


    /**
     * @param selectedRef
     *            the selectedRef to set
     */
    public void setSelectedRef ( String selectedRef ) {
        this.selectedRef = selectedRef;
    }


    /**
     * 
     * @return chunk infos
     */
    public List<ChunkedUploadInfo> getModel () {
        if ( this.model == null ) {
            this.model = makeModel();
        }
        return this.model;
    }


    /**
     * @return
     */
    private List<ChunkedUploadInfo> makeModel () {
        try {
            return this.fsp.getChunkUploadService().getIncompleteChunkInfo();
        }
        catch ( Exception e ) {
            ExceptionHandler.handleException(e);
            return Collections.EMPTY_LIST;
        }

    }


    /**
     * @return the selected chunked upload info
     */
    public ChunkedUploadInfo getSelectedInfo () {

        if ( this.fileSelection.getSingleSelectionId() == null || this.selectedRef == null ) {
            return null;
        }

        if ( this.selectedInfo == null ) {
            try {
                this.selectedInfo = this.fsp.getChunkUploadService().getChunkInfo(this.selectedRef);
            }
            catch ( Exception e ) {
                ExceptionHandler.handleException(e);
            }
        }

        return this.selectedInfo;
    }


    /**
     * @return the selected upload info chunk size
     */
    public Long getChunkSize () {
        ChunkedUploadInfo info = getSelectedInfo();
        if ( info == null ) {
            return null;
        }
        return info.getChunkSize();
    }


    /**
     * 
     * @return a comma seperated list of missing chunk numbers
     */
    public String getMissingChunks () {
        ChunkedUploadInfo info = getSelectedInfo();
        if ( info == null ) {
            return StringUtils.EMPTY;
        }

        StringBuilder sb = new StringBuilder();

        boolean first = true;

        for ( ChunkInfo chunkInfo : info.getMissingChunks() ) {
            if ( first ) {
                first = false;
            }
            else {
                sb.append(',');
            }
            sb.append(String.valueOf(chunkInfo.getIndex()));
        }

        return sb.toString();
    }


    /**
     * 
     * @return an regex filter to match the file
     */
    public String getExtensionFilter () {
        String fileName = this.getSelectedInfo().getLocalName();
        return "/^" + //$NON-NLS-1$
                escape(fileName) + "$/"; //$NON-NLS-1$
    }


    /**
     * @param fileName
     * @return
     */
    private static String escape ( String fileName ) {
        return StringUtils.replaceEach(fileName, new String[] {
            ".", //$NON-NLS-1$
            "[", //$NON-NLS-1$
            "]", //$NON-NLS-1$
            "(", //$NON-NLS-1$
            ")", //$NON-NLS-1$
        }, new String[] {
            "\\.", //$NON-NLS-1$
            "\\[", //$NON-NLS-1$
            "\\]", //$NON-NLS-1$
            "\\(", //$NON-NLS-1$
            "\\)", //$NON-NLS-1$
        });
    }


    /**
     * 
     * @param itm
     * @return progress formatted as percent
     */
    public static String getProgressPercent ( ChunkedUploadInfo itm ) {
        if ( itm.getTotalSize() != null ) {
            return String.format("%.1f %%", (double) itm.getCompleteSize() / itm.getTotalSize() * 100.0); //$NON-NLS-1$
        }

        return String.format("%.1f %%", (double) itm.getCompleteSize() //$NON-NLS-1$
                / ( itm.getChunkSize() * ( itm.getCompleteChunks().size() + itm.getMissingChunks().size() ) ) * 100.0);
    }


    /**
     * 
     * @param ev
     */
    public void onChunkUpload ( FileUploadEvent ev ) {
        HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpServletResponse resp = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        @Nullable
        EntityKey target = this.fileSelection.getSingleSelectionId();
        String token = this.getSelectedRef();
        String chunkSizeString = req.getHeader("X-Upload-ChunkSize"); //$NON-NLS-1$

        if ( this.fileSelection.getSingleSelection() == null || StringUtils.isBlank(chunkSizeString) || token == null ) {
            log.debug("Missing parameters"); //$NON-NLS-1$
            return;
        }

        long chunkSize = Long.parseLong(chunkSizeString);

        String range = req.getHeader("Content-Range"); //$NON-NLS-1$
        try {
            ChunkContext cc;
            FileEntity fe = new FileEntity();
            fe.setLocalName(URLDecoder.decode(ev.getFile().getFileName(), "UTF-8")); //$NON-NLS-1$
            fe.setContentType(ev.getFile().getContentType());
            if ( !StringUtils.isBlank(range) ) {
                log.debug("Using range header"); //$NON-NLS-1$
                cc = onRangeUpload(target, token, range, chunkSize, ev);
            }
            else {
                log.debug("Using chunk header"); //$NON-NLS-1$
                cc = onLegacyUpload(ev, req, target, token, chunkSize);
            }

            if ( cc.isComplete() ) {
                log.debug("Completed"); //$NON-NLS-1$
                completedChunkedUpload(req, resp, fe, cc);
            }
        }
        catch ( ChunkUploadCanceledException e ) {
            FacesContext.getCurrentInstance().getExternalContext().setResponseStatus(HttpServletResponse.SC_NOT_FOUND);
            ExceptionHandler.handleException(e);
        }
        catch ( Exception e ) {
            ExceptionHandler.handleException(e);
        }

    }


    /**
     * @param ev
     * @param externalContext
     * @param target
     * @param token
     * @param chunkSizeString
     * @param chunkSize
     * @return
     * @throws FileshareException
     * @throws IOException
     */
    private ChunkContext onLegacyUpload ( FileUploadEvent ev, HttpServletRequest req, EntityKey target, String token, long chunkSize )
            throws FileshareException, IOException {
        String chunkIdxString = req.getHeader("X-Upload-ChunkIndex"); //$NON-NLS-1$

        if ( StringUtils.isBlank(chunkIdxString) ) {
            throw new UploadException("Invalid request"); //$NON-NLS-1$
        }
        int chunkIdx;
        try {
            chunkIdx = Integer.parseInt(chunkIdxString);
        }
        catch ( NumberFormatException e ) {
            throw new UploadException("Illegal request size", e); //$NON-NLS-1$
        }

        if ( log.isTraceEnabled() ) {
            log.trace("Have chunk upload " + chunkIdx); //$NON-NLS-1$
        }

        ChunkContext cc = this.fsp.getChunkUploadService().getChunkContext(token);

        if ( chunkSize != cc.getChunkState().getChunkSize() ) {
            throw new UploadException("Incompatible chunk size"); //$NON-NLS-1$
        }

        cc.storeChunk(chunkIdx, Channels.newChannel(ev.getFile().getInputstream()), ev.getFile().getSize());
        return cc;
    }


    /**
     * @param target
     * @param range
     * @param range
     * @param ev
     * @return
     * @throws FileshareException
     */
    private ChunkContext onRangeUpload ( @Nullable EntityKey target, String token, String range, long chunkSize, FileUploadEvent ev )
            throws FileshareException {
        int rangeSep = range.indexOf('-', 6);
        int sizeSep = range.indexOf('/', 6);
        if ( !range.startsWith("bytes ") || rangeSep < 0 || sizeSep < 0 ) { //$NON-NLS-1$
            log.warn("Invalid range header: " + range); //$NON-NLS-1$
            throw new UploadException("Invalid content range"); //$NON-NLS-1$
        }

        try {
            long rangeStart = Long.parseLong(range.substring(6, rangeSep));
            long rangeEnd = Long.parseLong(range.substring(rangeSep + 1, sizeSep));
            long rangeLen = rangeEnd - rangeStart;
            if ( rangeLen <= 0 ) {
                throw new FileshareException("Invalid range"); //$NON-NLS-1$
            }
            long size = Long.parseLong(range.substring(sizeSep + 1));

            if ( log.isDebugEnabled() ) {
                log.debug("Transfer id " + token); //$NON-NLS-1$
                log.debug(String.format("%d to %d (total: %d)", rangeStart, rangeEnd, size)); //$NON-NLS-1$
            }

            ChunkContext cc = this.fsp.getChunkUploadService().getChunkContext(token);

            if ( cc.getChunkState().getChunkSize() != chunkSize ) {
                throw new UploadException("Chunk size does not match"); //$NON-NLS-1$
            }

            int chunkIdx = (int) ( rangeStart / cc.getChunkState().getChunkSize() );
            int chunkOff = (int) ( rangeStart % cc.getChunkState().getChunkSize() );
            if ( chunkOff != 0 ) {
                throw new UploadException("Data must start a chunk boundary, offset is " + chunkOff); //$NON-NLS-1$
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Transfer id " + token); //$NON-NLS-1$
                log.debug(String.format("Chunk %d of %d (off: %d)", chunkIdx, cc.getChunkState().getNumChunks(), chunkOff)); //$NON-NLS-1$
            }
            cc.storeChunk(chunkIdx, Channels.newChannel(ev.getFile().getInputstream()), ev.getFile().getSize());
            return cc;
        }
        catch ( IllegalArgumentException e ) {
            log.warn("Invalid range header: " + range, e); //$NON-NLS-1$
            throw new UploadException("Invalid content range"); //$NON-NLS-1$
        }
        catch ( IOException e ) {
            throw new UploadException("Chunked upload failed", e); //$NON-NLS-1$
        }
    }


    /**
     * @return dialog close on succes, null otherwise
     */
    public String retryCreation () {

        try {
            ChunkContext chunkContext = this.fsp.getChunkUploadService().getChunkContext(this.getSelectedRef());

            if ( !chunkContext.isComplete() ) {
                return null;
            }

            FileEntity fe = new FileEntity();
            fe.setLocalName(chunkContext.getMetadata().getLocalName());
            fe.setContentType(chunkContext.getMetadata().getContentType());
            if ( chunkContext.getChunkState().getTotalSize() == null ) {
                fe.setFileSize(chunkContext.getStoredSize());
            }
            else {
                fe.setFileSize(chunkContext.getChunkState().getTotalSize());
            }
            completedChunkedUpload(
                (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest(),
                (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse(),
                fe,
                chunkContext);

            return DialogContext.closeDialog(true);
        }
        catch ( Exception e ) {
            ExceptionHandler.handleException(e);
            return null;
        }
    }


    /**
     * @param externalContext
     * @param fe
     * @param chunkContext
     * @throws FileshareException
     * @throws IOException
     */
    private void completedChunkedUpload ( HttpServletRequest req, HttpServletResponse resp, VFSFileEntity fe, ChunkContext chunkContext )
            throws FileshareException, IOException {

        @Nullable
        VFSEntity selection = this.fileSelection.getSingleSelection();

        if ( selection == null ) {
            return;
        }

        this.fsp.getUploadService().retry(selection.getEntityKey(), fe, chunkContext, req, resp);
        this.selectedInfo.setMissingChunks(Collections.EMPTY_LIST);
        this.selectedInfo.setCompleteSize(this.selectedInfo.getTotalSize());
        this.cub.refreshNow();
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, FileshareMessages.get("resume-complete"), StringUtils.EMPTY)); //$NON-NLS-1$
    }


    /**
     * 
     * @param itm
     * @return null
     */
    public String discard ( ChunkedUploadInfo itm ) {
        try {
            this.fsp.getChunkUploadService().removeChunkContext(itm.getReference());
            this.refresh();
            this.cub.refreshNow();
        }
        catch ( Exception e ) {
            ExceptionHandler.handleException(e);
        }
        return null;
    }


    /**
     * 
     * @param st
     * @return translated status
     */
    public String translateStatus ( UploadState st ) {
        if ( st == null ) {
            return null;
        }
        return FileshareMessages.get("actions.resumeUploads.state." + st.name()); //$NON-NLS-1$
    }


    /**
     * 
     */
    public void refresh () {
        this.model = makeModel();
    }


    /**
     * @param ui
     * @return dialog outcome
     */
    public String reownUpload ( ChunkedUploadInfo ui ) {
        if ( ui.getExternalSource() ) {
            if ( ui.getState() != UploadState.FAILED ) {
                return null;
            }
            if ( log.isDebugEnabled() ) {
                log.debug("Not reowning " + ui.getReference()); //$NON-NLS-1$
            }
            return String.format(
                "/actions/resumeUpload.xhtml?target=%s%s&reference=%s", //$NON-NLS-1$
                ui.getTarget().getEntityKey(),
                this.userToken.getTokenQueryArg(),
                ui.getReference());
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Reown " + ui.getReference()); //$NON-NLS-1$
        }
        ChunkContext reownedContext;
        try {
            reownedContext = this.fsp.getChunkUploadService().reownContext(ui.getReference());
            this.selectedRef = reownedContext.getMetadata().getReference();
        }
        catch (
            IOException |
            FileshareException e ) {
            ExceptionHandler.handleException(e);
            return null;
        }
        return String.format(
            "/actions/resumeUpload.xhtml?target=%s%s&reference=%s", //$NON-NLS-1$
            reownedContext.getMetadata().getTarget(),
            this.userToken.getTokenQueryArg(),
            reownedContext.getMetadata().getReference());
    }


    /**
     * 
     */
    public void refreshInfo () {
        try {
            this.selectedInfo = this.fsp.getChunkUploadService().getChunkInfo(this.selectedRef);
        }
        catch ( Exception e ) {
            ExceptionHandler.handleException(e);
        }
    }
}
