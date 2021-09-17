/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.11.2014 by mbechler
 */
package org.primefaces.fixed.component.fileupload;


import java.io.IOException;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.mail.internet.ContentDisposition;
import javax.mail.internet.ParseException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.primefaces.component.fileupload.FileUpload;
import org.primefaces.component.fileupload.FileUploadRenderer;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.expression.SearchExpressionFacade;
import org.primefaces.util.WidgetBuilder;


/**
 * @author mbechler
 *
 */
public class FileUploadRendererFixed extends FileUploadRenderer {

    private static final Logger log = Logger.getLogger(FileUploadRendererFixed.class);

    /**
     * 
     */
    private static final String MULTIPART_FORM_DATA = "multipart/form-data"; //$NON-NLS-1$

    private static final String UPLOAD_TARGET_HEADER = "X-JSF-Upload-Component"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.component.fileupload.FileUploadRenderer#decode(javax.faces.context.FacesContext,
     *      javax.faces.component.UIComponent)
     */
    @Override
    public void decode ( FacesContext ctx, UIComponent comp ) {
        HttpServletRequest req = (HttpServletRequest) ctx.getExternalContext().getRequest();
        String contentType = req.getHeader("Content-Type"); //$NON-NLS-1$
        if ( contentType != null && contentType.toLowerCase().startsWith(MULTIPART_FORM_DATA) ) {
            super.decode(ctx, comp);
        }
        else if ( ctx.getExternalContext().getRequestHeaderMap().containsKey(UPLOAD_TARGET_HEADER) ) {
            try {
                decodeDirect(ctx, comp);
            }
            catch (
                IOException |
                ServletException e ) {
                log.warn("Failed to process upload", e); //$NON-NLS-1$
                throw new FacesException(e);
            }
        }

    }


    /**
     * @throws ServletException
     * @throws IOException
     * 
     */
    private static void decodeDirect ( FacesContext context, UIComponent comp ) throws IOException, ServletException {
        FileUpload fileUpload = (FileUpload) comp;
        if ( !fileUpload.isDisabled() ) {
            String clientId = fileUpload.getClientId(context);
            if ( !clientId.equals(context.getExternalContext().getRequestHeaderMap().get(UPLOAD_TARGET_HEADER)) ) {
                log.debug("Target mismatch"); //$NON-NLS-1$
                return;
            }

            HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
            String fileName = getUploadFileName(request);

            if ( log.isDebugEnabled() ) {
                log.debug("Have plain upload: " + fileName); //$NON-NLS-1$
            }

            if ( fileUpload.getMode().equals("simple") ) { //$NON-NLS-1$
                decodeSimple(context, fileUpload, request, fileName);
            }
            else {
                decodeAdvanced(context, fileUpload, request, fileName);
            }
        }
    }


    @Override
    @SuppressWarnings ( "nls" )
    protected void encodeScript ( FacesContext context, FileUpload fileUpload ) throws IOException {
        String clientId = fileUpload.getClientId(context);
        String update = fileUpload.getUpdate();
        String process = fileUpload.getProcess();
        WidgetBuilder wb = getWidgetBuilder(context);

        if ( fileUpload.getMode().equals("advanced") ) {
            wb.initWithDomReady("FileUploadFixed", fileUpload.resolveWidgetVar(), clientId);

            wb.attr("auto", fileUpload.isAuto(), false).attr("dnd", fileUpload.isDragDropSupport(), true)
                    .attr("update", SearchExpressionFacade.resolveClientIds(context, fileUpload, update), null)
                    .attr("process", SearchExpressionFacade.resolveClientIds(context, fileUpload, process), null)
                    .attr("maxFileSize", fileUpload.getSizeLimit(), Long.MAX_VALUE).attr("fileLimit", fileUpload.getFileLimit(), Integer.MAX_VALUE)
                    .attr("invalidFileMessage", fileUpload.getInvalidFileMessage(), null)
                    .attr("invalidSizeMessage", fileUpload.getInvalidSizeMessage(), null)
                    .attr("fileLimitMessage", fileUpload.getFileLimitMessage(), null).attr("messageTemplate", fileUpload.getMessageTemplate(), null)
                    .attr("previewWidth", fileUpload.getPreviewWidth(), 80).attr("disabled", fileUpload.isDisabled(), false)
                    .attr("sequentialUploads", fileUpload.isSequential(), false).callback("onstart", "function()", fileUpload.getOnstart())
                    .callback("onerror", "function()", fileUpload.getOnerror()).callback("oncomplete", "function(args)", fileUpload.getOncomplete());

            if ( fileUpload.getAllowTypes() != null ) {
                wb.append(",allowTypes:").append(fileUpload.getAllowTypes());
            }
        }
        else {
            wb.init("SimpleFileUpload", fileUpload.resolveWidgetVar(), clientId).attr("skinSimple", fileUpload.isSkinSimple(), false);
        }

        wb.finish();
    }


    /**
     * @param request
     * @return
     */
    private static String getUploadFileName ( HttpServletRequest request ) {

        String disposition = request.getHeader("Content-Disposition"); //$NON-NLS-1$

        if ( log.isDebugEnabled() ) {
            log.debug("Content-Disposition: " + disposition); //$NON-NLS-1$
        }

        try {
            ContentDisposition disp = new ContentDisposition(disposition);
            return disp.getParameter("filename"); //$NON-NLS-1$
        }
        catch ( ParseException e ) {
            log.debug("Failed to parse content disposition", e); //$NON-NLS-1$
            return null;
        }
    }


    private static void decodeSimple ( FacesContext context, FileUpload fileUpload, HttpServletRequest request, String fileName ) throws IOException {
        fileUpload.setTransient(true);
        fileUpload.setSubmittedValue(new HttpUploadedFile(request, fileName));
    }


    private static void decodeAdvanced ( FacesContext context, FileUpload fileUpload, HttpServletRequest request, String fileName )
            throws IOException {
        fileUpload.setTransient(true);
        fileUpload.queueEvent(new FileUploadEvent(fileUpload, new HttpUploadedFile(request, fileName)));
    }

}
