/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth.ldap;


import java.util.Set;

import javax.validation.Valid;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.web.LDAPObjectAttributeMapping;
import eu.agno3.orchestrator.config.web.LDAPObjectConfig;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:auth:authenticator:ldap:schema" )
public interface LDAPAuthSchemaConfig extends ConfigurationObject {

    /**
     * @return whether the group membership attributes are DN references
     */
    Boolean getReferencesAreDNs ();


    /**
     * @return whether nested groups are recursivly resolved
     */
    Boolean getRecursiveResolveGroups ();


    /**
     * @return whether the schema uses forward style (storing memberOf inside the user/group object)
     */
    Boolean getUseForwardGroups ();


    /**
     * @return overrides for operational attributes
     */
    @Valid
    Set<LDAPObjectAttributeMapping> getOperationalAttributeMappings ();


    /**
     * 
     * @return group schema configuration
     */
    @ReferencedObject
    @Valid
    LDAPObjectConfig getGroupSchema ();


    /**
     * 
     * @return user schema configuration
     */
    @ReferencedObject
    @Valid
    LDAPObjectConfig getUserSchema ();

}
