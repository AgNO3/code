/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.01.2017 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import org.apache.log4j.Logger;

import eu.agno3.runtime.crypto.tls.ExtendedHostnameVerifier;
import eu.agno3.runtime.crypto.tls.TLSConfiguration;


/**
 * @author mbechler
 *
 */
public class PinningHostnameVerifier implements HostnameVerifier, ExtendedHostnameVerifier {

    private static final Logger log = Logger.getLogger(PinningHostnameVerifier.class);

    private HostnameVerifier delegate;
    private TLSConfiguration tc;


    /**
     * @param delegate
     * @param tc
     */
    public PinningHostnameVerifier ( HostnameVerifier delegate, TLSConfiguration tc ) {
        this.delegate = delegate;
        this.tc = tc;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.net.ssl.HostnameVerifier#verify(java.lang.String, javax.net.ssl.SSLSession)
     */
    @Override
    public boolean verify ( String hostname, SSLSession session ) {
        try {
            Certificate[] certs = session.getPeerCertificates();
            if ( certs != null && certs.length >= 1 && certs[ 0 ] instanceof X509Certificate ) {
                X509Certificate eecert = (X509Certificate) certs[ 0 ];
                if ( this.tc.getPinPublicKeys().contains(eecert.getPublicKey()) ) {
                    log.debug(String.format(
                        "Certificate %s [0x%s] public key is pinned, ignoring name checks", //$NON-NLS-1$
                        eecert.getSubjectX500Principal().getName(),
                        eecert.getSerialNumber().toString(16)));
                    return true;
                }
            }
        }
        catch ( SSLPeerUnverifiedException e ) {
            log.debug("Failed to get certificates, call delegate", e); //$NON-NLS-1$
        }
        return this.delegate.verify(hostname, session);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.ExtendedHostnameVerifier#isBypassBuiltinChecks()
     */
    @Override
    public boolean isBypassBuiltinChecks () {
        return this.delegate instanceof ExtendedHostnameVerifier ? ( (ExtendedHostnameVerifier) this.delegate ).isBypassBuiltinChecks() : false;
    }

}
