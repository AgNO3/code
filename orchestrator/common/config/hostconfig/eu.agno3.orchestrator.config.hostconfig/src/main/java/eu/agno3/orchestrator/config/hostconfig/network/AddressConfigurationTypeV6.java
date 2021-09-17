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
public enum AddressConfigurationTypeV6 {

    /**
     * V6 disabled
     */
    NONE,

    /**
     * Static configuration
     */
    STATIC,

    /**
     * Stateless autoconfiguration
     */
    STATELESS,

    /**
     * DHCPv6
     */
    DHCP
}
