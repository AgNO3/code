/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.network;


import java.util.Set;

import eu.agno3.orchestrator.types.net.HardwareAddress;
import eu.agno3.orchestrator.types.net.NetworkSpecification;


/**
 * @author mbechler
 * 
 */
public interface InterfaceEntryMutable extends InterfaceEntry {

    /**
     * @param interfaceIndex
     *            the interfaceIndex to set
     */
    void setInterfaceIndex ( Integer interfaceIndex );


    /**
     * @param hardwareAddress
     *            the hardwareAddress to set
     */
    void setHardwareAddress ( HardwareAddress hardwareAddress );


    /**
     * @param alias
     *            the alias to set
     */
    void setAlias ( String alias );


    /**
     * @param overrideHardwareAddress
     *            the overrideHardwareAddress to set
     */
    void setOverrideHardwareAddress ( HardwareAddress overrideHardwareAddress );


    /**
     * @param mediaType
     *            the mediaType to set
     */
    void setMediaType ( MediaType mediaType );


    /**
     * @param staticAddresses
     */
    void setStaticAddresses ( Set<NetworkSpecification> staticAddresses );


    /**
     * @param v6AddressesConfigurationType
     */
    void setV6AddressConfigurationType ( AddressConfigurationTypeV6 v6AddressesConfigurationType );


    /**
     * @param v4AddressesConfigurationType
     */
    void setV4AddressConfigurationType ( AddressConfigurationTypeV4 v4AddressesConfigurationType );


    /**
     * @param mtu
     */
    void setMtu ( Integer mtu );

}