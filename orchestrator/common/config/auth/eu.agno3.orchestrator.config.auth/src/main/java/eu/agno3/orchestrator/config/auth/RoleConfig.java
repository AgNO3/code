/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth;


import java.util.Locale;
import java.util.Map;
import java.util.Set;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:auth:roles:role" )
public interface RoleConfig extends ConfigurationObject {

    /**
     * 
     * @return the role identifier
     */
    String getRoleId ();


    /**
     * 
     * @return the assigned permissions
     */
    Set<String> getPermissions ();


    /**
     * 
     * @return localized role descriptions
     */
    Map<Locale, String> getDescriptions ();


    /**
     * 
     * @return localized role titles
     */
    Map<Locale, String> getTitles ();


    /**
     * @return whether the role is shown for selection
     */
    Boolean getHidden ();

}
