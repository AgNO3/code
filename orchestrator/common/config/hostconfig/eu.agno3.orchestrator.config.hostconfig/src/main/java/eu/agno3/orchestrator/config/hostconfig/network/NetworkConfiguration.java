/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.network;


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
@ObjectTypeName ( "urn:agno3:objects:1.0:hostconfig:network" )
public interface NetworkConfiguration extends ConfigurationObject {

    /**
     * @return whether ipv6 networking is generally enabled
     */
    @NotNull ( groups = Materialized.class )
    Boolean getIpv6Enabled ();


    /**
     * @return interface configuration aspect
     */
    @NotNull ( groups = Materialized.class )
    @Valid
    @ReferencedObject
    InterfaceConfiguration getInterfaceConfiguration ();


    /**
     * 
     * @return routing configuration aspect
     */
    @NotNull ( groups = Materialized.class )
    @Valid
    @ReferencedObject
    RoutingConfiguration getRoutingConfiguration ();

}
