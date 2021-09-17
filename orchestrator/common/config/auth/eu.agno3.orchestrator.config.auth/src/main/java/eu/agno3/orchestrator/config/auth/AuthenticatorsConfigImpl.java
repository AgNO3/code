/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
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
@MapAs ( AuthenticatorsConfig.class )
@Entity
@PersistenceUnit ( unitName = "config" )
@Table ( name = "config_auths" )
@Audited
@DiscriminatorValue ( "authc" )
public class AuthenticatorsConfigImpl extends AbstractConfigurationObject<AuthenticatorsConfig> implements AuthenticatorsConfig,
        AuthenticatorsConfigMutable {

    /**
     * 
     */
    private static final long serialVersionUID = 6190910802634796332L;

    private Boolean enableLocalAuth;

    private Boolean allowInsecureAuth;

    private PasswordPolicyConfigImpl passwordPolicy;

    private LoginRateLimitConfigImpl loginRateLimit;

    private Set<AuthenticatorConfig> authenticators = new HashSet<>();


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.ConfigurationObject#getType()
     */
    @Override
    @Transient
    public @NonNull Class<AuthenticatorsConfig> getType () {
        return AuthenticatorsConfig.class;
    }


    /**
     * @return the enableLocalAuth
     */
    @Override
    public Boolean getEnableLocalAuth () {
        return this.enableLocalAuth;
    }


    /**
     * @param enableLocalAuth
     *            the enableLocalAuth to set
     */
    @Override
    public void setEnableLocalAuth ( Boolean enableLocalAuth ) {
        this.enableLocalAuth = enableLocalAuth;
    }


    /**
     * @return the allowInsecureAuth
     */
    @Override
    public Boolean getAllowInsecureAuth () {
        return this.allowInsecureAuth;
    }


    /**
     * @param allowInsecureAuth
     *            the allowInsecureAuth to set
     */
    @Override
    public void setAllowInsecureAuth ( Boolean allowInsecureAuth ) {
        this.allowInsecureAuth = allowInsecureAuth;
    }


    /**
     * @return the passwordPolicy
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = PasswordPolicyConfigImpl.class )
    public PasswordPolicyConfigMutable getPasswordPolicy () {
        return this.passwordPolicy;
    }


    /**
     * @param passwordPolicy
     *            the passwordPolicy to set
     */
    @Override
    public void setPasswordPolicy ( PasswordPolicyConfigMutable passwordPolicy ) {
        this.passwordPolicy = (PasswordPolicyConfigImpl) passwordPolicy;
    }


    /**
     * @return the loginRateLimit
     */
    @Override
    @ManyToOne ( optional = true, fetch = FetchType.LAZY, targetEntity = LoginRateLimitConfigImpl.class )
    public LoginRateLimitConfigMutable getLoginRateLimit () {
        return this.loginRateLimit;
    }


    /**
     * @param loginRateLimit
     *            the loginRateLimit to set
     */
    @Override
    public void setLoginRateLimit ( LoginRateLimitConfigMutable loginRateLimit ) {
        this.loginRateLimit = (LoginRateLimitConfigImpl) loginRateLimit;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.auth.AuthenticatorsConfig#getAuthenticators()
     */
    @Override
    @ManyToMany ( fetch = FetchType.LAZY, targetEntity = AbstractAuthenticatorConfigImpl.class )
    public Set<AuthenticatorConfig> getAuthenticators () {
        return this.authenticators;
    }


    /**
     * @param authenticators
     *            the authenticators to set
     */
    @Override
    public void setAuthenticators ( Set<AuthenticatorConfig> authenticators ) {
        this.authenticators = authenticators;
    }

}
