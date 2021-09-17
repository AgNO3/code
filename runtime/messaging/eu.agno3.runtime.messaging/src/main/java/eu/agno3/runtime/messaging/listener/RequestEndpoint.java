/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.listener;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.ErrorResponseMessage;
import eu.agno3.runtime.messaging.msg.RequestMessage;
import eu.agno3.runtime.messaging.msg.ResponseMessage;


/**
 * @author mbechler
 * @param <TMessage>
 * @param <TReply>
 * @param <TError>
 * 
 */
public interface RequestEndpoint <TMessage extends RequestMessage<@NonNull ? extends MessageSource, TReply, TError>, TReply extends ResponseMessage<@NonNull ? extends MessageSource>, TError extends ErrorResponseMessage<@NonNull ? extends MessageSource>> {

    /**
     * 
     * @param msg
     * @return a reply message if the message was successfully processed
     * @throws MessageProcessingException
     *             if the message was not sucessfully processed
     * @throws MessagingException
     */
    TReply onReceive ( @NonNull TMessage msg ) throws MessageProcessingException, MessagingException;


    /**
     * @return message type handled by this endpoint
     */
    Class<TMessage> getMessageType ();
}
