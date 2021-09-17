/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.01.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.service.file;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URLDecoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.eclipse.jdt.annotation.Nullable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.fixed.component.fileupload.HttpUploadedFile;
import org.primefaces.model.UploadedFile;

import eu.agno3.fileshare.exceptions.DisallowedMimeTypeException;
import eu.agno3.fileshare.exceptions.EntityNotFoundException;
import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.InvalidEntityException;
import eu.agno3.fileshare.exceptions.PolicyNotFulfilledException;
import eu.agno3.fileshare.exceptions.QuotaExceededException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.FileEntity;
import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.PolicyViolation;
import eu.agno3.fileshare.model.VFSContainerEntity;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.webgui.exceptions.ExceptionHandler;
import eu.agno3.fileshare.webgui.i18n.FileshareMessages;
import eu.agno3.fileshare.webgui.policy.PolicyBean;
import eu.agno3.fileshare.webgui.service.FileshareServiceProvider;
import eu.agno3.fileshare.webgui.service.label.LabelUtils;
import eu.agno3.fileshare.webgui.service.tree.ui.FileTreeBean;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
@Named ( "fileUploadBean" )
@ApplicationScoped
public class FileUploadBean {

    private static final Logger log = Logger.getLogger(FileUploadBean.class);

    @Inject
    private FileshareServiceProvider fsp;

    @Inject
    private FileTreeBean treeBean;

    @Inject
    private LabelUtils labelUtils;

    @Inject
    private PolicyBean policyBean;

    @Inject
    private URLFileSelectionBean fileSelection;


    /**
     * @param ev
     */
    public void handleFileUpload ( FileUploadEvent ev ) {
        handleFileUploadInternal(ev, true);
    }


    /**
     * @param ev
     */
    public void handleReplaceFileUpload ( FileUploadEvent ev ) {
        handleFileUploadInternal(ev, false);
    }


