/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.02.2015 by mbechler
 */
package eu.agno3.runtime.security.ldap.internal;


import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchRequest;

import eu.agno3.runtime.ldap.client.AttributeMatchStyle;
import eu.agno3.runtime.security.ldap.LDAPGroup;
import eu.agno3.runtime.security.ldap.LDAPGroupAttrs;
import eu.agno3.runtime.security.ldap.LDAPObjectMapper;
import eu.agno3.runtime.security.ldap.LDAPOperational;
import eu.agno3.runtime.security.ldap.LDAPOperationalAttrs;
import eu.agno3.runtime.security.ldap.LDAPRealmConfig;
import eu.agno3.runtime.security.ldap.LDAPSchemaStyle;
import eu.agno3.runtime.security.ldap.LDAPUser;
import eu.agno3.runtime.security.ldap.LDAPUserAttrs;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 */
public class LDAPRealmConfigImpl implements LDAPRealmConfig {

    private static final Logger log = Logger.getLogger(LDAPRealmConfigImpl.class);

    private LDAPSchemaStyle style;
    private LDAPSchemaStyle userStyle;
    private LDAPSchemaStyle groupStyle;
    private LDAPSchemaStyle operationalStyle;

    private LDAPObjectMapper<LDAPOperational, LDAPOperationalAttrs> operationalMapper;
    private LDAPObjectBaseConfig userConfig;
    private LDAPObjectMapper<LDAPUser, LDAPUserAttrs> userMapper;
    private LDAPObjectBaseConfig groupConfig;
    private LDAPObjectMapper<LDAPGroup, LDAPGroupAttrs> groupMapper;

    private boolean groupMemberIsDN = true;
    private boolean recursiveResolveGroups = false;
    private boolean useForwardGroups = false;

    private boolean removeMissing = true;
    private boolean removeUseUUIDs = true;

    private boolean addGroupNameAsRole = false;
    private Set<String> alwaysAddRoles = new HashSet<>();
    private Map<String, Set<String>> staticRoleMappings = new HashMap<>();
    private Map<Pattern, Set<String>> patternRoleMappings = new HashMap<>();

    private Map<String, AttributeMatchStyle> roleMapAttributes = new HashMap<>();
    private Map<String, Set<String>> attributeRoleMappings = new HashMap<>();

    private int pageSize = 64;
    private boolean enforcePasswordPolicy;

    private boolean enforcePasswordPolicyOnChange;

    private boolean provideUserDetails;

    private String[] groupAttrs;
    private String[] userAttrs;


    /**
     * 
     * @param properties
     * @return a new configuration from the properties
     * @throws LDAPException
     */
    public static LDAPRealmConfig parseConfig ( Dictionary<String, Object> properties ) throws LDAPException {
        LDAPRealmConfigImpl cfg = new LDAPRealmConfigImpl();

        cfg.style = getStyle(properties, "schemaStyle", LDAPSchemaStyle.LDAP); //$NON-NLS-1$
        cfg.userStyle = getStyle(properties, "user.style", cfg.style); //$NON-NLS-1$
        cfg.groupStyle = getStyle(properties, "group.style", cfg.style); //$NON-NLS-1$
        cfg.operationalStyle = getStyle(properties, "operational.style", cfg.style); //$NON-NLS-1$

        cfg.userConfig = LDAPObjectBaseConfig.parseConfig(
            properties,
            "user.", //$NON-NLS-1$
            cfg.userStyle.createUserFilter());
        cfg.groupConfig = LDAPObjectBaseConfig.parseConfig(
            properties,
            "group.", //$NON-NLS-1$
            cfg.groupStyle.createGroupFilter());
        cfg.operationalMapper = DefaultLDAPOperationalMapper.parseConfig(properties, "operational.attrs.", cfg.operationalStyle); //$NON-NLS-1$
        cfg.userMapper = DefaultLDAPUserMapper.parseConfig(properties, "user.attrs.", cfg.userStyle); //$NON-NLS-1$
        cfg.groupMapper = DefaultLDAPGroupMapper.parseConfig(properties, "group.attrs.", cfg.userStyle); //$NON-NLS-1$

        parseOptions(cfg, properties);
        parseRoleConfig(cfg, properties);

        buildAttributes(cfg);

        return cfg;
    }


    /**
     * @param cfg
     */
    private static void buildAttributes ( LDAPRealmConfigImpl cfg ) {
        List<String> groupAttrs = new LinkedList<>(
            Arrays.asList(
                SearchRequest.ALL_USER_ATTRIBUTES,
                cfg.getOperationalMapper().getAttributeName(LDAPOperationalAttrs.MODIFY_TIMESTAMP),
                cfg.getOperationalMapper().getAttributeName(LDAPOperationalAttrs.CREATE_TIMESTAMP),
                cfg.getOperationalMapper().getAttributeName(LDAPOperationalAttrs.UUID),
                cfg.getGroupMapper().getAttributeName(LDAPGroupAttrs.NAME),
                cfg.getGroupMapper().getAttributeName(LDAPGroupAttrs.ROLE),
                cfg.getGroupMapper().getAttributeName(LDAPGroupAttrs.MEMBER_OF)));

        groupAttrs.addAll(cfg.getRoleMapAttributes().keySet());
        cfg.groupAttrs = groupAttrs.toArray(new String[groupAttrs.size()]);

        List<String> userAttrs = new LinkedList<>(
            Arrays.asList(
                SearchRequest.ALL_USER_ATTRIBUTES,
                cfg.getOperationalMapper().getAttributeName(LDAPOperationalAttrs.MODIFY_TIMESTAMP),
                cfg.getOperationalMapper().getAttributeName(LDAPOperationalAttrs.CREATE_TIMESTAMP),
                cfg.getOperationalMapper().getAttributeName(LDAPOperationalAttrs.UUID)));

        userAttrs.addAll(cfg.getRoleMapAttributes().keySet());
        cfg.userAttrs = userAttrs.toArray(new String[userAttrs.size()]);
    }


