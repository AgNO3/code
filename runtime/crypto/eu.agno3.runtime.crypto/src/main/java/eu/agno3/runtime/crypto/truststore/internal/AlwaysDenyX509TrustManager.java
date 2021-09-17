/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.internal;


import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;


/**
 * @author mbechler
 *
 */
final class AlwaysDenyX509TrustManager implements X509TrustManager {

    /**
     * 
     */
    private static final String NO_TRUST_STORE_CONFIGURED = "No trust store configured"; //$NON-NLS-1$


    /**
     * 
     */
    public AlwaysDenyX509TrustManager () {}


    @Override
    public void checkClientTrusted ( X509Certificate[] chain, String authType ) throws CertificateException {
        throw new CertificateException(NO_TRUST_STORE_CONFIGURED);
    }


    @Override
    public void checkServerTrusted ( X509Certificate[] chain, String authType ) throws CertificateException {
        throw new CertificateException(NO_TRUST_STORE_CONFIGURED);
    }


    @Override
    public X509Certificate[] getAcceptedIssuers () {
        return new X509Certificate[] {};
    }
}