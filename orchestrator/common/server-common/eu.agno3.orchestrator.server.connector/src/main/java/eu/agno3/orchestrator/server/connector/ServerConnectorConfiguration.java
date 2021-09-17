/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.connector;


import java.net.InetAddress;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;


/**
 * @author mbechler
 *
 */
public interface ServerConnectorConfiguration {

    /**
     * @return the server address
     */
    @NonNull
    InetAddress getServerAddress ();


    /**
     * @return the server port
     */
    int getServerPort ();


    /**
     * @return the component id
     */
    @NonNull
    UUID getComponentId ();

}
