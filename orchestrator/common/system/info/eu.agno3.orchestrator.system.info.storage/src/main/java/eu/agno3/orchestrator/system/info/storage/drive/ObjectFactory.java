/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.drive;


/**
 * @author mbechler
 * 
 */
public class ObjectFactory {

    /**
     * 
     * @return the default physical drive information impl
     */
    public PhysicalDrive createPhysicalDrive () {
        return new PhysicalDriveImpl();
    }


    /**
     * 
     * @return the default raid drive information impl
     */
    public RAIDDrive createRAIDDrive () {
        return new RAIDDriveImpl();
    }


    /**
     * 
     * @return the default raid drive information impl
     */
    public VolumeGroup createVolumeGroup () {
        return new VolumeGroupImpl();
    }
}
