/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.network;


import eu.agno3.orchestrator.types.net.NetworkAddress;
import eu.agno3.orchestrator.types.net.NetworkSpecification;


/**
 * @author mbechler
 * 
 */
public interface StaticRouteEntryMutable extends StaticRouteEntry {

    /**
     * @param type
     *            the type to set
     */
    void setRouteType ( RouteType type );


    /**
     * @param target
     *            the target to set
     */
    void setTarget ( NetworkSpecification target );


    /**
     * @param gateway
     *            the gateway to set
     */
    void setGateway ( NetworkAddress gateway );


    /**
     * @param device
     *            the device to set
     */
    void setDevice ( String device );


    /**
     * @param sourceAddress
     *            the sourceAddress to set
     */
    void setSourceAddress ( NetworkAddress sourceAddress );


    /**
     * @param mtu
     *            the mtu to set
     */
    void setMtu ( Integer mtu );


    /**
     * @param advmss
     *            the advmss to set
     */
    void setAdvmss ( Integer advmss );

}