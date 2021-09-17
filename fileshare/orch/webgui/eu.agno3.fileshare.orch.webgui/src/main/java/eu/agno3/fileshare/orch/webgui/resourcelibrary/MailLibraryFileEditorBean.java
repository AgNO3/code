/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.webgui.resourcelibrary;


import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.agno3.fileshare.mail.tpl.FileshareMailTemplateBuilder;
import eu.agno3.fileshare.orch.webgui.FileshareOrchGUIMessages;
import eu.agno3.fileshare.orch.webgui.FileshareServiceProvider;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.resourcelibraries.AbstractResourceLibraryTextEditor;

import freemarker.template.Configuration;
import freemarker.template.Template;


/**
 * @author mbechler
 *
 */
@Named ( "fs_mailLibraryFileEditorBean" )
@ViewScoped
public class MailLibraryFileEditorBean extends AbstractResourceLibraryTextEditor implements Serializable {

    private static final Logger log = Logger.getLogger(MailLibraryFileEditorBean.class);

    /**
     * 
     */
    private static final long serialVersionUID = -1151967855955719141L;

    @Inject
    private FileshareServiceProvider fsp;

    private String createType;
    private String createSubtype;
    private Locale createLocale;


    /**
     * 
     * @return the usable template ids
     */
    public List<String> getTypes () {
        return FileshareMailTemplateBuilder.TEMPLATE_IDS;
    }


    /**
     * 
     * @return the template subtypes
     */
    public List<String> getSubtypes () {
        return Arrays.asList("html", //$NON-NLS-1$
            "plain", //$NON-NLS-1$
            "subject"); //$NON-NLS-1$
    }


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
     * @return the createSubtype
     */
    public String getCreateSubtype () {
        return this.createSubtype;
    }


    /**
     * @param createSubtype
     *            the createSubtype to set
     */
    public void setCreateSubtype ( String createSubtype ) {
        this.createSubtype = createSubtype;
    }


    /**
     * @return the createLocale
     */
    public Locale getCreateLocale () {
        return this.createLocale;
    }


    /**
     * @param createLocale
     *            the createLocale to set
     */
    public void setCreateLocale ( Locale createLocale ) {
        this.createLocale = createLocale;
    }


    /**
     * @return
     * @throws IOException
     */
    @Override
    protected String makeDefault () {
        try {
            return this.fsp.getMailTemplateBuilder().getTemplateSource(String.format("%s.%s", this.createType, this.createSubtype), //$NON-NLS-1$
                this.createLocale);
        }
        catch ( IOException e ) {
            ExceptionHandler.handle(e);
            return StringUtils.EMPTY;
        }
    }


    /**
     * @return
     */
    @Override
    protected String getPathName () {
        return String.format("%s.%s%s.ftl", //$NON-NLS-1$
            this.createType,
            this.createSubtype,
            this.createLocale != null ? "_" + this.createLocale.toLanguageTag().replace('-', '_') : StringUtils.EMPTY); //$NON-NLS-1$
    }


    /**
     * 
     */
    @Override
    protected boolean validateSource () {
        try {
            Configuration cfg = this.fsp.getTemplateConfig().create();
            this.fsp.getTemplateConfig().setup(cfg);
            new Template("validate", new StringReader(this.getText()), cfg); //$NON-NLS-1$
            return true;
        }
        catch ( IOException e ) {
            log.warn("Failed to validate template", e); //$NON-NLS-1$
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, FileshareOrchGUIMessages.get("resourceLibrary.mail.validationFailed"), e.getMessage())); //$NON-NLS-1$
            return false;
        }

    }

}
