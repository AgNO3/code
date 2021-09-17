/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.11.2014 by mbechler
 */
package eu.agno3.orchestrator.config.crypto.keystore;


import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.types.entities.crypto.KeyPairEntry;
import eu.agno3.orchestrator.types.entities.crypto.X509CertEntry;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( KeystoresConfig.class )
@Entity
@Table ( name = "config_crypto_keystore_import" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "cr_keys_ikp" )
public class ImportKeyPairEntryImpl extends AbstractConfigurationObject<ImportKeyPairEntry> implements ImportKeyPairEntry, ImportKeyPairEntryMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -4837514822055247508L;
    private String alias;
    private KeyPairEntry keyPair;
    private List<X509CertEntry> certificateChain = new ArrayList<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<ImportKeyPairEntry> getType () {
        return ImportKeyPairEntry.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.crypto.keystore.ImportKeyPairEntry#getAlias()
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
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.crypto.keystore.ImportKeyPairEntry#getCertificateChain()
     */
    @Override
    @OrderColumn ( name = "idx" )
    @JoinTable ( name = "config_crypto_keystore_import_certs" )
    @Audited ( targetAuditMode = RelationTargetAuditMode.NOT_AUDITED )
    @ManyToMany ( cascade = {} )
    public List<X509CertEntry> getCertificateChain () {
        return this.certificateChain;
    }


    /**
     * @param certificateChain
     *            the certificateChain to set
     */
    @Override
    public void setCertificateChain ( List<X509CertEntry> certificateChain ) {
        this.certificateChain = certificateChain;
    }


    /**
     * 
     * @return key pair entry
     */
    @Audited ( targetAuditMode = RelationTargetAuditMode.NOT_AUDITED )
    @JoinColumn ( name = "keyPair" )
    @ManyToOne ( cascade = {} )
    public KeyPairEntry getKeyPairEntry () {
        return this.keyPair;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.crypto.keystore.ImportKeyPairEntry#getKeyPair()
     */
    @Override
    @Transient
    public KeyPair getKeyPair () {
        KeyPairEntry e = getKeyPairEntry();
        if ( e == null ) {
            return null;
        }
        return e.getKeyPair();
    }


    /**
     * 
     * @param kp
     */
    public void setKeyPairEntry ( KeyPairEntry kp ) {
        this.keyPair = kp;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.crypto.keystore.ImportKeyPairEntryMutable#setKeyPair(java.security.KeyPair)
     */
    @Override
    @Transient
    public void setKeyPair ( KeyPair kp ) {
        if ( kp == null ) {
            this.setKeyPairEntry(null);
        }
        else {
            this.setKeyPairEntry(new KeyPairEntry(kp));
        }

    }
}
