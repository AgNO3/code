/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 29, 2016 by mbechler
 */
package eu.agno3.runtime.http.service.tls.internal;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SecureRequestCustomizer;


/**
 * @author mbechler
 *
 */
public class ExtendedSecureRequestCustomizer extends SecureRequestCustomizer {

    private static final Logger log = Logger.getLogger(ExtendedSecureRequestCustomizer.class);

    private static final String DIGEST_ALG = "SHA-256"; //$NON-NLS-1$
    private static final String KEY = "agno3.tlsSessionIdHash"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.jetty.server.SecureRequestCustomizer#customize(javax.net.ssl.SSLEngine,
     *      org.eclipse.jetty.server.Request)
     */
    @Override
    protected void customize ( SSLEngine engine, Request req ) {
        super.customize(engine, req);
        SSLSession sslSession = engine.getSession();
        byte[] cached = (byte[]) sslSession.getValue(KEY);
        if ( cached == null ) {
            try {
                MessageDigest dgst = MessageDigest.getInstance(DIGEST_ALG);
                cached = dgst.digest(sslSession.getId());
                sslSession.putValue(KEY, cached);
            }
            catch ( NoSuchAlgorithmException e ) {
                log.warn("Failed to create session id hash", e); //$NON-NLS-1$
            }
        }
        req.setAttribute(KEY, cached);
    }
}
