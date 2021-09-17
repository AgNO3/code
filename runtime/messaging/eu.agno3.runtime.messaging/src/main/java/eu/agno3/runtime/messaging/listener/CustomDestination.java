/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.listener;


import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;


/**
 * @author mbechler
 * 
 */
public interface CustomDestination {

    /**
     * @param s
     * @return a destination to listen on for requests
     * @throws JMSException
     */
    Destination createCustomDestination ( Session s ) throws JMSException;


    /**
     * @return an identifier for the destination
     */
    String createCustomDestinationId ();
}
