/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.network;


import javax.persistence.Basic;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.validation.Materialized;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( NetworkConfiguration.class )
@Entity
@Table ( name = "config_hostconfig_network" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "hc_net" )
public class NetworkConfigurationImpl extends AbstractConfigurationObject<NetworkConfiguration> implements NetworkConfiguration,
        NetworkConfigurationMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -4543559009531415837L;

    private Boolean ipv6Enabled;
    private InterfaceConfigurationImpl interfaceConfiguration;
    private RoutingConfigurationImpl routingConfiguration;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<NetworkConfiguration> getType () {
        return NetworkConfiguration.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.NetworkConfiguration#getIpv6Enabled()
     */
    @Override
    @Basic
    public Boolean getIpv6Enabled () {
        return this.ipv6Enabled;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.NetworkConfigurationMutable#setIpv6Enabled(java.lang.Boolean)
     */
    @Override
    public void setIpv6Enabled ( Boolean v6enabled ) {
        this.ipv6Enabled = v6enabled;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.NetworkConfiguration#getInterfaceConfiguration()
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = InterfaceConfigurationImpl.class )
    @NotNull ( groups = Materialized.class )
    public InterfaceConfigurationMutable getInterfaceConfiguration () {
        return this.interfaceConfiguration;
    }


    /**
     * @param interfaceConfiguration
     *            the interfaceConfiguration to set
     */
    @Override
    public void setInterfaceConfiguration ( InterfaceConfigurationMutable interfaceConfiguration ) {
        this.interfaceConfiguration = (InterfaceConfigurationImpl) interfaceConfiguration;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.NetworkConfiguration#getRoutingConfiguration()
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = RoutingConfigurationImpl.class )
    @NotNull ( groups = Materialized.class )
    public RoutingConfigurationMutable getRoutingConfiguration () {
        return this.routingConfiguration;
    }


    /**
     * @param routingConfiguration
     *            the routingConfiguration to set
     */
    @Override
    public void setRoutingConfiguration ( RoutingConfigurationMutable routingConfiguration ) {
        this.routingConfiguration = (RoutingConfigurationImpl) routingConfiguration;
    }

}
