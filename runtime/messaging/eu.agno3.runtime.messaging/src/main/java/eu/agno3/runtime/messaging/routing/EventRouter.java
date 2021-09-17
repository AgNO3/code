/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.routing;


import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import eu.agno3.runtime.messaging.addressing.EventScope;


/**
 * @author mbechler
 * 
 */
public interface EventRouter {

    /**
     * @param s
     * @param scope
     * @param m
     * @throws MessageRoutingException
     */
    void routeMessage ( Session s, EventScope scope, Message m ) throws MessageRoutingException;


    /**
     * 
     * @throws JMSException
     */
    void close () throws JMSException;
}
