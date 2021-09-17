/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.network;


import java.util.Set;


/**
 * @author mbechler
 * 
 */
public interface RoutingConfigurationMutable extends RoutingConfiguration {

    /**
     * @param autoconfigureV4Routes
     */
    void setAutoconfigureV4Routes ( Boolean autoconfigureV4Routes );


    /**
     * @param autoconfigureV6Routes
     */
    void setAutoconfigureV6Routes ( Boolean autoconfigureV6Routes );


    /**
     * @param entries
     * 
     */
    void setStaticRoutes ( Set<StaticRouteEntry> entries );


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.RoutingConfiguration#getStaticRoutes()
     */
    @Override
    Set<StaticRouteEntry> getStaticRoutes ();


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.RoutingConfiguration#getDefaultRouteV4()
     */
    @Override
    StaticRouteEntryMutable getDefaultRouteV4 ();


    /**
     * @param defaultRouteV4
     */
    void setDefaultRouteV4 ( StaticRouteEntryMutable defaultRouteV4 );


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.RoutingConfiguration#getDefaultRouteV6()
     */
    @Override
    StaticRouteEntryMutable getDefaultRouteV6 ();


    /**
     * @param defaultRouteV6
     */
    void setDefaultRouteV6 ( StaticRouteEntryMutable defaultRouteV6 );

}
