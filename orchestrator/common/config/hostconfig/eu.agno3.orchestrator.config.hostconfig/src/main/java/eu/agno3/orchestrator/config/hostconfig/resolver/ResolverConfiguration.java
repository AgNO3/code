/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.resolver;


import java.util.List;

import javax.validation.constraints.NotNull;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.model.validation.Materialized;
import eu.agno3.orchestrator.types.net.NetworkAddress;
import eu.agno3.orchestrator.types.net.validation.ValidNetworkAddress;


/**
 * @author mbechler
 * 
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:hostconfig:resolver" )
public interface ResolverConfiguration extends ConfigurationObject {

    /**
     * 
     * @return whether to autoconfigure DNS servers via DHCP
     */
    @NotNull ( groups = Materialized.class )
    Boolean getAutoconfigureDns ();


    /**
     * 
     * @return nameservers to query
     */
    @ValidNetworkAddress
    List<NetworkAddress> getNameservers ();
}
