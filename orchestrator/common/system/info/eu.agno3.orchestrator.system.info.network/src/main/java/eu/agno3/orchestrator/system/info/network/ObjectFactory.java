/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.network;


import eu.agno3.orchestrator.types.net.NetworkSpecification;


/**
 * @author mbechler
 * 
 */
public class ObjectFactory {

    /**
     * @return default interface address implementation
     */
    public NetworkSpecification createInterfaceAddress () {
        return new NetworkSpecification();
    }


    /**
     * @return default network interface impl
     */
    public NetworkInterface createNetworkInterface () {
        return new NetworkInterfaceStorageImpl();
    }


    /**
     * 
     * @return default network information impl
     */
    public NetworkInformation createNetworkInformation () {
        return new NetworkInformationStorageImpl();
    }

}
