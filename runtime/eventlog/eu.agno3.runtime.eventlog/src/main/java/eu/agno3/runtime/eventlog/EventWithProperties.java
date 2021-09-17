/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.05.2015 by mbechler
 */
package eu.agno3.runtime.eventlog;


import java.util.Map;


/**
 * @author mbechler
 *
 */
public interface EventWithProperties extends Event {

    /**
     * 
     * @return the event properties
     */
    Map<String, Object> getProperties ();
}
