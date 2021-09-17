/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.platform;


import java.io.Serializable;
import java.util.Set;


/**
 * @author mbechler
 * 
 */
public interface CPUCore extends Serializable {

    /**
     * @return the cpu number
     */
    int getPhysicalIndex ();


    /**
     * 
     * @return the cpu core number
     */
    int getCoreIndex ();


    /**
     * 
     * @return the cpu model
     */
    String getModel ();


    /**
     * 
     * @return the maximum clock frequency
     */
    int getMaximumFrequency ();


    /**
     * 
     * @return cache size in bytes
     */
    int getCacheSize ();


    /**
     * 
     * @return the supported cpu features
     */
    Set<CPUFeature> getFeatures ();

}
