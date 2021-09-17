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
public interface LogAnonymizer {

    /**
     * @param stream
     * @param map
     *            implementations are allowed to modify this parameter
     * @return anonymized map
     */
    Map<?, ?> anonymize ( String stream, Map<Object, Object> map );

}
