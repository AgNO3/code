/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.network;


import java.io.Serializable;
import java.util.List;

import eu.agno3.orchestrator.system.info.SystemInformation;
import eu.agno3.orchestrator.types.net.NetworkAddress;


/**
 * @author mbechler
 * 
 */
public interface NetworkInformation extends SystemInformation, Serializable {

    /**
     * @return a list of discovered network interfaces
     */
    List<NetworkInterface> getNetworkInterfaces ();


    /**
     * @return entries of the routing table
     */
    List<RouteEntry> getRoutes ();


    /**
     * @return active DNS servers
     */
    List<NetworkAddress> getDnsServers ();

}
