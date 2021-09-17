/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.storage;


import java.util.Set;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( StorageConfiguration.class )
public interface StorageConfigurationMutable extends StorageConfiguration {

    /**
     * 
     * @param entries
     */
    void setMountEntries ( Set<MountEntry> entries );


    /**
     * @param backupStorage
     */
    void setBackupStorage ( String backupStorage );
}
