/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.Set;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareStorageConfig.class )
public interface FileshareStorageConfigMutable extends FileshareStorageConfig {

    /**
     * 
     * @param localStorage
     */
    void setLocalStorage ( String localStorage );


    /**
     * 
     * @param fileStorage
     */
    void setFileStorage ( String fileStorage );


    /**
     * @param passthroughGroups
     */
    void setPassthroughGroups ( Set<FilesharePassthroughGroup> passthroughGroups );
}
