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
public class ObjectFactory {

    /**
     * 
     * @return the default lv information impl
     */
    public LogicalVolume createLogicalVolume () {
        return new LVMLogicalVolumeImpl();
    }


    /**
     * 
     * @return the default physical volume information impl
     */
    public PhysicalVolume createPhysicalVolume () {
        return new PhysicalVolumeImpl();
    }


    /**
     * 
     * @return the default system volume impl
     */
    public SystemVolume createSystemVolume () {
        return new SystemVolumeImpl();
    }
}
