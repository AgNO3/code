/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.09.2013 by mbechler
 */
package eu.agno3.orchestrator.server.connector;


import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.server.component.ComponentConfig;
import eu.agno3.runtime.messaging.addressing.MessageSource;


/**
 * @author mbechler
 * @param <TConfig>
 * 
 */
public interface ServerConnector <TConfig extends ComponentConfig> {

    /**
     * Open server connection
     * 
     * @throws ServerConnectorException
     * @throws InterruptedException
     */
    void connect () throws ServerConnectorException, InterruptedException;


    /**
     * Tries to open a server connection
     * 
     * @return whether connection was successful
     */
    boolean tryConnect ();


    /**
     * Close server connection
     * 
     * @throws ServerConnectorException
     */
    void disconnect () throws ServerConnectorException;


    /**
     * @throws ServerConnectorException
     * @throws InterruptedException
     * 
     */
    void reconnect () throws ServerConnectorException, InterruptedException;


    /**
     * @return the current connection state
     */
    ServerConnectorState getState ();


    /**
     * @return the configured agent id
     */
    UUID getComponentId ();


    /**
     * @return the local message source
     */
    @NonNull
    MessageSource getMessageSource ();


    /**
     * @return the configured server address
     */
    String getServerAddress ();


    /**
     * 
     */
    void sendPing ();


    /**
     * @param config
     * @throws ServerConnectorException
     */
    void updateConfig ( TConfig config ) throws ServerConnectorException;


    /**
     * @throws ServerConnectorException
     * 
     */
    void serverShutdown () throws ServerConnectorException;

}
