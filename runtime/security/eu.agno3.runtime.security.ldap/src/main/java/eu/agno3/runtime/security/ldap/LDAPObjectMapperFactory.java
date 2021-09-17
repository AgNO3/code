/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jan 14, 2017 by mbechler
 */
package eu.agno3.runtime.security.ldap;


import java.util.Map;


/**
 * @author mbechler
 *
 */
public interface LDAPObjectMapperFactory {

    /**
     * @param style
     * @param attributeOverrides
     * @return user attribute mapper
     */
    LDAPObjectMapper<LDAPUser, LDAPUserAttrs> createUserMapper ( LDAPSchemaStyle style, Map<String, String> attributeOverrides );


    /**
     * @param style
     * @param attributeOverrides
     * @return group attribute mapper
     */
    LDAPObjectMapper<LDAPGroup, LDAPGroupAttrs> createGroupMapper ( LDAPSchemaStyle style, Map<String, String> attributeOverrides );


    /**
     * @param style
     * @param attributeOverrides
     * @return operational attribute mapper
     */
    LDAPObjectMapper<LDAPOperational, LDAPOperationalAttrs> createOperationalMapper ( LDAPSchemaStyle style, Map<String, String> attributeOverrides );

}
