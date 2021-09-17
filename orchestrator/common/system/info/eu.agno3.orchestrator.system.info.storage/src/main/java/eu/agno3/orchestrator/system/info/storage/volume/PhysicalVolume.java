/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.volume;


/**
 * @author mbechler
 * 
 */
public interface PhysicalVolume extends Volume, Comparable<PhysicalVolume> {

    /**
     * 
     * @return the index of this volume
     */
    int getIndex ();


    /**
     * 
     * @return the volume type
     */
    VolumeType getType ();

}
