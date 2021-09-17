/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.01.2017 by mbechler
 */
package eu.agno3.runtime.crypto.tls;


import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * @author mbechler
 *
 */
public class X509TrustManagerWrapper extends X509ExtendedTrustManager {

    private final X509TrustManager delegate;


    /**
     * @param tm
     * 
     */
    public X509TrustManagerWrapper ( X509TrustManager tm ) {
        Objects.requireNonNull(tm);
        this.delegate = tm;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
     */
    @Override
    public X509Certificate[] getAcceptedIssuers () {
        return this.delegate.getAcceptedIssuers();
    }


    /**
     * @param chain
     * @param authType
     * @param object
     * @return whether to skip remaining validation
     * @throws CertificateException
     */
    protected boolean preValidate ( X509Certificate[] chain, String authType, SSLSession sess ) throws CertificateException {
        return false;
    }


    /**
     * @param chain
     * @param authType
     * @param object
     * @throws CertificateException
     */
    protected void postValidate ( X509Certificate[] chain, String authType, SSLSession sess ) throws CertificateException {

    }


    /**
     * @param e
     * @param chain
     * @param object
     * @return whether to ignore the validation error
     * @throws CertificateException
     */
    protected boolean handleValidationError ( CertificateException e, X509Certificate[] chain, String authType, SSLSession sess ) {
        return false;
    }


    /**
     * @param chain
     * @param authType
     * @param object
     * @return whether to skip remaining validation
     * @throws CertificateException
     */
    protected boolean preValidateClient ( X509Certificate[] chain, String authType, SSLSession sess ) throws CertificateException {
        return preValidate(chain, authType, sess);
    }


    /**
     * @param chain
     * @param authType
     * @param object
     * @throws CertificateException
     */
    protected void postValidateClient ( X509Certificate[] chain, String authType, SSLSession sess ) throws CertificateException {
        postValidate(chain, authType, sess);
    }


    /**
     * @param e
     * @param chain
     * @param object
     * @return whether to ignore the validation error
     * @throws CertificateException
     */
    protected boolean handleClientValidationError ( CertificateException e, X509Certificate[] chain, String authType, SSLSession sess )
            throws CertificateException {
        return handleValidationError(e, chain, authType, sess);
    }


    /**
     * @param chain
     * @param authType
     * @param object
     * @return whether to skip remaining validation
     * @throws CertificateException
     */
    protected boolean preValidateServer ( X509Certificate[] chain, String authType, SSLSession sess ) throws CertificateException {
        return preValidate(chain, authType, sess);
    }


    /**
     * @param chain
     * @param authType
     * @param object
     * @throws CertificateException
     */
    protected void postValidateServer ( X509Certificate[] chain, String authType, SSLSession sess ) throws CertificateException {
        postValidate(chain, authType, sess);
    }


    /**
     * @param e
     * @param chain
     * @param object
     * @return whether to ignore the validation error
     * @throws CertificateException
     */
    protected boolean handleServerValidationError ( CertificateException e, X509Certificate[] chain, String authType, SSLSession sess )
            throws CertificateException {
        return handleValidationError(e, chain, authType, sess);
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[], java.lang.String)
     */
    @Override
    public final void checkClientTrusted ( X509Certificate[] chain, String authType ) throws CertificateException {
        if ( preValidateClient(chain, authType, null) ) {
            return;
        }

        try {
            this.delegate.checkClientTrusted(chain, authType);
            postValidateClient(chain, authType, null);
        }
        catch ( CertificateException e ) {
            if ( handleClientValidationError(e, chain, authType, null) ) {
                return;
            }
            throw e;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.net.ssl.X509ExtendedTrustManager#checkClientTrusted(java.security.cert.X509Certificate[],
     *      java.lang.String, java.net.Socket)
     */
    @Override
    public final void checkClientTrusted ( X509Certificate[] chain, String authType, Socket socket ) throws CertificateException {
        SSLSession sess = ( socket instanceof SSLSocket ) ? ( (SSLSocket) socket ).getHandshakeSession() : null;
        if ( preValidateClient(chain, authType, sess) ) {
            return;
        }

        try {
            this.delegate.checkClientTrusted(chain, authType);
            postValidateClient(chain, authType, sess);
        }
        catch ( CertificateException e ) {
            if ( handleClientValidationError(e, chain, authType, sess) ) {
                return;
            }
            throw e;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.net.ssl.X509ExtendedTrustManager#checkClientTrusted(java.security.cert.X509Certificate[],
     *      java.lang.String, javax.net.ssl.SSLEngine)
     */
    @Override
    public final void checkClientTrusted ( X509Certificate[] chain, String authType, SSLEngine engine ) throws CertificateException {
        SSLSession sess = engine.getHandshakeSession();
        if ( preValidateClient(chain, authType, sess) ) {
            return;
        }

        try {
            this.delegate.checkClientTrusted(chain, authType);
            postValidateClient(chain, authType, sess);
        }
        catch ( CertificateException e ) {
            if ( handleClientValidationError(e, chain, authType, sess) ) {
                return;
            }
            throw e;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[], java.lang.String)
     */
    @Override
    public final void checkServerTrusted ( X509Certificate[] chain, String authType ) throws CertificateException {
        if ( preValidateServer(chain, authType, null) ) {
            return;
        }

        try {
            this.delegate.checkServerTrusted(chain, authType);
            postValidateServer(chain, authType, null);
        }
        catch ( CertificateException e ) {
            if ( handleServerValidationError(e, chain, authType, null) ) {
                return;
            }
            throw e;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.net.ssl.X509ExtendedTrustManager#checkServerTrusted(java.security.cert.X509Certificate[],
     *      java.lang.String, java.net.Socket)
     */
    @Override
    public final void checkServerTrusted ( X509Certificate[] chain, String authType, Socket socket ) throws CertificateException {
        SSLSession sess = ( socket instanceof SSLSocket ) ? ( (SSLSocket) socket ).getHandshakeSession() : null;
        if ( preValidateServer(chain, authType, sess) ) {
            return;
        }

        try {
            this.delegate.checkServerTrusted(chain, authType);
            postValidateServer(chain, authType, sess);
        }
        catch ( CertificateException e ) {
            if ( handleServerValidationError(e, chain, authType, sess) ) {
                return;
            }
            throw e;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.net.ssl.X509ExtendedTrustManager#checkServerTrusted(java.security.cert.X509Certificate[],
     *      java.lang.String, javax.net.ssl.SSLEngine)
     */
    @Override
    public final void checkServerTrusted ( X509Certificate[] chain, String authType, SSLEngine engine ) throws CertificateException {
        SSLSession sess = engine.getHandshakeSession();
        if ( preValidateServer(chain, authType, sess) ) {
            return;
        }

        try {
            this.delegate.checkServerTrusted(chain, authType);
            postValidateServer(chain, authType, sess);
        }
        catch ( CertificateException e ) {
            if ( handleServerValidationError(e, chain, authType, sess) ) {
                return;
            }
            throw e;
        }
    }

}
