/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.client;


import javax.jms.Session;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.ErrorResponseMessage;
import eu.agno3.runtime.messaging.msg.EventMessage;
import eu.agno3.runtime.messaging.msg.RequestMessage;
import eu.agno3.runtime.messaging.msg.ResponseMessage;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public interface MessagingClient <T extends MessageSource> {

    /**
     * @throws MessagingException
     */
    void open () throws MessagingException;


    /**
     * @throws MessagingException
     */
    void close () throws MessagingException;


    /**
     * @param e
     * @throws MessagingException
     * @throws InterruptedException
     */
    void publishEvent ( @NonNull EventMessage<@NonNull ? extends MessageSource> e ) throws MessagingException, InterruptedException;


    /**
     * @param s
     * @param evt
     * @throws MessagingException
     * @throws InterruptedException
     */
    void publishEvent ( Session s, @NonNull EventMessage<@NonNull ? extends MessageSource> evt ) throws MessagingException, InterruptedException;


    /**
     * @param msg
     * @return the messages reply
     * @throws MessagingException
     * @throws InterruptedException
     */
    <@NonNull TMsg extends RequestMessage<@NonNull ? extends MessageSource, TResponse, TError>, @Nullable TResponse extends ResponseMessage<@NonNull ? extends MessageSource>, @NonNull TError extends ErrorResponseMessage<@NonNull ? extends MessageSource>> TResponse sendMessage (
            @NonNull TMsg msg ) throws MessagingException, InterruptedException;


    /**
     * @return the message source for this client
     */
    @NonNull
    T getMessageSource ();


    /**
     * @return a session for this client
     * @throws MessagingException
     */
    Session getSession () throws MessagingException;


    /**
     * @param transacted
     * @param ackMode
     * @return a session for this client
     * @throws MessagingException
     */
    Session getSession ( boolean transacted, int ackMode ) throws MessagingException;

}
