/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth;


import java.util.Set;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:auth:roleMap:pattern" )
public interface PatternRoleMapEntry extends ConfigurationObject {

    /**
     * 
     * @return pattern to match, context dependant
     */
    String getPattern ();


    /**
     * 
     * @return roles to add
     */
    Set<String> getAddRoles ();

}
