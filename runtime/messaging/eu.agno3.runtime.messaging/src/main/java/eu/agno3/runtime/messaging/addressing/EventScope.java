/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.addressing;


/**
 * @author mbechler
 * 
 */
public interface EventScope {

    /**
     * @return the parent scope
     */
    EventScope getParent ();


    /**
     * @return the server side event topic for this scope
     */
    String getEventTopic ();
}
