/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.routing;


import javax.jms.Destination;
import javax.jms.Session;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.ErrorResponseMessage;
import eu.agno3.runtime.messaging.msg.RequestMessage;
import eu.agno3.runtime.messaging.msg.ResponseMessage;


/**
 * @author mbechler
 * 
 */
public interface MessageDestinationResolver {

    /**
     * @param s
     * @param msg
     * @return the JMS destination to which this message should be sentF
     * @throws MessagingException
     */
    Destination createDestination ( Session s,
            RequestMessage<@NonNull ? extends MessageSource, ResponseMessage<@NonNull ? extends MessageSource>, ErrorResponseMessage<@NonNull ? extends MessageSource>> msg )
                    throws MessagingException;

}
