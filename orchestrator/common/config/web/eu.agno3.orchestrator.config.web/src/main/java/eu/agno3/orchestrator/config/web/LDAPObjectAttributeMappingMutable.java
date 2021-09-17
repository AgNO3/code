/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( LDAPObjectAttributeMapping.class )
public interface LDAPObjectAttributeMappingMutable extends LDAPObjectAttributeMapping {

    /**
     * 
     * @param attributeName
     */
    void setAttributeName ( String attributeName );


    /**
     * 
     * @param attributeId
     */
    void setAttributeId ( String attributeId );

}
