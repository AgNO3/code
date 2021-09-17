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
public interface ConfigContribution {

    /**
     * @return the priority of this configuration source
     */
    int getPriority ();


    /**
     * Initially load the configuration
     */
    void load ();


    /**
     * 
     * @param hint
     */
    void reload ( String hint );


    /**
     * @return the regular properties provided by this source
     */
    Map<String, Map<String, Object>> getRegularProperties ();


    /**
     * @return the factory properties provided by this source
     */
    Map<String, Map<String, FactoryContribution>> getFactoryContributions ();

}
