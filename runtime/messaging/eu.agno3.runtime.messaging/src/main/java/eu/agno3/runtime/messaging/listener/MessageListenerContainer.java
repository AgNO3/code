/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.09.2013 by mbechler
 */
package eu.agno3.runtime.messaging.listener;


import javax.jms.JMSException;
import javax.jms.Session;

import eu.agno3.runtime.messaging.MessagingException;


/**
 * @author mbechler
 * 
 */
public interface MessageListenerContainer {

    /**
     * Start listening for messages
     * 
     * @param s
     * 
     * @throws JMSException
     * @throws MessagingException
     */
    void start ( Session s ) throws JMSException, MessagingException;


    /**
     * @param s
     * @param exclusive
     *            whether session is exclusive and should be closed on stop
     * @throws MessagingException
     * @throws JMSException
     */
    void start ( Session s, boolean exclusive ) throws JMSException, MessagingException;


    /**
     * Stop listening for messages
     * 
     * @throws JMSException
     */
    void stop () throws JMSException;
}
