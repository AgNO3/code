/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 15, 2017 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.test;


import java.security.cert.X509Certificate;


/**
 * @author mbechler
 *
 */
public class TrustCertificateInteraction implements ConfigTestInteraction {

    /**
     * 
     */
    private static final long serialVersionUID = 7450710070480693993L;
    private X509Certificate certificate;


    /**
     * @param cert
     */
    public TrustCertificateInteraction ( X509Certificate cert ) {
        this.certificate = cert;
    }


    /**
     * @return the certificate
     */
    public X509Certificate getCertificate () {
        return this.certificate;
    }
}
