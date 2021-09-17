/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 27, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.network;


import eu.agno3.orchestrator.types.net.NetworkAddress;


/**
 * @author mbechler
 *
 */
public enum NetworkType {

    /**
     * Unknown
     */
    UNKNOWN,

    /**
     * Ipv4 network
     */
    IPV4,

    /**
     * Ipv6 network
     */
    IPV6;

    /**
     * 
     * @param na
     * @return the network type of the address
     */
    public static final NetworkType fromNetworkAddress ( NetworkAddress na ) {
        if ( na == null ) {
            return NetworkType.UNKNOWN;
        }
        switch ( na.getBitSize() ) {
        case 32:
            return NetworkType.IPV4;
        case 128:
            return NetworkType.IPV6;
        default:
            return NetworkType.UNKNOWN;
        }

    }
}
