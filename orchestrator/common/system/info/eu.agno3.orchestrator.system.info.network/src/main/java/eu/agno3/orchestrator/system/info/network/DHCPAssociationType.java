/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2016 by mbechler
 */
package eu.agno3.orchestrator.system.info.network;


/**
 * @author mbechler
 *
 */
public enum DHCPAssociationType {

    /**
     * DHCPv4
     */
    V4,

    /**
     * BOOTP
     */
    BOOTP,

    /**
     * DHCPv6 temporary address
     */
    V6_TA,

    /**
     * DHCPv6 non-temporary address
     */
    V6_NA,

    /**
     * DHCPv6 prefix delegation
     */
    V6_PD,
}
