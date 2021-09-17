/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


import java.util.Set;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( RealmsConfig.class )
public interface RealmsConfigMutable extends RealmsConfig {

    /**
     * @param realms
     */
    void setRealms ( Set<RealmConfig> realms );

}