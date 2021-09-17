/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.network;


/**
 * @author mbechler
 * 
 */
public interface NetworkConfigurationMutable extends NetworkConfiguration {

    /**
     * 
     * @param v6enabled
     */
    void setIpv6Enabled ( Boolean v6enabled );


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.NetworkConfiguration#getInterfaceConfiguration()
     */
    @Override
    InterfaceConfigurationMutable getInterfaceConfiguration ();


    /**
     * @param interfaceConfiguration
     */
    void setInterfaceConfiguration ( InterfaceConfigurationMutable interfaceConfiguration );


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.NetworkConfiguration#getRoutingConfiguration()
     */
    @Override
    RoutingConfigurationMutable getRoutingConfiguration ();


    /**
     * @param routingConfiguration
     */
    void setRoutingConfiguration ( RoutingConfigurationMutable routingConfiguration );
}
