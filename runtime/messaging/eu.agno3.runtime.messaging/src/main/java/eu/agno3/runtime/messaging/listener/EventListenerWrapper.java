/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.listener;


import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.activemq.RedeliveryPolicy;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.marshalling.MarshallingException;
import eu.agno3.runtime.messaging.marshalling.MessageUnmarshaller;
import eu.agno3.runtime.messaging.marshalling.UnmarshallerManager;
import eu.agno3.runtime.messaging.msg.EventMessage;


/**
 * @author mbechler
 * 
 */
public class EventListenerWrapper extends AbstractMessageListenerWrapper<EventMessage<@NonNull ? extends MessageSource>>
        implements MessageListener, RedeliveryPolicyProvider {

    private static final String EVENT_PROCESS_FAIL = "Failed to process event message:"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(EventListenerWrapper.class);

    private UnmarshallerManager unmarshallManager;
    private EventListener<EventMessage<@NonNull ? extends MessageSource>> listener;

    private RedeliveryPolicy redeliveryPolicy;


    /**
     * @param listener
     * @param eventType
     * @param unmarshallManager
     */
    public EventListenerWrapper ( EventListener<EventMessage<@NonNull ? extends MessageSource>> listener,
            @NonNull Class<@NonNull ? extends EventMessage<@NonNull ? extends MessageSource>> eventType, UnmarshallerManager unmarshallManager ) {
        super(eventType);
        this.listener = listener;
        this.unmarshallManager = unmarshallManager;

        eu.agno3.runtime.messaging.listener.RedeliveryPolicy annotation = listener.getClass()
                .getAnnotation(eu.agno3.runtime.messaging.listener.RedeliveryPolicy.class);

        if ( annotation != null ) {
            RedeliveryPolicy pol = new RedeliveryPolicy();
            pol.setUseExponentialBackOff(annotation.useExponentialBackoff());
            pol.setInitialRedeliveryDelay(annotation.initialRedeliveryDelay());
            pol.setMaximumRedeliveries(annotation.maximumRedeliveries());
            pol.setBackOffMultiplier(annotation.backOffMultiplier());
            this.redeliveryPolicy = pol;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.RedeliveryPolicyProvider#getRedeliveryPolicy()
     */
    @Override
    public RedeliveryPolicy getRedeliveryPolicy () {
        return this.redeliveryPolicy;
    }


    @Override
    public Destination getDestination ( MessageListener l, Session s ) throws MessagingException {
        try {
            return s.createTopic(this.getMessageType().getName());
        }
        catch ( JMSException e ) {
            throw new MessagingException("Failed to create destination", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.MessageListener#onMessage(javax.jms.Message, javax.jms.Session)
     */
    @Override
    public void onMessage ( Message msg, Session s ) {
        try {
            this.listener.onEvent(parseEvent(msg));
        }
        catch ( Exception e ) {
            log.error(EVENT_PROCESS_FAIL, e);
            throw new RuntimeException(EVENT_PROCESS_FAIL, e);
        }
    }


    /**
     * @param msg
     * @return
     * @throws JMSException
     * @throws MessagingException
     * @throws MarshallingException
     */
    private @NonNull EventMessage<@NonNull ? extends MessageSource> parseEvent ( Message msg ) throws JMSException, MessagingException {
        String gotEventType = msg.getStringProperty(eu.agno3.runtime.messaging.msg.MessageProperties.TYPE);
        if ( gotEventType == null || !gotEventType.equals(this.getMessageType().getName()) ) {
            throw new MessagingException(String.format(
                "Incoming eventType %s does not match type specified by listener %s", //$NON-NLS-1$
                gotEventType,
                this.getMessageType().getName()));
        }

        MessageUnmarshaller<@NonNull ? extends EventMessage<@NonNull ? extends MessageSource>> unmarshaller = this.unmarshallManager
                .getUnmarshaller(this.getMessageType());
        return unmarshaller.unmarshall(msg, this.getMessageType().getClassLoader(), null);
    }
}
