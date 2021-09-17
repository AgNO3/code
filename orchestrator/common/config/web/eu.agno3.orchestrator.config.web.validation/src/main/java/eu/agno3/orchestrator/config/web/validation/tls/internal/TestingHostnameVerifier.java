/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 26, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.web.validation.tls.internal;


import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.config.web.SSLClientConfiguration;
import eu.agno3.runtime.crypto.tls.ExtendedHostnameVerifier;


/**
 * @author mbechler
 *
 */
final class TestingHostnameVerifier implements HostnameVerifier, ExtendedHostnameVerifier {

    private static final Logger log = Logger.getLogger(TestingHostnameVerifier.class);
    private SSLClientConfiguration cfg;
    private HostnameVerifier delegate;
    private Map<String, X509Certificate[]> failed = new HashMap<>();


    /**
     * @param sec
     * @param delegate
     */
    public TestingHostnameVerifier ( SSLClientConfiguration sec, HostnameVerifier delegate ) {
        this.cfg = sec;
        this.delegate = delegate;
    }


    @Override
    public boolean verify ( String hostname, SSLSession session ) {
        if ( this.cfg.getDisableHostnameVerification() ) {
            log.debug("Hostname verification is disabled"); //$NON-NLS-1$
            return true;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Checking " + hostname); //$NON-NLS-1$
        }

        if ( this.delegate == null || !this.delegate.verify(hostname, session) ) {
            log.debug("Delegate verifier failed"); //$NON-NLS-1$
            try {

                Certificate[] peerCertificates = session.getPeerCertificates();
                X509Certificate peerChain[] = new X509Certificate[peerCertificates.length];
                int i = 0;
                for ( Certificate cert : peerCertificates ) {
                    if ( ! ( cert instanceof X509Certificate ) ) {
                        log.warn("Non X509 certificate found"); //$NON-NLS-1$
                        return false;
                    }
                    peerChain[ i++ ] = (X509Certificate) cert;
                }
                this.failed.put(hostname, peerChain);
            }
            catch ( SSLPeerUnverifiedException e ) {
                log.debug("Peer certificate validation failed", e); //$NON-NLS-1$
            }
            return false;
        }

        return true;
    }


    /**
     * @return the failed
     */
    public Map<String, X509Certificate[]> getFailed () {
        return Collections.unmodifiableMap(this.failed);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.ExtendedHostnameVerifier#isBypassBuiltinChecks()
     */
    @Override
    public boolean isBypassBuiltinChecks () {
        return this.cfg.getDisableHostnameVerification()
                || ( this.delegate instanceof ExtendedHostnameVerifier && ( (ExtendedHostnameVerifier) this.delegate ).isBypassBuiltinChecks() );
    }
}