/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.resourcelibraries;


import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;


/**
 * @author mbechler
 *
 */
@Named ( "termsLibraryFileEditorBean" )
@ViewScoped
public class TermsLibraryFileEditorBean extends AbstractResourceLibraryTextEditor implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8438215296204464405L;

    private transient Control ctrl = new ResourceBundle.Control() {

    };

    private static final String DEFAULT_TERMS_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + //$NON-NLS-1$
            "<!DOCTYPE html>\n" + //$NON-NLS-1$
            "<html>\n" + //$NON-NLS-1$
            "<head>\n" + //$NON-NLS-1$
            "</head>\n" + //$NON-NLS-1$
            "<body>\n" + //$NON-NLS-1$
            "</body>\n" + //$NON-NLS-1$
            "</html>\n"; //$NON-NLS-1$

    private String termsId;
    private String initialTermsId;
    private Locale locale;
    private String format = "html"; //$NON-NLS-1$


    /**
     * @return the termsId
     */
    public String getTermsId () {
        if ( StringUtils.isBlank(this.termsId) ) {
            return this.initialTermsId;
        }
        return this.termsId;
    }


    /**
     * @param termsId
     *            the termsId to set
     */
    public void setTermsId ( String termsId ) {
        this.termsId = termsId;
    }


    /**
     * @return the initialTermsId
     */
    public String getInitialTermsId () {
        return this.initialTermsId;
    }


    /**
     * @param initialTermsId
     *            the initialTermsId to set
     */
    public void setInitialTermsId ( String initialTermsId ) {
        this.initialTermsId = initialTermsId;
    }


    /**
     * @return the locale
     */
    public Locale getLocale () {
        return this.locale;
    }


    /**
     * @param locale
     *            the locale to set
     */
    public void setLocale ( Locale locale ) {
        this.locale = locale;
    }


    /**
     * @return the format
     */
    public String getFormat () {
        return this.format;
    }


    /**
     * @param format
     *            the format to set
     */
    public void setFormat ( String format ) {
        this.format = format;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.structure.resourcelibraries.AbstractResourceLibraryTextEditor#validateSource()
     */
    @Override
    protected boolean validateSource () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.structure.resourcelibraries.AbstractResourceLibraryTextEditor#getPathName()
     */
    @Override
    protected String getPathName () {
        return getControl().toBundleName(this.termsId, this.locale != null ? this.locale : Locale.ROOT) + '.' + this.format;
    }


    private Control getControl () {
        if ( this.ctrl == null ) {
            this.ctrl = new ResourceBundle.Control() {};
        }
        return this.ctrl;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.webgui.structure.resourcelibraries.AbstractResourceLibraryTextEditor#makeDefault()
     */
    @Override
    protected String makeDefault () {
        return DEFAULT_TERMS_TEMPLATE;
    }

}
