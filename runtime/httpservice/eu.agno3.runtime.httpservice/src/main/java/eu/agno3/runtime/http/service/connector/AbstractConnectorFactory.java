/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2013 by mbechler
 */
package eu.agno3.runtime.http.service.connector;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.runtime.http.service.HttpServiceConfig;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractConnectorFactory implements ConnectorFactory {

    private static final Logger log = Logger.getLogger(AbstractConnectorFactory.class);

    private int port = 0;
    private List<InetAddress> bindAddresses = Collections.singletonList(InetAddress.getLoopbackAddress());

    private int idleTimeout = 30000;
    private boolean reuseAddress = true;
    private int acceptQueueSize = -1;

    private String connectorName;


    /**
     * @return the port
     */
    public final int getPort () {
        return this.port;
    }


    /**
     * @return the bindAddress
     */
    public final List<InetAddress> getBindAddresses () {
        return this.bindAddresses;
    }


    /**
     * @return the idleTimeout
     */
    public final int getIdleTimeout () {
        return this.idleTimeout;
    }


    /**
     * @return the reuseAddress
     */
    public final boolean isReuseAddress () {
        return this.reuseAddress;
    }


    /**
     * @return the acceptQueueSize
     */
    public final int getAcceptQueueSize () {
        return this.acceptQueueSize;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.http.service.connector.ConnectorFactory#createConnectors(org.eclipse.jetty.server.Server)
     */
    @Override
    public List<ServerConnector> createConnectors ( Server s ) {
        List<ServerConnector> connectors = new ArrayList<>();
        List<ConnectionFactory> wrapped = wrapConnectionFactories(createConnectionFactories());
        ConnectionFactory[] cfgs = wrapped.toArray(new ConnectionFactory[0]);
        for ( InetAddress bindAddress : this.bindAddresses ) {
            @SuppressWarnings ( "resource" )
            ServerConnector connector = new ServerConnector(s, cfgs);
            connector.setPort(this.port);
            connector.setHost(bindAddress.getHostAddress());

            if ( this.acceptQueueSize > 0 ) {
                connector.setAcceptQueueSize(this.acceptQueueSize);
            }
            connector.setReuseAddress(this.reuseAddress);
            connector.setIdleTimeout(this.idleTimeout);
            connector.setName(getConnectorName());
            if ( log.isTraceEnabled() ) {
                log.trace("Created connector with name " + connector.getName()); //$NON-NLS-1$
            }

            connectors.add(connector);
        }

        return connectors;
    }


    /**
     * @param createConnectionFactories
     * @return
     */
    protected List<ConnectionFactory> wrapConnectionFactories ( List<ConnectionFactory> cfs ) {
        return cfs;
    }


    /**
     * @return connection factories used for this connector
     */
    protected List<ConnectionFactory> createConnectionFactories () {
        return Collections.singletonList(createConnectionFactory());
    }


    /**
     * @return a single connection factory used for this connector
     */
    protected abstract ConnectionFactory createConnectionFactory ();


    /**
     * Update configuration from service propertites
     * 
     * If overriding, always call the parent function to setup basic properties.
     * 
     * @param context
     */
    @Activate
    @Modified
    protected void updateConfig ( ComponentContext context ) {
        Dictionary<String, Object> props = context.getProperties();

        this.connectorName = (String) props.get(HttpServiceConfig.CONNECTOR_NAME);

        if ( props.get(HttpServiceConfig.PORT_ATTRIBUTE) != null ) {
            this.port = Integer.parseInt((String) props.get(HttpServiceConfig.PORT_ATTRIBUTE));

        }

        Collection<String> bindAddrSpecs = ConfigUtil.parseStringCollection(props, HttpServiceConfig.BIND_ATTRIBUTE, Collections.EMPTY_LIST);
        List<InetAddress> bindAddrs = new ArrayList<>();

        for ( String bindAddrSpec : bindAddrSpecs ) {
            try {
                bindAddrs.add(InetAddress.getByName(bindAddrSpec.trim()));
            }
            catch ( UnknownHostException e ) {
                log.error("Failed to resolve bind address:", e); //$NON-NLS-1$
            }
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Bind addresses are " + bindAddrs); //$NON-NLS-1$
        }
        this.bindAddresses = bindAddrs;

        if ( props.get(HttpServiceConfig.IDLE_TIMEOUT_ATTRIBUTE) != null ) {
            this.idleTimeout = Integer.parseInt((String) props.get(HttpServiceConfig.IDLE_TIMEOUT_ATTRIBUTE));
        }

        String soReuseSpec = (String) props.get(HttpServiceConfig.SOCKREUSE_ATTRIBUTE);
        if ( soReuseSpec != null ) {
            if ( soReuseSpec.equals(Boolean.TRUE.toString()) ) {
                this.reuseAddress = true;
            }
            else if ( soReuseSpec.equals(Boolean.FALSE.toString()) ) {
                this.reuseAddress = false;
            }
        }

        if ( props.get(HttpServiceConfig.ACCEPT_QUEUE_SIZE_ATTRIBUTE) != null ) {
            this.acceptQueueSize = Integer.parseInt((String) props.get(HttpServiceConfig.ACCEPT_QUEUE_SIZE_ATTRIBUTE));
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.http.service.connector.ConnectorFactory#getConnectorName()
     */
    @Override
    public String getConnectorName () {
        return this.connectorName;
    }
}
