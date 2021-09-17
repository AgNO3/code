/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.types.entities.crypto.PublicKeyEntry;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( SSLClientConfiguration.class )
@Entity
@Table ( name = "config_web_sslclient" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "webc_sslcl" )
public class SSLClientConfigurationImpl extends AbstractConfigurationObject<SSLClientConfiguration>
        implements SSLClientConfiguration, SSLClientConfigurationMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 8571720095497791468L;
    private String truststoreAlias;
    private Boolean disableHostnameVerification;
    private Boolean disableCertificateVerification;

    private SSLSecurityMode securityMode;

    private PublicKeyPinMode publicKeyPinMode;

    private Set<PublicKeyEntry> pinnedPublicKeys = new HashSet<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.ConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<SSLClientConfiguration> getType () {
        return SSLClientConfiguration.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.SSLClientConfiguration#getTruststoreAlias()
     */
    @Override
    public String getTruststoreAlias () {
        return this.truststoreAlias;
    }


    /**
     * @param truststoreAlias
     *            the truststoreAlias to set
     */
    @Override
    public void setTruststoreAlias ( String truststoreAlias ) {
        this.truststoreAlias = truststoreAlias;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.SSLClientConfiguration#getDisableHostnameVerification()
     */
    @Override
    public Boolean getDisableHostnameVerification () {
        return this.disableHostnameVerification;
    }


    /**
     * @param disableHostnameVerification
     *            the disableHostnameVerification to set
     */
    @Override
    public void setDisableHostnameVerification ( Boolean disableHostnameVerification ) {
        this.disableHostnameVerification = disableHostnameVerification;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.SSLClientConfiguration#getSecurityMode()
     */
    @Override
    public SSLSecurityMode getSecurityMode () {
        return this.securityMode;
    }


    /**
     * @param securityMode
     *            the securityMode to set
     */
    @Override
    public void setSecurityMode ( SSLSecurityMode securityMode ) {
        this.securityMode = securityMode;
    }


    /**
     * @param e
     * @return cloned object
     */
    public static SSLClientConfigurationImpl clone ( SSLClientConfiguration e ) {
        SSLClientConfigurationImpl cloned = new SSLClientConfigurationImpl();
        cloned.truststoreAlias = e.getTruststoreAlias();
        cloned.securityMode = e.getSecurityMode();
        cloned.disableHostnameVerification = e.getDisableHostnameVerification();
        return cloned;
    }


    /**
     * @return the disableCertificateVerification
     */
    @Override
    public Boolean getDisableCertificateVerification () {
        return this.disableCertificateVerification;
    }


    /**
     * @param disableCertificateVerification
     *            the disableCertificateVerification to set
     */
    @Override
    public void setDisableCertificateVerification ( Boolean disableCertificateVerification ) {
        this.disableCertificateVerification = disableCertificateVerification;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.SSLClientConfiguration#getPublicKeyPinMode()
     */
    @Override
    @Enumerated ( EnumType.STRING )
    public PublicKeyPinMode getPublicKeyPinMode () {
        return this.publicKeyPinMode;
    }


    @Override
    public void setPublicKeyPinMode ( PublicKeyPinMode publicKeyPinMode ) {
        this.publicKeyPinMode = publicKeyPinMode;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.web.SSLClientConfiguration#getPinnedPublicKeys()
     */
    @Override
    @Audited ( targetAuditMode = RelationTargetAuditMode.NOT_AUDITED )
    @JoinTable ( name = "config_web_sslclient_pinkeys" )
    @ManyToMany ( cascade = {} )
    public Set<PublicKeyEntry> getPinnedPublicKeys () {
        return this.pinnedPublicKeys;
    }


    /**
     * @param pinnedPublicKeys
     *            the pinnedPublicKeys to set
     */
    @Override
    public void setPinnedPublicKeys ( Set<PublicKeyEntry> pinnedPublicKeys ) {
        this.pinnedPublicKeys = pinnedPublicKeys;
    }
}
