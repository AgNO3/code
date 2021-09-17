/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.crypto.truststores;


import java.security.cert.X509Certificate;

import eu.agno3.orchestrator.config.crypto.truststore.RevocationConfigMutable;
import eu.agno3.orchestrator.types.entities.crypto.X509CertEntry;


/**
 * @author mbechler
 *
 */
public class TrustedResponderCertificateWrapper {

    private RevocationConfigMutable cfg;


    /**
     * @param cfg
     */
    public TrustedResponderCertificateWrapper ( RevocationConfigMutable cfg ) {
        this.cfg = cfg;
    }


    /**
     * 
     * @return the trusted responder certificate from the revocation config
     */
    public X509Certificate getCertificate () {
        X509CertEntry respCert = this.cfg.getTrustedResponderTrustCertificate();
        if ( respCert == null ) {
            return null;
        }
        return respCert.getCertificate();

    }


    /**
     * Set a new trusted responder certificate in the delegate revocation config
     * 
     * @param cert
     */
    public void setCertificate ( X509Certificate cert ) {
        this.cfg.setTrustedResponderTrustCertificate(new X509CertEntry(cert));
    }

}
