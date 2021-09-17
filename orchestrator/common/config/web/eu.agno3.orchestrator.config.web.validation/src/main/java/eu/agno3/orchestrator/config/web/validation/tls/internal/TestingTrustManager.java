/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 26, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.web.validation.tls.internal;


import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.web.SSLClientConfiguration;
import eu.agno3.runtime.crypto.tls.X509TrustManagerWrapper;


/**
 * @author mbechler
 *
 */
final class TestingTrustManager extends X509TrustManagerWrapper {

    private static final Logger log = Logger.getLogger(TestingTrustManager.class);
    private SSLClientConfiguration cfg;
    private List<X509Certificate[]> failed = new ArrayList<>();
    private Set<PublicKey> pinnedKeys;


    /**
     * @param sec
     * @param delegate
     * @param pinnedKeys
     */
    public TestingTrustManager ( SSLClientConfiguration sec, X509TrustManager delegate, Set<PublicKey> pinnedKeys ) {
        super(delegate);
        this.cfg = sec;
        this.pinnedKeys = pinnedKeys;
    }


    /**
     * @return chains that failed validation
     */
    public List<X509Certificate[]> getFailed () {
        return this.failed;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.X509TrustManagerWrapper#preValidate(java.security.cert.X509Certificate[],
     *      java.lang.String, javax.net.ssl.SSLSession)
     */
    @Override
    protected boolean preValidate ( X509Certificate[] chain, String authType, SSLSession sess ) throws CertificateException {
        if ( log.isTraceEnabled() ) {
            log.trace(String.format(
                "Checking %s on %s,chain %s", //$NON-NLS-1$
                authType,
                sess != null ? sess.getPeerHost() : null,
                Arrays.toString(chain)));
        }

        if ( chain != null && chain.length >= 1 ) {
            X509Certificate eecert = chain[ 0 ];
            if ( this.pinnedKeys.contains(eecert.getPublicKey()) ) {
                log.debug(String.format(
                    "Certificate %s [0x%s] public key is pinned, skipping chain validation", //$NON-NLS-1$
                    eecert.getSubjectX500Principal().getName(),
                    eecert.getSerialNumber().toString(16)));
                return true;
            }
        }
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.X509TrustManagerWrapper#handleValidationError(java.security.cert.CertificateException,
     *      java.security.cert.X509Certificate[], java.lang.String, javax.net.ssl.SSLSession)
     */
    @Override
    protected boolean handleValidationError ( CertificateException e, X509Certificate[] chain, String authType, SSLSession sess ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Chain validation failed", e); //$NON-NLS-1$
        }

        this.failed.add(chain);

        if ( this.cfg.getDisableCertificateVerification() != null && this.cfg.getDisableCertificateVerification() ) {
            log.debug("Ignoring validation error"); //$NON-NLS-1$
            return true;
        }

        return false;
    }

}