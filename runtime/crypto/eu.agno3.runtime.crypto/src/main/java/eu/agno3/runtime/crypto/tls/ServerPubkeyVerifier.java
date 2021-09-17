/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.01.2015 by mbechler
 */
package eu.agno3.runtime.crypto.tls;


import java.security.PublicKey;
import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.security.cert.X509Certificate;

import org.apache.log4j.Logger;


/**
 * 
 * 
 * @author mbechler
 *
 */
public class ServerPubkeyVerifier implements HostnameVerifier {

    private static final Logger log = Logger.getLogger(ServerPubkeyVerifier.class);

    private PublicKey pubkey;
    private HostnameVerifier delegate;


    /**
     * @param pubkey
     * @param delegate
     */
    public ServerPubkeyVerifier ( PublicKey pubkey, HostnameVerifier delegate ) {
        this.pubkey = pubkey;
        this.delegate = delegate;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.net.ssl.HostnameVerifier#verify(java.lang.String, javax.net.ssl.SSLSession)
     */
    @Override
    public boolean verify ( String addr, SSLSession sess ) {

        if ( this.pubkey == null ) {
            return this.delegate.verify(addr, sess);
        }

        X509Certificate[] chain;
        try {
            chain = sess.getPeerCertificateChain();
        }
        catch ( SSLPeerUnverifiedException e ) {
            log.warn("Unverified peer", e); //$NON-NLS-1$
            return this.delegate.verify(addr, sess);
        }

        X509Certificate eeCert = chain[ 0 ];
        PublicKey peerPubkey = eeCert.getPublicKey();

        if ( log.isDebugEnabled() ) {
            log.debug("Key is " + peerPubkey); //$NON-NLS-1$
        }

        if ( peerPubkey == null ) {
            return this.delegate.verify(addr, sess);
        }

        if ( Arrays.equals(this.pubkey.getEncoded(), peerPubkey.getEncoded()) ) {
            return true;
        }

        return this.delegate.verify(addr, sess);
    }
}
