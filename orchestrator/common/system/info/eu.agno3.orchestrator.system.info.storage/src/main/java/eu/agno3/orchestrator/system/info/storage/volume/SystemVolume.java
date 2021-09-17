/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.volume;


/**
 * @author mbechler
 * 
 */
public interface SystemVolume extends PhysicalVolume {

    /**
     * 
     * @return the purpose of this system volume
     */
    SystemVolumeType getSystemVolumeType ();
}
