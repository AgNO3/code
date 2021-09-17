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
public interface Anonymizer {

    /**
     * @param val
     * @param opts
     * @return the anonymizer field value
     */
    String anonymize ( String val, Map<String, String> opts );


    /**
     * @return an idenfitifer for this type of anonymization
     */
    String getId ();

}
