/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.network;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.agno3.orchestrator.types.net.HardwareAddress;
import eu.agno3.orchestrator.types.net.NetworkSpecification;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( NetworkInterface.class )
public class NetworkInterfaceStorageImpl implements NetworkInterface, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 7613387157657377380L;
    private int ifIndex;
    private int mtu;
    private String name;
    private String displayName;
    private InterfaceStatus ifStatus;
    private InterfaceType ifType;

    private HardwareAddress hwAddress;
    private Set<NetworkSpecification> ifAddresses = new HashSet<>();
    private NetworkInterface parent;
    private List<NetworkInterface> subInterfaces = new ArrayList<>();
    private V4ConfigurationType v4ConfigurationType;
    private V6ConfigurationType v6ConfigurationType;

    private List<LeaseEntry> dhcpLeases = new ArrayList<>();
    private String alias;


    /**
     * @param o
     */
    public void update ( NetworkInterface o ) {
        this.displayName = o.getDisplayName();
        this.name = o.getName();
        this.alias = o.getAlias();
        this.ifIndex = o.getInterfaceIndex();
        this.ifType = o.getInterfaceType();
        this.ifStatus = o.getInterfaceStatus();
        this.mtu = o.getMtu();

        this.v4ConfigurationType = o.getV4ConfigurationType();
        this.v6ConfigurationType = o.getV6ConfigurationType();

        if ( o.getHardwareAddress() != null && !o.getHardwareAddress().equals(this.hwAddress) ) {
            this.hwAddress = o.getHardwareAddress();
        }

        if ( o.getParent() != null && !o.getParent().equals(this.parent) ) {
            this.parent = o.getParent();
        }

        if ( !this.ifAddresses.equals(o.getInterfaceAddresses()) ) {
            this.ifAddresses = o.getInterfaceAddresses();
        }

        if ( !this.subInterfaces.equals(o.getSubInterfaces()) ) {
            this.subInterfaces = o.getSubInterfaces();
        }

        if ( !this.dhcpLeases.equals(o.getDhcpLeases()) ) {
            this.dhcpLeases = o.getDhcpLeases();
        }
    }


    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.dhcpLeases == null ) ? 0 : this.dhcpLeases.hashCode() );
        result = prime * result + ( ( this.displayName == null ) ? 0 : this.displayName.hashCode() );
        result = prime * result + ( ( this.hwAddress == null ) ? 0 : this.hwAddress.hashCode() );
        result = prime * result + ( ( this.ifAddresses == null ) ? 0 : this.ifAddresses.hashCode() );
        result = prime * result + this.ifIndex;
        result = prime * result + ( ( this.ifStatus == null ) ? 0 : this.ifStatus.hashCode() );
        result = prime * result + ( ( this.ifType == null ) ? 0 : this.ifType.hashCode() );
        result = prime * result + this.mtu;
        result = prime * result + ( ( this.name == null ) ? 0 : this.name.hashCode() );
        result = prime * result + ( ( this.alias == null ) ? 0 : this.alias.hashCode() );
        result = prime * result + ( ( this.subInterfaces == null ) ? 0 : this.subInterfaces.hashCode() );
        result = prime * result + ( ( this.v4ConfigurationType == null ) ? 0 : this.v4ConfigurationType.hashCode() );
        result = prime * result + ( ( this.v6ConfigurationType == null ) ? 0 : this.v6ConfigurationType.hashCode() );
        return result;
    }

    // -GENERATED


    // +GENERATED
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        NetworkInterfaceStorageImpl other = (NetworkInterfaceStorageImpl) obj;
        if ( this.dhcpLeases == null ) {
            if ( other.dhcpLeases != null )
                return false;
        }
        else if ( !this.dhcpLeases.equals(other.dhcpLeases) )
            return false;
        if ( this.displayName == null ) {
            if ( other.displayName != null )
                return false;
        }
        else if ( !this.displayName.equals(other.displayName) )
            return false;
        if ( this.hwAddress == null ) {
            if ( other.hwAddress != null )
                return false;
        }
        else if ( !this.hwAddress.equals(other.hwAddress) )
            return false;
        if ( this.ifAddresses == null ) {
            if ( other.ifAddresses != null )
                return false;
        }
        else if ( !this.ifAddresses.equals(other.ifAddresses) )
            return false;
        if ( this.ifIndex != other.ifIndex )
            return false;
        if ( this.ifStatus != other.ifStatus )
            return false;
        if ( this.ifType != other.ifType )
            return false;
        if ( this.mtu != other.mtu )
            return false;
        if ( this.name == null ) {
            if ( other.name != null )
                return false;
        }
        else if ( !this.name.equals(other.name) )
            return false;
        if ( this.alias == null ) {
            if ( other.alias != null )
                return false;
        }
        else if ( !this.alias.equals(other.alias) )
            return false;
        if ( this.subInterfaces == null ) {
            if ( other.subInterfaces != null )
                return false;
        }
        else if ( !this.subInterfaces.equals(other.subInterfaces) )
            return false;
        if ( this.v4ConfigurationType != other.v4ConfigurationType )
            return false;
        if ( this.v6ConfigurationType != other.v6ConfigurationType )
            return false;
        return true;
    }


    // -GENERATED

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.network.NetworkInterface#getInterfaceIndex()
     */
    @Override
    public int getInterfaceIndex () {
        return this.ifIndex;
    }


    /**
     * @param ifIndex
     *            the ifIndex to set
     */
    public void setInterfaceIndex ( int ifIndex ) {
        this.ifIndex = ifIndex;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.network.NetworkInterface#getHardwareAddress()
     */
    @Override
    public HardwareAddress getHardwareAddress () {
        return this.hwAddress;
    }


    /**
     * 
     * @param addr
     */
    public void setHardwareAddress ( HardwareAddress addr ) {
        this.hwAddress = addr;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.network.NetworkInterface#getInterfaceAddresses()
     */
    @Override
    public Set<NetworkSpecification> getInterfaceAddresses () {
        return this.ifAddresses;
    }


    /**
     * @param ifAddresses
     *            the ifAddresses to set
     */
    public void setInterfaceAddresses ( Set<NetworkSpecification> ifAddresses ) {
        this.ifAddresses = ifAddresses;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.network.NetworkInterface#getParent()
     */
    @Override
    public NetworkInterface getParent () {
        return this.parent;
    }


    /**
     * @param parent
     *            the parent to set
     */
    public void setParent ( NetworkInterface parent ) {
        this.parent = parent;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.network.NetworkInterface#getSubInterfaces()
     */
    @Override
    public List<NetworkInterface> getSubInterfaces () {
        return this.subInterfaces;
    }


    /**
     * @param subInterfaces
     *            the subInterfaces to set
     */
    public void setSubInterfaces ( List<NetworkInterface> subInterfaces ) {
        this.subInterfaces = subInterfaces;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.network.NetworkInterface#getMtu()
     */
    @Override
    public int getMtu () {
        return this.mtu;
    }


    /**
     * @param mtu
     *            the mtu to set
     */
    public void setMtu ( int mtu ) {
        this.mtu = mtu;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.network.NetworkInterface#getName()
     */
    @Override
    public String getName () {
        return this.name;
    }


    /**
     * @param name
     *            the name to set
     */
    public void setName ( String name ) {
        this.name = name;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.network.NetworkInterface#getDisplayName()
     */
    @Override
    public String getDisplayName () {
        return this.displayName;
    }


    /**
     * @param displayName
     *            the displayName to set
     */
    public void setDisplayName ( String displayName ) {
        this.displayName = displayName;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.info.network.NetworkInterface#getAlias()
     */
    @Override
    public String getAlias () {
        return this.alias;
    }


    /**
     * @param alias
     *            the alias to set
     */
    public void setAlias ( String alias ) {
        this.alias = alias;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.network.NetworkInterface#getInterfaceStatus()
     */
    @Override
    public InterfaceStatus getInterfaceStatus () {
        return this.ifStatus;
    }


    /**
     * @param ifStatus
     *            the ifStatus to set
     */
    public void setInterfaceStatus ( InterfaceStatus ifStatus ) {
        this.ifStatus = ifStatus;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.network.NetworkInterface#getInterfaceType()
     */
    @Override
    public InterfaceType getInterfaceType () {
        return this.ifType;
    }


    /**
     * @param ifType
     *            the ifType to set
     */
    public void setInterfaceType ( InterfaceType ifType ) {
        this.ifType = ifType;
    }


    /**
     * @return the v4ConfigurationType
     */
    @Override
    public V4ConfigurationType getV4ConfigurationType () {
        return this.v4ConfigurationType;
    }


    /**
     * @param v4ConfigurationType
     *            the v4ConfigurationType to set
     */
    public void setV4ConfigurationType ( V4ConfigurationType v4ConfigurationType ) {
        this.v4ConfigurationType = v4ConfigurationType;
    }


    /**
     * @return the v6ConfigurationType
     */
    @Override
    public V6ConfigurationType getV6ConfigurationType () {
        return this.v6ConfigurationType;
    }


    /**
     * @param v6ConfigurationType
     *            the v6ConfigurationType to set
     */
    public void setV6ConfigurationType ( V6ConfigurationType v6ConfigurationType ) {
        this.v6ConfigurationType = v6ConfigurationType;
    }


    /**
     * @return the dhcpLeases
     */
    @Override
    public List<LeaseEntry> getDhcpLeases () {
        return this.dhcpLeases;
    }


    /**
     * @param dhcpLeases
     *            the dhcpLeases to set
     */
    public void setDhcpLeases ( List<LeaseEntry> dhcpLeases ) {
        this.dhcpLeases = dhcpLeases;
    }
}
