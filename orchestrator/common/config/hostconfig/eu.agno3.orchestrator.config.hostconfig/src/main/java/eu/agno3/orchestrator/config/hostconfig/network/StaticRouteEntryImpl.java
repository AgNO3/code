/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.network;


import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.EmptyCheckableObject;
import eu.agno3.orchestrator.types.net.NetworkAddress;
import eu.agno3.orchestrator.types.net.NetworkSpecification;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( StaticRouteEntry.class )
@Entity
@Table ( name = "config_hostconfig_network_routing_route" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "hc_net_re" )
public class StaticRouteEntryImpl extends AbstractConfigurationObject<StaticRouteEntry>
        implements StaticRouteEntry, StaticRouteEntryMutable, EmptyCheckableObject {

    private static final long serialVersionUID = 4288007094350061966L;
    private RouteType routeType = RouteType.UNICAST;
    private NetworkSpecification target;
    private NetworkAddress gateway;
    private String device;

    private NetworkAddress sourceAddress;

    private Integer mtu;
    private Integer advmss;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<StaticRouteEntry> getType () {
        return StaticRouteEntry.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.EmptyCheckableObject#isEmpty()
     */
    @Override
    @Transient
    public boolean isEmpty () {
        return this.target == null && this.gateway == null && this.device == null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.StaticRouteEntry#getRouteType()
     */
    @Override
    @Column ( nullable = false )
    @Enumerated ( EnumType.STRING )
    public RouteType getRouteType () {
        return this.routeType;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.StaticRouteEntryMutable#setRouteType(eu.agno3.orchestrator.config.hostconfig.network.RouteType)
     */
    @Override
    public void setRouteType ( RouteType type ) {
        this.routeType = type;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.StaticRouteEntry#getTarget()
     */
    @Override
    @Column ( nullable = true )
    @Basic ( optional = true )
    public NetworkSpecification getTarget () {
        return this.target;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.StaticRouteEntryMutable#setTarget(eu.agno3.orchestrator.types.net.NetworkSpecification)
     */
    @Override
    public void setTarget ( NetworkSpecification target ) {
        this.target = target;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.StaticRouteEntry#getGateway()
     */
    @Override
    @Column ( nullable = true )
    @Basic ( optional = true )
    public NetworkAddress getGateway () {
        return this.gateway;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.StaticRouteEntryMutable#setGateway(eu.agno3.orchestrator.types.net.NetworkAddress)
     */
    @Override
    public void setGateway ( NetworkAddress gateway ) {
        this.gateway = gateway;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.StaticRouteEntry#getDevice()
     */
    @Override
    @Column ( nullable = true )
    @Basic ( optional = true )
    public String getDevice () {
        return this.device;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.StaticRouteEntryMutable#setDevice(java.lang.String)
     */
    @Override
    public void setDevice ( String device ) {
        this.device = device;
    }


    /**
     * @return the sourceAddress
     */
    @Override
    @Column ( nullable = true )
    @Basic ( optional = true )
    public NetworkAddress getSourceAddress () {
        return this.sourceAddress;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.StaticRouteEntryMutable#setSourceAddress(eu.agno3.orchestrator.types.net.NetworkAddress)
     */
    @Override
    public void setSourceAddress ( NetworkAddress sourceAddress ) {
        this.sourceAddress = sourceAddress;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.StaticRouteEntry#getMtu()
     */
    @Override
    @Column ( nullable = true )
    @Basic ( optional = true )
    public Integer getMtu () {
        return this.mtu;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.StaticRouteEntryMutable#setMtu(java.lang.Integer)
     */
    @Override
    public void setMtu ( Integer mtu ) {
        this.mtu = mtu;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.StaticRouteEntry#getAdvmss()
     */
    @Override
    @Column ( nullable = true )
    @Basic ( optional = true )
    public Integer getAdvmss () {
        return this.advmss;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.StaticRouteEntryMutable#setAdvmss(java.lang.Integer)
     */
    @Override
    public void setAdvmss ( Integer advmss ) {
        this.advmss = advmss;
    }
}
