/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 25, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.auth.validation.internal;


import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;

import eu.agno3.orchestrator.config.auth.UserPasswordAuthTestParams;
import eu.agno3.orchestrator.config.auth.ldap.LDAPAuthSchemaConfig;
import eu.agno3.orchestrator.config.auth.ldap.LDAPAuthenticatorConfig;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestAsyncHandler;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestContext;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPlugin;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPluginAsync;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPluginRunOn;
import eu.agno3.orchestrator.config.model.validation.ConfigTestParams;
import eu.agno3.orchestrator.config.model.validation.ConfigTestResult;
import eu.agno3.orchestrator.config.model.validation.ConfigTestState;
import eu.agno3.orchestrator.config.web.LDAPConfiguration;
import eu.agno3.orchestrator.config.web.LDAPConfigurationObjectTypeDescriptor;
import eu.agno3.orchestrator.config.web.LDAPObjectAttributeMapping;
import eu.agno3.orchestrator.config.web.LDAPObjectConfig;
import eu.agno3.orchestrator.config.web.LDAPSearchScope;
import eu.agno3.orchestrator.config.web.LDAPServerType;
import eu.agno3.orchestrator.config.web.validation.ldap.LDAPConfigTestPlugin;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.ldap.client.LDAPClient;
import eu.agno3.runtime.ldap.client.LDAPConfigurationException;
import eu.agno3.runtime.security.ldap.LDAPGroup;
import eu.agno3.runtime.security.ldap.LDAPGroupAttrs;
import eu.agno3.runtime.security.ldap.LDAPObjectMapper;
import eu.agno3.runtime.security.ldap.LDAPObjectMapperFactory;
import eu.agno3.runtime.security.ldap.LDAPOperational;
import eu.agno3.runtime.security.ldap.LDAPOperationalAttrs;
import eu.agno3.runtime.security.ldap.LDAPSchemaStyle;
import eu.agno3.runtime.security.ldap.LDAPUser;
import eu.agno3.runtime.security.ldap.LDAPUserAttrs;
import eu.agno3.runtime.util.uuid.UUIDUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    ConfigTestPlugin.class
} )
public class LDAPAuthenticatorConfigurationTestPlugin implements ConfigTestPluginAsync<LDAPAuthenticatorConfig> {

    private static final Logger log = Logger.getLogger(LDAPAuthenticatorConfigurationTestPlugin.class);

    private LDAPConfigTestPlugin ldapConfigTest;

    private LDAPObjectMapperFactory objectMapperFactory;

    private static final String[] MINIMAL_ATTRS = new String[] {
        "objectClass", //$NON-NLS-1$
        "distinguishedName" //$NON-NLS-1$
    };


    @Override
    public Set<ConfigTestPluginRunOn> getRunOn () {
        return Collections.singleton(ConfigTestPluginRunOn.SERVER);
    }


    @Reference
    protected synchronized void setLDAPClientBuilder ( LDAPConfigTestPlugin lcf ) {
        this.ldapConfigTest = lcf;
    }


    protected synchronized void unsetLDAPClientBuilder ( LDAPConfigTestPlugin lcf ) {
        if ( this.ldapConfigTest == lcf ) {
            this.ldapConfigTest = null;
        }
    }


    @Reference
    protected synchronized void setLDAPObjectMapperFactory ( LDAPObjectMapperFactory omf ) {
        this.objectMapperFactory = omf;
    }


