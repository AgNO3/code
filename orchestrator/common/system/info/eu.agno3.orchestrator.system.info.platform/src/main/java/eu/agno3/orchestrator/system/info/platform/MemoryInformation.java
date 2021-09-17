/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.platform;


import java.io.Serializable;


/**
 * @author mbechler
 * 
 */
public interface MemoryInformation extends Serializable {

    /**
     * 
     * @return the amount of physical memory available in bytes
     */
    long getTotalPhysicalMemory ();


    /**
     * 
     * @return the amount of unused memory
     */
    long getCurrentPhysicalMemoryFree ();


    /**
     * 
     * @return the currently used amount of memory in bytes (including buffers, cache) == Total - Free
     */
    long getCurrentPhysicalMemoryUsedTotal ();


    /**
     * 
     * @return the amount of memory used for buffers in bytes
     */
    long getCurrentPhysicalMemoryUsedBuffers ();


    /**
     * 
     * @return the amount of memory used as cache in bytes
     */
    long getCurrentPhysicalMemoryUsedCache ();


    /**
     * 
     * @return the amount of Swapspace available in bytes
     */
    long getTotalSwapMemory ();


    /**
     * 
     * @return currently used amount of swapspace in bytes
     */
    long getCurrentSwapMemoryUsed ();


    /**
     * 
     * @return currently unused amount of swapspace in bytes
     */
    long getCurrentSwapMemoryFree ();
}
