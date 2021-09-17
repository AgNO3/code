/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.drive;


/**
 * @author mbechler
 * 
 */
public interface PhysicalDrive extends Drive {

    /**
     * @return vendor
     */
    String getVendor ();


    /**
     * 
     * @return model
     */
    String getModel ();


    /**
     * 
     * @return serial
     */
    String getSerial ();


    /**
     * 
     * @return the partition table type
     */
    PartitionTableType getPartitionTableType ();


    /**
     * @return the block device name
     */
    String getBlockDeviceName ();


    /**
     * 
     * @return the estimated amount of unpartioned space
     */
    Long getUncapturedSpace ();

}
