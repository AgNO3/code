/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.network;


/**
 * @author mbechler
 * 
 */
public enum RouteType {

    /**
     * Regular unicast route
     */
    UNICAST,

    /**
     * Discard packets with unreachable
     */
    UNREACHABLE,

    /**
     * Discard packets
     */
    BLACKHOLE,

    /**
     * Discard packets with prohibited
     */
    PROHIBIT,

    /**
     * Deliver packets locally
     */
    LOCAL,

    /**
     * Deliver as broadcast
     */
    BROADCAST

}
