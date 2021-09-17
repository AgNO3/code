/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.broker.transport;


/**
 * @author mbechler
 * 
 */
public final class TransportConfiguration {

    private TransportConfiguration () {}

    /**
     * Configuration PID for VM transport
     */
    public static final String VM_PID = "messaging.broker.transport.vm"; //$NON-NLS-1$

    /**
     * VM Transport: Broker name
     */
    public static final String VM_BROKER_ATTR = "brokerName"; //$NON-NLS-1$

    /**
     * Configuration PID for TCP transport
     */
    public static final String TCP_PID = "messaging.broker.transport.tcp"; //$NON-NLS-1$

    /**
     * Configuration PID for SSL transport
     */
    public static final String SSL_PID = "messaging.broker.transport.ssl"; //$NON-NLS-1$

    /**
     * Network transports: bind address
     */
    public static final String BIND_ADDRESS_ATTR = "bindTo"; //$NON-NLS-1$

    /**
     * Network transport: bind port
     */
    public static final String BIND_PORT_ATTR = "port"; //$NON-NLS-1$

    /**
     * Username to use for default connection
     */
    public static final Object USER_ATTR = "user"; //$NON-NLS-1$

    /**
     * Password to use for default connection
     */
    public static final String PASSWORD_ATTR = "password"; //$NON-NLS-1$

    /**
     * Connection factory URL service property
     */
    public static final String CONNECTION_FACTORY_URL_PROP = "url"; //$NON-NLS-1$

    /**
     * Connection factory type service property
     */
    public static final String CONNECTION_FACTORY_TYPE_PROP = "type"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String CONNECTION_FACTORY_MAX_POOL_SIZE = "maxPoolSize"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String CONNECTION_FACTORY_BORROW_TIMEOUT = "borrowTimeout"; //$NON-NLS-1$
}
