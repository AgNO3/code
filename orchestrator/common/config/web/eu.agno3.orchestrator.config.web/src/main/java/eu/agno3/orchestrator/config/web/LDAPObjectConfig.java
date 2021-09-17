/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.util.Set;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.types.validation.ValidLDAPFilter;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:web:ldap:object" )
public interface LDAPObjectConfig extends ConfigurationObject {

    /**
     * 
     * @return attribute style identifier
     */
    String getAttributeStyle ();


    /**
     * 
     * @return custom filter and'ed to the context specific filter
     */
    @ValidLDAPFilter
    String getCustomFilter ();


    /**
     * 
     * @return search scope
     */
    LDAPSearchScope getScope ();


    /**
     * 
     * @return the search base DN, if the connection base is not included will be relative to the connection base
     */
    String getBaseDN ();


    /**
     * 
     * @return custom attribute mappings
     */
    Set<LDAPObjectAttributeMapping> getCustomAttributeMappings ();
}
