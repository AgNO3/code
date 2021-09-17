/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.09.2013 by mbechler
 */
package eu.agno3.orchestrator.server.component;


/**
 * @author mbechler
 * 
 */
public enum ComponentState {

    /**
     * Agent state is unknown
     */
    UNKNOWN,

    /**
     * Agent is currently connecting, initializing
     */
    CONNECTING,

    /**
     * Agent is connected, available
     */
    CONNECTED,

    /**
     * Agent is disconnecting
     */
    DISCONNECTED,

    /**
     * Agent connection has failed
     */
    FAILURE
}
