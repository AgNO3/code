/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.terms.TermsConfigurationImpl;
import eu.agno3.orchestrator.config.terms.TermsConfigurationMutable;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareUserConfig.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_fileshare_user" )
@Audited
@DiscriminatorValue ( "filesh_user" )
public class FileshareUserConfigImpl extends AbstractConfigurationObject<FileshareUserConfig>
        implements FileshareUserConfig, FileshareUserConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -8926961152118896965L;

    private FileshareUserQuotaConfigImpl quotaConfig;
    private FileshareUserSelfServiceConfigImpl selfServiceConfig;
    private FileshareUserTrustLevelConfigImpl userTrustLevelConfig;

    private TermsConfigurationImpl termsConfig;

    private Set<String> defaultRoles = new HashSet<>();

    private Set<String> noSubjectRootRoles = new HashSet<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.ConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<FileshareUserConfig> getType () {
        return FileshareUserConfig.class;
    }


    /**
     * @return the quotaConfig
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = FileshareUserQuotaConfigImpl.class )
    public FileshareUserQuotaConfigMutable getQuotaConfig () {
        return this.quotaConfig;
    }


    /**
     * @param quotaConfig
     *            the quotaConfig to set
     */
    @Override
    public void setQuotaConfig ( FileshareUserQuotaConfigMutable quotaConfig ) {
        this.quotaConfig = (FileshareUserQuotaConfigImpl) quotaConfig;
    }


    /**
     * @return the selfServiceConfig
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = FileshareUserSelfServiceConfigImpl.class )
    public FileshareUserSelfServiceConfigMutable getSelfServiceConfig () {
        return this.selfServiceConfig;
    }


    /**
     * @param selfServiceConfig
     *            the selfServiceConfig to set
     */
    @Override
    public void setSelfServiceConfig ( FileshareUserSelfServiceConfigMutable selfServiceConfig ) {
        this.selfServiceConfig = (FileshareUserSelfServiceConfigImpl) selfServiceConfig;
    }


    /**
     * @return the userTrustLevelConfig
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = FileshareUserTrustLevelConfigImpl.class )
    public FileshareUserTrustLevelConfigMutable getUserTrustLevelConfig () {
        return this.userTrustLevelConfig;
    }


    /**
     * @param userTrustLevelConfig
     *            the userTrustLevelConfig to set
     */
    @Override
    public void setUserTrustLevelConfig ( FileshareUserTrustLevelConfigMutable userTrustLevelConfig ) {
        this.userTrustLevelConfig = (FileshareUserTrustLevelConfigImpl) userTrustLevelConfig;
    }


    /**
     * @return the termsConfig
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = TermsConfigurationImpl.class )
    public TermsConfigurationMutable getTermsConfig () {
        return this.termsConfig;
    }


    /**
     * @param termsConfig
     *            the termsConfig to set
     */
    @Override
    public void setTermsConfig ( TermsConfigurationMutable termsConfig ) {
        this.termsConfig = (TermsConfigurationImpl) termsConfig;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.orch.common.config.FileshareUserConfig#getDefaultRoles()
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_fileshare_user_defroles" )
    public Set<String> getDefaultRoles () {
        return this.defaultRoles;
    }


    /**
     * @param defaultRoles
     *            the defaultRoles to set
     */
    @Override
    public void setDefaultRoles ( Set<String> defaultRoles ) {
        this.defaultRoles = defaultRoles;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.orch.common.config.FileshareUserConfig#getNoSubjectRootRoles()
     */
    @Override
    @ElementCollection
    @CollectionTable ( name = "config_fileshare_user_norootroles" )
    public Set<String> getNoSubjectRootRoles () {
        return this.noSubjectRootRoles;
    }


    /**
     * @param noSubjectRootRoles
     *            the noSubjectRootRoles to set
     */
    @Override
    public void setNoSubjectRootRoles ( Set<String> noSubjectRootRoles ) {
        this.noSubjectRootRoles = noSubjectRootRoles;
    }

}
