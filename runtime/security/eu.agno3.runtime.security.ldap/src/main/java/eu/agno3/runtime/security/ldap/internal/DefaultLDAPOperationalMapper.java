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

import com.unboundid.ldap.sdk.SearchResultEntry;

import eu.agno3.runtime.security.ldap.LDAPOperational;
import eu.agno3.runtime.security.ldap.LDAPOperationalAttrs;
import eu.agno3.runtime.security.ldap.LDAPSchemaStyle;


/**
 * @author mbechler
 *
 */
public class DefaultLDAPOperationalMapper extends AbstractObjectMapper<LDAPOperational, LDAPOperationalAttrs> {

    private static final Map<LDAPOperationalAttrs, String> DEFAULT_ATTRS = new HashMap<>();

    private static final Map<LDAPOperationalAttrs, String> AD_ATTRS = new HashMap<>();


    static {
        DEFAULT_ATTRS.put(LDAPOperationalAttrs.UUID, "entryUUID"); //$NON-NLS-1$
        DEFAULT_ATTRS.put(LDAPOperationalAttrs.CREATE_TIMESTAMP, "creationTimestamp"); //$NON-NLS-1$
        DEFAULT_ATTRS.put(LDAPOperationalAttrs.MODIFY_TIMESTAMP, "modifyTimestamp"); //$NON-NLS-1$

        AD_ATTRS.put(LDAPOperationalAttrs.UUID, "objectGUID"); //$NON-NLS-1$
        AD_ATTRS.put(LDAPOperationalAttrs.CREATE_TIMESTAMP, "creationTimestamp"); //$NON-NLS-1$
        AD_ATTRS.put(LDAPOperationalAttrs.MODIFY_TIMESTAMP, "modifyTimestamp"); //$NON-NLS-1$
    }


    /**
     * @param attrs
     * 
     */
    public DefaultLDAPOperationalMapper ( Map<LDAPOperationalAttrs, String> attrs ) {
        super(attrs);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPObjectMapper#mapObject(com.unboundid.ldap.sdk.SearchResultEntry)
     */
    @Override
    public LDAPOperational mapObject ( SearchResultEntry entry ) {
        return null;
    }


    /**
     * @param properties
     * @param prefix
     * @param style
     * @return cfg
     */
    public static DefaultLDAPOperationalMapper parseConfig ( Dictionary<String, Object> properties, String prefix, LDAPSchemaStyle style ) {
        DefaultLDAPOperationalMapper cfg = new DefaultLDAPOperationalMapper(getAttrsFor(style));
        AbstractObjectMapper.parseConfig(cfg, properties, LDAPOperationalAttrs.values(), prefix);
        return cfg;
    }


    /**
     * @param style
     * @param attributeOverrides
     * @return user mapper
     */
    public static DefaultLDAPOperationalMapper create ( LDAPSchemaStyle style, Map<String, String> attributeOverrides ) {
        DefaultLDAPOperationalMapper cfg = new DefaultLDAPOperationalMapper(getAttrsFor(style));
        AbstractObjectMapper.setOverrides(cfg, attributeOverrides, LDAPOperationalAttrs.values());
        return cfg;
    }


    /**
     * @param style
     * @return
     */
    private static Map<LDAPOperationalAttrs, String> getAttrsFor ( LDAPSchemaStyle style ) {

        if ( style == LDAPSchemaStyle.AD ) {
            return AD_ATTRS;
        }

        return DEFAULT_ATTRS;
    }

}
