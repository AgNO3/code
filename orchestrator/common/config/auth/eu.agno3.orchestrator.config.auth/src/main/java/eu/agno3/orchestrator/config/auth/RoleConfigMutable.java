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

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( RoleConfig.class )
public interface RoleConfigMutable extends RoleConfig {

    /**
     * 
     * @param descriptions
     */
    void setDescriptions ( Map<Locale, String> descriptions );


    /**
     * 
     * @param titles
     */
    void setTitles ( Map<Locale, String> titles );


    /**
     * 
     * @param permissions
     */
    void setPermissions ( Set<String> permissions );


    /**
     * 
     * @param roleId
     */
    void setRoleId ( String roleId );


    /**
     * @param hidden
     */
    void setHidden ( Boolean hidden );

}
