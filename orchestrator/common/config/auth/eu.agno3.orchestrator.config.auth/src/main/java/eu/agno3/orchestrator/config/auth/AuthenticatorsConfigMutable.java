/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth;


import java.util.Set;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( AuthenticatorsConfig.class )
public interface AuthenticatorsConfigMutable extends AuthenticatorsConfig {

    /**
     * @param authenticators
     */
    void setAuthenticators ( Set<AuthenticatorConfig> authenticators );


    /**
     * 
     * @param passwordPolicy
     */
    void setPasswordPolicy ( PasswordPolicyConfigMutable passwordPolicy );


    /**
     * 
     * @param enableLocalAuth
     */
    void setEnableLocalAuth ( Boolean enableLocalAuth );


    /**
     * @param loginRateLimit
     */
    void setLoginRateLimit ( LoginRateLimitConfigMutable loginRateLimit );


    /**
     * @param allowInsecureAuth
     */
    void setAllowInsecureAuth ( Boolean allowInsecureAuth );

}
