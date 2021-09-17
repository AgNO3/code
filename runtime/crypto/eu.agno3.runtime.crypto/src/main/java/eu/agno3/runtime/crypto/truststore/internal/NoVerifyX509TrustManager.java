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

import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
final class NoVerifyX509TrustManager implements X509TrustManager {

    private static final Logger log = Logger.getLogger(NoVerifyX509TrustManager.class);


    /**
     * 
     */
    public NoVerifyX509TrustManager () {
        log.warn("NoVerifyX509TrustManager is being used"); //$NON-NLS-1$
    }


    @Override
    public void checkClientTrusted ( X509Certificate[] chain, String authType ) throws CertificateException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("checkClientTrusted for %s: %s", authType, chain == null || chain.length < 1 ? null : chain[ 0 ] //$NON-NLS-1$
                    .getSubjectX500Principal().getName()));
        }
    }


    @Override
    public void checkServerTrusted ( X509Certificate[] chain, String authType ) throws CertificateException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("checkServerTrusted for %s: %s", authType, chain == null || chain.length < 1 ? null : chain[ 0 ] //$NON-NLS-1$
                    .getSubjectX500Principal().getName()));
        }
    }


    @Override
    public X509Certificate[] getAcceptedIssuers () {
        return new X509Certificate[] {};
    }
}