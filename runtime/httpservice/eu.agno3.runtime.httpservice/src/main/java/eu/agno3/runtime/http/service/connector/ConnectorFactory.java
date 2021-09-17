/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2013 by mbechler
 */
package eu.agno3.runtime.http.service.connector;


import java.util.List;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;


/**
 * @author mbechler
 * 
 */
public interface ConnectorFactory {

    /**
     * @param s
     *            the server instance to create connector for
     * @return connectors for this instance
     */
    List<ServerConnector> createConnectors ( Server s );


    /**
     * @return the connector name
     */
    String getConnectorName ();


    /**
     * @return the configuration PID for this connector
     */
    String getConfigurationPID ();
}
