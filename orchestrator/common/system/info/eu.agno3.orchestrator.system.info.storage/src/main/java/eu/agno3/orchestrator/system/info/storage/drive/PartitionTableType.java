/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.01.2016 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.drive;


/**
 * @author mbechler
 *
 */
public enum PartitionTableType {

    /**
     * Unknown partition table
     */
    UNKNOWN,

    /**
     * No partition table
     */
    NONE,

    /**
     * DOS partition table
     */
    DOS,

    /**
     * GPT partition table
     */
    GPT
}
