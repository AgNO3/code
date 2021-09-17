/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.network;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( RoutingConfiguration.class )
@Entity
@Table ( name = "config_hostconfig_network_routing" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "hc_net_rt" )
public class RoutingConfigurationImpl extends AbstractConfigurationObject<RoutingConfiguration> implements RoutingConfiguration,
        RoutingConfigurationMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 994031604287120678L;

    private Set<StaticRouteEntry> staticRoutes = new HashSet<>();

    private Boolean autoconfigureV4Routes;
    private Boolean autoconfigureV6Routes;

    private StaticRouteEntryImpl defaultRouteV4;
    private StaticRouteEntryImpl defaultRouteV6;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<RoutingConfiguration> getType () {
        return RoutingConfiguration.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.RoutingConfiguration#getAutoconfigureV4Routes()
     */
    @Override
    public Boolean getAutoconfigureV4Routes () {
        return this.autoconfigureV4Routes;
    }


    /**
     * @param autoconfigureV4Routes
     *            the autoconfigureV4Routes to set
     */
    @Override
    public void setAutoconfigureV4Routes ( Boolean autoconfigureV4Routes ) {
        this.autoconfigureV4Routes = autoconfigureV4Routes;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.RoutingConfiguration#getAutoconfigureV6Routes()
     */
    @Override
    public Boolean getAutoconfigureV6Routes () {
        return this.autoconfigureV6Routes;
    }


    /**
     * @param autoconfigureV6Routes
     *            the autoconfigureV6Routes to set
     */
    @Override
    public void setAutoconfigureV6Routes ( Boolean autoconfigureV6Routes ) {
        this.autoconfigureV6Routes = autoconfigureV6Routes;
    }


    /**
     * @return the defaultRouteV4
     */
    @Override
    @OneToOne ( cascade = {
        CascadeType.ALL
    }, orphanRemoval = true, targetEntity = StaticRouteEntryImpl.class )
    public StaticRouteEntryMutable getDefaultRouteV4 () {
        return this.defaultRouteV4;
    }


    /**
     * @param defaultRouteV4
     *            the defaultRouteV4 to set
     */
    @Override
    public void setDefaultRouteV4 ( StaticRouteEntryMutable defaultRouteV4 ) {
        this.defaultRouteV4 = (StaticRouteEntryImpl) defaultRouteV4;
    }


    /**
     * @return the defaultRouteV6
     */
    @Override
    @OneToOne ( cascade = {
        CascadeType.ALL
    }, orphanRemoval = true, targetEntity = StaticRouteEntryImpl.class )
    public StaticRouteEntryMutable getDefaultRouteV6 () {
        return this.defaultRouteV6;
    }


    /**
     * @param defaultRouteV6
     *            the defaultRouteV6 to set
     */
    @Override
    public void setDefaultRouteV6 ( StaticRouteEntryMutable defaultRouteV6 ) {
        this.defaultRouteV6 = (StaticRouteEntryImpl) defaultRouteV6;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.RoutingConfiguration#getStaticRoutes()
     */
    @Override
    @OneToMany ( cascade = {
        CascadeType.ALL
    }, orphanRemoval = true, targetEntity = StaticRouteEntryImpl.class )
    @OrderColumn ( name = "idx" )
    public Set<StaticRouteEntry> getStaticRoutes () {
        return this.staticRoutes;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.RoutingConfigurationMutable#setStaticRoutes(java.util.Set)
     */
    @Override
    public void setStaticRoutes ( Set<StaticRouteEntry> entries ) {
        this.staticRoutes = entries;
    }

}
