/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.resourcelibraries;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.server.webgui.CoreServiceProvider;
import eu.agno3.orchestrator.server.webgui.GuiMessages;
import eu.agno3.runtime.xml.XMLParserConfigurationException;


/**
 * @author mbechler
 *
 */
@Named ( "webThemeLibraryFileEditorBean" )
@ViewScoped
public class WebThemeLibraryFileEditorBean extends AbstractResourceLibraryTextEditor implements Serializable {

    /**
     * 
     */
    private static final String BINARY_TYPE = "binary"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String CSS_TYPE = "css"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String FACELET_TYPE = "facelet"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(WebThemeLibraryFileEditorBean.class);

    @Inject
    private CoreServiceProvider csp;

    /**
     * 
     */
    private static final long serialVersionUID = -1151967855955719141L;

    private static final String CUSTOM = "custom"; //$NON-NLS-1$

    private static final List<String> TEMPLATE_IDS = Arrays.asList(
        "theme.css", //$NON-NLS-1$
        "header.xhtml", //$NON-NLS-1$
        "footer.xhtml", //$NON-NLS-1$
        "rawHeader.xhtml", //$NON-NLS-1$
        "rawFooter.xhtml", //$NON-NLS-1$
        "dialogHeader.xhtml", //$NON-NLS-1$
        "dialogFooter.xhtml", //$NON-NLS-1$
        CUSTOM);

    private static final String DEFAULT_FACELET_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + //$NON-NLS-1$
            "<ui:composition xmlns=\"http://www.w3.org/1999/xhtml\" \n" + //$NON-NLS-1$
            "       xmlns:ui=\"http://java.sun.com/jsf/facelets\" \n" + //$NON-NLS-1$
            "       xmlns:h=\"http://java.sun.com/jsf/html\">\n" + //$NON-NLS-1$
            "</ui:composition>\n"; //$NON-NLS-1$

    private String createType;


    /**
     * @return the createType
     */
    public String getCreateType () {
        return this.createType;
    }


    /**
     * @param createType
     *            the createType to set
     */
    public void setCreateType ( String createType ) {
        this.createType = createType;
    }


    /**
     * 
     * @return the usable template ids
     */
    public List<String> getTypes () {
        return TEMPLATE_IDS;
    }


    /**
     * @return
     * @throws IOException
     */
    @Override
    protected String makeDefault () {
        switch ( getFileType() ) {
        case FACELET_TYPE:
            return DEFAULT_FACELET_TEMPLATE;
        case CSS_TYPE:
        case BINARY_TYPE:
        default:
            return StringUtils.EMPTY;
        }
    }


    /**
     * 
     * @return the desired editor mode
     */
    public String getEditorMode () {
        switch ( getFileType() ) {
        case FACELET_TYPE:
            return "application/xml"; //$NON-NLS-1$
        case CSS_TYPE:
            return "text/css"; //$NON-NLS-1$
        default:
            return StringUtils.EMPTY;
        }
    }


    /**
     * @return
     */
    @Override
    protected String getPathName () {
        if ( CUSTOM.equals(this.createType) ) {
            return getFileEditor().getCreateFilename();
        }
        log.debug(this.createType);
        return this.createType;
    }


    /**
     * 
     * @return the loaded file's type
     */
    public String getFileType () {
        ResourceLibraryFileEditorBean fe = this.getFileEditor();
        String fileName = fe.getFilename();
        if ( !StringUtils.isBlank(fileName) && fileName.endsWith(".xhtml") ) { //$NON-NLS-1$
            return FACELET_TYPE;
        }
        else if ( !StringUtils.isBlank(fileName) && fileName.endsWith(".css") ) { //$NON-NLS-1$
            return CSS_TYPE;
        }
        else {
            return BINARY_TYPE;
        }
    }


    /**
     * 
     */
    @Override
    protected boolean validateSource () {
        switch ( getFileType() ) {
        case BINARY_TYPE:
            return true;
        case CSS_TYPE:
            return validateCSS();
        case FACELET_TYPE:
            return validateFacelet();
        }
        return true;

    }


    /**
     * @return
     */
    private boolean validateFacelet () {

        try {
            XMLStreamReader sr = this.csp.getXmlParserFactory()
                    .createStreamReader(new ByteArrayInputStream(this.getFileEditor().getSelectedFileData()));
            while ( sr.hasNext() ) {
                sr.next();
            }

            return true;
        }
        catch (
            XMLParserConfigurationException |
            XMLStreamException e ) {
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, GuiMessages.get("resourceLibrary.web.faceletValidationFailed"), e.getMessage())); //$NON-NLS-1$
            return false;
        }
    }


    /**
     * @return
     */
    protected boolean validateCSS () {
        // TODO: not implemented
        return true;
    }

}
