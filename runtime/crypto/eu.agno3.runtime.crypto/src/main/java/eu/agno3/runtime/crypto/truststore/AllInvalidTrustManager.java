/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore;


import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;


/**
 * @author mbechler
 *
 */
public final class AllInvalidTrustManager implements X509TrustManager {

    private String trustStore;


    /**
     * 
     */
    public AllInvalidTrustManager () {
        this.trustStore = "allInvalid"; //$NON-NLS-1$
    }


    /**
     * @param ts
     * 
     */
    public AllInvalidTrustManager ( String ts ) {
        this.trustStore = ts;
    }


    @Override
    public void checkClientTrusted ( X509Certificate[] chain, String authType ) throws CertificateException {
        throw new EmptyTruststoreCertificateException("Truststore is empty", this.trustStore); //$NON-NLS-1$
    }


    @Override
    public void checkServerTrusted ( X509Certificate[] chain, String authType ) throws CertificateException {
        throw new EmptyTruststoreCertificateException("Truststore is empty", this.trustStore); //$NON-NLS-1$
    }


    @Override
    public X509Certificate[] getAcceptedIssuers () {
        return new X509Certificate[] {};
    }
}