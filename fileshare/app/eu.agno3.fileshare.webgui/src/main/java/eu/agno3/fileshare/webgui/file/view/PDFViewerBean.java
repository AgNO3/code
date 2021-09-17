/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.02.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.file.view;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;

import eu.agno3.fileshare.model.Grant;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.webgui.service.file.FileDownloadController;
import eu.agno3.runtime.jsf.prefs.AbstractLocaleSettingsBean;


/**
 * @author mbechler
 *
 */
@Named ( "pdfViewerBean" )
@ApplicationScoped
public class PDFViewerBean {

    /**
     * 
     */
    private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$

    @Inject
    private FileDownloadController downloadController;

    @Inject
    private AbstractLocaleSettingsBean localeSettings;


    /**
     * @param file
     * @param g
     * @param token
     * @return a viewer URL
     * @throws UnsupportedEncodingException
     */
    public String makePDFViewerUrl ( VFSFileEntity file, Grant g, Object token ) throws UnsupportedEncodingException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ServletContext ctx = (ServletContext) facesContext.getExternalContext().getContext();
        String contextPath = ctx.getContextPath();
        String viewLink = this.downloadController.getViewLink(file, g, token);
        String fileUrl = URLEncoder.encode(viewLink, UTF_8); // $NON-NLS-1$
        String opts = makeOptions(facesContext);
        return String.format("%s/javax.faces.resource/documentviewer/pdfviewer.html.xhtml?ln=pdf&file=%s#%s", contextPath, fileUrl, opts); //$NON-NLS-1$
    }


    /**
     * @param facesContext
     * @return
     */
    private String makeOptions ( FacesContext facesContext ) {
        return String.format("locale=%s", this.localeSettings.getUserLocale()); //$NON-NLS-1$
    }
}
