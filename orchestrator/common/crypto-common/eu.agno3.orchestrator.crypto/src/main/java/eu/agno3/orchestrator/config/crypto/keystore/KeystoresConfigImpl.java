/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.11.2014 by mbechler
 */
package eu.agno3.orchestrator.config.crypto.keystore;


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
@MapAs ( KeystoresConfig.class )
@Entity
@Table ( name = "config_crypto_keystores" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "cr_keys_ks" )
public class KeystoresConfigImpl extends AbstractConfigurationObject<KeystoresConfig> implements KeystoresConfig, KeystoresConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -1368661456358000304L;
    private Set<KeystoreConfig> keystores = new HashSet<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<KeystoresConfig> getType () {
        return KeystoresConfig.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.crypto.keystore.KeystoresConfig#getKeystores()
     */
    @Override
    @ManyToMany ( fetch = FetchType.LAZY, targetEntity = KeystoreConfigImpl.class )
    public Set<KeystoreConfig> getKeystores () {
        return this.keystores;
    }


    /**
     * @param keystores
     *            the keystores to set
     */
    @Override
    public void setKeystores ( Set<KeystoreConfig> keystores ) {
        this.keystores = keystores;
    }

}
