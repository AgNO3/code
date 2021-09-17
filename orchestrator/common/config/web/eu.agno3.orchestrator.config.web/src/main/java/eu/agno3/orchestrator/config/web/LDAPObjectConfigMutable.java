/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.util.Set;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( LDAPObjectConfig.class )
public interface LDAPObjectConfigMutable extends LDAPObjectConfig {

    /**
     * 
     * @param customAttributeMappings
     */
    void setCustomAttributeMappings ( Set<LDAPObjectAttributeMapping> customAttributeMappings );


    /**
     * 
     * @param attributeStyle
     */
    void setAttributeStyle ( String attributeStyle );


    /**
     * 
     * @param customFilter
     */
    void setCustomFilter ( String customFilter );


    /**
     * 
     * @param scope
     */
    void setScope ( LDAPSearchScope scope );


    /**
     * 
     * @param baseDN
     */
    void setBaseDN ( String baseDN );

}
