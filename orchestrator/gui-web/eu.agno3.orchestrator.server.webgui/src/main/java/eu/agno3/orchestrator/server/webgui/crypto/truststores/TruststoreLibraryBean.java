/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.07.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.crypto.truststores;


import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import eu.agno3.orchestrator.config.crypto.truststore.TruststoreUtil;
import eu.agno3.orchestrator.server.webgui.exceptions.ExceptionHandler;
import eu.agno3.orchestrator.server.webgui.structure.resourcelibraries.ResourceLibraryFileEditorBean;


/**
 * @author mbechler
 *
 */
@ViewScoped
@Named ( "truststoreLibraryBean" )
public class TruststoreLibraryBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8621413977660572562L;

    @Inject
    private ResourceLibraryFileEditorBean fileEditor;

    private X509Certificate certificate;


    /**
     * @return the certificate
     */
    public X509Certificate getCertificate () {
        return this.certificate;
    }


    /**
     * @param certificate
     *            the certificate to set
     */
    public void setCertificate ( X509Certificate certificate ) {
        this.certificate = certificate;
        this.create();
    }


    /**
     * 
     * @return whether a certificate file is selected
     */
    public boolean haveSelectedCertificate () {
        return this.fileEditor.getSelectedFileData() != null && this.fileEditor.getSelectedFileData().length > 0;
    }


    /**
     * 
     * @return the selected certificate
     */
    public X509Certificate getSelectedCertificate () {
        try {

            if ( !haveSelectedCertificate() ) {
                return null;
            }

            return (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate( //$NON-NLS-1$
                new ByteArrayInputStream(this.fileEditor.getSelectedFileData()));
        }
        catch ( CertificateException e ) {
            ExceptionHandler.handle(e);
            return null;
        }
    }


    public String create () {
        if ( this.certificate == null ) {
            return null;
        }
        try {
            this.fileEditor.createFromData(TruststoreUtil.makeCertificatePath(this.certificate), this.certificate.getEncoded());
            this.certificate = null;
        }
        catch ( CertificateEncodingException e ) {
            ExceptionHandler.handle(e);
        }
        return null;
    }

}
