/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.09.2015 by mbechler
 */
package eu.agno3.runtime.messaging.listener;


import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.Session;

import eu.agno3.runtime.messaging.MessagingException;


/**
 * @author mbechler
 *
 */
public interface MessageListener extends BaseListener {

    /**
     * @param msg
     * @param s
     */
    void onMessage ( Message msg, Session s );


    /**
     * @param listener
     * @param session
     * @return the destination to listen on
     * @throws MessagingException
     */
    Destination getDestination ( MessageListener listener, Session session ) throws MessagingException;

}
