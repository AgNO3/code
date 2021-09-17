/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.09.2015 by mbechler
 */
package eu.agno3.runtime.messaging.listener;


import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;


/**
 * @author mbechler
 *
 */
public interface DestinationStrategy {

    /**
     * 
     * @param listener
     * @param s
     * @return the destination
     * @throws JMSException
     */
    Destination getDestination ( BaseListener listener, Session s ) throws JMSException;


    /**
     * @param listener
     * @return a destination id
     */
    String getDestinationId ( BaseListener listener );

}
