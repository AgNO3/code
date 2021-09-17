/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.network.java.impl;


import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.system.info.network.InterfaceStatus;
import eu.agno3.orchestrator.system.info.network.InterfaceType;
import eu.agno3.orchestrator.system.info.network.LeaseEntry;
import eu.agno3.orchestrator.system.info.network.NetworkInterface;
import eu.agno3.orchestrator.system.info.network.V4ConfigurationType;
import eu.agno3.orchestrator.system.info.network.V6ConfigurationType;
import eu.agno3.orchestrator.types.net.AbstractIPAddress;
import eu.agno3.orchestrator.types.net.HardwareAddress;
import eu.agno3.orchestrator.types.net.MACAddress;
import eu.agno3.orchestrator.types.net.NetworkSpecification;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( NetworkInterface.class )
public class JavaNetworkInterfaceProxy implements NetworkInterface {

    private static final Logger log = Logger.getLogger(JavaNetworkInterfaceProxy.class);

    private java.net.NetworkInterface delegate;

    private V4ConfigurationType v4ConfigurationType;
    private V6ConfigurationType v6ConfigurationType;

    private List<LeaseEntry> dhcpLeases = new ArrayList<>();

    private final Map<String, String> aliasMap;


    /**
     * @param delegate
     * @param aliasMap
     */
    public JavaNetworkInterfaceProxy ( java.net.NetworkInterface delegate, Map<String, String> aliasMap ) {
        this.delegate = delegate;
        this.aliasMap = aliasMap;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.network.NetworkInterface#getInterfaceIndex()
     */
    @Override
    public int getInterfaceIndex () {
        return this.delegate.getIndex() - 1;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.network.NetworkInterface#getHardwareAddress()
     */
    @Override
    public HardwareAddress getHardwareAddress () {
        try {
            byte[] macAddr = this.delegate.getHardwareAddress();

            if ( macAddr == null ) {
                return null;
            }

            MACAddress hwAddr = new MACAddress();
            hwAddr.fromByteArray(macAddr);
            return hwAddr;
        }
        catch ( SocketException e ) {
            log.debug("Exception in getHardwareAddress():", e); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.network.NetworkInterface#getInterfaceAddresses()
     */
    @Override
    public Set<NetworkSpecification> getInterfaceAddresses () {
        Set<NetworkSpecification> res = new HashSet<>();

        for ( java.net.InterfaceAddress ifAddr : this.delegate.getInterfaceAddresses() ) {
            res.add(new NetworkSpecification(AbstractIPAddress.parse(ifAddr.getAddress().getHostAddress()), ifAddr.getNetworkPrefixLength()));
        }

        return res;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.network.NetworkInterface#getParent()
     */
    @Override
    public NetworkInterface getParent () {
        java.net.NetworkInterface parent = this.delegate.getParent();

        if ( parent == null ) {
            return null;
        }

        return new JavaNetworkInterfaceProxy(parent, this.aliasMap);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.network.NetworkInterface#getSubInterfaces()
     */
    @Override
    public List<NetworkInterface> getSubInterfaces () {
        List<NetworkInterface> res = new ArrayList<>();

        Enumeration<java.net.NetworkInterface> it = this.delegate.getSubInterfaces();
        while ( it.hasMoreElements() ) {
            res.add(new JavaNetworkInterfaceProxy(it.nextElement(), this.aliasMap));
        }

        return res;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.network.NetworkInterface#getMtu()
     */
    @Override
    public int getMtu () {
        try {
            return this.delegate.getMTU();
        }
        catch ( SocketException e ) {
            log.debug("Exception in getMTU():", e); //$NON-NLS-1$
            return -1;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.info.network.NetworkInterface#getAlias()
     */
    @Override
    public String getAlias () {
        return this.aliasMap.get(getName());
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.network.NetworkInterface#getName()
     */
    @Override
    public String getName () {
        return this.delegate.getName();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.network.NetworkInterface#getDisplayName()
     */
    @Override
    public String getDisplayName () {
        return this.delegate.getDisplayName();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.network.NetworkInterface#getInterfaceStatus()
     */
    @Override
    public InterfaceStatus getInterfaceStatus () {

        try {
            if ( this.delegate.isUp() ) {
                return InterfaceStatus.UP;
            }
        }
        catch ( SocketException e ) {
            log.debug("Exception in getInterfaceStatus():", e); //$NON-NLS-1$
            return InterfaceStatus.UNKNOWN;
        }

        return InterfaceStatus.DOWN;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.network.NetworkInterface#getInterfaceType()
     */
    @Override
    public InterfaceType getInterfaceType () {

        try {
            if ( this.delegate.isPointToPoint() ) {
                return InterfaceType.PPP;
            }

            if ( this.delegate.isLoopback() ) {
                return InterfaceType.LOOPBACK;
            }

            if ( this.delegate.isVirtual() ) {
                return InterfaceType.VIRTUAL;
            }

            return InterfaceType.ETH;
        }
        catch ( SocketException e ) {
            log.debug("Exception in getInterfaceType():", e); //$NON-NLS-1$
            return InterfaceType.UNKNOWN;
        }
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
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.info.network.NetworkInterface#getDhcpLeases()
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
