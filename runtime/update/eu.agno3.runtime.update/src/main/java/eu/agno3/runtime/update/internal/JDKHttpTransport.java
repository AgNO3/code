/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.09.2014 by mbechler
 */
package eu.agno3.runtime.update.internal;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.repository.AuthenticationFailedException;
import org.eclipse.equinox.internal.p2.repository.Transport;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.update.License;
import eu.agno3.runtime.update.LicensingService;


/**
 * @author mbechler
 * 
 */
@SuppressWarnings ( "restriction" )
@Component ( service = Transport.class )
public class JDKHttpTransport extends Transport {

    private static final Logger log = Logger.getLogger(JDKHttpTransport.class);

    private static final String GET_METHOD = "GET"; //$NON-NLS-1$
    private static final String HEAD_METHOD = "HEAD"; //$NON-NLS-1$

    private static final String PLUGIN_ID = "eu.agno3.runtime.update"; //$NON-NLS-1$

    private static final DateTimeFormatter RFC1123_DATE_TIME_FORMATTER = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withZoneUTC() //$NON-NLS-1$
            .withLocale(Locale.US);

    private static final int BUFFER_SIZE = 4096 * 4;

    private TLSContext tlsContext;

    private LicensingService licensingService;


    @Reference ( target = "(|(subsystem=runtime/update/httpClient)(role=client)(role=default))" )
    protected void setTLSContext ( TLSContext tc ) {
        this.tlsContext = tc;
    }


    protected void unsetTLSContext ( TLSContext tc ) {
        if ( this.tlsContext == tc ) {
            this.tlsContext = null;
        }
    }


    @Reference
    protected void setLicensingService ( LicensingService lic ) {
        this.licensingService = lic;
    }


    protected void unsetLicensingService ( LicensingService lic ) {
        if ( this.licensingService == lic ) {
            this.licensingService = null;
        }
    }


