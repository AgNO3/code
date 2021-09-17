/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.routing.internal;


import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.JMSQueueMessageTarget;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.ErrorResponseMessage;
import eu.agno3.runtime.messaging.msg.RequestMessage;
import eu.agno3.runtime.messaging.msg.ResponseMessage;
import eu.agno3.runtime.messaging.routing.MessageDestinationResolver;


/**
 * @author mbechler
 * 
 */
@Component ( service = MessageDestinationResolver.class, property = {
    "targetClass=eu.agno3.runtime.messaging.addressing.JMSQueueMessageTarget"
} )
public class JMSQueueMessageTargetDestinationResolver implements MessageDestinationResolver {

    /**
     * 
     * {@inheritDoc}
     * 
     * @throws MessagingException
     * 
     * @see eu.agno3.runtime.messaging.routing.MessageDestinationResolver#createDestination(javax.jms.Session,
     *      eu.agno3.runtime.messaging.msg.RequestMessage)
     */
    @Override
    public Destination createDestination ( Session s,
            RequestMessage<@NonNull ? extends MessageSource, ResponseMessage<@NonNull ? extends MessageSource>, ErrorResponseMessage<@NonNull ? extends MessageSource>> msg )
                    throws MessagingException {
        JMSQueueMessageTarget t = (JMSQueueMessageTarget) msg.getTarget();
        try {
            return s.createQueue(t.getQueueName());
        }
        catch ( JMSException e ) {
            throw new MessagingException(e);
        }
    }
}
