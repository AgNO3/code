/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.network;


import java.util.Set;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import eu.agno3.orchestrator.config.model.base.config.ObjectName;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.model.validation.ValidReferenceAlias;
import eu.agno3.orchestrator.types.net.HardwareAddress;
import eu.agno3.orchestrator.types.net.NetworkSpecification;
import eu.agno3.orchestrator.types.net.validation.ValidHardwareAddress;
import eu.agno3.orchestrator.types.net.validation.ValidNetworkSpecification;


/**
 * @author mbechler
 * 
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:hostconfig:network:interface" )
public interface InterfaceEntry extends ConfigurationObject {

    /**
     * 
     * @return the interface index (for identification)
     */
    @Min ( value = 0 )
    Integer getInterfaceIndex ();


    /**
     * 
     * @return the interface hardware address (for identification)
     */
    @ValidHardwareAddress
    HardwareAddress getHardwareAddress ();


    /**
     * 
     * @return the interface alias
     */
    @ValidReferenceAlias
    @ObjectName
    String getAlias ();


    /**
     * 
     * @return the hardware address to set on the interface
     */
    @ValidHardwareAddress
    HardwareAddress getOverrideHardwareAddress ();


    /**
     * 
     * @return the media type to set on the interface
     */
    MediaType getMediaType ();


    /**
     * @return the v4 address configuration type
     */
    AddressConfigurationTypeV4 getV4AddressConfigurationType ();


    /**
     * 
     * @return the v6 address configuration type
     */
    AddressConfigurationTypeV6 getV6AddressConfigurationType ();


    /**
     * 
     * @return the configured static addresses
     */
    @ValidNetworkSpecification ( requireNetworkAddress = false )
    Set<NetworkSpecification> getStaticAddresses ();


    /**
     * @return the mtu
     */
    @Min ( value = 576 )
    @Max ( value = 65536 )
    Integer getMtu ();

}
