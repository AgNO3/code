/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.broker.transport;


import java.net.URI;

import javax.jms.XAConnectionFactory;

import org.apache.activemq.transport.TransportServer;

import eu.agno3.runtime.messaging.broker.BrokerConfigurationException;


/**
 * @author mbechler
 * 
 */
public interface TransportFactory {

    /**
     * @return a transport instance
     * @throws BrokerConfigurationException
     */
    TransportServer createTransport () throws BrokerConfigurationException;


    /**
     * @return a connection factory for this transport
     */
    XAConnectionFactory createConnectionFactory ();


    /**
     * @return this transport URI
     */
    URI getBrokerURI ();


    /**
     * @return pool borrow timeout in seconds
     */
    int getBorrowTimeout ();


    /**
     * @return maximum pool size
     */
    int getPoolSize ();
}
