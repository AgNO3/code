/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.network;


/**
 * @author mbechler
 * 
 */
public enum MediaType {

    /**
     * Perform auto negotiation
     */
    AUTO,

    /**
     * Advertise both 1000TX full/half-duplex
     */
    ETH1000BASETX,
    /**
     * Force 1000TX full-duplex
     */
    ETH1000BASETXFD,
    /**
     * Force 1000TX half-duplex
     */
    ETH1000BASETXHD,

    /**
     * Advertise both 10TX full/half-duplex
     */
    ETH100BASETX,
    /**
     * Force 100TX full-duplex
     */
    ETH100BASETXFD,
    /**
     * Force 100TX half-duplex
     */
    ETH100BASETXHD,

    /**
     * Advertise both 10T full/half-duplex
     */
    ETH10BASET,
    /**
     * Force 10T full-duplex
     */
    ETH10BASETFD,
    /**
     * Force 10T half-duplex
     */
    ETH10BASETHD
}
