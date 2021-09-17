/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.01.2017 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;

import eu.agno3.runtime.crypto.tls.TLSConfiguration;
import eu.agno3.runtime.crypto.tls.X509TrustManagerWrapper;


/**
 * @author mbechler
 *
 */
public class PinningX509TrustManager extends X509TrustManagerWrapper {

    private static final Logger log = Logger.getLogger(PinningX509TrustManager.class);
    private final TLSConfiguration tc;


    /**
     * @param tm
     * @param tc
     * 
     */
    public PinningX509TrustManager ( X509TrustManager tm, TLSConfiguration tc ) {
        super(tm);
        this.tc = tc;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.X509TrustManagerWrapper#preValidate(java.security.cert.X509Certificate[],
     *      java.lang.String, javax.net.ssl.SSLSession)
     */
    @Override
    protected boolean preValidate ( X509Certificate[] chain, String authType, SSLSession sess ) throws CertificateException {

        if ( chain != null && chain.length >= 1 ) {
            X509Certificate eecert = chain[ 0 ];
            if ( this.tc.getPinPublicKeys().contains(eecert.getPublicKey()) ) {
                log.debug(String.format(
                    "Certificate %s [0x%s] public key is pinned, skipping chain validation", //$NON-NLS-1$
                    eecert.getSubjectX500Principal().getName(),
                    eecert.getSerialNumber().toString(16)));
                return true;
            }
        }

        // TODO: also check pinning on CAs? needs custom chain validation

        return super.preValidate(chain, authType, sess);
    }
}
