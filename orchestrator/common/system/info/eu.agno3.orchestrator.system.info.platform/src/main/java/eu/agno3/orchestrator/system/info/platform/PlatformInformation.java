/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.platform;


import java.io.Serializable;
import java.util.UUID;

import eu.agno3.orchestrator.system.info.SystemInformation;


/**
 * @author mbechler
 * 
 */
public interface PlatformInformation extends Serializable, SystemInformation {

    /**
     * 
     * @return the cpu information object
     */
    CPUInformation getCpuInformation ();


    /**
     * 
     * @return the memory information object
     */
    MemoryInformation getMemoryInformation ();


    /**
     * @return a guess at the platform type
     */
    PlatformType getPlatformType ();


    /**
     * @return the agent id
     */
    UUID getAgentId ();
}
