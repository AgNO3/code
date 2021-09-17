/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth.ldap;


import java.util.Set;

import javax.validation.Valid;

import eu.agno3.orchestrator.config.auth.AuthenticatorConfig;
import eu.agno3.orchestrator.config.auth.PatternRoleMapEntry;
import eu.agno3.orchestrator.config.auth.StaticRoleMapEntry;
import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.web.LDAPConfiguration;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( LDAPAuthenticatorConfigObjectTypeDescriptor.TYPE_NAME )
public interface LDAPAuthenticatorConfig extends AuthenticatorConfig {

    /**
     * 
     * @return auth schema config
     */
    @ReferencedObject
    @Valid
    LDAPAuthSchemaConfig getSchemaConfig ();


    /**
     * 
     * @return auth connection config
     */
    @ReferencedObject
    @Valid
    LDAPConfiguration getConnectionConfig ();


    /**
     * 
     * @return whether to enforce password policy on password change
     */
    Boolean getEnforcePasswordPolicyOnChange ();


    /**
     * 
     * @return whether to enforce password policy on login
     */
    Boolean getEnforcePasswordPolicy ();


    /**
     * 
     * @return DN patterns, add roles when matched
     */
    @ReferencedObject
    @Valid
    Set<PatternRoleMapEntry> getPatternRoleMappings ();


    /**
     * 
     * @return DNs, add roles when matched
     */
    @ReferencedObject
    @Valid
    Set<StaticRoleMapEntry> getStaticRoleMappings ();


    /**
     * 
     * @return roles to add to all authenticated users
     */
    Set<String> getAlwaysAddRoles ();


    /**
     * 
     * @return add the group names as roles
     */
    Boolean getAddGroupNameAsRole ();


    /**
     * @return whether to enable directory synchronization
     */
    Boolean getEnableSynchronization ();


    /**
     * @return directory synchronization options
     */
    @ReferencedObject
    @Valid
    LDAPSyncOptions getSyncOptions ();


    /**
     * @return whether authentication is disabled, will be only used for role mapping/synchronization
     */
    Boolean getDisableAuthentication ();

}
