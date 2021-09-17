/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
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

@MapAs ( FileshareQuotaRule.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_fileshare_content_quotaRule" )
@Audited
@DiscriminatorValue ( "filesh_cont_qt" )
public class FileshareQuotaRuleImpl extends AbstractConfigurationObject<FileshareQuotaRule> implements FileshareQuotaRule, FileshareQuotaRuleMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 2577959721246153079L;

    private String matchRole;
    private Long quota;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<FileshareQuotaRule> getType () {
        return FileshareQuotaRule.class;
    }


    /**
     * @return the matchRole
     */
    @Override
    public String getMatchRole () {
        return this.matchRole;
    }


    /**
     * @param matchRole
     *            the matchRole to set
     */
    @Override
    public void setMatchRole ( String matchRole ) {
        this.matchRole = matchRole;
    }


    /**
     * @return the quota
     */
    @Override
    public Long getQuota () {
        return this.quota;
    }


    /**
     * @param quota
     *            the quota to set
     */
    @Override
    public void setQuota ( Long quota ) {
        this.quota = quota;
    }
}
