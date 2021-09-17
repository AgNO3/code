/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.03.2016 by mbechler
 */
package eu.agno3.orchestrator.agent.bootstrap.internal;


import java.security.cert.X509Certificate;


/**
 * @author mbechler
 *
 */
public class CryptoBootstrapResult {

    private X509Certificate caCert;
    private X509Certificate webCert;


    /**
     * @param caCert
     */
    public CryptoBootstrapResult ( X509Certificate caCert ) {
        this.caCert = caCert;
    }


    /**
     * @return the ca certificate
     */
    public X509Certificate getCaCert () {
        return this.caCert;
    }


    /**
     * @return the web certificate
     */
    public X509Certificate getWebCert () {
        return this.webCert;
    }


    /**
     * @param webCert
     *            the webCert to set
     */
    public void setWebCert ( X509Certificate webCert ) {
        this.webCert = webCert;
    }
}