    protected synchronized void unsetLDAPObjectMapperFactory ( LDAPObjectMapperFactory omf ) {
        if ( this.objectMapperFactory == omf ) {
            this.objectMapperFactory = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPlugin#getTargetType()
     */
    @Override
    public Class<LDAPAuthenticatorConfig> getTargetType () {
        return LDAPAuthenticatorConfig.class;
    }


    @Override
    public ConfigTestResult testAsync ( LDAPAuthenticatorConfig config, ConfigTestContext ctx, ConfigTestParams params, ConfigTestResult r,
            ConfigTestAsyncHandler h ) throws ModelServiceException {
        log.debug("Running LDAP authenticator test"); //$NON-NLS-1$

        if ( ! ( params instanceof UserPasswordAuthTestParams ) ) {
            throw new ModelServiceException("Invalid test parameters"); //$NON-NLS-1$
        }

        UserPasswordAuthTestParams tp = (UserPasswordAuthTestParams) params;

        LDAPConfiguration connectionConfig = config.getConnectionConfig();
        @SuppressWarnings ( "resource" )
        LDAPClient conn = this.ldapConfigTest.getConnection(connectionConfig, params, r.withType(LDAPConfigurationObjectTypeDescriptor.TYPE_NAME), h);
        if ( conn == null ) {
            return r.state(ConfigTestState.FAILURE);
        }

        try {

            LDAPAuthSchemaConfig sc = config.getSchemaConfig();
            LDAPObjectConfig us = sc.getUserSchema();
            LDAPObjectConfig gs = sc.getGroupSchema();

            LDAPSchemaStyle opStyle = connectionConfig.getServerType() == LDAPServerType.AD ? LDAPSchemaStyle.AD : LDAPSchemaStyle.LDAP;
            LDAPObjectMapper<LDAPOperational, LDAPOperationalAttrs> opMapper = this.objectMapperFactory
                    .createOperationalMapper(opStyle, makeAttributeOverrides(sc.getOperationalAttributeMappings()));

            LDAPSchemaStyle groupStyle = connectionConfig.getServerType() == LDAPServerType.AD ? LDAPSchemaStyle.AD
                    : LDAPSchemaStyle.valueOf(gs.getAttributeStyle());
            LDAPObjectMapper<LDAPGroup, LDAPGroupAttrs> groupMapper = this.objectMapperFactory
                    .createGroupMapper(groupStyle, makeAttributeOverrides(gs.getCustomAttributeMappings()));

            LDAPSchemaStyle userStyle = connectionConfig.getServerType() == LDAPServerType.AD ? LDAPSchemaStyle.AD
                    : LDAPSchemaStyle.valueOf(us.getAttributeStyle());
            LDAPObjectMapper<LDAPUser, LDAPUserAttrs> userMapper = this.objectMapperFactory
                    .createUserMapper(userStyle, makeAttributeOverrides(us.getCustomAttributeMappings()));

            SearchResultEntry testUserEntry = testUsers(r, h, conn, us, userStyle);
            if ( testUserEntry != null ) {
                testUserMapping(conn, r, sc, opStyle, userStyle, testUserEntry, opMapper, userMapper);
            }

            SearchResultEntry testGroupEntry = testGroups(r, h, conn, gs, groupStyle);
            if ( testGroupEntry != null ) {
                testGroupMapping(conn, r, sc, opStyle, groupStyle, testGroupEntry, opMapper, groupMapper);
            }

            return testBind(r, h, tp, connectionConfig, conn, us, userStyle, userMapper);
        }
        catch ( Exception e ) {
            log.debug("Caught exception", e); //$NON-NLS-1$
            r.error("LDAP_FAIL", e.getMessage()); //$NON-NLS-1$
            return r.state(ConfigTestState.FAILURE);
        }
        finally {
            try {
                conn.close();
            }
            catch ( Exception e ) {
                log.warn("Failed to close LDAP connection", e); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param r
     * @param h
     * @param tp
     * @param connectionConfig
     * @param conn
     * @param us
     * @param userStyle
     * @param userMapper
     * @return
     * @throws LDAPException
     * @throws LDAPConfigurationException
     * @throws CryptoException
     */
    private ConfigTestResult testBind ( ConfigTestResult r, ConfigTestAsyncHandler h, UserPasswordAuthTestParams tp,
            LDAPConfiguration connectionConfig, LDAPClient conn, LDAPObjectConfig us, LDAPSchemaStyle userStyle,
            LDAPObjectMapper<LDAPUser, LDAPUserAttrs> userMapper ) throws LDAPException, LDAPConfigurationException, CryptoException {
        String testUser = tp.getUsername();
        String testPassword = tp.getPassword();
        if ( !StringUtils.isBlank(testUser) && !StringUtils.isBlank(testPassword) ) {
            DN userDn = lookupTestUser(r, conn, us, userStyle, userMapper, testUser);
            if ( userDn == null ) {
                return r.state(ConfigTestState.FAILURE);
            }
            try {
                r.info("LDAP_BIND_BINDING", userDn.toString()); //$NON-NLS-1$
                h.update(r);
                this.ldapConfigTest.tryBind(connectionConfig, userDn, testPassword);
                r.info("LDAP_BIND_SUCESSS", userDn.toString()); //$NON-NLS-1$
            }
            catch ( LDAPException e ) {
                log.debug("Bind failed", e); //$NON-NLS-1$
                r.error("LDAP_BIND_FAILURE", userDn.toString(), e.getMessage()); //$NON-NLS-1$
                return r.state(ConfigTestState.FAILURE);
            }
        }
        else {
            r.warn("LDAP_BIND_NO_USER"); //$NON-NLS-1$
        }
        return r.state(ConfigTestState.SUCCESS);
    }


    /**
     * @param conn
     * @param r
     * @param testGroupEntry
     * @param opMapper
     * @param groupMapper
     */
    private static void testGroupMapping ( LDAPClient conn, ConfigTestResult r, LDAPAuthSchemaConfig sc, LDAPSchemaStyle opStyle,
            LDAPSchemaStyle style, SearchResultEntry testGroupEntry, LDAPObjectMapper<LDAPOperational, LDAPOperationalAttrs> opMapper,
            LDAPObjectMapper<LDAPGroup, LDAPGroupAttrs> groupMapper ) {
        try {
            SearchResultEntry entry = conn.getEntry(
                testGroupEntry.getDN(),
                SearchRequest.ALL_USER_ATTRIBUTES,
                opMapper.getAttributeName(LDAPOperationalAttrs.MODIFY_TIMESTAMP),
                opMapper.getAttributeName(LDAPOperationalAttrs.CREATE_TIMESTAMP),
                opMapper.getAttributeName(LDAPOperationalAttrs.UUID));

            testOperationalAttributes(r, opStyle, opMapper, entry);

            LDAPGroup group = groupMapper.mapObject(entry);

            r.info(
                "LDAP_GROUP_MAPPED", //$NON-NLS-1$
                entry.getDN(),
                group.getName() != null ? group.getName() : StringUtils.EMPTY,
                group.getDisplayName() != null ? group.getDisplayName() : StringUtils.EMPTY);

            if ( !sc.getUseForwardGroups() ) {
                String[] members = entry.getAttributeValues(groupMapper.getAttributeName(LDAPGroupAttrs.MEMBER));
                if ( members != null && members.length > 0 ) {
                    r.info("LDAP_GROUP_MEMBERS", String.valueOf(members.length)); //$NON-NLS-1$
                    for ( String member : members ) {
                        checkReference(r, style, member);
                    }
                }
                else {
                    r.info("LDAP_GROUP_NO_MEMBERS"); //$NON-NLS-1$
                }
            }
        }
        catch ( LDAPException e ) {
            r.error("LDAP_GROUP_MAPPING_FAIL", e.getMessage()); //$NON-NLS-1$
        }
    }


    /**
     * @param conn
     * @param sc
     * @param testUserEntry
     * @param userMapper
     * @param opMapper
     * @param us
     * @param userStyle
     */
    private static void testUserMapping ( LDAPClient conn, ConfigTestResult r, LDAPAuthSchemaConfig sc, LDAPSchemaStyle opStyle,
            LDAPSchemaStyle style, SearchResultEntry testUserEntry, LDAPObjectMapper<LDAPOperational, LDAPOperationalAttrs> opMapper,
            LDAPObjectMapper<LDAPUser, LDAPUserAttrs> userMapper ) {

        try {
            SearchResultEntry entry = conn.getEntry(
                testUserEntry.getDN(),
                SearchRequest.ALL_USER_ATTRIBUTES,
                opMapper.getAttributeName(LDAPOperationalAttrs.MODIFY_TIMESTAMP),
                opMapper.getAttributeName(LDAPOperationalAttrs.CREATE_TIMESTAMP),
                opMapper.getAttributeName(LDAPOperationalAttrs.UUID));

            testOperationalAttributes(r, opStyle, opMapper, entry);

            LDAPUser user = userMapper.mapObject(entry);
            r.info(
                "LDAP_USER_MAPPED", //$NON-NLS-1$
                entry.getDN(),
                user.getUsername() != null ? user.getUsername() : StringUtils.EMPTY,
                user.getDisplayName() != null ? user.getDisplayName() : StringUtils.EMPTY,
                user.getMailAddress() != null ? user.getMailAddress() : StringUtils.EMPTY);

            if ( sc.getUseForwardGroups() ) {
                String[] membership = entry.getAttributeValues(userMapper.getAttributeName(LDAPUserAttrs.MEMBER_OF));
                if ( membership != null && membership.length > 0 ) {
                    r.info("LDAP_USER_MEMBERSHIP", String.valueOf(membership.length)); //$NON-NLS-1$
                    for ( String member : membership ) {
                        checkReference(r, style, member);
                    }
                }
                else {
                    r.info("LDAP_USER_NO_MEMBERSHIP"); //$NON-NLS-1$
                }
            }

        }
        catch ( LDAPException e ) {
            r.error("LDAP_USER_MAPPING_FAIL", e.getMessage()); //$NON-NLS-1$
        }
    }


    /**
     * @param r
     * @param sc
     * @param member
     */
    static void checkReference ( ConfigTestResult r, LDAPSchemaStyle sc, String member ) {
        if ( sc.isGroupMembersAreDN() ) {
            try {
                new DN(member);
            }
            catch ( LDAPException e ) {
                log.debug("Invalid member DN " + member, e); //$NON-NLS-1$
                r.warn("LDAP_REFERENCE_NODN", member); //$NON-NLS-1$
            }
        }
        else {
            if ( member.indexOf('=') >= 0 ) {
                r.warn("LDAP_REFERENCE_DN", member); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param r
     * @param opMapper
     * @param entry
     */
    private static void testOperationalAttributes ( ConfigTestResult r, LDAPSchemaStyle style,
            LDAPObjectMapper<LDAPOperational, LDAPOperationalAttrs> opMapper, SearchResultEntry entry ) {
        String uuidAttributeName = opMapper.getAttributeName(LDAPOperationalAttrs.UUID);
        String modifyAttributeName = opMapper.getAttributeName(LDAPOperationalAttrs.MODIFY_TIMESTAMP);
        Date modifyTimestamp = entry.getAttributeValueAsDate(modifyAttributeName);

        if ( !entry.hasAttribute(uuidAttributeName) ) {
            r.warn("LDAP_OPERATIONAL_NOUUID", uuidAttributeName); //$NON-NLS-1$
        }
        else {
            try {
                extractUUID(style, entry, uuidAttributeName);
            }
            catch ( IllegalArgumentException e ) {
                log.debug("Failed to parse UUID", e); //$NON-NLS-1$
                r.error("LDAP_OPERATIONAL_UUID_INVALID", style.isIdsAreBinary() ? StringUtils.EMPTY : entry.getAttributeValue(uuidAttributeName)); //$NON-NLS-1$
            }
        }

        if ( modifyTimestamp == null ) {
            r.warn("LDAP_OPERATIONAL_NOMODIFY", modifyAttributeName); //$NON-NLS-1$
        }

    }


    /**
     * @param handler
     * @param entry
     * @param uuidAttr
     * @return
     */
    private static UUID extractUUID ( LDAPSchemaStyle style, SearchResultEntry entry, String uuidAttr ) {
        if ( style.isIdsAreBinary() ) {
            return UUIDUtil.fromBytes(entry.getAttributeValueBytes(uuidAttr));
        }
        return UUID.fromString(entry.getAttributeValue(uuidAttr));
    }


    /**
     * @param mappings
     * @return
     */
    private static Map<String, String> makeAttributeOverrides ( Set<LDAPObjectAttributeMapping> mappings ) {
        Map<String, String> attrOverrides = new HashMap<>();
        if ( mappings == null ) {
            return attrOverrides;
        }
        for ( LDAPObjectAttributeMapping ldapObjectAttributeMapping : mappings ) {
            attrOverrides.put(ldapObjectAttributeMapping.getAttributeId(), ldapObjectAttributeMapping.getAttributeName());
        }
        return attrOverrides;
    }


    /**
     * @param userStyle
     * @param us
     * @param conn
     * @param userMapper
     * @param testUser
     * @return
     * @throws LDAPException
     */
    private static DN lookupTestUser ( ConfigTestResult r, LDAPClient conn, LDAPObjectConfig us, LDAPSchemaStyle userStyle,
            LDAPObjectMapper<LDAPUser, LDAPUserAttrs> userMapper, String testUser ) throws LDAPException {

        Filter uf = Filter.createANDFilter(
            userStyle.createUserFilter(),
            Filter.createEqualityFilter(userMapper.getAttributeName(LDAPUserAttrs.NAME), testUser));

        if ( !StringUtils.isBlank(us.getCustomFilter()) ) {
            try {
                uf = Filter.createANDFilter(uf, Filter.create(us.getCustomFilter()));
            }
            catch ( LDAPException e ) {
                r.error("LDAP_FILTER_INVALID", us.getCustomFilter()); //$NON-NLS-1$
                return null;
            }
        }

        SearchResult userResult = conn.search(us.getBaseDN(), mapScope(us.getScope()), uf, MINIMAL_ATTRS);

        if ( userResult.getEntryCount() == 0 ) {
            r.error("LDAP_BIND_LOOKUP_NORES", testUser); //$NON-NLS-1$
            return null;
        }
        else if ( userResult.getEntryCount() > 1 ) {
            r.error("LDAP_BIND_LOOKUP_MULTIRES", testUser); //$NON-NLS-1$
            return null;
        }
        else {
            SearchResultEntry ue = userResult.getSearchEntries().get(0);
            r.info("LDAP_BIND_LOOKUP_DN", testUser, ue.getDN()); //$NON-NLS-1$
            return ue.getParsedDN();
        }

    }


    /**
     * @param r
     * @param conn
     * @param us
     * @param groupStyle
     * @return
     * @throws LDAPException
     */
    private static SearchResultEntry testGroups ( ConfigTestResult r, ConfigTestAsyncHandler h, LDAPClient conn, LDAPObjectConfig gs,
            LDAPSchemaStyle groupStyle ) throws LDAPException {
        Filter gf = groupStyle.createGroupFilter();

        if ( !StringUtils.isBlank(gs.getCustomFilter()) ) {
            try {
                gf = Filter.createANDFilter(gf, Filter.create(gs.getCustomFilter()));
            }
            catch ( LDAPException e ) {
                r.error("LDAP_FILTER_INVALID", gs.getCustomFilter()); //$NON-NLS-1$
                return null;
            }
        }

        try {
            r.info("LDAP_GROUP_LOOKUP"); //$NON-NLS-1$
            h.update(r);
            SearchResult groupResult = conn.search(gs.getBaseDN(), mapScope(gs.getScope()), gf, MINIMAL_ATTRS);
            int ngroups = groupResult.getEntryCount();

            if ( log.isDebugEnabled() ) {
                log.debug("Found groups " + ngroups); //$NON-NLS-1$
            }

            SearchResultEntry firstGroupResult = null;
            if ( ngroups == 0 ) {
                r.warn("LDAP_NO_GROUP_ENTRIES"); //$NON-NLS-1$
            }
            else {
                firstGroupResult = groupResult.getSearchEntries().get(0);

                if ( log.isDebugEnabled() ) {
                    log.debug("First group result " + firstGroupResult.getDN()); //$NON-NLS-1$
                }

                r.info("LDAP_FOUND_GROUPS", String.valueOf(ngroups), firstGroupResult.getDN()); //$NON-NLS-1$
            }

            return firstGroupResult;
        }
        catch ( LDAPException e ) {
            log.debug("Group search failed", e); //$NON-NLS-1$
            if ( e.getResultCode() == ResultCode.NO_SUCH_OBJECT ) {
                r.error("FAIL_LDAP_INVALID_GROUP_BASE", gs.getBaseDN(), e.getMessage()); //$NON-NLS-1$
            }
            else {
                r.error("FAIL_LDAP_GROUP_LOOKUP", e.getMessage()); //$NON-NLS-1$
            }
            throw e;
        }
    }


    /**
     * @param r
     * @param h
     * @param conn
     * @param us
     * @param userStyle
     * @return
     * @throws LDAPException
     * @throws LDAPSearchException
     */
    private static SearchResultEntry testUsers ( ConfigTestResult r, ConfigTestAsyncHandler h, LDAPClient conn, LDAPObjectConfig us,
            LDAPSchemaStyle userStyle ) throws LDAPException {
        Filter uf = userStyle.createUserFilter();

        if ( !StringUtils.isBlank(us.getCustomFilter()) ) {
            try {
                uf = Filter.createANDFilter(uf, Filter.create(us.getCustomFilter()));
            }
            catch ( LDAPException e ) {
                r.error("LDAP_FILTER_INVALID", us.getCustomFilter()); //$NON-NLS-1$
                return null;
            }
        }

        try {
            r.info("LDAP_USER_LOOKUP"); //$NON-NLS-1$
            h.update(r);
            SearchResult userResult = conn.search(us.getBaseDN(), mapScope(us.getScope()), uf, MINIMAL_ATTRS);
            int nusers = userResult.getEntryCount();

            if ( log.isDebugEnabled() ) {
                log.debug("Found users " + nusers); //$NON-NLS-1$
            }

            SearchResultEntry firstUserResult = null;
            if ( nusers == 0 ) {
                r.warn("LDAP_NO_USER_ENTRIES"); //$NON-NLS-1$
            }
            else {
                firstUserResult = userResult.getSearchEntries().get(0);

                if ( log.isDebugEnabled() ) {
                    log.debug("First user result " + firstUserResult.getDN()); //$NON-NLS-1$
                }

                r.info("LDAP_FOUND_USERS", String.valueOf(nusers), firstUserResult.getDN()); //$NON-NLS-1$
            }

            return firstUserResult;
        }
        catch ( LDAPException e ) {
            log.debug("User search failed", e); //$NON-NLS-1$
            if ( e.getResultCode() == ResultCode.NO_SUCH_OBJECT ) {
                r.error("FAIL_LDAP_INVALID_USER_BASE", us.getBaseDN(), e.getMessage()); //$NON-NLS-1$
            }
            else {
                r.error("FAIL_LDAP_USER_LOOKUP", e.getMessage()); //$NON-NLS-1$
            }
            throw e;
        }
    }


    /**
     * @param scope
     * @return
     */
    private static SearchScope mapScope ( LDAPSearchScope scope ) {
        switch ( scope ) {
        case ONE:
            return SearchScope.ONE;
        case SUB:
        default:
            return SearchScope.SUB;
        }

    }

}
