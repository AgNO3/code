/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.msg;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.MessageTarget;


/**
 * @author mbechler
 * @param <T>
 * @param <TReply>
 * @param <TError>
 * 
 */
public interface RequestMessage <@NonNull T extends MessageSource, TReply extends ResponseMessage<@NonNull ? extends MessageSource>, TError extends ErrorResponseMessage<@NonNull ? extends MessageSource>>
        extends Message<T> {

    /**
     * @return the type of the expected response message
     */
    Class<TReply> getResponseType ();


    /**
     * @return the type of an expected error response message
     */
    Class<TError> getErrorResponseType ();


    /**
     * @return the target to which this message should be delivered
     */
    MessageTarget getTarget ();


    /**
     * @return the maximum amount of time to wait for a reply
     */
    long getReplyTimeout ();

}