    /**
     * @param properties
     * @param ldap
     * @return
     */
    private static LDAPSchemaStyle getStyle ( Dictionary<String, Object> properties, String name, LDAPSchemaStyle def ) {
        String styleAttr = (String) properties.get(name);
        LDAPSchemaStyle style;
        if ( StringUtils.isBlank(styleAttr) ) {
            style = def;
        }
        else {
            style = LDAPSchemaStyle.valueOf(styleAttr.trim());
        }
        return style;
    }


    /**
     * @param cfg
     * @param properties
     * @param style
     */
    private static void parseOptions ( LDAPRealmConfigImpl cfg, Dictionary<String, Object> properties ) {
        cfg.groupMemberIsDN = ConfigUtil.parseBoolean(properties, "groupMemberIsDN", true); //$NON-NLS-1$
        cfg.recursiveResolveGroups = ConfigUtil.parseBoolean(properties, "recursiveResolveGroups", false); //$NON-NLS-1$
        cfg.useForwardGroups = ConfigUtil.parseBoolean(properties, "useForwardGroups", false); //$NON-NLS-1$
        cfg.enforcePasswordPolicy = ConfigUtil.parseBoolean(properties, "enforcePasswordPolicy", false); //$NON-NLS-1$
        cfg.enforcePasswordPolicyOnChange = ConfigUtil.parseBoolean(properties, "enforcePasswordPolicyOnChange", true); //$NON-NLS-1$

        cfg.removeMissing = ConfigUtil.parseBoolean(properties, "removeMissing", false); //$NON-NLS-1$
        cfg.removeUseUUIDs = ConfigUtil.parseBoolean(properties, "removeUseUUIDs", false); //$NON-NLS-1$
        cfg.pageSize = ConfigUtil.parseInt(properties, "pageSize", 64); //$NON-NLS-1$

        cfg.provideUserDetails = ConfigUtil.parseBoolean(properties, "provideUserDetails", false); //$NON-NLS-1$
    }


