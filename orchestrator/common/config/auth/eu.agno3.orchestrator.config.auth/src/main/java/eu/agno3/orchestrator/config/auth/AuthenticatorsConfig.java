/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth;


import java.util.Set;

import javax.validation.Valid;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( AuthenticatorsConfigObjectTypeDescriptor.OBJECT_TYPE )
public interface AuthenticatorsConfig extends ConfigurationObject {

    /**
     * 
     * @return the configured authenticators
     */
    @ReferencedObject
    @Valid
    Set<AuthenticatorConfig> getAuthenticators ();


    /**
     * 
     * @return whether to enable local authentication
     */
    Boolean getEnableLocalAuth ();


    /**
     * 
     * @return the password policy
     */
    @ReferencedObject
    @Valid
    PasswordPolicyConfig getPasswordPolicy ();


    /**
     * @return the login rate limiter config
     */
    @ReferencedObject
    @Valid
    LoginRateLimitConfig getLoginRateLimit ();


    /**
     * @return whether to allow authentication attempts over insecure channels
     */
    Boolean getAllowInsecureAuth ();

}
