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

import eu.agno3.runtime.security.ldap.LDAPSchemaStyle;
import eu.agno3.runtime.security.ldap.LDAPUser;
import eu.agno3.runtime.security.ldap.LDAPUserAttrs;


/**
 * @author mbechler
 *
 */
public class DefaultLDAPUserMapper extends AbstractObjectMapper<LDAPUser, LDAPUserAttrs> {

    private static final Map<LDAPUserAttrs, String> DEFAULT_ATTRS = new HashMap<>();
    private static final Map<LDAPUserAttrs, String> AD_ATTRS;


    static {
        DEFAULT_ATTRS.put(LDAPUserAttrs.NAME, "uid"); //$NON-NLS-1$
        DEFAULT_ATTRS.put(LDAPUserAttrs.COMMON_NAME, "cn"); //$NON-NLS-1$
        DEFAULT_ATTRS.put(LDAPUserAttrs.FIRST_NAME, "givenName"); //$NON-NLS-1$
        DEFAULT_ATTRS.put(LDAPUserAttrs.INITIALS, "initials"); //$NON-NLS-1$
        DEFAULT_ATTRS.put(LDAPUserAttrs.LAST_NAME, "sn"); //$NON-NLS-1$
        DEFAULT_ATTRS.put(LDAPUserAttrs.DISPLAY_NAME, "displayName"); //$NON-NLS-1$
        DEFAULT_ATTRS.put(LDAPUserAttrs.MAIL, "mail"); //$NON-NLS-1$
        DEFAULT_ATTRS.put(LDAPUserAttrs.ORGANIZATION, "o"); //$NON-NLS-1$
        DEFAULT_ATTRS.put(LDAPUserAttrs.ORGANIZATION_UNIT, "ou"); //$NON-NLS-1$
        DEFAULT_ATTRS.put(LDAPUserAttrs.TITLE, "title"); //$NON-NLS-1$
        DEFAULT_ATTRS.put(LDAPUserAttrs.MEMBER_OF, "memberOf"); //$NON-NLS-1$
        DEFAULT_ATTRS.put(LDAPUserAttrs.ROLE, "roles"); //$NON-NLS-1$
        DEFAULT_ATTRS.put(LDAPUserAttrs.PREFERRED_LANGUAGE, "preferredLanguage"); //$NON-NLS-1$
        DEFAULT_ATTRS.put(LDAPUserAttrs.TIMEZONE, "timezone"); //$NON-NLS-1$
        DEFAULT_ATTRS.put(LDAPUserAttrs.LAST_PW_CHANGE, "pwdChangedTime"); //$NON-NLS-1$

        AD_ATTRS = new HashMap<>(DEFAULT_ATTRS);
        AD_ATTRS.put(LDAPUserAttrs.LAST_PW_CHANGE, "pwdLastSet"); //$NON-NLS-1$
        AD_ATTRS.put(LDAPUserAttrs.NAME, "sAMAccountName"); //$NON-NLS-1$
    }


    /**
     * @param attrs
     * 
     */
    public DefaultLDAPUserMapper ( Map<LDAPUserAttrs, String> attrs ) {
        super(attrs);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPObjectMapper#mapObject(com.unboundid.ldap.sdk.SearchResultEntry)
     */
    @Override
    public LDAPUser mapObject ( SearchResultEntry entry ) {
        LDAPUserImpl user = new LDAPUserImpl();
        user.setUsername(entry.getAttributeValue(getAttributeName(LDAPUserAttrs.NAME)));
        setDisplayName(entry, user);
        user.setMailAddress(entry.getAttributeValue(getAttributeName(LDAPUserAttrs.MAIL)));
        user.setOrganization(entry.getAttributeValue(getAttributeName(LDAPUserAttrs.ORGANIZATION)));
        user.setOrganizationUnit(entry.getAttributeValue(getAttributeName(LDAPUserAttrs.ORGANIZATION_UNIT)));
        user.setJobTitle(entry.getAttributeValue(getAttributeName(LDAPUserAttrs.TITLE)));
        user.setPreferredLanguage(entry.getAttributeValue(getAttributeName(LDAPUserAttrs.PREFERRED_LANGUAGE)));
        user.setTimezone(entry.getAttributeValue(getAttributeName(LDAPUserAttrs.TIMEZONE)));
        return user;
    }


    /**
     * @param properties
     * @param prefix
     * @param style
     * @return cfg
     */
    public static DefaultLDAPUserMapper parseConfig ( Dictionary<String, Object> properties, String prefix, LDAPSchemaStyle style ) {
        DefaultLDAPUserMapper cfg = new DefaultLDAPUserMapper(getAttrsForStyle(style));
        AbstractObjectMapper.parseConfig(cfg, properties, LDAPUserAttrs.values(), prefix);
        return cfg;
    }


    /**
     * @param style
     * @param attributeOverrides
     * @return user mapper
     */
    public static DefaultLDAPUserMapper create ( LDAPSchemaStyle style, Map<String, String> attributeOverrides ) {
        DefaultLDAPUserMapper cfg = new DefaultLDAPUserMapper(getAttrsForStyle(style));
        AbstractObjectMapper.setOverrides(cfg, attributeOverrides, LDAPUserAttrs.values());
        return cfg;
    }


    /**
     * @param style
     * @return
     */
    private static Map<LDAPUserAttrs, String> getAttrsForStyle ( LDAPSchemaStyle style ) {
        if ( style == LDAPSchemaStyle.AD ) {
            return AD_ATTRS;
        }
        return DEFAULT_ATTRS;
    }


    /**
     * @param entry
     * @param user
     */
    private void setDisplayName ( SearchResultEntry entry, LDAPUserImpl user ) {
        if ( entry.hasAttribute(this.getAttributeName(LDAPUserAttrs.DISPLAY_NAME)) ) {
            user.setDisplayName(entry.getAttributeValue(getAttributeName(LDAPUserAttrs.DISPLAY_NAME)));
        }
        else if ( entry.hasAttribute(this.getAttributeName(LDAPUserAttrs.COMMON_NAME)) ) {
            user.setDisplayName(entry.getAttributeValue(getAttributeName(LDAPUserAttrs.COMMON_NAME)));
        }
        else if ( entry.hasAttribute(this.getAttributeName(LDAPUserAttrs.FIRST_NAME))
                && entry.hasAttribute(this.getAttributeName(LDAPUserAttrs.LAST_NAME)) ) {
            String firstName = entry.getAttributeValue(this.getAttributeName(LDAPUserAttrs.FIRST_NAME));
            String initials = entry.getAttributeValue(this.getAttributeName(LDAPUserAttrs.INITIALS));
            String lastName = entry.getAttributeValue(this.getAttributeName(LDAPUserAttrs.LAST_NAME));
            user.setDisplayName(String.format(
                "%s%s %s", //$NON-NLS-1$
                firstName,
                initials != null ? " " + initials : StringUtils.EMPTY, //$NON-NLS-1$
                lastName));
        }
        else {
            user.setDisplayName(user.getUsername());
        }
    }

}
