/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.11.2014 by mbechler
 */
package eu.agno3.runtime.jsf.components.crypto;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.faces.application.FacesMessage;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import eu.agno3.runtime.jsf.i18n.BaseMessages;


/**
 * @author mbechler
 *
 */
public class CertificateEditor extends UIInput implements NamingContainer {

    private static final Logger log = Logger.getLogger(CertificateEditor.class);

    private static final String TEXT_INPUT = "textInput"; //$NON-NLS-1$
    private static final String CHOOSE_OTHER = "chooseOther"; //$NON-NLS-1$
    private static final String MODIFIED = "modified"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#getFamily()
     */
    @Override
    public String getFamily () {
        return UINamingContainer.COMPONENT_FAMILY;
    }


    /**
     * 
     * @return the current text (paste) input
     */
    public String getTextInput () {
        return (String) this.getStateHelper().get(TEXT_INPUT);
    }


    /**
     * 
     * @param input
     */
    public void setTextInput ( String input ) {
        this.getStateHelper().put(TEXT_INPUT, input);
    }


    /**
     * @return the modified
     */
    public boolean getModified () {
        return (boolean) getStateHelper().eval(MODIFIED, false);
    }


    protected void markModified () {
        getStateHelper().put(MODIFIED, true);
    }


    /**
     * 
     * @return whether the upload/paste part is shown
     */
    public boolean shouldShowUpdate () {
        return this.getValue() == null || (Boolean) this.getStateHelper().eval(CHOOSE_OTHER, false);
    }


    /**
     * 
     * @param ev
     */
    public void chooseOther ( ActionEvent ev ) {
        this.getStateHelper().put(CHOOSE_OTHER, Boolean.TRUE);
    }


    /**
     * 
     * @param ev
     */
    public void handleFileUpload ( FileUploadEvent ev ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Got file upload " + ev); //$NON-NLS-1$
        }
        UploadedFile f = ev.getFile();

        if ( f != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Content type: " + f.getContentType()); //$NON-NLS-1$
            }

            try {
                parseCertificate(f.getInputstream());
            }
            catch ( IOException e ) {
                log.warn("Error processing file upload", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * 
     */
    protected void parseCertificate ( InputStream is ) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X509"); //$NON-NLS-1$
            X509Certificate cert = (X509Certificate) cf.generateCertificate(is);
            this.setValue(cert);
            markModified();
            this.getStateHelper().put(CHOOSE_OTHER, false);
        }
        catch ( CertificateException e ) {
            log.debug("Failed to parse certificate", e); //$NON-NLS-1$
            FacesContext.getCurrentInstance().addMessage(
                this.getClientId(FacesContext.getCurrentInstance()),
                new FacesMessage(FacesMessage.SEVERITY_WARN, BaseMessages.get("certificateEditor.cannotParse"), StringUtils.EMPTY)); //$NON-NLS-1$
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#processValidators(javax.faces.context.FacesContext)
     */
    @Override
    public void processValidators ( FacesContext context ) {
        this.pushComponentToEL(context, this);
        try {
            super.processValidators(context);
        }
        finally {
            this.popComponentFromEL(context);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.faces.component.UIInput#processUpdates(javax.faces.context.FacesContext)
     */
    @Override
    public void processUpdates ( FacesContext context ) {
        this.pushComponentToEL(context, this);
        try {
            super.processUpdates(context);
        }
        finally {
            this.popComponentFromEL(context);
        }
    }


    /**
     * 
     * @param ev
     */
    public void useTextInput ( ActionEvent ev ) {
        String textInput = this.getTextInput();
        if ( textInput != null ) {
            this.parseCertificate(new ByteArrayInputStream(textInput.getBytes(Charset.forName("UTF-8")))); //$NON-NLS-1$
            setTextInput(StringUtils.EMPTY);
        }
        else {
            this.getStateHelper().put(CHOOSE_OTHER, Boolean.FALSE);
        }
    }

}
