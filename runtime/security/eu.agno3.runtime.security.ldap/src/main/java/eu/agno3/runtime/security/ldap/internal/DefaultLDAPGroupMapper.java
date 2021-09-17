/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2015 by mbechler
 */
package eu.agno3.runtime.security.ldap.internal;


import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.unboundid.ldap.sdk.SearchResultEntry;

import eu.agno3.runtime.security.ldap.LDAPGroup;
import eu.agno3.runtime.security.ldap.LDAPGroupAttrs;
import eu.agno3.runtime.security.ldap.LDAPSchemaStyle;
import eu.agno3.runtime.security.ldap.LDAPSynchronizationRuntimeException;


/**
 * @author mbechler
 *
 */
public class DefaultLDAPGroupMapper extends AbstractObjectMapper<LDAPGroup, LDAPGroupAttrs> {

    private static final Map<LDAPGroupAttrs, String> DEFAULT_ATTRS = new HashMap<>();

    private static final Map<LDAPGroupAttrs, String> LDAP_STYLE = new HashMap<>();
    private static final Map<LDAPGroupAttrs, String> LDAP_UNIQUE_STYLE = new HashMap<>();
    private static final Map<LDAPGroupAttrs, String> POSIX_STYLE = new HashMap<>();
    private static final Map<LDAPGroupAttrs, String> AD_STYLE = new HashMap<>();


    static {
        // default attrs
        DEFAULT_ATTRS.put(LDAPGroupAttrs.NAME, "cn"); //$NON-NLS-1$
        DEFAULT_ATTRS.put(LDAPGroupAttrs.DISPLAY_NAME, "displayName"); //$NON-NLS-1$
        DEFAULT_ATTRS.put(LDAPGroupAttrs.ROLE, "roles"); //$NON-NLS-1$

        // LDAP style using groupOfUniqueNames
        LDAP_STYLE.putAll(DEFAULT_ATTRS);
        LDAP_STYLE.put(LDAPGroupAttrs.MEMBER, "member"); //$NON-NLS-1$
        LDAP_STYLE.put(LDAPGroupAttrs.MEMBER_OF, "memberOf"); //$NON-NLS-1$

        // LDAP style using groupOfNames
        LDAP_UNIQUE_STYLE.putAll(DEFAULT_ATTRS);
        LDAP_UNIQUE_STYLE.put(LDAPGroupAttrs.MEMBER, "uniqueMember"); //$NON-NLS-1$
        LDAP_UNIQUE_STYLE.put(LDAPGroupAttrs.MEMBER_OF, "memberOf"); //$NON-NLS-1$

        // POSIX style using posixGroup
        POSIX_STYLE.putAll(DEFAULT_ATTRS);
        POSIX_STYLE.put(LDAPGroupAttrs.MEMBER, "memberUid"); //$NON-NLS-1$

        // AD Style
        AD_STYLE.putAll(DEFAULT_ATTRS);
        AD_STYLE.put(LDAPGroupAttrs.NAME, "sAMAccountName"); //$NON-NLS-1$
        AD_STYLE.put(LDAPGroupAttrs.DISPLAY_NAME, "name"); //$NON-NLS-1$
        AD_STYLE.put(LDAPGroupAttrs.MEMBER, "member"); //$NON-NLS-1$
        AD_STYLE.put(LDAPGroupAttrs.MEMBER_OF, "memberOf"); //$NON-NLS-1$
    }


    /**
     * @param attrs
     * 
     */
    public DefaultLDAPGroupMapper ( Map<LDAPGroupAttrs, String> attrs ) {
        super(attrs);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPObjectMapper#mapObject(com.unboundid.ldap.sdk.SearchResultEntry)
     */
    @Override
    public LDAPGroup mapObject ( SearchResultEntry entry ) {
        LDAPGroupImpl group = new LDAPGroupImpl();
        String name = entry.getAttributeValue(this.getAttributeName(LDAPGroupAttrs.NAME));

        if ( StringUtils.isBlank(name) ) {
            throw new LDAPSynchronizationRuntimeException("Group name is empty"); //$NON-NLS-1$
        }
        group.setName(name);
        if ( entry.hasAttribute(this.getAttributeName(LDAPGroupAttrs.DISPLAY_NAME)) ) {
            group.setDisplayName(entry.getAttributeValue(this.getAttributeName(LDAPGroupAttrs.DISPLAY_NAME)));
        }
        else {
            group.setDisplayName(group.getName());
        }
        return group;
    }


    protected static DefaultLDAPGroupMapper parseConfig ( Dictionary<String, Object> properties, String prefix, LDAPSchemaStyle style ) {
        DefaultLDAPGroupMapper cfg = new DefaultLDAPGroupMapper(getForStyle(style));
        AbstractObjectMapper.parseConfig(cfg, properties, LDAPGroupAttrs.values(), prefix);
        return cfg;
    }


    /**
     * @param style
     * @param attributeOverrides
     * @return user mapper
     */
    public static DefaultLDAPGroupMapper create ( LDAPSchemaStyle style, Map<String, String> attributeOverrides ) {
        DefaultLDAPGroupMapper cfg = new DefaultLDAPGroupMapper(getForStyle(style));
        AbstractObjectMapper.setOverrides(cfg, attributeOverrides, LDAPGroupAttrs.values());
        return cfg;
    }


    /**
     * @param style
     * @return
     */
    private static Map<LDAPGroupAttrs, String> getForStyle ( LDAPSchemaStyle style ) {
        switch ( style ) {
        case LDAP:
            return LDAP_STYLE;
        case LDAP_UNIQUE:
            return LDAP_UNIQUE_STYLE;
        case POSIX:
            return POSIX_STYLE;
        case AD:
            return AD_STYLE;
        default:
            return LDAP_STYLE;
        }
    }

}
