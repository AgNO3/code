/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth.ldap;


import java.util.Set;

import eu.agno3.orchestrator.config.web.LDAPObjectAttributeMapping;
import eu.agno3.orchestrator.config.web.LDAPObjectConfigMutable;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( LDAPAuthSchemaConfig.class )
public interface LDAPAuthSchemaConfigMutable extends LDAPAuthSchemaConfig {

    /**
     * @param customAttributeMappings
     */
    void setOperationalAttributeMappings ( Set<LDAPObjectAttributeMapping> customAttributeMappings );


    /**
     * 
     * @param groupSchema
     */
    void setGroupSchema ( LDAPObjectConfigMutable groupSchema );


    /**
     * 
     * @param userSchema
     */
    void setUserSchema ( LDAPObjectConfigMutable userSchema );


    /**
     * 
     * @param useFowardGroups
     */
    void setUseForwardGroups ( Boolean useFowardGroups );


    /**
     * 
     * @param referencesAreDNs
     */
    void setReferencesAreDNs ( Boolean referencesAreDNs );


    /**
     * 
     * @param recursiveResolveGroups
     */
    void setRecursiveResolveGroups ( Boolean recursiveResolveGroups );

}
