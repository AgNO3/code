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
public interface LogicalVolume extends Volume {

    /**
     * 
     * @return the logical volume name
     */
    String getName ();
}