    @Override
    public InputStream stream ( URI toDownload, IProgressMonitor monitor )
            throws FileNotFoundException, CoreException, AuthenticationFailedException {
        HttpURLConnection conn = createHttpClient(toDownload);

        if ( log.isDebugEnabled() ) {
            log.debug("Streaming " + toDownload); //$NON-NLS-1$
        }

        int status;
        try {
            conn.setRequestMethod(GET_METHOD);
            setupConn(conn);
            conn.connect();
            status = conn.getResponseCode();
        }
        catch ( IOException e ) {
            log.warn("Failed to connect:", e); //$NON-NLS-1$
            throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, "Failed connect", e)); //$NON-NLS-1$
        }

        if ( status == HttpURLConnection.HTTP_FORBIDDEN || status == HttpURLConnection.HTTP_UNAUTHORIZED ) {
            throw new AuthenticationFailedException();
        }
        else if ( status == HttpURLConnection.HTTP_NOT_FOUND ) {
            throw new FileNotFoundException();
        }

        try {
            return conn.getInputStream();
        }
        catch ( IOException e ) {
            log.debug("Failed to open stream:", e); //$NON-NLS-1$
            throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, "Failed to open stream", e)); //$NON-NLS-1$
        }
    }


    @Override
    public long getLastModified ( URI toDownload, IProgressMonitor monitor )
            throws CoreException, FileNotFoundException, AuthenticationFailedException {

        HttpURLConnection conn = createHttpClient(toDownload);

        int status;
        try {
            conn.setRequestMethod(HEAD_METHOD);
            setupConn(conn);
            conn.connect();
            status = conn.getResponseCode();
        }
        catch ( IOException e ) {
            log.debug("Failed to connect:", e); //$NON-NLS-1$
            throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, "Failed connect", e)); //$NON-NLS-1$
        }

        if ( status == HttpURLConnection.HTTP_FORBIDDEN || status == HttpURLConnection.HTTP_UNAUTHORIZED ) {
            throw new AuthenticationFailedException();
        }
        else if ( status == HttpURLConnection.HTTP_NOT_FOUND ) {
            throw new FileNotFoundException();
        }

        return conn.getLastModified();
    }


    private HttpURLConnection createHttpClient ( URI toDownload ) throws CoreException {
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) toDownload.toURL().openConnection();
            setupConn(conn);
        }
        catch ( IOException e ) {
            log.debug("Failed to parse url:", e); //$NON-NLS-1$
            throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, "Failed to parse URL", e)); //$NON-NLS-1$
        }

        if ( conn instanceof HttpsURLConnection ) {
            try {
                setupSSL((HttpsURLConnection) conn);
            }
            catch ( CryptoException e ) {
                log.debug("Failed to setup ssl transport:", e); //$NON-NLS-1$
                throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, "Failed to setup SSL security", e)); //$NON-NLS-1$
            }
        }

        conn.setInstanceFollowRedirects(true);
        conn.setAllowUserInteraction(false);
        return conn;
    }


    /**
     * @param conn
     */
    private void setupConn ( HttpURLConnection conn ) {
        License license = this.licensingService.getLicense();
        if ( license == null ) {
            conn.setRequestProperty(
                "X-Is-Demo", //$NON-NLS-1$
                "true"); //$NON-NLS-1$
        }
        else {
            conn.setRequestProperty(
                "X-License-Id", //$NON-NLS-1$
                license.getLicenseId().toString());
        }
        conn.setRequestProperty("Date", DateTime.now().toString(RFC1123_DATE_TIME_FORMATTER)); //$NON-NLS-1$

    }


    private void setupSSL ( HttpsURLConnection conn ) throws CryptoException {
        conn.setSSLSocketFactory(this.tlsContext.getSocketFactory());
        conn.setHostnameVerifier(this.tlsContext.getHostnameVerifier());

    }


    @Override
    public IStatus download ( URI toDownload, OutputStream target, long startPos, IProgressMonitor monitor ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Downloading " + toDownload); //$NON-NLS-1$
        }
        HttpURLConnection conn;
        try {
            conn = createHttpClient(toDownload);
        }
        catch ( CoreException e ) {
            if ( log.isDebugEnabled() ) {
                log.debug("HTTP client returned response status " + e.getStatus(), e); //$NON-NLS-1$
            }
            return e.getStatus();
        }

        if ( startPos >= 0 ) {
            conn.setRequestProperty(
                "Range", //$NON-NLS-1$
                String.format("bytes=%d-", startPos)); //$NON-NLS-1$
        }

        int status;
        try {
            conn.connect();
            status = conn.getResponseCode();
        }
        catch ( IOException e ) {
            log.warn("Failed to download:", e); //$NON-NLS-1$
            return new Status(IStatus.ERROR, PLUGIN_ID, "Failed to download repository data", e); //$NON-NLS-1$
        }

        if ( status != HttpURLConnection.HTTP_OK ) {
            return new Status(IStatus.ERROR, PLUGIN_ID, "Failed to download repository data: HTTP " + status); //$NON-NLS-1$
        }

        int len = conn.getContentLength();

        if ( log.isDebugEnabled() ) {
            log.debug("Content length is " + len); //$NON-NLS-1$
        }

        if ( len >= 0 ) {
            monitor.beginTask(null, 1000);
            monitor.subTask("Downloading " + toDownload); //$NON-NLS-1$
        }

        long cum = 0;

        byte[] buffer = new byte[BUFFER_SIZE];
        try ( InputStream is = conn.getInputStream() ) {

            while ( true ) {
                int read = is.read(buffer);
                cum += read;

                if ( read == -1 ) {
                    break;
                }
                target.write(buffer, 0, read);
                if ( len >= 0 ) {
                    int work = (int) ( 1000d * cum / len );
                    cum = 0;
                    monitor.worked(work);
                }

                if ( monitor.isCanceled() ) {
                    return Status.CANCEL_STATUS;
                }
            }
        }
        catch ( IOException e ) {
            log.debug("Failed to download:", e); //$NON-NLS-1$
            monitor.done();
            return new Status(IStatus.ERROR, PLUGIN_ID, "Failed to download repository metadata", e); //$NON-NLS-1$
        }

        monitor.done();
        return Status.OK_STATUS;
    }


    @Override
    public IStatus download ( URI toDownload, OutputStream target, IProgressMonitor monitor ) {
        return this.download(toDownload, target, -1, monitor);
    }
}