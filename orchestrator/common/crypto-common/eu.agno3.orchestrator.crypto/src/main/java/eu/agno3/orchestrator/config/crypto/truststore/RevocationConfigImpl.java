/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.11.2014 by mbechler
 */
package eu.agno3.orchestrator.config.crypto.truststore;


import java.net.URI;

import javax.persistence.Basic;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.validator.constraints.Range;
import org.joda.time.Duration;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.types.entities.crypto.X509CertEntry;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( RevocationConfig.class )
@Entity
@Table ( name = "config_crypto_truststores_revocation" )
@Audited
@PersistenceUnit ( unitName = "config" )
@DiscriminatorValue ( "cr_trust_revoke" )
public class RevocationConfigImpl extends AbstractConfigurationObject<RevocationConfig> implements RevocationConfig, RevocationConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 1181267224662571836L;

    private CRLCheckLevel crlCheckLevel;
    private Boolean onDemandCRLDownload;
    private Integer onDemandCRLCacheSize;
    private Duration crlUpdateInterval;

    private OCSPCheckLevel ocspCheckLevel;
    private Integer ocspCacheSize;

    private Boolean useTrustedResponder;
    private URI trustedResponderUri;
    private X509CertEntry trustedResponderTrustCert;
    private Boolean trustedResponderCheckAll;

    private Boolean checkOnlyEndEntity;
    private Duration networkTimeout;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<RevocationConfig> getType () {
        return RevocationConfig.class;
    }


    /**
     * @return the crlCheckLevel
     */
    @Override
    @Enumerated ( EnumType.STRING )
    public CRLCheckLevel getCrlCheckLevel () {
        return this.crlCheckLevel;
    }


    /**
     * @param crlCheckLevel
     *            the crlCheckLevel to set
     */
    @Override
    public void setCrlCheckLevel ( CRLCheckLevel crlCheckLevel ) {
        this.crlCheckLevel = crlCheckLevel;
    }


    /**
     * @return the onDemandCRLDownload
     */
    @Override
    @Basic
    public Boolean getOnDemandCRLDownload () {
        return this.onDemandCRLDownload;
    }


    /**
     * @param onDemandCRLDownload
     *            the onDemandCRLDownload to set
     */
    @Override
    public void setOnDemandCRLDownload ( Boolean onDemandCRLDownload ) {
        this.onDemandCRLDownload = onDemandCRLDownload;
    }


    /**
     * @return the onDemandCRLCacheSize
     */
    @Override
    @Basic
    @Range ( min = 0, max = 128 )
    public Integer getOnDemandCRLCacheSize () {
        return this.onDemandCRLCacheSize;
    }


    /**
     * @param onDemandCRLCacheSize
     *            the onDemandCRLCacheSize to set
     */
    @Override
    public void setOnDemandCRLCacheSize ( Integer onDemandCRLCacheSize ) {
        this.onDemandCRLCacheSize = onDemandCRLCacheSize;
    }


    /**
     * @return the crlUpdateInterval
     */
    @Override
    @Basic
    public Duration getCrlUpdateInterval () {
        return this.crlUpdateInterval;
    }


    /**
     * @param crlUpdateInterval
     *            the crlUpdateInterval to set
     */
    @Override
    public void setCrlUpdateInterval ( Duration crlUpdateInterval ) {
        this.crlUpdateInterval = crlUpdateInterval;
    }


    /**
     * @return the ocspCheckLevel
     */
    @Override
    @Enumerated ( EnumType.STRING )
    public OCSPCheckLevel getOcspCheckLevel () {
        return this.ocspCheckLevel;
    }


    /**
     * @param ocspCheckLevel
     *            the ocspCheckLevel to set
     */
    @Override
    public void setOcspCheckLevel ( OCSPCheckLevel ocspCheckLevel ) {
        this.ocspCheckLevel = ocspCheckLevel;
    }


    /**
     * @return the ocspCacheSize
     */
    @Override
    @Basic
    @Range ( min = 0, max = 4096 )
    public Integer getOcspCacheSize () {
        return this.ocspCacheSize;
    }


    /**
     * @param ocspCacheSize
     *            the ocspCacheSize to set
     */
    @Override
    public void setOcspCacheSize ( Integer ocspCacheSize ) {
        this.ocspCacheSize = ocspCacheSize;
    }


    /**
     * @return the useTrustedResponder
     */
    @Override
    @Basic
    public Boolean getUseTrustedResponder () {
        return this.useTrustedResponder;
    }


    /**
     * @param useTrustedResponder
     *            the useTrustedResponder to set
     */
    @Override
    public void setUseTrustedResponder ( Boolean useTrustedResponder ) {
        this.useTrustedResponder = useTrustedResponder;
    }


    /**
     * @return the trustedResponderUri
     */
    @Override
    @Basic
    public URI getTrustedResponderUri () {
        return this.trustedResponderUri;
    }


    /**
     * @param trustedResponderUri
     *            the trustedResponderUri to set
     */
    @Override
    public void setTrustedResponderUri ( URI trustedResponderUri ) {
        this.trustedResponderUri = trustedResponderUri;
    }


    /**
     * @return the trustedResponderTrustCertificate
     */
    @Override
    @Audited ( targetAuditMode = RelationTargetAuditMode.NOT_AUDITED )
    @JoinColumn ( name = "trustedResponderCert" )
    @ManyToOne ( cascade = {} )
    public X509CertEntry getTrustedResponderTrustCertificate () {
        return this.trustedResponderTrustCert;
    }


    /**
     * @param trustedResponderCert
     *            the trustedResponderTrustCertificate to set
     */
    @Override
    public void setTrustedResponderTrustCertificate ( X509CertEntry trustedResponderCert ) {
        this.trustedResponderTrustCert = trustedResponderCert;
    }


    /**
     * @param trustedResponderCheckAll
     *            the trustedResponderCheckAll to set
     */
    @Override
    @Basic
    public void setTrustedResponderCheckAll ( Boolean trustedResponderCheckAll ) {
        this.trustedResponderCheckAll = trustedResponderCheckAll;
    }


    /**
     * @return the trustedResponderCheckAll
     */
    @Override
    public Boolean getTrustedResponderCheckAll () {
        return this.trustedResponderCheckAll;
    }


    /**
     * @return the checkOnlyEndEntity
     */
    @Override
    @Basic
    public Boolean getCheckOnlyEndEntity () {
        return this.checkOnlyEndEntity;
    }


    /**
     * @param checkOnlyEndEntity
     *            the checkOnlyEndEntity to set
     */
    @Override
    public void setCheckOnlyEndEntity ( Boolean checkOnlyEndEntity ) {
        this.checkOnlyEndEntity = checkOnlyEndEntity;
    }


    /**
     * @return the networkTimeout
     */
    @Override
    public Duration getNetworkTimeout () {
        return this.networkTimeout;
    }


    /**
     * @param networkTimeout
     *            the networkTimeout to set
     */
    @Override
    public void setNetworkTimeout ( Duration networkTimeout ) {
        this.networkTimeout = networkTimeout;
    }
}
