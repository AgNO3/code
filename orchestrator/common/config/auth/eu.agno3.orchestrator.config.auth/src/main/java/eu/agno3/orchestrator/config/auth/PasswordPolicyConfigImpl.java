/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.eclipse.jdt.annotation.NonNull;
import org.hibernate.envers.Audited;
import org.joda.time.Duration;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( PasswordPolicyConfig.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_auth_pwpolicy" )
@Audited
@DiscriminatorValue ( "auth_pwpol" )
public class PasswordPolicyConfigImpl extends AbstractConfigurationObject<PasswordPolicyConfig> implements PasswordPolicyConfig,
        PasswordPolicyConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = -387717594593898647L;

    private Integer entropyLowerLimit;

    private Boolean enableAgeCheck;
    private Duration maximumPasswordAge;
    private Boolean ignoreUnknownAge;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<PasswordPolicyConfig> getType () {
        return PasswordPolicyConfig.class;
    }


    /**
     * @return the entropyLowerLimit
     */
    @Override
    public Integer getEntropyLowerLimit () {
        return this.entropyLowerLimit;
    }


    /**
     * @param entropyLowerLimit
     *            the entropyLowerLimit to set
     */
    @Override
    public void setEntropyLowerLimit ( Integer entropyLowerLimit ) {
        this.entropyLowerLimit = entropyLowerLimit;
    }


    /**
     * @return the enableAgeCheck
     */
    @Override
    public Boolean getEnableAgeCheck () {
        return this.enableAgeCheck;
    }


    /**
     * @param enableAgeCheck
     *            the enableAgeCheck to set
     */
    @Override
    public void setEnableAgeCheck ( Boolean enableAgeCheck ) {
        this.enableAgeCheck = enableAgeCheck;
    }


    /**
     * @return the maximumPasswordAge
     */
    @Override
    public Duration getMaximumPasswordAge () {
        return this.maximumPasswordAge;
    }


    /**
     * @param maximumPasswordAge
     *            the maximumPasswordAge to set
     */
    @Override
    public void setMaximumPasswordAge ( Duration maximumPasswordAge ) {
        this.maximumPasswordAge = maximumPasswordAge;
    }


    /**
     * @return the ignoreUnknownAge
     */
    @Override
    public Boolean getIgnoreUnknownAge () {
        return this.ignoreUnknownAge;
    }


    /**
     * @param ignoreUnknownAge
     *            the ignoreUnknownAge to set
     */
    @Override
    public void setIgnoreUnknownAge ( Boolean ignoreUnknownAge ) {
        this.ignoreUnknownAge = ignoreUnknownAge;
    }
}