    /**
     * @param cfg
     * @param properties
     * @param style
     */
    private static void parseRoleConfig ( LDAPRealmConfigImpl cfg, Dictionary<String, Object> properties ) {
        cfg.alwaysAddRoles = ConfigUtil.parseStringSet(properties, "alwaysAddRoles", new HashSet<>()); //$NON-NLS-1$
        cfg.addGroupNameAsRole = ConfigUtil.parseBoolean(properties, "addGroupNameAsRole", false); //$NON-NLS-1$

        cfg.staticRoleMappings = new HashMap<>();
        Map<String, List<String>> sRoles = ConfigUtil.parseStringMultiMap(properties, "staticRoleMappings", new HashMap<>()); //$NON-NLS-1$
        for ( Entry<String, List<String>> e : sRoles.entrySet() ) {
            cfg.staticRoleMappings.put(e.getKey().toLowerCase(Locale.ROOT), new HashSet<>(e.getValue()));
        }

        cfg.patternRoleMappings = new HashMap<>();
        Map<String, List<String>> pRoles = ConfigUtil.parseStringMultiMap(properties, "patternRoleMappings", new HashMap<>()); //$NON-NLS-1$
        for ( Entry<String, List<String>> e : pRoles.entrySet() ) {
            try {
                cfg.patternRoleMappings.put(Pattern.compile(e.getKey(), Pattern.CASE_INSENSITIVE), new HashSet<>(e.getValue()));
            }
            catch ( PatternSyntaxException ex ) {
                log.warn("Failed to compile pattern for role mapping", ex); //$NON-NLS-1$
            }
        }

        AttributeMatchStyle defaultMatchStyle = AttributeMatchStyle.STRING;
        try {
            defaultMatchStyle = AttributeMatchStyle.valueOf(ConfigUtil.parseString(
                properties,
                "roleMapAttributeMatchStyle", //$NON-NLS-1$
                AttributeMatchStyle.STRING.name()));
        }
        catch ( IllegalArgumentException e ) {
            log.error("Invalid roleMapAttributeMatchStyle", e); //$NON-NLS-1$
        }

        String singleMapAttribute = ConfigUtil.parseString(properties, "roleMapAttribute", null); //$NON-NLS-1$
        Map<String, AttributeMatchStyle> roleMapAttributes = new HashMap<>();
        if ( !StringUtils.isBlank(singleMapAttribute) ) {
            roleMapAttributes.put(singleMapAttribute, defaultMatchStyle);
        }

        Map<String, String> roleMapAttrs = ConfigUtil.parseStringMap(properties, "roleMapAttributes", Collections.EMPTY_MAP); //$NON-NLS-1$
        for ( Entry<String, String> entry : roleMapAttrs.entrySet() ) {
            AttributeMatchStyle style = defaultMatchStyle;
            try {
                if ( !StringUtils.isEmpty(entry.getValue()) ) {
                    style = AttributeMatchStyle.valueOf(entry.getValue().trim());
                }
            }
            catch ( IllegalArgumentException e ) {
                log.warn("Invalid match style " + entry.getValue()); //$NON-NLS-1$
            }
            roleMapAttributes.put(entry.getKey(), style);
        }
        cfg.roleMapAttributes = roleMapAttributes;

        cfg.attributeRoleMappings = new HashMap<>();
        Map<String, List<String>> sAttrs = ConfigUtil.parseStringMultiMap(properties, "attributeRoleMappings", new HashMap<>()); //$NON-NLS-1$
        for ( Entry<String, List<String>> e : sAttrs.entrySet() ) {
            String lk = e.getKey().toLowerCase(Locale.ROOT);
            cfg.attributeRoleMappings.put(lk, new HashSet<>(e.getValue()));
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPRealmConfig#getPageSize()
     */
    @Override
    public int getPageSize () {
        return this.pageSize;
    }


    /**
     * 
     * @return the schema style
     */
    @Override
    public LDAPSchemaStyle getStyle () {
        return this.style;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPRealmConfig#getOperationalMapper()
     */
    @Override
    public LDAPObjectMapper<LDAPOperational, LDAPOperationalAttrs> getOperationalMapper () {
        return this.operationalMapper;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPRealmConfig#getUserMapper()
     */
    @Override
    public LDAPObjectMapper<LDAPUser, LDAPUserAttrs> getUserMapper () {
        return this.userMapper;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPRealmConfig#getUserConfig()
     */
    @Override
    public LDAPObjectBaseConfig getUserConfig () {
        return this.userConfig;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPRealmConfig#getGroupMapper()
     */
    @Override
    public LDAPObjectMapper<LDAPGroup, LDAPGroupAttrs> getGroupMapper () {
        return this.groupMapper;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPRealmConfig#getGroupConfig()
     */
    @Override
    public LDAPObjectBaseConfig getGroupConfig () {
        return this.groupConfig;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPRealmConfig#isReferencesAreDNs()
     */
    @Override
    public boolean isReferencesAreDNs () {
        return this.groupMemberIsDN;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPRealmConfig#isRecursiveResolveGroups()
     */
    @Override
    public boolean isRecursiveResolveGroups () {
        return this.recursiveResolveGroups;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPRealmConfig#isUseForwardGroups()
     */
    @Override
    public boolean isUseForwardGroups () {
        return this.useForwardGroups;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPRealmConfig#getAlwaysAddRoles()
     */
    @Override
    public Set<String> getAlwaysAddRoles () {
        return this.alwaysAddRoles;
    }


    /**
     * @return the provideUserDetails
     */
    @Override
    public boolean isProvideUserDetails () {
        return this.provideUserDetails;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPRealmConfig#getStaticRoleMappings()
     */
    @Override
    public Map<String, Set<String>> getStaticRoleMappings () {
        return this.staticRoleMappings;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPRealmConfig#getPatternRoleMappings()
     */
    @Override
    public Map<Pattern, Set<String>> getPatternRoleMappings () {
        return this.patternRoleMappings;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPRealmConfig#isAddGroupNameAsRole()
     */
    @Override
    public boolean isAddGroupNameAsRole () {
        return this.addGroupNameAsRole;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPRealmConfig#isRemoveMissing()
     */
    @Override
    public boolean isRemoveMissing () {
        return this.removeMissing;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPRealmConfig#isRemovalUseUUIDs()
     */
    @Override
    public boolean isRemovalUseUUIDs () {
        return this.removeUseUUIDs;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPRealmConfig#isEnforcePasswordPolicy()
     */
    @Override
    public boolean isEnforcePasswordPolicy () {
        return this.enforcePasswordPolicy;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPRealmConfig#isEnforcePasswordPolicyOnChange()
     */
    @Override
    public boolean isEnforcePasswordPolicyOnChange () {
        return this.enforcePasswordPolicyOnChange;
    }


    /**
     * @return the roleMapAttribute
     */
    @Override
    public Map<String, AttributeMatchStyle> getRoleMapAttributes () {
        return this.roleMapAttributes;
    }


    /**
     * @return the attributeRoleMappings
     */
    @Override
    public Map<String, Set<String>> getAttributeRoleMappings () {
        return this.attributeRoleMappings;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPRealmConfig#getGroupAttrs()
     */
    @Override
    public String[] getGroupAttrs () {
        return this.groupAttrs;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPRealmConfig#getUserAttrs()
     */
    @Override
    public String[] getUserAttrs () {
        return this.userAttrs;
    }

}