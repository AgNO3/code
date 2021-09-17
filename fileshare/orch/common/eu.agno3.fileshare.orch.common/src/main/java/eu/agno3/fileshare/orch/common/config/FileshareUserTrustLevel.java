/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.Locale;
import java.util.Map;
import java.util.Set;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:fileshare:user:trustLevel" )
public interface FileshareUserTrustLevel extends ConfigurationObject {

    /**
     * 
     * @return localized message to display
     */
    Map<Locale, String> getMessages ();


    /**
     * 
     * @return set of roles to match
     */
    Set<String> getMatchRoles ();


    /**
     * 
     * @return color (hex html color code)
     */
    String getColor ();


    /**
     * 
     * @return display title
     */
    String getTitle ();


    /**
     * 
     * @return identifier
     */
    String getTrustLevelId ();

}
