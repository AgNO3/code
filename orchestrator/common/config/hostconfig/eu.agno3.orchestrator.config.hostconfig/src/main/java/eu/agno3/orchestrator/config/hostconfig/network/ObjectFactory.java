/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.network;


/**
 * @author mbechler
 * 
 */
public class ObjectFactory {

    /**
     * 
     * @return default system configuration impl
     */
    public NetworkConfiguration createNetworkConfiguration () {
        return new NetworkConfigurationImpl();
    }


    /**
     * 
     * @return the default implementation
     */
    public InterfaceConfiguration createInterfaceConfiguration () {
        return new InterfaceConfigurationImpl();
    }


    /**
     * 
     * @return the default implementation
     */
    public RoutingConfiguration createRoutingConfiguration () {
        return new RoutingConfigurationImpl();
    }


    /**
     * 
     * @return the default implementation
     */
    public InterfaceEntry createInterfaceEntry () {
        return new InterfaceEntryImpl();
    }


    /**
     * 
     * @return the default implementation
     */
    public StaticRouteEntry createStaticRouteEntry () {
        return new StaticRouteEntryImpl();
    }

}