    /**
     * @param ev
     */
    void handleFileUploadInternal ( FileUploadEvent ev, boolean refresh ) {
        HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpServletResponse resp = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();

        log.debug("Recieving file upload"); //$NON-NLS-1$

        if ( log.isTraceEnabled() ) {
            Enumeration<String> hdrs = req.getHeaderNames();
            while ( hdrs.hasMoreElements() ) {
                String hdr = hdrs.nextElement();
                List<String> vals = Collections.list(req.getHeaders(hdr));
                log.trace(hdr + ": " + StringUtils.join(vals, ',')); //$NON-NLS-1$
            }
        }

        try {
            Object handledUploadException = req.getAttribute("uploaded-file-exception"); //$NON-NLS-1$
            if ( handledUploadException instanceof FileshareException ) {
                throw (FileshareException) handledUploadException;
            }
            else if ( handledUploadException instanceof IOException ) {
                throw (IOException) handledUploadException;
            }

            Object handledUpload = req.getAttribute("uploaded-file"); //$NON-NLS-1$
            if ( handledUpload instanceof VFSFileEntity ) {
                VFSFileEntity newFile = (VFSFileEntity) handledUpload;
                updateView(newFile, newFile);
            }
            else {
                doHandleUpload(ev, refresh, req, resp);
            }

        }
        catch ( PolicyNotFulfilledException e ) {
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, this.policyBean.getPolicyViolationMessage(e.getViolation()), StringUtils.EMPTY));
        }
        catch (
            IOException |
            UndeclaredThrowableException |
            FileshareException e ) {
            log.warn("Exception in upload", e); //$NON-NLS-1$
            ExceptionHandler.handleException(e);
        }
    }


    /**
     * @param ev
     * @param refresh
     * @param req
     * @param resp
     * @throws FileshareException
     * @throws InvalidEntityException
     * @throws IOException
     */
    void doHandleUpload ( FileUploadEvent ev, boolean refresh, HttpServletRequest req, HttpServletResponse resp )
            throws FileshareException, InvalidEntityException, IOException {
        VFSFileEntity newFile;
        VFSEntity target;
        try ( ReadableByteChannel inputData = Channels.newChannel(ev.getFile().getInputstream()) ) {
            boolean headers = ev.getFile() instanceof HttpUploadedFile;
            UploadedFile f = ev.getFile();
            Grant g = getTargetGrant(headers);
            String replaceFileParam = getReplaceFileParam(headers);
            boolean replaceing = !StringUtils.isBlank(replaceFileParam) && Boolean.parseBoolean(replaceFileParam);
            if ( replaceing ) {
                target = getTargetFile(headers);
                if ( ! ( target instanceof VFSFileEntity ) ) {
                    throw new InvalidEntityException("Not a file"); //$NON-NLS-1$
                }

                newFile = handleReplaceFile((VFSFileEntity) target, g, f, req, resp, inputData, refresh);
                if ( newFile == null ) {
                    return;
                }
            }
            else {
                target = getTargetDirectory(headers);
                boolean replaceFiles = false;
                String replaceFilesParam = getReplaceFilesConfirmed(headers);
                if ( !StringUtils.isBlank(replaceFilesParam) ) {
                    replaceFiles = Boolean.parseBoolean(replaceFilesParam);
                }

                newFile = handleDirectoryUpload((VFSContainerEntity) target, g, f, replaceFiles, req, resp, inputData, refresh);
                if ( newFile == null ) {
                    return;
                }
            }
        }
        updateView(newFile, target);
    }


    /**
     * @param newFile
     * @param target
     * @throws FileshareException
     */
    void updateView ( VFSFileEntity newFile, VFSEntity target ) throws FileshareException {
        VFSContainerEntity targetDir;
        if ( target != null ) {
            if ( target instanceof VFSContainerEntity ) {
                targetDir = (VFSContainerEntity) target;
            }
            else {
                targetDir = this.fsp.getEntityService().getParent(target.getEntityKey());
            }

            this.labelUtils.addWarningIfHigherThanContainer(newFile, targetDir);

            @Nullable
            EntityKey selected = this.fileSelection.getSingleSelectionId();
            if ( selected != null && selected.equals(target.getEntityKey()) ) {
                log.debug("File is selected, refreshing"); //$NON-NLS-1$
                this.fileSelection.refreshSelection();
            }
        }
    }


    /**
     * @param headers
     * @return
     */
    private static String getReplaceFilesConfirmed ( boolean headers ) {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        if ( !headers ) {
            return externalContext.getRequestParameterMap().get("upload_replaceFilesConfirmed"); //$NON-NLS-1$
        }

        return externalContext.getRequestHeaderMap().get("X-Upload-Replace-Files-Confirmed"); //$NON-NLS-1$
    }


    private static String getReplaceFileParam ( boolean headers ) {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        if ( !headers ) {
            return externalContext.getRequestParameterMap().get("upload_replaceFile"); //$NON-NLS-1$
        }

        return externalContext.getRequestHeaderMap().get("X-Upload-Replace-File"); //$NON-NLS-1$
    }


    /**
     * @param ev
     */
    public void checkUploadCommand ( ActionEvent ev ) {

        String label = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("upload_targetLabel"); //$NON-NLS-1$
        String sizeString = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("upload_fileSize"); //$NON-NLS-1$
        String mimeTypeString = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("upload_mimeType"); //$NON-NLS-1$
        String conflictSizeString = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("upload_conflictFileSize"); //$NON-NLS-1$
        String replaceFileParam = getReplaceFileParam(false);

        RequestContext.getCurrentInstance().getCallbackParams().clear();

        PolicyViolation violation = this.policyBean.getViolationForString(label);
        if ( violation != null ) {
            RequestContext.getCurrentInstance().addCallbackParam("valid", false); //$NON-NLS-1$
            RequestContext.getCurrentInstance().addCallbackParam("policyFulfilled", false); //$NON-NLS-1$
            String violationMessage = this.policyBean.getPolicyViolationMessage(violation);
            if ( log.isDebugEnabled() ) {
                log.debug("Policy violation: " + violationMessage); //$NON-NLS-1$
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, violationMessage, StringUtils.EMPTY));
            RequestContext.getCurrentInstance().addCallbackParam("policyMessage", violationMessage); //$NON-NLS-1$
            return;
        }

        if ( !StringUtils.isEmpty(mimeTypeString) ) {
            try {
                this.fsp.getConfigurationProvider().getMimeTypePolicyConfiguration().checkMimeType(mimeTypeString, true);
            }
            catch ( DisallowedMimeTypeException e ) {
                log.debug("Invalid mime type", e); //$NON-NLS-1$
                RequestContext.getCurrentInstance().addCallbackParam("valid", false); //$NON-NLS-1$
                RequestContext.getCurrentInstance().addCallbackParam("policyFulfilled", false); //$NON-NLS-1$
                String violationMessage = FileshareMessages.format("policy.mimeType.disallowed", mimeTypeString); //$NON-NLS-1$
                FacesContext.getCurrentInstance()
                        .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, violationMessage, StringUtils.EMPTY));
                RequestContext.getCurrentInstance().addCallbackParam("policyMessage", violationMessage); //$NON-NLS-1$
                return;
            }
        }

        RequestContext.getCurrentInstance().addCallbackParam("policyFulfilled", true); //$NON-NLS-1$

        VFSEntity target = null;
        try {
            if ( Boolean.parseBoolean(replaceFileParam) ) {
                target = getTargetFile(false);
            }
            else {
                target = getTargetDirectory(false);
            }

            this.fsp.getEntityService().checkWriteAccess(target.getEntityKey());
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            log.debug("Failed to check write access", e); //$NON-NLS-1$
            ExceptionHandler.handleException(e);
            RequestContext.getCurrentInstance().addCallbackParam("valid", false); //$NON-NLS-1$
            return;
        }

        long uploadSize = -1;
        if ( !StringUtils.isBlank(sizeString) ) {
            uploadSize = Long.parseLong(sizeString);
            long addedSize = uploadSize;
            String replaceFilesParam = getReplaceFilesConfirmed(false);

            try {

                if ( target instanceof FileEntity ) {
                    addedSize -= ( (FileEntity) target ).getFileSize();
                }
                else if ( !StringUtils.isBlank(replaceFilesParam) && Boolean.parseBoolean(replaceFilesParam) ) {
                    // indefinite overwrite
                    if ( log.isDebugEnabled() ) {
                        log.debug("Indefinite overwrite with conflict size " + conflictSizeString); //$NON-NLS-1$
                    }
                    addedSize -= StringUtils.isBlank(conflictSizeString) ? 0 : Long.parseLong(conflictSizeString);
                }

                log.debug("Checking quota"); //$NON-NLS-1$

                if ( addedSize > 0 ) {
                    this.fsp.getQuotaService().checkFreeSpace(target, addedSize, uploadSize);
                }

                checkTemporaryQuota(uploadSize);
            }
            catch (
                FileshareException |
                UndeclaredThrowableException e ) {
                log.debug("Failed to check quota", e); //$NON-NLS-1$
                ExceptionHandler.handleException(e);
                RequestContext.getCurrentInstance().addCallbackParam("valid", false); //$NON-NLS-1$
                return;
            }
        }

        RequestContext.getCurrentInstance().addCallbackParam("valid", true); //$NON-NLS-1$
        RequestContext.getCurrentInstance().addCallbackParam("transferId", UUID.randomUUID().toString()); //$NON-NLS-1$
        RequestContext.getCurrentInstance().addCallbackParam("chunkSize", getChunkSize(uploadSize)); //$NON-NLS-1$
    }


    /**
     * @param uploadSize
     * @throws QuotaExceededException
     */
    private void checkTemporaryQuota ( long uploadSize ) throws QuotaExceededException {
        Subject subject = SecurityUtils.getSubject();
        boolean user = subject.isAuthenticated() && subject.getPrincipals().byType(UserPrincipal.class) != null;
        Long limit;
        if ( user ) {
            limit = this.fsp.getConfigurationProvider().getFrontendConfiguration().getPerUserIncompleteSizeLimit();
        }
        else {
            limit = this.fsp.getConfigurationProvider().getFrontendConfiguration().getPerSessionIncompleteSizeLimit();
        }

        if ( limit != null && uploadSize > limit ) {
            throw new QuotaExceededException(limit, uploadSize - limit);
        }
    }


    /**
     * @param uploadSize
     * @return
     */
    private long getChunkSize ( long uploadSize ) {
        long defaultChunkSize = this.fsp.getChunkUploadService().getDefaultChunkSize();
        int optimalChunkCount = this.fsp.getChunkUploadService().getOptimalChunkCount();
        long maxChunkSize = this.fsp.getChunkUploadService().getMaximumChunkSize();

        if ( uploadSize < ( defaultChunkSize * optimalChunkCount ) ) {
            // small file, no more than 32 chunks
            return defaultChunkSize;
        }

        long opt = uploadSize / optimalChunkCount;
        if ( opt > maxChunkSize ) {
            return maxChunkSize;
        }
        return ( opt >> 4 ) << 4;
    }


    /**
     * @param targetFile
     * @param g
     * @param f
     * @param req
     * @return
     * @throws IOException
     * @throws FileshareException
     */
    private VFSFileEntity handleReplaceFile ( VFSFileEntity targetFile, Grant g, UploadedFile f, ServletRequest req, ServletResponse resp,
            ReadableByteChannel data, boolean refresh ) throws IOException, FileshareException {
        if ( log.isDebugEnabled() ) {
            log.debug("File replace upload"); //$NON-NLS-1$
            log.debug("content type: " + f.getContentType()); //$NON-NLS-1$
            log.debug("file name: " + f.getFileName()); //$NON-NLS-1$
            log.debug("file size: " + f.getSize()); //$NON-NLS-1$
            log.debug("target file: " + targetFile); //$NON-NLS-1$
            log.debug("target grant: " + g); //$NON-NLS-1$
        }

        VFSFileEntity file = makeFileEntity(f, targetFile);
        file = this.fsp.getUploadService().replaceFile(targetFile.getEntityKey(), file, data, req, resp);
        if ( refresh ) {
            this.treeBean.refresh();
        }
        return file;
    }


    /**
     * @param targetDir
     * @param replaceFiles
     * @param f
     * @param req
     * @return
     * @throws IOException
     * @throws FileshareException
     */
    private VFSFileEntity handleDirectoryUpload ( VFSContainerEntity targetDir, Grant g, UploadedFile f, boolean replaceFiles, ServletRequest req,
            ServletResponse resp, ReadableByteChannel data, boolean refresh ) throws FileshareException, IOException {
        if ( log.isDebugEnabled() ) {
            log.debug("File to directory upload"); //$NON-NLS-1$
            log.debug("Replace existing " + replaceFiles); //$NON-NLS-1$
            log.debug("content type: " + f.getContentType()); //$NON-NLS-1$
            log.debug("file name: " + f.getFileName()); //$NON-NLS-1$
            log.debug("file size: " + f.getSize()); //$NON-NLS-1$
            log.debug("target dir: " + targetDir); //$NON-NLS-1$
            log.debug("target grant: " + g); //$NON-NLS-1$
        }

        VFSFileEntity file = makeFileEntity(f, targetDir);
        if ( replaceFiles ) {
            file = this.fsp.getUploadService().createOrReplace(targetDir.getEntityKey(), file, data, req, resp);
        }
        else {
            file = this.fsp.getUploadService().create(targetDir.getEntityKey(), file, data, req, resp);
        }

        if ( file == null ) {
            return null;
        }
        if ( refresh ) {
            this.treeBean.ensureExpanded(targetDir, this.fsp.getEntityService().getParents(targetDir.getEntityKey()), g);
            this.treeBean.refresh();
        }
        return file;
    }


    /**
     * @param ev
     */
    public void checkNameConflict ( ActionEvent ev ) {
        String targetId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("targetId"); //$NON-NLS-1$
        String filename = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("filename"); //$NON-NLS-1$

        if ( StringUtils.isBlank(filename) ) {
            return;
        }

        try {
            EntityKey containerId = parseId(targetId);
            VFSFileEntity res = this.fsp.getEntityService().checkNameConflict(containerId, filename);
            if ( res != null ) {
                RequestContext.getCurrentInstance().addCallbackParam("conflictFileName", res.getLocalName()); //$NON-NLS-1$
                RequestContext.getCurrentInstance().addCallbackParam("conflictFileSize", res.getFileSize()); //$NON-NLS-1$
            }
        }
        catch (
            FileshareException |
            UndeclaredThrowableException e ) {
            ExceptionHandler.handleException(e);
        }

    }


    /**
     * @param targetGrantId
     * @return
     * @throws FileshareException
     */
    private Grant getTargetGrant ( boolean headers ) throws FileshareException {
        String targetGrantId = getUploadTargetGrant(headers);

        if ( !StringUtils.isBlank(targetGrantId) ) {
            UUID grantId = UUID.fromString(targetGrantId);
            if ( log.isDebugEnabled() ) {
                log.debug("Found target grant " + grantId); //$NON-NLS-1$
            }
            return this.fsp.getShareService().getGrant(grantId);
        }

        return null;
    }


    private static String getUploadTargetGrant ( boolean headers ) {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        if ( !headers ) {
            return externalContext.getRequestParameterMap().get("upload_targetGrant"); //$NON-NLS-1$
        }

        return externalContext.getRequestHeaderMap().get("X-Upload-Target-Grant"); //$NON-NLS-1$
    }


    /**
     * @param f
     * @param targetDir
     * @return
     * @throws UnsupportedEncodingException
     */
    private static VFSFileEntity makeFileEntity ( UploadedFile f, VFSEntity ref ) throws UnsupportedEncodingException {
        FileEntity file = new FileEntity();
        String fixed = f.getFileName().replace(
            "+", //$NON-NLS-1$
            "%2B"); //$NON-NLS-1$
        file.setLocalName(URLDecoder.decode(fixed, "UTF-8")); //$NON-NLS-1$
        file.setFileSize(f.getSize());
        file.setContentType(f.getContentType());
        file.setSecurityLabel(ref.getSecurityLabel());
        return file;
    }


    /**
     * @return
     * @throws FileshareException
     */
    private VFSContainerEntity getTargetDirectory ( boolean headers ) throws FileshareException {
        String targetId;
        if ( !headers ) {
            targetId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("upload_target"); //$NON-NLS-1$
        }
        else {
            targetId = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("X-Upload-Target"); //$NON-NLS-1$
        }

        return this.fsp.getDirectoryService().getById(parseId(targetId));

    }


    /**
     * @return
     * @throws FileshareException
     */
    private VFSEntity getTargetFile ( boolean headers ) throws FileshareException {
        String targetId;
        if ( !headers ) {
            targetId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("upload_target"); //$NON-NLS-1$
        }
        else {
            targetId = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("X-Upload-Target"); //$NON-NLS-1$
        }
        return this.fsp.getEntityService().getEntity(parseId(targetId));
    }


    /**
     * @param targetId
     * @return
     * @throws EntityNotFoundException
     */
    private EntityKey parseId ( String targetId ) throws EntityNotFoundException {
        if ( StringUtils.isBlank(targetId) ) {
            throw new EntityNotFoundException("No target given"); //$NON-NLS-1$
        }

        try {
            return this.fsp.getEntityService().parseEntityKey(targetId);
        }
        catch ( IllegalArgumentException e ) {
            throw new EntityNotFoundException("Invalid id " + targetId, e); //$NON-NLS-1$
        }
    }
}
