/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.network;


import java.util.List;
import java.util.Set;

import eu.agno3.orchestrator.types.net.HardwareAddress;
import eu.agno3.orchestrator.types.net.NetworkSpecification;


/**
 * @author mbechler
 * 
 */
public interface NetworkInterface {

    /**
     * 
     * @return the interface index
     */
    int getInterfaceIndex ();


    /**
     * 
     * @return the interface's hardware address
     */
    HardwareAddress getHardwareAddress ();


    /**
     * 
     * @return a set of addresses assigned to this interface
     */
    Set<NetworkSpecification> getInterfaceAddresses ();


    /**
     * 
     * @return the parent interface if this is a subinterface, null otherwise
     */
    NetworkInterface getParent ();


    /**
     * 
     * @return this interface's sub interfaces
     */
    List<NetworkInterface> getSubInterfaces ();


    /**
     * 
     * @return the maximum transfer unit for this interface
     */
    int getMtu ();


    /**
     * 
     * @return the interface name
     */
    String getName ();


    /**
     * 
     * @return the interface display name
     */
    String getDisplayName ();


    /**
     * 
     * @return the interface alias
     */
    String getAlias ();


    /**
     * 
     * @return this interface's status (UP,DOWN,UNKNOWN)
     */
    InterfaceStatus getInterfaceStatus ();


    /**
     * 
     * @return this interface's type (e.g. LOOPBACK, ETH, PPP, VLAN...)
     */
    InterfaceType getInterfaceType ();


    /**
     * @return V4 address configuration type
     */
    V4ConfigurationType getV4ConfigurationType ();


    /**
     * @return V6 address configuration type
     */
    V6ConfigurationType getV6ConfigurationType ();


    /**
     * @return dhcp lease status
     */
    List<LeaseEntry> getDhcpLeases ();

}
