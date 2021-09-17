/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.09.2013 by mbechler
 */
package eu.agno3.runtime.messaging.routing.internal;


import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.messaging.MessagingException;
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
    "targetClass=eu.agno3.runtime.messaging.addressing.DefaultMessageTarget"
} )
public class DefaultMessageTargetDestinationResolver implements MessageDestinationResolver {

    /**
     * {@inheritDoc}
     * 
     * 
     * @see eu.agno3.runtime.messaging.routing.MessageDestinationResolver#createDestination(javax.jms.Session,
     *      eu.agno3.runtime.messaging.msg.RequestMessage)
     */
    @Override
    public Destination createDestination ( Session s,
            RequestMessage<@NonNull ? extends MessageSource, ResponseMessage<@NonNull ? extends MessageSource>, ErrorResponseMessage<@NonNull ? extends MessageSource>> msg )
                    throws MessagingException {
        String queueName = msg.getClass().getName();
        try {
            return s.createQueue(queueName);
        }
        catch ( JMSException e ) {
            throw new MessagingException("Failed to connect to destination queue:", e); //$NON-NLS-1$
        }
    }

}
