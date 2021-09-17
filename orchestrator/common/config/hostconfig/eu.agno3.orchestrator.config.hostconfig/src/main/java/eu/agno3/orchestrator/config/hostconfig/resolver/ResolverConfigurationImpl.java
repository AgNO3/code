/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.resolver;


import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OrderColumn;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.types.net.NetworkAddress;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( ResolverConfiguration.class )
@Entity
@Table ( name = "config_hostconfig_resolver" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "hc_res" )
public class ResolverConfigurationImpl extends AbstractConfigurationObject<ResolverConfiguration> implements ResolverConfiguration,
        ResolverConfigurationMutable {

    private static final long serialVersionUID = 1051490803877635312L;
    private List<NetworkAddress> nameservers = new ArrayList<>();
    private Boolean autoconfigureDns;


    /**
     * 
     */
    public ResolverConfigurationImpl () {}


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<ResolverConfiguration> getType () {
        return ResolverConfiguration.class;
    }


    /**
     * @param resolverConfiguration
     */
    public ResolverConfigurationImpl ( ResolverConfiguration resolverConfiguration ) {
        this.nameservers = resolverConfiguration.getNameservers();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.resolver.ResolverConfiguration#getAutoconfigureDns()
     */
    @Override
    @Basic
    public Boolean getAutoconfigureDns () {
        return this.autoconfigureDns;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.hostconfig.resolver.ResolverConfigurationMutable#setAutoconfigureDns(java.lang.Boolean)
     */
    @Override
    public void setAutoconfigureDns ( Boolean autoconfigure ) {
        this.autoconfigureDns = autoconfigure;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.resolver.ResolverConfiguration#getNameservers()
     */
    @Override
    @ElementCollection ( fetch = FetchType.EAGER )
    @Column ( name = "addr", nullable = false )
    @CollectionTable ( name = "config_hostconfig_resolver_nameservers" )
    @OrderColumn ( name = "idx" )
    public List<NetworkAddress> getNameservers () {
        return this.nameservers;
    }


    /**
     * @param nameservers
     *            the nameservers to set
     */
    @Override
    public void setNameservers ( List<NetworkAddress> nameservers ) {
        this.nameservers = nameservers;
    }

}
