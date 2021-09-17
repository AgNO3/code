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

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareUserTrustLevel.class )
public interface FileshareUserTrustLevelMutable extends FileshareUserTrustLevel {

    /**
     * 
     * @param messages
     */
    void setMessages ( Map<Locale, String> messages );


    /**
     * 
     * @param matchRoles
     */
    void setMatchRoles ( Set<String> matchRoles );


    /**
     * 
     * @param color
     */
    void setColor ( String color );


    /**
     * 
     * @param title
     */
    void setTitle ( String title );


    /**
     * 
     * @param trustLevelId
     */
    void setTrustLevelId ( String trustLevelId );
}
