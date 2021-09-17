/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth;


/**
 * @author mbechler
 *
 */
public class ObjectFactory {

    /**
     * 
     * @return default impl
     */
    public AuthenticatorsConfig makeAuthenticatorsConfig () {
        return new AuthenticatorsConfigImpl();
    }


    /**
     * 
     * @return default impl
     */
    public PasswordPolicyConfig makePasswordPolicyConfig () {
        return new PasswordPolicyConfigImpl();
    }


    /**
     * 
     * @return default impl
     */
    public LoginRateLimitConfig makeLoginRateLimitConfig () {
        return new LoginRateLimitConfigImpl();
    }


    /**
     * 
     * @return default impl
     */
    public StaticRoleMapEntry makeStaticRoleMapEntry () {
        return new StaticRoleMapEntryImpl();
    }


    /**
     * 
     * @return default impl
     */
    public PatternRoleMapEntry makePatternRoleMapEntry () {
        return new PatternRoleMapEntryImpl();
    }


    /**
     * 
     * @return default impl
     */
    public StaticRolesConfig makeStaticRolesConfig () {
        return new StaticRolesConfigImpl();
    }


    /**
     * 
     * @return default impl
     */
    public RoleConfig makeRoleConfig () {
        return new RoleConfigImpl();
    }
}
