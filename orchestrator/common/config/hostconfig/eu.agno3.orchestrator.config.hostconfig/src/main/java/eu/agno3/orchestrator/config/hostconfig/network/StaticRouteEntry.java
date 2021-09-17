/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.network;


import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.model.validation.ValidReferenceAlias;
import eu.agno3.orchestrator.types.net.NetworkAddress;
import eu.agno3.orchestrator.types.net.NetworkAddressType;
import eu.agno3.orchestrator.types.net.NetworkSpecification;
import eu.agno3.orchestrator.types.net.validation.ValidNetworkAddress;
import eu.agno3.orchestrator.types.net.validation.ValidNetworkSpecification;


/**
 * @author mbechler
 * 
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:hostconfig:network:routing:route" )
public interface StaticRouteEntry extends ConfigurationObject {

    /**
     * @return the type
     */
    @NotNull
    RouteType getRouteType ();


    /**
     * @return the target
     */
    @ValidNetworkSpecification
    NetworkSpecification getTarget ();


    /**
     * @return the gateway
     */
    @ValidNetworkAddress ( allowedTypes = {
        NetworkAddressType.UNICAST
    } )
    NetworkAddress getGateway ();


    /**
     * @return the device
     */
    @ValidReferenceAlias
    String getDevice ();


    /**
     * @return the mtu
     */
    @Min ( value = 576 )
    @Max ( value = 65536 )
    Integer getMtu ();


    /**
     * @return the advmss
     */
    @Min ( value = 536 )
    @Max ( value = 65496 )
    Integer getAdvmss ();


    /**
     * @return the source address to use for this route
     */
    @ValidNetworkAddress ( allowedTypes = NetworkAddressType.UNICAST )
    NetworkAddress getSourceAddress ();

}