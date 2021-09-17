/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.09.2013 by mbechler
 */
package eu.agno3.runtime.messaging.listener;


import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import eu.agno3.runtime.messaging.MessagingException;


/**
 * @author mbechler
 * 
 */
public interface MessageListenerFactory {

    /**
     * @param c
     * @param listener
     * @param dest
     * @return a MessageListener contained which runs the recieve/listener code inside a transaction
     */
    MessageListenerContainer createMessageListener ( MessageListener listener );


    /**
     * @param service
     * @throws JMSException
     */
    void remove ( MessageListenerContainer service ) throws JMSException;


    /**
     * @param messageListener
     * @throws MessagingException
     * @throws JMSException
     */
    void startListener ( MessageListenerContainer messageListener ) throws MessagingException, JMSException;


    /**
     * @param dest
     * @param listener
     * @return a destination
     * @throws JMSException
     */
    Destination createDestination ( DestinationStrategy dest, BaseListener listener ) throws JMSException;


    /**
     * @param messageListener
     * @param session
     * @throws MessagingException
     * @throws JMSException
     */
    void startListener ( MessageListenerContainer messageListener, Session session ) throws MessagingException, JMSException;

}
