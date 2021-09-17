/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.network;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.types.net.HardwareAddress;
import eu.agno3.orchestrator.types.net.NetworkSpecification;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( InterfaceEntry.class )
@Entity
@Table ( name = "config_hostconfig_network_interface_entry" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "hc_net_if" )
public class InterfaceEntryImpl extends AbstractConfigurationObject<InterfaceEntry> implements InterfaceEntry, InterfaceEntryMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -638593612773840635L;

    private Integer interfaceIndex;
    private HardwareAddress hardwareAddress;
    private String alias;
    private HardwareAddress overrideHardwareAddress;
    private Integer mtu;
    private MediaType mediaType;
    private AddressConfigurationTypeV4 v4AddressConfigurationType;
    private AddressConfigurationTypeV6 v6AddressesConfigurationType;

    private Set<NetworkSpecification> staticAddresses = new HashSet<>();


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<InterfaceEntry> getType () {
        return InterfaceEntry.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntry#getInterfaceIndex()
     */
    @Basic ( optional = true )
    @Column ( nullable = true )
    @Override
    public Integer getInterfaceIndex () {
        return this.interfaceIndex;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntryMutable#setInterfaceIndex(java.lang.Integer)
     */
    @Override
    public void setInterfaceIndex ( Integer interfaceIndex ) {
        this.interfaceIndex = interfaceIndex;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntry#getHardwareAddress()
     */
    @Basic ( optional = true )
    @Column ( nullable = true )
    @Override
    public HardwareAddress getHardwareAddress () {
        return this.hardwareAddress;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntryMutable#setHardwareAddress(eu.agno3.orchestrator.types.net.HardwareAddress)
     */
    @Override
    public void setHardwareAddress ( HardwareAddress hardwareAddress ) {
        this.hardwareAddress = hardwareAddress;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntry#getAlias()
     */
    @Override
    public String getAlias () {
        return this.alias;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntryMutable#setAlias(java.lang.String)
     */
    @Override
    public void setAlias ( String alias ) {
        this.alias = alias;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntry#getOverrideHardwareAddress()
     */
    @Override
    @Basic ( optional = true )
    @Column ( nullable = true )
    public HardwareAddress getOverrideHardwareAddress () {
        return this.overrideHardwareAddress;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntryMutable#setOverrideHardwareAddress(eu.agno3.orchestrator.types.net.HardwareAddress)
     */
    @Override
    public void setOverrideHardwareAddress ( HardwareAddress overrideHardwareAddress ) {
        this.overrideHardwareAddress = overrideHardwareAddress;
    }


    /**
     * @return the mtu
     */

    @Override
    public Integer getMtu () {
        return this.mtu;
    }


    /**
     * @param mtu
     *            the mtu to set
     */
    @Override
    public void setMtu ( Integer mtu ) {
        this.mtu = mtu;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntry#getMediaType()
     */
    @Override
    @Column ( nullable = true )
    @Enumerated ( EnumType.STRING )
    public MediaType getMediaType () {
        return this.mediaType;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntryMutable#setMediaType(eu.agno3.orchestrator.config.hostconfig.network.MediaType)
     */
    @Override
    public void setMediaType ( MediaType mediaType ) {
        this.mediaType = mediaType;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntry#getV4AddressConfigurationType()
     */
    @Override
    @Enumerated ( EnumType.STRING )
    public AddressConfigurationTypeV4 getV4AddressConfigurationType () {
        return this.v4AddressConfigurationType;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntryMutable#setV4AddressConfigurationType(eu.agno3.orchestrator.config.hostconfig.network.AddressConfigurationTypeV4)
     */
    @Override
    public void setV4AddressConfigurationType ( AddressConfigurationTypeV4 v4AddressConfigurationType ) {
        this.v4AddressConfigurationType = v4AddressConfigurationType;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntry#getV6AddressConfigurationType()
     */
    @Override
    @Enumerated ( EnumType.STRING )
    public AddressConfigurationTypeV6 getV6AddressConfigurationType () {
        return this.v6AddressesConfigurationType;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntryMutable#setV6AddressConfigurationType(eu.agno3.orchestrator.config.hostconfig.network.AddressConfigurationTypeV6)
     */
    @Override
    public void setV6AddressConfigurationType ( AddressConfigurationTypeV6 v6AddressConfigurationType ) {
        this.v6AddressesConfigurationType = v6AddressConfigurationType;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntry#getStaticAddresses()
     */
    @Override
    @ElementCollection ( fetch = FetchType.EAGER )
    @Column ( name = "addr", nullable = false )
    @CollectionTable ( name = "config_hostconfig_network_interface_static" )
    public Set<NetworkSpecification> getStaticAddresses () {
        return this.staticAddresses;
    }


    /**
     * @param staticAddresses
     *            the staticAddresses to set
     */
    @Override
    public void setStaticAddresses ( Set<NetworkSpecification> staticAddresses ) {
        this.staticAddresses = staticAddresses;
    }

}
