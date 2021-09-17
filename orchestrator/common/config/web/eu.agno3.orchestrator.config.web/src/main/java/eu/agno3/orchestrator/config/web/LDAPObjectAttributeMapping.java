/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:web:ldap:object:attr" )
public interface LDAPObjectAttributeMapping extends ConfigurationObject {

    /**
     * 
     * @return the attribute id, depending on context
     */
    String getAttributeId ();


    /**
     * 
     * @return the actual attribute name to use
     */
    String getAttributeName ();

}
