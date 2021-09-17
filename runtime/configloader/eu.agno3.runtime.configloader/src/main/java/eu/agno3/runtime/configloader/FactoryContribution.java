/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.09.2014 by mbechler
 */
package eu.agno3.runtime.configloader;


import java.util.Map;


/**
 * @author mbechler
 * 
 */
public interface FactoryContribution {

    /**
     * @return the configuration source
     */
    ConfigContribution getSource ();


    /**
     * @return the instance id
     */
    String getInstanceId ();


    /**
     * @return the factory pid
     */
    String getFactoryPid ();


    /**
     * @return the config properties
     */
    Map<String, Object> getProperties ();

}