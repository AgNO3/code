/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.marshalling;


import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.eclipse.jdt.annotation.NonNull;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public interface MessageMarshaller <T extends eu.agno3.runtime.messaging.msg.Message<@NonNull ?>> {

    /**
     * @param s
     * @param msg
     * @return a marshalled message
     * @throws MarshallingException
     * @throws JMSException
     */
    <TMsg extends T> Message marshall ( Session s, TMsg msg ) throws MarshallingException, JMSException;

}
