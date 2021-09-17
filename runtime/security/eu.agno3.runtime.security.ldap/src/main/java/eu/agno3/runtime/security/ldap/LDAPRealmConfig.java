/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2015 by mbechler
 */
package eu.agno3.runtime.security.ldap;


import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import eu.agno3.runtime.ldap.client.AttributeMatchStyle;
import eu.agno3.runtime.security.ldap.internal.LDAPObjectBaseConfig;


/**
 * @author mbechler
 *
 */
public interface LDAPRealmConfig {

    /**
     * @return the pageSize
     */
    int getPageSize ();


    /**
     * @return the operationalMapper
     */
    LDAPObjectMapper<LDAPOperational, LDAPOperationalAttrs> getOperationalMapper ();


    /**
     * @return the userMapper
     */
    LDAPObjectMapper<LDAPUser, LDAPUserAttrs> getUserMapper ();


    /**
     * @return the userConfig
     */
    LDAPObjectBaseConfig getUserConfig ();


    /**
     * @return the groupMapper
     */
    LDAPObjectMapper<LDAPGroup, LDAPGroupAttrs> getGroupMapper ();


    /**
     * @return the groupConfig
     */
    LDAPObjectBaseConfig getGroupConfig ();


    /**
     * @return the groupMemberIsDN
     */
    boolean isReferencesAreDNs ();


    /**
     * @return the recursiveResolveGroups
     */
    boolean isRecursiveResolveGroups ();


    /**
     * @return the useForwardGroups
     */
    boolean isUseForwardGroups ();


    /**
     * @return the alwaysAddRoles
     */
    Set<String> getAlwaysAddRoles ();


    /**
     * @return the staticRoleMappings
     */
    Map<String, Set<String>> getStaticRoleMappings ();


    /**
     * @return the patternRoleMappings
     */
    Map<Pattern, Set<String>> getPatternRoleMappings ();


    /**
     * @return the addGroupNameAsRole
     */
    boolean isAddGroupNameAsRole ();


    /**
     * @return whether to remove missing entries
     */
    boolean isRemoveMissing ();


    /**
     * @return whether to remove based on UUIDs (otherwise DNs will be used)
     */
    boolean isRemovalUseUUIDs ();


    /**
     * @return whether to check password policy on login
     */
    boolean isEnforcePasswordPolicy ();


    /**
     * @return whether to check password policy on password change
     */
    boolean isEnforcePasswordPolicyOnChange ();


    /**
     * @return the schema style used
     */
    LDAPSchemaStyle getStyle ();


    /**
     * @return attribute based on whichs value additional roles may be added
     */
    Map<String, AttributeMatchStyle> getRoleMapAttributes ();


    /**
     * @return map of value to roles to add for role map attribute
     */
    Map<String, Set<String>> getAttributeRoleMappings ();


    /**
     * @return whether to provide user details from LDAP
     */
    boolean isProvideUserDetails ();


    /**
     * 
     * @return attributes to fetch for group objects
     */
    String[] getGroupAttrs ();


    /**
     * 
     * @return attributes to fetch for user objects
     */
    String[] getUserAttrs ();

}