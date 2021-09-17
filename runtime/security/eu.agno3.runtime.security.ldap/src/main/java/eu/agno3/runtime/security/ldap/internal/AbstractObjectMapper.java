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

import eu.agno3.runtime.security.ldap.LDAPObjectMapper;


/**
 * @author mbechler
 * @param <T>
 * @param <TAttrs>
 *
 */
public abstract class AbstractObjectMapper <T, TAttrs extends Enum<?>> implements LDAPObjectMapper<T, TAttrs> {

    private final Map<TAttrs, String> attrs;


    /**
     * 
     */
    protected AbstractObjectMapper () {
        super();
        this.attrs = new HashMap<>();
    }


    /**
     * @param attrs
     */
    protected AbstractObjectMapper ( Map<TAttrs, String> attrs ) {
        super();
        this.attrs = new HashMap<>(attrs);
    }


    protected void overrideAttr ( TAttrs attr, String attrOverride ) {
        this.attrs.put(attr, attrOverride);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.ldap.LDAPObjectMapper#getAttributeName(java.lang.Enum)
     */
    @Override
    public String getAttributeName ( TAttrs attr ) {
        return this.attrs.get(attr);
    }


    /**
     * @param mapper
     * @param properties
     * @param attrs
     * @param prefix
     */
    public static <TAttrs extends Enum<?>> void parseConfig ( AbstractObjectMapper<?, TAttrs> mapper, Dictionary<String, Object> properties,
            TAttrs[] attrs, String prefix ) {
        for ( TAttrs attr : attrs ) {
            String spec = (String) properties.get(prefix + attr.name());
            if ( !StringUtils.isBlank(spec) ) {
                mapper.overrideAttr(attr, spec.trim());
            }
        }

    }


    /**
     * 
     * @param mapper
     * @param attributeOverrides
     * @param attrs
     */
    public static <TAttrs extends Enum<?>> void setOverrides ( AbstractObjectMapper<?, TAttrs> mapper, Map<String, String> attributeOverrides,
            TAttrs[] attrs ) {
        for ( TAttrs attr : attrs ) {
            String override = attributeOverrides.get(attr.name());
            if ( !StringUtils.isBlank(override) ) {
                mapper.overrideAttr(attr, override);
            }
        }
    }
}