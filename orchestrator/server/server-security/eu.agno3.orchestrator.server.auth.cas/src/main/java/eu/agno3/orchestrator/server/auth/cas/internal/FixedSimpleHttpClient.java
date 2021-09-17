/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.auth.cas.internal;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.Generated;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jasig.cas.util.http.HttpMessage;


/**
 * @author mbechler
 *
 */
@Generated ( "Copied from CAS client" )
@SuppressWarnings ( "all" )
public class FixedSimpleHttpClient implements org.jasig.cas.util.http.HttpClient {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = -5306738686476129516L;

    private static final Logger log = Logger.getLogger(FixedSimpleHttpClient.class);

    /** The default status codes we accept. */
    private static final int[] DEFAULT_ACCEPTABLE_CODES = new int[] {
        HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_NOT_MODIFIED, HttpURLConnection.HTTP_MOVED_TEMP, HttpURLConnection.HTTP_MOVED_PERM,
        HttpURLConnection.HTTP_ACCEPTED
    };

    private ExecutorService executorService = Executors.newFixedThreadPool(100);

    /** List of HTTP status codes considered valid by this AuthenticationHandler. */
    @NotNull
    @Size ( min = 1 )
    private int[] acceptableCodes = DEFAULT_ACCEPTABLE_CODES;

    @Min ( 0 )
    private int connectionTimeout = 5000;

    @Min ( 0 )
    private int readTimeout = 5000;

    private boolean followRedirects = true;

    /**
     * The socket factory to be used when verifying the validity of the endpoint.
     *
     * @see #setSSLSocketFactory(SSLSocketFactory)
     */
    private SSLSocketFactory sslSocketFactory = null;

    /**
     * The hostname verifier to be used when verifying the validity of the endpoint.
     *
     * @see #setHostnameVerifier(HostnameVerifier)
     */
    private HostnameVerifier hostnameVerifier = null;


    /**
     * Note that changing this executor will affect all httpClients. While not ideal, this change
     * was made because certain ticket registries
     * were persisting the HttpClient and thus getting serializable exceptions.
     * 
     * @param executorService
     *            The executor service to send messages to end points.
     */
    public void setExecutorService ( @NotNull final ExecutorService executorService ) {
        this.executorService = executorService;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see org.jasig.cas.util.http.HttpClient#sendMessageToEndPoint(org.jasig.cas.util.http.HttpMessage)
     */
    @Override
    public boolean sendMessageToEndPoint ( HttpMessage msg ) {

        URL url = msg.getUrl();
        String message = msg.getMessage();
        String contentType = msg.getContentType();
        boolean async = msg.isAsynchronous();

        final Future<Boolean> result = executorService.submit(new MessageSender(
            url,
            message,
            contentType,
            this.readTimeout,
            this.connectionTimeout,
            this.followRedirects,
            this.sslSocketFactory,
            this.hostnameVerifier));

        if ( async ) {
            return true;
        }

        try {
            return result.get();
        }
        catch ( final Exception e ) {
            return false;
        }
    }


    /**
     * Make a synchronous HTTP(S) call to ensure that the url is reachable.
     *
     * @param url
     *            the url to call
     * @return whether the url is valid
     */
    @Override
    public boolean isValidEndPoint ( final String url ) {
        try {
            final URL u = new URL(url);
            return isValidEndPoint(u);
        }
        catch ( final MalformedURLException e ) {
            log.error(e.getMessage(), e);
            return false;
        }
    }


    /**
     * Make a synchronous HTTP(S) call to ensure that the url is reachable.
     *
     * @param url
     *            the url to call
     * @return whether the url is valid
     */
    @Override
    public boolean isValidEndPoint ( final URL url ) {
        HttpURLConnection connection = null;
        InputStream is = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(this.connectionTimeout);
            connection.setReadTimeout(this.readTimeout);
            connection.setInstanceFollowRedirects(this.followRedirects);

            if ( connection instanceof HttpsURLConnection ) {
                final HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;

                setupTLS(httpsConnection);
            }

            connection.connect();

            final int responseCode = connection.getResponseCode();

            for ( final int acceptableCode : this.acceptableCodes ) {
                if ( responseCode == acceptableCode ) {
                    log.debug("Response code from server matched " + responseCode);
                    return true;
                }
            }

            log.debug("Response Code did not match any of the acceptable response codes. Code returned was " + responseCode);

            // if the response code is an error and we don't find that error acceptable above:
            if ( responseCode == 500 ) {
                is = connection.getInputStream();
                final String value = IOUtils.toString(is);
                log.error(String.format("There was an error contacting the endpoint: %s; The error was:\n%s", url.toExternalForm(), value));
            }
        }
        catch ( final IOException e ) {
            log.error(e.getMessage(), e);
        }
        finally {
            IOUtils.closeQuietly(is);
            if ( connection != null ) {
                connection.disconnect();
            }
        }
        return false;
    }


