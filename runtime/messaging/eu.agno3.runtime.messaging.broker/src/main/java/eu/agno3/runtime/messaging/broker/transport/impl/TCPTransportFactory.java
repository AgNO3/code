/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.broker.transport.impl;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import javax.jms.XAConnectionFactory;
import javax.net.ServerSocketFactory;

import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.apache.activemq.transport.TransportServer;
import org.apache.activemq.transport.nio.NIOTransportFactory;
import org.apache.activemq.transport.tcp.TcpTransportFactory;
import org.apache.activemq.transport.tcp.TcpTransportServer;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import eu.agno3.runtime.messaging.broker.BrokerConfigurationException;
import eu.agno3.runtime.messaging.broker.transport.TransportConfiguration;
import eu.agno3.runtime.messaging.broker.transport.TransportFactory;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    TransportFactory.class
}, configurationPid = TransportConfiguration.TCP_PID, configurationPolicy = ConfigurationPolicy.REQUIRE )
public class TCPTransportFactory implements TransportFactory {

    /**
     * Default port for TCP connector
     */
    public static final int DEFAULT_PORT = 61616;

    private URI location;
    private String user;
    private String password;

    private int maxPoolSize;
    private int borrowTimeout;


    @Activate
    protected void activate ( ComponentContext context ) throws BrokerConfigurationException, IOException {

        String bindToSpec = (String) context.getProperties().get(TransportConfiguration.BIND_ADDRESS_ATTR);

        if ( bindToSpec == null ) {
            throw new BrokerConfigurationException("No bind address configured for TCP connector"); //$NON-NLS-1$
        }

        String bindPortSpec = (String) context.getProperties().get(TransportConfiguration.BIND_PORT_ATTR);

        int port = DEFAULT_PORT;
        if ( bindPortSpec != null ) {
            port = Integer.parseInt(bindPortSpec);
        }

        try {
            this.location = createBrokerURI(bindToSpec, port);
        }
        catch ( URISyntaxException e ) {
            throw new BrokerConfigurationException(e);
        }

        this.user = (String) context.getProperties().get(TransportConfiguration.USER_ATTR);
        this.password = ConfigUtil.parseSecret(context.getProperties(), TransportConfiguration.PASSWORD_ATTR, null);

        this.maxPoolSize = ConfigUtil.parseInt(context.getProperties(), "maxPoolSize", 30); //$NON-NLS-1$
        this.borrowTimeout = ConfigUtil.parseInt(context.getProperties(), "borrowTimeout", 10); //$NON-NLS-1$

    }


    protected URI createBrokerURI ( String bindToSpec, int port ) throws URISyntaxException {
        return new URI("tcp", null, bindToSpec, port, null, null, null); //$NON-NLS-1$
    }


    /**
     * @return the location
     */
    protected URI getLocation () {
        return this.location;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.broker.transport.TransportFactory#createTransport()
     */
    @Override
    public TransportServer createTransport () throws BrokerConfigurationException {
        try {
            TcpTransportFactory tf = new NIOTransportFactory();
            TcpTransportServer s = new TcpTransportServer(tf, this.location, ServerSocketFactory.getDefault());
            s.setTransportOption(new HashMap<String, Object>());
            s.setDaemon(true);
            s.bind();
            return s;
        }
        catch (
            URISyntaxException |
            IOException e ) {
            throw new BrokerConfigurationException(e);
        }

    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.broker.transport.TransportFactory#createConnectionFactory()
     */
    @Override
    public XAConnectionFactory createConnectionFactory () {
        return new ActiveMQXAConnectionFactory(this.user, this.password, this.location);

    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.broker.transport.TransportFactory#getBrokerURI()
     */
    @Override
    public URI getBrokerURI () {
        return this.location;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.broker.transport.TransportFactory#getBorrowTimeout()
     */
    @Override
    public int getBorrowTimeout () {
        return this.borrowTimeout;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.broker.transport.TransportFactory#getPoolSize()
     */
    @Override
    public int getPoolSize () {
        return this.maxPoolSize;
    }

}
