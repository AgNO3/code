/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.truststore.revocation;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509CRL;

import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public final class RemoteLoaderUtil {

    private static final Logger log = Logger.getLogger(RemoteLoaderUtil.class);


    /**
     * 
     */
    private RemoteLoaderUtil () {}


    /**
     * @param params
     * @param rand
     * @param currentCrl
     * @param uriToTry
     * @param connectTimeout
     * @param readTimeout
     * @param maxRedirects
     * @return the loaded CRL (not validated!), or null if load failed
     * @throws IOException
     */
    public static X509CRL loadCRL ( PKIXBuilderParameters params, SecureRandom rand, X509CRL currentCrl, URI uriToTry, int connectTimeout,
            int readTimeout, int maxRedirects ) throws IOException {
        try {
            URL toLoad = uriToTry.toURL();
            Long ifModifiedSince = null;
            if ( currentCrl != null ) {
                ifModifiedSince = currentCrl.getThisUpdate().getTime();
            }

            HttpURLConnection httpConn = getHttpCRLConnection(params, rand, ifModifiedSince, toLoad, connectTimeout, readTimeout, maxRedirects);
            if ( httpConn == null ) {
                return null;
            }

            if ( httpConn.getResponseCode() == 304 ) {
                log.debug("Not modified since last check"); //$NON-NLS-1$
                return currentCrl;
            }

            try ( InputStream is = httpConn.getInputStream() ) {
                return parseCRL(is);
            }
            catch ( CRLException e ) {
                log.warn("Recieved invalid CRL from " + uriToTry, e); //$NON-NLS-1$
                return null;
            }

        }
        catch ( GeneralSecurityException e ) {
            throw new IOException("Failed to load CRL", e); //$NON-NLS-1$
        }
    }


    protected static HttpURLConnection getHttpCRLConnection ( PKIXBuilderParameters params, SecureRandom rand, Long ifModifiedSince, URL toLoad,
            int connectTimeout, int readTimeout, int maxRedirects ) throws MalformedURLException, IOException {

        if ( maxRedirects <= 0 ) {
            throw new IOException("Too many redirects"); //$NON-NLS-1$
        }

        URLConnection conn = toLoad.openConnection();
        if ( ! ( conn instanceof HttpURLConnection ) ) {
            log.warn("Not a HTTP connection for " + toLoad); //$NON-NLS-1$
            return null;
        }
        HttpURLConnection httpConn = (HttpURLConnection) conn;
        setupConnection(httpConn, rand, ifModifiedSince, params, connectTimeout, readTimeout);

        return handleResponse(params, rand, ifModifiedSince, toLoad, connectTimeout, readTimeout, maxRedirects, httpConn);
    }


    /**
     * @param params
     * @param currentCrl
     * @param toLoad
     * @param connectTimeout
     * @param readTimeout
     * @param maxRedirects
     * @param httpConn
     * @return
     * @throws IOException
     * @throws MalformedURLException
     */
    protected static HttpURLConnection handleResponse ( PKIXBuilderParameters params, SecureRandom rand, Long ifModifiedSince, URL toLoad,
            int connectTimeout, int readTimeout, int maxRedirects, HttpURLConnection httpConn ) throws IOException, MalformedURLException {
        if ( httpConn.getResponseCode() == 301 || httpConn.getResponseCode() == 302 || httpConn.getResponseCode() == 303
                || httpConn.getResponseCode() == 307 || httpConn.getResponseCode() == 308 ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Got redirect that was not followed by client from " + toLoad); //$NON-NLS-1$
            }
            String location = httpConn.getHeaderField("Location"); //$NON-NLS-1$
            if ( location == null ) {
                return null;
            }

            URL redirectURL = new URL(toLoad, location);
            return getHttpCRLConnection(params, rand, ifModifiedSince, redirectURL, connectTimeout, readTimeout, maxRedirects - 1);
        }

        if ( httpConn.getResponseCode() != 200 ) {
            log.warn(String.format(
                "HTTP %d error %s while loading %s", //$NON-NLS-1$
                httpConn.getResponseCode(),
                httpConn.getResponseMessage(),
                toLoad));
            return null;
        }

        return httpConn;
    }


    /**
     * @param cf
     * @param issuer
     * @param uriToTry
     * @param is
     * @return
     * @throws CRLException
     * @throws CertificateException
     */
    protected static X509CRL parseCRL ( InputStream is ) throws CRLException, CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X509"); //$NON-NLS-1$
        CRL crl = cf.generateCRL(is);
        if ( ! ( crl instanceof X509CRL ) ) {
            log.warn("Did not obtain a X509CRL"); //$NON-NLS-1$
            return null;
        }

        return (X509CRL) crl;
    }


    protected static void setupConnection ( HttpURLConnection conn, SecureRandom rand, Long ifModifiedSince, PKIXBuilderParameters params,
            int connectTimeout, int readTimeout ) throws IOException {
        conn.setAllowUserInteraction(false);
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);
        conn.setInstanceFollowRedirects(true);

        if ( ifModifiedSince != null ) {
            conn.setIfModifiedSince(ifModifiedSince);
        }

        if ( conn instanceof HttpsURLConnection ) {
            try {
                setupSSL((HttpsURLConnection) conn, params, rand);
            }
            catch (
                NoSuchAlgorithmException |
                KeyManagementException |
                InvalidAlgorithmParameterException e ) {
                throw new IOException("Failed to setup SSL for distribution point", e); //$NON-NLS-1$
            }
        }

    }


    /**
     * @param conn
     * @param certs
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws InvalidAlgorithmParameterException
     */
    private static void setupSSL ( HttpsURLConnection conn, PKIXBuilderParameters params, SecureRandom rand )
            throws NoSuchAlgorithmException, KeyManagementException, InvalidAlgorithmParameterException {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX"); //$NON-NLS-1$
        tmf.init(new CertPathTrustManagerParameters(params));
        SSLContext context = SSLContext.getInstance("TLSv1.2"); //$NON-NLS-1$
        context.init(null, tmf.getTrustManagers(), rand);
        conn.setSSLSocketFactory(context.getSocketFactory());
    }

}
