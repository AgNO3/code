/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.09.2013 by mbechler
 */
package eu.agno3.orchestrator.server.connector;


/**
 * @author mbechler
 * 
 */
public enum ServerConnectorState {

    /**
     * server connection could not be established, was reset or produced errors
     */
    ERROR,

    /**
     * connection has been initiated but is not complete
     */
    CONNECTING,

    /**
     * connection established
     */
    CONNECTED,

    /**
     * disconnect has been requested
     */
    DISCONNECTING,

    /**
     * connection was not started or has been administratively disconnected
     */
    DISCONNECTED
}
