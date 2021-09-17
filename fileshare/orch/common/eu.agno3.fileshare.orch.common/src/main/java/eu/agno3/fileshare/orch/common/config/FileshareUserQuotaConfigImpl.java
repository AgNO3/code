/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
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
@MapAs ( FileshareUserQuotaConfig.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_fileshare_user_quota" )
@Audited
@DiscriminatorValue ( "filesh_user_quot" )
public class FileshareUserQuotaConfigImpl extends AbstractConfigurationObject<FileshareUserQuotaConfig> implements FileshareUserQuotaConfig,
        FileshareUserQuotaConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 2533058110917410389L;

    private List<FileshareQuotaRule> defaultQuotaRules = new ArrayList<>();

    private Boolean enableDefaultQuota;
    private Long globalDefaultQuota;
    private Boolean disableSizeTrackingWithoutQuota;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<FileshareUserQuotaConfig> getType () {
        return FileshareUserQuotaConfig.class;
    }


    /**
     * @return the defaultQuotaRules
     */
    @Override
    @OneToMany ( cascade = {
        CascadeType.ALL
    }, targetEntity = FileshareQuotaRuleImpl.class )
    @OrderColumn ( name = "idx" )
    public List<FileshareQuotaRule> getDefaultQuotaRules () {
        return this.defaultQuotaRules;
    }


    /**
     * @return the enableDefaultQuota
     */
    @Override
    public Boolean getEnableDefaultQuota () {
        return this.enableDefaultQuota;
    }


    /**
     * @param enableDefaultQuota
     *            the enableDefaultQuota to set
     */
    @Override
    public void setEnableDefaultQuota ( Boolean enableDefaultQuota ) {
        this.enableDefaultQuota = enableDefaultQuota;
    }


    /**
     * @param defaultQuotaRules
     *            the defaultQuotaRules to set
     */
    @Override
    public void setDefaultQuotaRules ( List<FileshareQuotaRule> defaultQuotaRules ) {
        this.defaultQuotaRules = defaultQuotaRules;
    }


    /**
     * @return the globalDefaultQuota
     */
    @Override
    public Long getGlobalDefaultQuota () {
        return this.globalDefaultQuota;
    }


    /**
     * @param globalDefaultQuota
     *            the globalDefaultQuota to set
     */
    @Override
    public void setGlobalDefaultQuota ( Long globalDefaultQuota ) {
        this.globalDefaultQuota = globalDefaultQuota;
    }


    /**
     * @return the disableSizeTrackingWithoutQuota
     */
    @Override
    public Boolean getDisableSizeTrackingWithoutQuota () {
        return this.disableSizeTrackingWithoutQuota;
    }


    /**
     * @param disableSizeTrackingWithoutQuota
     *            the disableSizeTrackingWithoutQuota to set
     */
    @Override
    public void setDisableSizeTrackingWithoutQuota ( Boolean disableSizeTrackingWithoutQuota ) {
        this.disableSizeTrackingWithoutQuota = disableSizeTrackingWithoutQuota;
    }

}
