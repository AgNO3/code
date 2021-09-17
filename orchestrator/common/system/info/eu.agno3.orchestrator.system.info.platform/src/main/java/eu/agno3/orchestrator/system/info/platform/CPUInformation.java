/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.platform;


import java.io.Serializable;
import java.util.List;


/**
 * @author mbechler
 * 
 */
public interface CPUInformation extends Serializable {

    /**
     * 
     * @return total number of physical CPUs
     */
    int getTotalCPUCount ();


    /**
     * 
     * @return total number of cpu cores
     */
    int getTotalCoreCount ();


    /**
     * 
     * @return information about the installed cpu cores
     */
    List<CPUCore> getCpuCores ();


    /**
     * 
     * @return load average over the last minute, normalized to CPU core count
     */
    float getLoad1 ();


    /**
     * 
     * @return load average over the last 5 minutes, normalized to CPU core count
     */
    float getLoad5 ();


    /**
     * 
     * @return load average over the last 15 minutes, normalized to CPU core count
     */
    float getLoad15 ();
}
