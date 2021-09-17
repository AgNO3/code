/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2015 by mbechler
 */
package eu.agno3.runtime.ldap.client.internal;


import java.io.IOException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;

import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.util.ssl.SSLSocketVerifier;


/**
 * @author mbechler
 *
 */
public class DelegatingSSLSocketVerifier extends SSLSocketVerifier {

    private HostnameVerifier hostnameVerifier;


    /**
     * @param hostnameVerifier
     */
    public DelegatingSSLSocketVerifier ( HostnameVerifier hostnameVerifier ) {
        this.hostnameVerifier = hostnameVerifier;
    }


    /**
     * {@inheritDoc}
     *
     * @see com.unboundid.util.ssl.SSLSocketVerifier#verifySSLSocket(java.lang.String, int, javax.net.ssl.SSLSocket)
     */
    @Override
    public void verifySSLSocket ( String host, int post, SSLSocket sock ) throws LDAPException {

        if ( "SSL_NULL_WITH_NULL_NULL".equals(sock.getSession().getCipherSuite()) ) { //$NON-NLS-1$
            try {
                sock.startHandshake();
            }
            catch ( IOException e ) {
                throw new LDAPException(ResultCode.CONNECT_ERROR, "SSL handshake failure, possibly cipher or protocol mismatch", e); //$NON-NLS-1$
            }
        }

        if ( !this.hostnameVerifier.verify(host, sock.getSession()) ) {
            throw new LDAPException(
                ResultCode.CONNECT_ERROR,
                "Failed to verify remote identity " + host, //$NON-NLS-1$
                new SSLPeerUnverifiedException("Hostname mismatch")); //$NON-NLS-1$
        }
    }
}
