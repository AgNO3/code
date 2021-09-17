/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.06.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.chunkupload;


import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.channels.Channels;
import java.nio.charset.Charset;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.webbeans.util.StringUtil;
import org.eclipse.jetty.server.MultiParts;
import org.eclipse.jetty.server.Request;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.QuotaExceededException;
import eu.agno3.fileshare.model.EntityKey;
import eu.agno3.fileshare.model.FileEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.service.ChunkContext;
import eu.agno3.fileshare.service.ChunkUploadService;
import eu.agno3.fileshare.service.VFSService;


/**
 * @author mbechler
 *
 */
public class IncompleteUploadRequestListener implements Filter {

    private static final Logger log = Logger.getLogger(IncompleteUploadRequestListener.class);
    private static final Charset UTF8 = Charset.forName("UTF-8"); //$NON-NLS-1$

    private ChunkUploadService cus;
    private VFSService vfs;


    /**
     * @param cus
     * @param vfs
     * 
     */
    public IncompleteUploadRequestListener ( ChunkUploadService cus, VFSService vfs ) {
        this.cus = cus;
        this.vfs = vfs;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init ( FilterConfig cfg ) throws ServletException {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy () {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     *      javax.servlet.FilterChain)
     */
    @Override
    public void doFilter ( ServletRequest req, ServletResponse resp, FilterChain chain ) throws IOException, ServletException {
        try {
            chain.doFilter(req, resp);
        }
        catch ( UndeclaredThrowableException e ) {
            log.debug("Undeclared throwable"); //$NON-NLS-1$
        }
        catch ( IOException e ) {
            log.debug("Caught IO exception"); //$NON-NLS-1$
            if ( !SecurityUtils.getSubject().isAuthenticated() ) {
                log.debug("Not authenticated"); //$NON-NLS-1$
                return;
            }

            handleIOException(req, e);
        }
    }


    /**
     * @param req
     * @param e
     * @throws IOException
     */
    @SuppressWarnings ( "resource" )
    private void handleIOException ( ServletRequest req, IOException e ) throws IOException {
        MultiParts mpis = (MultiParts) req.getAttribute(Request.__MULTIPARTS);
        if ( mpis != null ) {
            if ( mpis.getContext() == req.getServletContext() ) {
                try {
                    Part uploadPart = null;
                    String target = null;
                    String targetGrant = null;
                    String fileSize = null;
                    String replaceFile = null;
                    String replaceConfirmed = null;

                    for ( Part p : mpis.getParts() ) {
                        if ( !StringUtil.isEmpty(p.getSubmittedFileName()) ) {
                            uploadPart = p;
                        }
                        else if ( "upload_target".equals(p.getName()) ) { //$NON-NLS-1$
                            target = getFormValue(p);
                        }
                        else if ( "upload_targetGrant".equals(p.getName()) ) { //$NON-NLS-1$
                            targetGrant = getFormValue(p);
                        }
                        else if ( "upload_fileSize".equals(p.getName()) ) { //$NON-NLS-1$
                            fileSize = getFormValue(p);
                        }
                        else if ( "upload_replaceFile".equals(p.getName()) ) { //$NON-NLS-1$
                            replaceFile = getFormValue(p);
                        }
                        else if ( "upload_replaceFilesConfirmed".equals(p.getName()) ) { //$NON-NLS-1$
                            replaceConfirmed = getFormValue(p);
                        }
                    }

                    if ( uploadPart != null ) {
                        if ( uploadPart.getSize() < this.cus.getDefaultChunkSize() ) {
                            log.debug("Ignoring upload as it is smaller than the chunk size"); //$NON-NLS-1$
                            throw e;
                        }
                        handleUploadPart(e, uploadPart, target, targetGrant, fileSize, replaceFile, replaceConfirmed);
                    }
                }
                catch ( IOException ex ) {
                    log.warn("Failed to get multipart data", ex); //$NON-NLS-1$
                }
            }
        }

        throw e;
    }


    /**
     * @param e
     * @param uploadPart
     * @param target
     * @param targetGrant
     * @param fileSize
     * @param replaceFile
     * @param replaceConfirmed
     * @throws IOException
     */
    private void handleUploadPart ( IOException e, Part uploadPart, String target, String targetGrant, String fileSize, String replaceFile,
            String replaceConfirmed ) throws IOException {

        if ( StringUtils.isBlank(target) || StringUtil.isBlank(fileSize) ) {
            log.warn("Upload does not contain target or total size"); //$NON-NLS-1$
            throw e;
        }

        EntityKey targetId = this.vfs.parseEntityKey(target);

        boolean isTargetFile = false;
        boolean replacing = !StringUtil.isBlank(replaceConfirmed) && Boolean.TRUE.toString().equals(replaceConfirmed);
        if ( !StringUtil.isBlank(replaceFile) && Boolean.TRUE.toString().equals(replaceFile) ) {
            replacing = true;
            isTargetFile = true;
        }

        long completeSize = Long.parseLong(fileSize);
        if ( completeSize == uploadPart.getSize() ) {
            log.warn("Upload is complete, something else must have gone wrong"); //$NON-NLS-1$
            throw e;
        }

        log.debug(String.format(
            "Found incomplete upload, target %s targetGrants %s totalSize %s", //$NON-NLS-1$
            targetId,
            targetGrant,
            fileSize));

        log.debug(String.format(
            "Have %d of %d (%.2f) bytes", //$NON-NLS-1$
            uploadPart.getSize(),
            completeSize,
            (double) uploadPart.getSize() / completeSize * 100.0f));

        VFSFileEntity file = new FileEntity();
        file.setLocalName(uploadPart.getSubmittedFileName());
        file.setContentType(uploadPart.getContentType());
        file.setFileSize(completeSize);

        try ( InputStream inputStream = uploadPart.getInputStream() ) {
            ChunkContext chunkContext = this.cus
                    .getOrCreateChunkContext(targetId, isTargetFile, UUID.randomUUID().toString(), file, null, replacing, false);
            chunkContext.storeInput(Channels.newChannel(inputStream), uploadPart.getSize());
        }
        catch ( IOException ex ) {
            log.warn("Failed to write upload chunks", ex); //$NON-NLS-1$
            throw e;
        }
        catch (
            FileshareException |
            UndeclaredThrowableException ex ) {
            if ( ex.getCause() instanceof InvocationTargetException && ex.getCause().getCause() instanceof QuotaExceededException ) {
                log.warn("User upload exceeds resumable upload quota by " + ( (QuotaExceededException) ex.getCause().getCause() ).getExceedBy()); //$NON-NLS-1$
                return;
            }
            log.warn("Failed to get chunk context for incomplete upload", ex); //$NON-NLS-1$
            throw e;
        }
    }


    /**
     * @param p
     * @return
     * @throws IOException
     */
    private static String getFormValue ( Part p ) {
        byte buffer[] = new byte[255];
        int len;
        try ( InputStream is = p.getInputStream() ) {
            len = is.read(buffer);
        }
        catch ( IOException e ) {
            log.warn("Failed to read form field", e); //$NON-NLS-1$
            return null;
        }

        if ( len == -1 ) {
            return StringUtils.EMPTY;
        }

        return new String(buffer, 0, len, UTF8);
    }

}
