/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 9, 2016 by mbechler
 */
package eu.agno3.runtime.eventlog;


import java.util.Map;


/**
 * @author mbechler
 *
 */
public interface AnonymizerConfig {

    /**
     * @return stream to match
     */
    String getMatchStream ();


    /**
     * @return the rules
     */
    Map<String, AnonymizerRule> getRules ();

}
