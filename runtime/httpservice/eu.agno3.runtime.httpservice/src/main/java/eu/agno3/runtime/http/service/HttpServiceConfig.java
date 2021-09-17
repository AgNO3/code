/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2013 by mbechler
 */
package eu.agno3.runtime.http.service;


/**
 * @author mbechler
 * 
 */
public final class HttpServiceConfig {

    private HttpServiceConfig () {}

    /**
     * PID of http service
     */
    public static final String PID = "http.service"; //$NON-NLS-1$

    /**
     * Connector to use
     */
    public static final String CONNECTORS = "connectors"; //$NON-NLS-1$

    /**
     * Default connector specification
     */
    public static final String CONNECTOR_DEFAULT = "http"; //$NON-NLS-1$

    /**
     * Exported connector name
     */
    public static final String CONNECTOR_NAME = "name"; //$NON-NLS-1$

    /**
     * Port to bind to (connector)
     */
    public static final String PORT_ATTRIBUTE = "port"; //$NON-NLS-1$

    /**
     * Address to bind to (connector)
     */
    public static final String BIND_ATTRIBUTE = "bind"; //$NON-NLS-1$

    /**
     * Idle timeout in ms (connector)
     */
    public static final String IDLE_TIMEOUT_ATTRIBUTE = "idleTimeout"; //$NON-NLS-1$

    /**
     * Reuse address (connector)
     */
    public static final String SOCKREUSE_ATTRIBUTE = "reuseAddress"; //$NON-NLS-1$

    /**
     * SO_LINGER (connector)
     */
    public static final String SOCKLINGER_ATTRIBUTE = "soLingerTime"; //$NON-NLS-1$

    /**
     * accept queue size (connector)
     */
    public static final String ACCEPT_QUEUE_SIZE_ATTRIBUTE = "acceptQueueSize"; //$NON-NLS-1$

    /**
     * Header cache size (connector)
     */
    public static final String HEADER_CACHE_SIZE_ATTRIBUTE = "headerCacheSize"; //$NON-NLS-1$

    /**
     * Output buffer size (connector)
     */
    public static final String OUTPUT_BUFFER_SIZE_ATTRIBUTE = "outputBufferSize"; //$NON-NLS-1$

    /**
     * Maximum request header size
     */
    public static final String REQUEST_HEADER_SIZE_ATTRIBUTE = "requestHeaderSize"; //$NON-NLS-1$

    /**
     * Maximum response header size
     */
    public static final String RESPONSE_HEADER_SIZE_ATTRIBUTE = "responseHeaderSize"; //$NON-NLS-1$

    /**
     * Port for CONFIDENTIAL and INTEGRAL redirections (http connector)
     */
    public static final String SECURE_PORT_ATTRIBUTE = "securePort"; //$NON-NLS-1$

    /**
     * Scheme for CONFIDENTIAL and INTEGRAL redirections (http connector)
     */
    public static final String SECURE_SCHEME_ATTRIBUTE = "secureScheme"; //$NON-NLS-1$

    /**
     * Send server date header with response (connector)
     */
    public static final String SEND_DATE_ATTRIBUTE = "sendDate"; //$NON-NLS-1$

    /**
     * Send server identity with response (connector)
     */
    public static final String SEND_VERSION_ATTRIBUTE = "sendVersion"; //$NON-NLS-1$

    /**
     * Override the connector base URI
     */
    public static final String OVERRIDE_BASE_URI = "overrideUri"; //$NON-NLS-1$
}
