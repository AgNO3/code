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

import javax.jms.XAConnectionFactory;

import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.apache.activemq.transport.TransportServer;
import org.apache.activemq.transport.vm.VMTransportServer;
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
}, configurationPid = TransportConfiguration.VM_PID, configurationPolicy = ConfigurationPolicy.REQUIRE, property = {
    "type=vm"
} )
public class VMTransportFactory implements TransportFactory {

    private URI location;
    private String user;
    private String password;
    private int borrowTimeout;
    private int maxPoolSize;


    @Activate
    protected synchronized void activate ( ComponentContext context ) throws BrokerConfigurationException, IOException {

        String brokerNameSpec = (String) context.getProperties().get(TransportConfiguration.VM_BROKER_ATTR);

        if ( brokerNameSpec == null ) {
            throw new BrokerConfigurationException("No broker name set for VM transport"); //$NON-NLS-1$
        }

        try {
            this.location = new URI(String.format("vm://%s?create=false", brokerNameSpec)); //$NON-NLS-1$
        }
        catch ( URISyntaxException e ) {
            throw new BrokerConfigurationException(e);
        }

        this.user = (String) context.getProperties().get(TransportConfiguration.USER_ATTR);
        this.password = ConfigUtil.parseSecret(context.getProperties(), TransportConfiguration.PASSWORD_ATTR, null);

        this.maxPoolSize = ConfigUtil.parseInt(context.getProperties(), "maxPoolSize", 30); //$NON-NLS-1$
        this.borrowTimeout = ConfigUtil.parseInt(context.getProperties(), "borrowTimeout", 10); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @throws BrokerConfigurationException
     * 
     * @see eu.agno3.runtime.messaging.broker.transport.TransportFactory#createTransport()
     */
    @Override
    public TransportServer createTransport () throws BrokerConfigurationException {
        return new VMTransportServer(this.location, false);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.broker.transport.TransportFactory#createConnectionFactory()
     */
    @Override
    public XAConnectionFactory createConnectionFactory () {
        return new ActiveMQXAConnectionFactory(this.user, this.password, this.location);
    }


    /**
     * 
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
