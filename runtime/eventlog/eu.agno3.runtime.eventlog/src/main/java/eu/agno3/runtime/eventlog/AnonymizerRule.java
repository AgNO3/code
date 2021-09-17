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
public interface AnonymizerRule {

    /**
     * @return the type of anonymizer to use
     */
    String getType ();


    /**
     * @return the field to apply this anonymizer to
     */
    String getField ();


    /**
     * @return the options to pass to the anonymizer
     */
    Map<String, String> getOpts ();

}
