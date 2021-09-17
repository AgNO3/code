/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.11.2014 by mbechler
 */
package eu.agno3.orchestrator.config.crypto.truststore;


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

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationInstance;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( TruststoresConfig.class )
@Entity
@Table ( name = "config_crypto_truststores" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "cr_trust_ts" )
public class TruststoresConfigImpl extends AbstractConfigurationInstance<TruststoresConfig> implements TruststoresConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -258512523841601627L;

    private Set<TruststoreConfig> truststores = new HashSet<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.ConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<TruststoresConfig> getType () {
        return TruststoresConfig.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.crypto.truststore.TruststoresConfig#getTruststores()
     */
    @Override
    @ManyToMany ( fetch = FetchType.LAZY, targetEntity = TruststoreConfigImpl.class )
    public Set<TruststoreConfig> getTruststores () {
        return this.truststores;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.crypto.truststore.TruststoresConfigMutable#setTruststores(java.util.Set)
     */
    @Override
    public void setTruststores ( Set<TruststoreConfig> truststores ) {
        this.truststores = truststores;
    }

}
