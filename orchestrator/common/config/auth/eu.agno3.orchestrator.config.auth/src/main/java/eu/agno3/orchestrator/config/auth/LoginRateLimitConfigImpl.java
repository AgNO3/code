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
@MapAs ( LoginRateLimitConfig.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_auth_ratelimit" )
@Audited
@DiscriminatorValue ( "auth_rate" )
public class LoginRateLimitConfigImpl extends AbstractConfigurationObject<LoginRateLimitConfig> implements LoginRateLimitConfig,
        LoginRateLimitConfigMutable {

    private Boolean disableLaxSourceCheck;
    private Boolean disableGlobalDelay;
    private Boolean disableUserLockout;
    private Duration cleanInterval;

    /**
     * 
     */
    private static final long serialVersionUID = -387717594593898647L;


    /**
     * @return the disableLaxSourceCheck
     */
    @Override
    public Boolean getDisableLaxSourceCheck () {
        return this.disableLaxSourceCheck;
    }


    /**
     * @param disableLaxSourceCheck
     *            the disableLaxSourceCheck to set
     */
    @Override
    public void setDisableLaxSourceCheck ( Boolean disableLaxSourceCheck ) {
        this.disableLaxSourceCheck = disableLaxSourceCheck;
    }


    /**
     * @return the disableUserLockout
     */
    @Override
    public Boolean getDisableUserLockout () {
        return this.disableUserLockout;
    }


    /**
     * @param disableUserLockout
     *            the disableUserLockout to set
     */
    @Override
    public void setDisableUserLockout ( Boolean disableUserLockout ) {
        this.disableUserLockout = disableUserLockout;
    }


    /**
     * @return the disableGlobalDelay
     */
    @Override
    public Boolean getDisableGlobalDelay () {
        return this.disableGlobalDelay;
    }


    /**
     * @param disableGlobalDelay
     *            the disableGlobalDelay to set
     */
    @Override
    public void setDisableGlobalDelay ( Boolean disableGlobalDelay ) {
        this.disableGlobalDelay = disableGlobalDelay;
    }


    /**
     * @return the cleanInterval
     */
    @Override
    public Duration getCleanInterval () {
        return this.cleanInterval;
    }


    /**
     * @param cleanInterval
     *            the cleanInterval to set
     */
    @Override
    public void setCleanInterval ( Duration cleanInterval ) {
        this.cleanInterval = cleanInterval;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<LoginRateLimitConfig> getType () {
        return LoginRateLimitConfig.class;
    }

}
