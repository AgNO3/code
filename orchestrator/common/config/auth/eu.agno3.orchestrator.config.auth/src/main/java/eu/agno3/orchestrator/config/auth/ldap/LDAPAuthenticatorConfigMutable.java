/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth.ldap;


import java.util.Set;

import eu.agno3.orchestrator.config.auth.AuthenticatorConfigMutable;
import eu.agno3.orchestrator.config.auth.PatternRoleMapEntry;
import eu.agno3.orchestrator.config.auth.StaticRoleMapEntry;
import eu.agno3.orchestrator.config.web.LDAPConfigurationMutable;


/**
 * @author mbechler
 *
 */
public interface LDAPAuthenticatorConfigMutable extends LDAPAuthenticatorConfig, AuthenticatorConfigMutable {

    /**
     * 
     * @param schemaConfig
     */
    void setSchemaConfig ( LDAPAuthSchemaConfigMutable schemaConfig );


    /**
     * 
     * @param connectionConfig
     */
    void setConnectionConfig ( LDAPConfigurationMutable connectionConfig );


    /**
     * 
     * @param enforcePasswordPolicyOnChange
     */
    void setEnforcePasswordPolicyOnChange ( Boolean enforcePasswordPolicyOnChange );


    /**
     * 
     * @param enforcePasswordPolicy
     */
    void setEnforcePasswordPolicy ( Boolean enforcePasswordPolicy );


    /**
     * 
     * @param patternRoleMappings
     */
    void setPatternRoleMappings ( Set<PatternRoleMapEntry> patternRoleMappings );


    /**
     * 
     * @param staticRoleMappings
     */
    void setStaticRoleMappings ( Set<StaticRoleMapEntry> staticRoleMappings );


    /**
     * 
     * @param alwaysAddRoles
     */
    void setAlwaysAddRoles ( Set<String> alwaysAddRoles );


    /**
     * 
     * @param addGroupNameAsRole
     */
    void setAddGroupNameAsRole ( Boolean addGroupNameAsRole );


    /**
     * @param enableSynchronization
     */
    void setEnableSynchronization ( Boolean enableSynchronization );


    /**
     * @param syncOptions
     */
    void setSyncOptions ( LDAPSyncOptionsMutable syncOptions );


    /**
     * @param disableAuthentication
     */
    void setDisableAuthentication ( Boolean disableAuthentication );

}