    /**
     * @param httpsConnection
     */
    protected void setupTLS ( final HttpsURLConnection httpsConnection ) {
        if ( this.sslSocketFactory != null ) {
            httpsConnection.setSSLSocketFactory(this.sslSocketFactory);
        }

        if ( this.hostnameVerifier != null ) {
            httpsConnection.setHostnameVerifier(this.hostnameVerifier);
        }
    }


    /**
     * Set the acceptable HTTP status codes that we will use to determine if the
     * response from the URL was correct.
     *
     * @param acceptableCodes
     *            an array of status code integers.
     */
    public void setAcceptableCodes ( final int[] acceptableCodes ) {
        this.acceptableCodes = acceptableCodes;
    }


    /**
     * Sets a specified timeout value, in milliseconds, to be used when opening the endpoint url.
     * 
     * @param connectionTimeout
     *            specified timeout value in milliseconds
     */
    public void setConnectionTimeout ( final int connectionTimeout ) {
        this.connectionTimeout = connectionTimeout;
    }


    /**
     * Sets a specified timeout value, in milliseconds, to be used when reading from the endpoint url.
     * 
     * @param readTimeout
     *            specified timeout value in milliseconds
     */
    public void setReadTimeout ( final int readTimeout ) {
        this.readTimeout = readTimeout;
    }


    /**
     * Determines the behavior on receiving 3xx responses from HTTP endpoints.
     *
     * @param follow
     *            True to follow 3xx redirects (default), false otherwise.
     */
    public void setFollowRedirects ( final boolean follow ) {
        this.followRedirects = follow;
    }


    /**
     * Set the SSL socket factory be used by the URL when submitting
     * request to check for URL endpoint validity.
     * 
     * @param factory
     *            ssl socket factory instance to use
     * @see #isValidEndPoint(URL)
     */
    public void setSSLSocketFactory ( final SSLSocketFactory factory ) {
        this.sslSocketFactory = factory;
    }


    /**
     * Set the hostname verifier be used by the URL when submitting
     * request to check for URL endpoint validity.
     * 
     * @param verifier
     *            hostname verifier instance to use
     * @see #isValidEndPoint(URL)
     */
    public void setHostnameVerifier ( final HostnameVerifier verifier ) {
        this.hostnameVerifier = verifier;
    }


    /**
     * Shutdown the executor service.
     * 
     * @throws Exception
     *             if the executor cannot properly shut down
     */
    public void destroy () throws Exception {
        executorService.shutdown();
    }

    private static final class MessageSender implements Callable<Boolean> {

        private URL url;
        private String message;
        private int readTimeout;
        private int connectionTimeout;
        private boolean followRedirects;
        private SSLSocketFactory sf;
        private HostnameVerifier hv;
        private String contentType;


        public MessageSender ( final URL url, final String message, final String contentType, final int readTimeout, final int connectionTimeout,
                final boolean followRedirects, SSLSocketFactory sf, HostnameVerifier hv ) {
            this.url = url;
            this.message = message;
            this.contentType = contentType;
            this.readTimeout = readTimeout;
            this.connectionTimeout = connectionTimeout;
            this.followRedirects = followRedirects;
            this.sf = sf;
            this.hv = hv;
        }


        @Override
        public Boolean call () throws Exception {
            HttpURLConnection connection = null;
            BufferedReader in = null;
            try {
                if ( log.isDebugEnabled() ) {
                    log.debug("Attempting to access " + url);
                }

                connection = (HttpURLConnection) this.url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setReadTimeout(this.readTimeout);
                connection.setConnectTimeout(this.connectionTimeout);
                connection.setInstanceFollowRedirects(this.followRedirects);
                connection.setRequestProperty("Content-Length", Integer.toString(this.message.getBytes().length));
                connection.setRequestProperty("Content-Type", this.contentType);

                if ( connection instanceof HttpsURLConnection ) {
                    final HttpsURLConnection tlsConn = (HttpsURLConnection) connection;
                    if ( this.sf != null ) {
                        tlsConn.setSSLSocketFactory(this.sf);
                    }
                    if ( this.hv != null ) {
                        tlsConn.setHostnameVerifier(this.hv);
                    }
                }

                final DataOutputStream printout = new DataOutputStream(connection.getOutputStream());
                printout.writeBytes(this.message);
                printout.flush();
                printout.close();

                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                boolean readInput = true;
                while ( readInput ) {
                    readInput = StringUtils.isNotBlank(in.readLine());
                }

                if ( log.isDebugEnabled() ) {
                    log.debug("Finished sending message to " + url);
                }
                return true;
            }
            catch ( final SocketTimeoutException e ) {
                log.warn("Socket Timeout Detected while attempting to send message to " + url);
                return false;
            }
            catch ( final Exception e ) {
                log.warn(String.format("Error Sending message to url endpoint [%s]. Error is [%s]", url, e.getMessage()));
                return false;
            }
            finally {
                IOUtils.closeQuietly(in);
                if ( connection != null ) {
                    connection.disconnect();
                }
            }
        }

    }
}
