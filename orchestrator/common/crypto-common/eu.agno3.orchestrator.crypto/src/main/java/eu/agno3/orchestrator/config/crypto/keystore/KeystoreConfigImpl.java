/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.11.2014 by mbechler
 */
package eu.agno3.orchestrator.config.crypto.keystore;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
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
@MapAs ( KeystoreConfig.class )
@Entity
@Table ( name = "config_crypto_keystore" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "cr_keys_kc" )
public class KeystoreConfigImpl extends AbstractConfigurationObject<KeystoreConfig> implements KeystoreConfig, KeystoreConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 560865105091325832L;
    private String alias;
    private String validationTrustStore;
    private Set<ImportKeyPairEntry> importKeyPairs = new HashSet<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<KeystoreConfig> getType () {
        return KeystoreConfig.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.crypto.keystore.KeystoreConfig#getAlias()
     */
    @Override
    @Basic
    public String getAlias () {
        return this.alias;
    }


    /**
     * @param alias
     *            the alias to set
     */
    @Override
    public void setAlias ( String alias ) {
        this.alias = alias;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.crypto.keystore.KeystoreConfig#getValidationTrustStore()
     */
    @Override
    @Basic
    public String getValidationTrustStore () {
        return this.validationTrustStore;
    }


    /**
     * @param valodationTrustStore
     *            the valodationTrustStore to set
     */
    @Override
    public void setValidationTrustStore ( String valodationTrustStore ) {
        this.validationTrustStore = valodationTrustStore;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.crypto.keystore.KeystoreConfig#getImportKeyPairs()
     */
    @Override
    @ManyToMany ( fetch = FetchType.LAZY, targetEntity = ImportKeyPairEntryImpl.class )
    public Set<ImportKeyPairEntry> getImportKeyPairs () {
        return this.importKeyPairs;
    }


    /**
     * @param importKeyPairs
     *            the importKeyPairs to set
     */
    @Override
    public void setImportKeyPairs ( Set<ImportKeyPairEntry> importKeyPairs ) {
        this.importKeyPairs = importKeyPairs;
    }
}
