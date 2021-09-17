/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.network;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
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
@MapAs ( InterfaceConfiguration.class )
@Entity
@Table ( name = "config_hostconfig_network_interfaces" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "hc_net_ifs" )
public class InterfaceConfigurationImpl extends AbstractConfigurationObject<InterfaceConfiguration>
        implements InterfaceConfiguration, InterfaceConfigurationMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 5031611179882465645L;

    private Set<InterfaceEntry> interfaces = new HashSet<>();


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<InterfaceConfiguration> getType () {
        return InterfaceConfiguration.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.hostconfig.network.InterfaceConfiguration#getInterfaces()
     */
    @Override
    @ManyToMany ( fetch = FetchType.LAZY, targetEntity = InterfaceEntryImpl.class )
    public Set<InterfaceEntry> getInterfaces () {
        return this.interfaces;
    }


    /**
     * @param interfaces
     *            the interfaces to set
     */
    @Override
    public void setInterfaces ( Set<InterfaceEntry> interfaces ) {
        this.interfaces = interfaces;
    }

}
