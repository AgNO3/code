/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


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
@Entity
@Table ( name = "config_realms" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "re_res" )
@MapAs ( RealmsConfig.class )
public class RealmsConfigImpl extends AbstractConfigurationObject<RealmsConfig> implements RealmsConfig, RealmsConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -7616214802961842495L;

    private Set<RealmConfig> realms = new HashSet<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<RealmsConfig> getType () {
        return RealmsConfig.class;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.RealmsConfig#getRealms()
     */
    @Override
    @ManyToMany ( fetch = FetchType.LAZY, targetEntity = AbstractRealmConfigImpl.class )
    public Set<RealmConfig> getRealms () {
        return this.realms;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.realms.RealmsConfigMutable#setRealms(java.util.Set)
     */
    @Override
    public void setRealms ( Set<RealmConfig> realms ) {
        this.realms = realms;
    }

}
