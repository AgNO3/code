/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.listener;


import javax.jms.Destination;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.DestinationDoesNotExistException;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.InvalidSessionException;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.marshalling.MarshallerManager;
import eu.agno3.runtime.messaging.marshalling.MarshallingException;
import eu.agno3.runtime.messaging.marshalling.MessageMarshaller;
import eu.agno3.runtime.messaging.marshalling.MessageUnmarshaller;
import eu.agno3.runtime.messaging.marshalling.UnmarshallerManager;
import eu.agno3.runtime.messaging.msg.ErrorResponseMessage;
import eu.agno3.runtime.messaging.msg.MessageProperties;
import eu.agno3.runtime.messaging.msg.RequestMessage;
import eu.agno3.runtime.messaging.msg.ResponseMessage;
import eu.agno3.runtime.messaging.msg.ResponseStatus;
import eu.agno3.runtime.messaging.msg.impl.DefaultErrorResponseMessage;


/**
 * @author mbechler
 * 
 */
@SuppressWarnings ( "rawtypes" )
public class RequestEndpointWrapper extends
        AbstractMessageListenerWrapper<RequestMessage<@NonNull ? extends MessageSource, ResponseMessage<@NonNull ? extends MessageSource>, ErrorResponseMessage<@NonNull ? extends MessageSource>>> {

    private static final String MSG_PROCESS_FAIL = "Failed to process request message:"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(RequestEndpointWrapper.class);

    private static final long DEFAULT_EXCEPTION_MSG_TIMEOUT = 30000;

    private RequestEndpoint endpoint;
    private MarshallerManager marshallerManager;
    private UnmarshallerManager unmarshallerManager;

    private String defaultQueuePrefix;

    @NonNull
    private MessageSource msgSource;


    /**
     * 
     * @param msgType
     * @param endpoint
     * @param mm
     * @param um
     * @param defaultQueuePrefix
     * @param msgSource
     */

    public RequestEndpointWrapper (
            @NonNull Class<@NonNull ? extends RequestMessage<@NonNull ? extends MessageSource, ResponseMessage<@NonNull ? extends MessageSource>, ErrorResponseMessage<@NonNull ? extends MessageSource>>> msgType,
            RequestEndpoint endpoint, MarshallerManager mm, UnmarshallerManager um, String defaultQueuePrefix, @NonNull MessageSource msgSource ) {
        super(msgType);
        this.endpoint = endpoint;
        this.marshallerManager = mm;
        this.unmarshallerManager = um;
        this.defaultQueuePrefix = defaultQueuePrefix;
        this.msgSource = msgSource;
    }


    /**
     * {@inheritDoc}
     * 
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    @Override
    public void onMessage ( Message msg, Session s ) {

        if ( log.isTraceEnabled() ) {
            log.trace("Handling message " + msg); //$NON-NLS-1$
        }

        RequestMessage<@NonNull ? extends MessageSource, ResponseMessage<@NonNull ? extends MessageSource>, ErrorResponseMessage<@NonNull ? extends MessageSource>> request = null;
        try {
            MessageUnmarshaller<? extends RequestMessage<@NonNull ? extends MessageSource, ResponseMessage<@NonNull ? extends MessageSource>, ErrorResponseMessage<@NonNull ? extends MessageSource>>> unmarshaller = getUnmarshaller(
                msg);

            request = unmarshaller.unmarshall(msg, this.getMessageType().getClassLoader(), null);

            if ( request == null ) {
                throw new MessagingException("Request is NULL"); //$NON-NLS-1$
            }

            processRequestMessage(msg, s, request);
        }
        catch (
            JMSException |
            InvalidSessionException e ) {
            // there is a big chance that we won't be able to send the reply
            throw new RuntimeException("JMS session invalid, do not attempt to send response", e); //$NON-NLS-1$
        }
        catch ( RuntimeException e ) {
            throw new RuntimeException(MSG_PROCESS_FAIL, e);
        }
        catch ( Exception e ) {
            try {
                sendExceptionResponse(msg, s, request, e);
            }
            catch (
                MarshallingException |
                JMSException e1 ) {
                log.error("Failed to send error response", e1); //$NON-NLS-1$
            }
            throw new RuntimeException(MSG_PROCESS_FAIL, e);
        }

    }


    /**
     * @param msg
     * @return
     * @throws JMSException
     * @throws MessagingException
     * @throws MarshallingException
     */
    protected MessageUnmarshaller<? extends RequestMessage<@NonNull ? extends MessageSource, ResponseMessage<@NonNull ? extends MessageSource>, ErrorResponseMessage<@NonNull ? extends MessageSource>>> getUnmarshaller (
            Message msg ) throws JMSException, MessagingException, MarshallingException {
        String gotMessageType = msg.getStringProperty(eu.agno3.runtime.messaging.msg.MessageProperties.TYPE);
        if ( gotMessageType == null || !gotMessageType.equals(this.getMessageType().getName()) ) {
            throw new MessagingException(String.format(
                "Incoming messageType %s does not match type specified by endpoint %s", //$NON-NLS-1$
                gotMessageType,
                this.getMessageType().getName()));
        }

        MessageUnmarshaller<? extends RequestMessage<@NonNull ? extends MessageSource, ResponseMessage<@NonNull ? extends MessageSource>, ErrorResponseMessage<@NonNull ? extends MessageSource>>> unmarshaller = this.unmarshallerManager
                .getUnmarshaller(this.getMessageType());
        return unmarshaller;
    }


    /**
     * @param msg
     * @param request
     * @param e
     * @throws JMSException
     * @throws MarshallingException
     */
    @SuppressWarnings ( "unchecked" )
    private void sendExceptionResponse ( Message msg, Session s,
            RequestMessage<@NonNull ? extends MessageSource, ResponseMessage<@NonNull ? extends MessageSource>, ErrorResponseMessage<@NonNull ? extends MessageSource>> request,
            Exception e ) throws JMSException, MarshallingException {
        if ( msg.getJMSReplyTo() != null ) {

            MessageMarshaller marshaller = this.marshallerManager.getMarshaller(DefaultErrorResponseMessage.class);
            Message m = marshaller.marshall(
                s,
                new DefaultErrorResponseMessage(e.getMessage(), this.msgSource, (RequestMessage<@NonNull MessageSource, ?, ?>) request));
            try {
                MessageProducer replyToProducer = s.createProducer(msg.getJMSReplyTo());
                try {
                    m.setStringProperty(MessageProperties.STATUS, "failure"); //$NON-NLS-1$
                    m.setJMSCorrelationID(msg.getJMSCorrelationID());
                    if ( request != null ) {
                        m.setJMSExpiration(
                            System.currentTimeMillis() + ( request.getDeliveryTTL() != 0 ? request.getDeliveryTTL() : request.getReplyTimeout() ));
                    }
                    else {
                        m.setJMSExpiration(System.currentTimeMillis() + DEFAULT_EXCEPTION_MSG_TIMEOUT);
                    }
                    replyToProducer.send(m, m.getJMSDeliveryMode(), m.getJMSPriority(), m.getJMSExpiration());
                }
                finally {
                    replyToProducer.close();
                }
            }
            catch ( DestinationDoesNotExistException ex ) {
                log.debug("Destination does not exist anymore", ex); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param msg
     * @param request
     * @throws JMSException
     * @throws MessagingException
     */
    @SuppressWarnings ( "unchecked" )
    protected void processRequestMessage ( Message msg, Session s,
            @NonNull RequestMessage<@NonNull ? extends MessageSource, ResponseMessage<@NonNull ? extends MessageSource>, ErrorResponseMessage<@NonNull ? extends MessageSource>> request )
                    throws JMSException, MessagingException {
        ResponseMessage response;
        MessageMarshaller responseMarshaller;
        ResponseStatus status = ResponseStatus.ERROR;
        try {

            response = this.endpoint.onReceive(request);

            if ( !request.hasResponse() ) {
                return;
            }

            responseMarshaller = this.marshallerManager.getMarshaller(request.getResponseType());
            status = ResponseStatus.SUCCESS;
        }
        catch ( MessageProcessingException procEx ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Failed to process event message:", procEx); //$NON-NLS-1$
            }
            response = procEx.getErrorResponse();
            responseMarshaller = this.marshallerManager.getMarshaller(request.getErrorResponseType());
        }

        if ( msg.getJMSReplyTo() != null ) {
            Message m = responseMarshaller.marshall(s, response);

            try {
                MessageProducer replyToProducer = s.createProducer(msg.getJMSReplyTo());
                try {
                    m.setStringProperty(MessageProperties.STATUS, status.toString());
                    m.setJMSCorrelationID(msg.getJMSCorrelationID());
                    m.setJMSDeliveryMode(request.getDeliveryMode());
                    m.setJMSPriority(request.getDeliveryPriority());
                    long expire = System.currentTimeMillis()
                            + ( request.getDeliveryTTL() != 0 ? request.getDeliveryTTL() : request.getReplyTimeout() );
                    m.setJMSExpiration(expire);
                    replyToProducer.send(msg.getJMSReplyTo(), m, request.getDeliveryMode(), request.getDeliveryPriority(), expire);
                }

                finally {
                    replyToProducer.close();
                }
            }
            catch (
                DestinationDoesNotExistException |
                InvalidDestinationException e ) {
                log.debug("Requestor is gone", e); //$NON-NLS-1$
            }
            catch ( JMSException e ) {
                throw new InvalidSessionException("Session is invalid", e); //$NON-NLS-1$
            }
        }
        else {
            log.warn("Produced response message, but there is no reply-to header set"); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.MessageListener#getDestination(eu.agno3.runtime.messaging.listener.MessageListener,
     *      javax.jms.Session)
     */
    @Override
    public Destination getDestination ( MessageListener listener, Session s ) throws MessagingException {
        try {
            if ( this.endpoint instanceof CustomDestination ) {
                CustomDestination customDest = (CustomDestination) this.endpoint;
                return customDest.createCustomDestination(s);
            }

            String queueName = this.getMessageType().getName();
            return s.createQueue(this.defaultQueuePrefix + queueName);
        }
        catch ( JMSException e ) {
            throw new MessagingException("Failed to open destination:", e); //$NON-NLS-1$
        }
    }
}
