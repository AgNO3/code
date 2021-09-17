/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.11.2014 by mbechler
 */
package eu.agno3.orchestrator.config.crypto.truststore;


import javax.persistence.Basic;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
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
@MapAs ( TruststoreConfig.class )
@Entity
@Table ( name = "config_crypto_truststores_store" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "cr_trust_store" )
public class TruststoreConfigImpl extends AbstractConfigurationObject<TruststoreConfig> implements TruststoreConfig, TruststoreConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -4622820925797398207L;
    private RevocationConfigMutable revocationConfig;

    private String trustLibrary;

    private String alias;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<TruststoreConfig> getType () {
        return TruststoreConfig.class;
    }


    /**
     * @return the alias
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
     * @return the trustLibrary
     */
    @Override
    public String getTrustLibrary () {
        return this.trustLibrary;
    }


    /**
     * @param trustLibrary
     *            the trustLibrary to set
     */
    @Override
    public void setTrustLibrary ( String trustLibrary ) {
        this.trustLibrary = trustLibrary;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.crypto.truststore.TruststoreConfig#getRevocationConfiguration()
     */
    @Override
    @ManyToOne ( fetch = FetchType.LAZY, targetEntity = RevocationConfigImpl.class )
    public RevocationConfigMutable getRevocationConfiguration () {
        return this.revocationConfig;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.crypto.truststore.TruststoreConfigMutable#setRevocationConfiguration(eu.agno3.orchestrator.config.crypto.truststore.RevocationConfigMutable)
     */
    @Override
    public void setRevocationConfiguration ( RevocationConfigMutable config ) {
        this.revocationConfig = config;
    }

}
