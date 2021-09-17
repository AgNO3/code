/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 14, 2017 by mbechler
 */
package eu.agno3.runtime.security.ldap.internal;


import java.util.Map;

import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.security.ldap.LDAPGroup;
import eu.agno3.runtime.security.ldap.LDAPGroupAttrs;
import eu.agno3.runtime.security.ldap.LDAPObjectMapper;
import eu.agno3.runtime.security.ldap.LDAPObjectMapperFactory;
import eu.agno3.runtime.security.ldap.LDAPOperational;
import eu.agno3.runtime.security.ldap.LDAPOperationalAttrs;
import eu.agno3.runtime.security.ldap.LDAPSchemaStyle;
import eu.agno3.runtime.security.ldap.LDAPUser;
import eu.agno3.runtime.security.ldap.LDAPUserAttrs;


/**
 * @author mbechler
 *
 */
@Component ( service = LDAPObjectMapperFactory.class )
public class LDAPObjectMapperFactoryImpl implements LDAPObjectMapperFactory {

    @Override
    public LDAPObjectMapper<LDAPUser, LDAPUserAttrs> createUserMapper ( LDAPSchemaStyle style, Map<String, String> attributeOverrides ) {
        return DefaultLDAPUserMapper.create(style, attributeOverrides);
    }


    @Override
    public LDAPObjectMapper<LDAPGroup, LDAPGroupAttrs> createGroupMapper ( LDAPSchemaStyle style, Map<String, String> attributeOverrides ) {
        return DefaultLDAPGroupMapper.create(style, attributeOverrides);
    }


    @Override
    public LDAPObjectMapper<LDAPOperational, LDAPOperationalAttrs> createOperationalMapper ( LDAPSchemaStyle style,
            Map<String, String> attributeOverrides ) {
        return DefaultLDAPOperationalMapper.create(style, attributeOverrides);
    }
}
