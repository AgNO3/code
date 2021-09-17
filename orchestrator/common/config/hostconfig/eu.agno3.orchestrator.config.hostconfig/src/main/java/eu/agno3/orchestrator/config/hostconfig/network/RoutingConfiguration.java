/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.network;


import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.model.validation.Materialized;


/**
 * @author mbechler
 * 
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:hostconfig:network:routing" )
public interface RoutingConfiguration extends ConfigurationObject {

    /**
     * 
     * @return whether to use autoconfiguration (via address configuration method) for Ipv4 routes
     */
    @NotNull ( groups = Materialized.class )
    Boolean getAutoconfigureV4Routes ();


    /**
     * 
     * @return whether to use autoconfiguration (via address configuration method) for Ipv6 routes
     */
    @NotNull ( groups = Materialized.class )
    Boolean getAutoconfigureV6Routes ();


    /**
     * @return the configured static routes
     */
    @Valid
    @ReferencedObject
    Set<StaticRouteEntry> getStaticRoutes ();


    /**
     * @return the v4 default route
     */
    @Valid
    @NotNull
    @ReferencedObject
    StaticRouteEntry getDefaultRouteV4 ();


    /**
     * @return the v6 default route
     */
    @Valid
    @NotNull
    @ReferencedObject
    StaticRouteEntry getDefaultRouteV6 ();

}
