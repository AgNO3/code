/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
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
@MapAs ( FileshareSecurityPolicyConfig.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_fileshare_secpolicies" )
@Audited
@DiscriminatorValue ( "filesh_secpols" )
public class FileshareSecurityPolicyConfigImpl extends AbstractConfigurationObject<FileshareSecurityPolicyConfig> implements
        FileshareSecurityPolicyConfig, FileshareSecurityPolicyConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 5876645864215111149L;

    private Set<FileshareSecurityPolicy> policies = new HashSet<>();

    private String defaultEntityLabel;
    private String defaultRootLabel;

    private List<FileshareUserLabelRule> userLabelRules = new ArrayList<>();

    private Integer defaultSharePasswordBits;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<FileshareSecurityPolicyConfig> getType () {
        return FileshareSecurityPolicyConfig.class;
    }


    /**
     * @return the policies
     */
    @Override
    @ManyToMany ( fetch = FetchType.LAZY, targetEntity = FileshareSecurityPolicyImpl.class )
    public Set<FileshareSecurityPolicy> getPolicies () {
        return this.policies;
    }


    /**
     * @param policies
     *            the policies to set
     */
    @Override
    public void setPolicies ( Set<FileshareSecurityPolicy> policies ) {
        this.policies = policies;
    }


    /**
     * @return the defaultEntityPolicy
     */
    @Override
    public String getDefaultEntityLabel () {
        return this.defaultEntityLabel;
    }


    /**
     * @param defaultEntityPolicy
     *            the defaultEntityPolicy to set
     */
    @Override
    public void setDefaultEntityLabel ( String defaultEntityPolicy ) {
        this.defaultEntityLabel = defaultEntityPolicy;
    }


    /**
     * @return the defaultRootPolicy
     */
    @Override
    public String getDefaultRootLabel () {
        return this.defaultRootLabel;
    }


    /**
     * @param defaultRootPolicy
     *            the defaultRootPolicy to set
     */
    @Override
    public void setDefaultRootLabel ( String defaultRootPolicy ) {
        this.defaultRootLabel = defaultRootPolicy;
    }


    /**
     * @return the userLabelRules
     */
    @Override
    @OneToMany ( cascade = {
        CascadeType.ALL
    }, targetEntity = FileshareUserLabelRuleImpl.class )
    @OrderColumn ( name = "idx" )
    public List<FileshareUserLabelRule> getUserLabelRules () {
        return this.userLabelRules;
    }


    /**
     * @param userLabelRules
     *            the userLabelRules to set
     */
    @Override
    public void setUserLabelRules ( List<FileshareUserLabelRule> userLabelRules ) {
        this.userLabelRules = userLabelRules;
    }


    /**
     * @return the defaultSharePasswordBits
     */
    @Override
    public Integer getDefaultSharePasswordBits () {
        return this.defaultSharePasswordBits;
    }


    /**
     * @param defaultSharePasswordBits
     *            the defaultSharePasswordBits to set
     */
    @Override
    public void setDefaultSharePasswordBits ( Integer defaultSharePasswordBits ) {
        this.defaultSharePasswordBits = defaultSharePasswordBits;
    }

}
