/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.04.2014 by mbechler
 */
package eu.agno3.orchestrator.types.net;


/**
 * @author mbechler
 * 
 */
public enum NetworkAddressType {

    /**
     * An unspecified address (e.g. 0.0.0.0 or 0::0)
     */
    UNSPECIFIED,

    /**
     * An unicast address
     */
    UNICAST,

    /**
     * A loopback address (e.g. 127/8 or ::1)
     */
    LOOPBACK,

    /**
     * A multicast adddress
     */
    MULTICAST,

    /**
     * A broadcast address
     */
    BROADCAST,

    /**
     * An anycast address
     */
    ANYCAST,

    /**
     * A reserved address, e.g. specified documentation addresses that must not be used in practical applications
     */
    RESERVED
}
