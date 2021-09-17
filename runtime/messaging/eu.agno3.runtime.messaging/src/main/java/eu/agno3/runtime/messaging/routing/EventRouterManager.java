/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.routing;


import javax.jms.JMSException;

import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.EventScope;


/**
 * @author mbechler
 * 
 */
public interface EventRouterManager {

    /**
     * @param scope
     * @return a router for the given scope
     * @throws MessagingException
     */
    EventRouter getRouterFor ( EventScope scope ) throws MessagingException;


    /**
     * 
     * @throws JMSException
     */
    void close () throws JMSException;
}
