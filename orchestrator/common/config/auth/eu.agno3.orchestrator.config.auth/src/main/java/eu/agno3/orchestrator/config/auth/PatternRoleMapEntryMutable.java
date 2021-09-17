/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth;


import java.util.Set;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( PatternRoleMapEntry.class )
public interface PatternRoleMapEntryMutable extends PatternRoleMapEntry {

    /**
     * 
     * @param addRoles
     */
    void setAddRoles ( Set<String> addRoles );


    /**
     * 
     * @param pattern
     */
    void setPattern ( String pattern );

}
