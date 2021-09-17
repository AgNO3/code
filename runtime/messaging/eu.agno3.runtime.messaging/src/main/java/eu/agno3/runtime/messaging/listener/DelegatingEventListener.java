/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.09.2013 by mbechler
 */
package eu.agno3.runtime.messaging.listener;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.marshalling.MessageUnmarshaller;
import eu.agno3.runtime.messaging.marshalling.UnmarshallerManager;
import eu.agno3.runtime.messaging.msg.EventMessage;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public class DelegatingEventListener <T extends EventListener<? extends EventMessage<@NonNull ? extends MessageSource>>> implements MessageListener {

    private static final Logger log = Logger.getLogger(DelegatingEventListener.class);
    private Map<String, Set<T>> listenerMap = new HashMap<>();
    private MessageListenerContainer messageListener;
    private UnmarshallerManager unmarshallerManager;
    private MessageListenerFactory msf;
    private Destination destination;


    /**
     * @param msf
     * @param unmarshallerManager
     * @param destination
     * @throws JMSException
     * @throws MessagingException
     */
    public DelegatingEventListener ( MessageListenerFactory msf, UnmarshallerManager unmarshallerManager, Destination destination )
            throws JMSException, MessagingException {
        this.msf = msf;
        this.unmarshallerManager = unmarshallerManager;
        this.destination = destination;
        this.messageListener = msf.createMessageListener(this);
        msf.startListener(this.messageListener);
    }


    /**
     * @throws JMSException
     */
    public void close () throws JMSException {
        this.msf.remove(this.messageListener);
    }


    /**
     * @param listener
     */
    public void addDelegate ( T listener ) {

        synchronized ( this.listenerMap ) {
            String eventType = listener.getEventType().getName();

            if ( !this.listenerMap.containsKey(eventType) ) {
                this.listenerMap.put(eventType, new HashSet<T>());
            }

            this.listenerMap.get(eventType).add(listener);
        }

    }


    /**
     * @param listener
     */
    public void removeDelegate ( EventListener<EventMessage<@NonNull ? extends MessageSource>> listener ) {

        synchronized ( this.listenerMap ) {
            String eventType = listener.getEventType().getName();
            Set<T> delegates = this.listenerMap.get(eventType);
            if ( delegates != null ) {
                delegates.remove(listener);

                if ( delegates.isEmpty() ) {
                    this.listenerMap.remove(eventType);
                }
            }
        }

    }


    /**
     * @return number of registered delegates
     */
    public long getNumDelegates () {

        synchronized ( this.listenerMap ) {
            long num = 0;

            for ( Set<T> typeListeners : this.listenerMap.values() ) {
                num += typeListeners.size();
            }

            return num;
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
    public Destination getDestination ( MessageListener listener, Session session ) throws MessagingException {
        return this.destination;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.MessageListener#onMessage(javax.jms.Message, javax.jms.Session)
     */
    @Override
    public void onMessage ( Message msg, Session s ) {
        String msgType;
        try {
            msgType = msg.getStringProperty(eu.agno3.runtime.messaging.msg.MessageProperties.TYPE);
        }
        catch ( JMSException e ) {
            log.error("Failed to get message type:", e); //$NON-NLS-1$
            return;
        }

        if ( log.isTraceEnabled() ) {
            log.trace("Determined message type " + msgType); //$NON-NLS-1$
        }

        Set<T> applicableListeners = getListenersForType(msgType);
        Map<Class<? extends EventMessage<@NonNull ? extends MessageSource>>, EventMessage<@NonNull ? extends MessageSource>> parserCache = new HashMap<>();

        try {
            for ( T listener : applicableListeners ) {
                if ( listener != null ) {
                    deliverToListener(msg, parserCache, (EventListener<?>) listener);
                }
            }
        }
        catch ( MessagingException e ) {
            log.warn("Failed to deliver event:", e); //$NON-NLS-1$
            throw new RuntimeException("Failed to deliver event message:", e); //$NON-NLS-1$
        }

    }


    /**
     * @param msgType
     * @return
     * @throws MessagingException
     */
    private Set<T> getListenersForType ( String msgType ) {
        Set<T> applicableListeners = Collections.EMPTY_SET;
        synchronized ( this.listenerMap ) {
            if ( !this.listenerMap.containsKey(msgType) || this.listenerMap.get(msgType) == null || this.listenerMap.get(msgType).isEmpty() ) {
                return Collections.EMPTY_SET;
            }

            applicableListeners = this.listenerMap.get(msgType);
        }
        return applicableListeners;
    }


    /**
     * @param msg
     * @param parserCache
     * @param listener
     */
    @SuppressWarnings ( "unchecked" )
    private <@NonNull TEvent extends EventMessage<@NonNull ? extends MessageSource>> void deliverToListener ( Message msg,
            Map<Class<? extends EventMessage<@NonNull ? extends MessageSource>>, EventMessage<@NonNull ? extends MessageSource>> parserCache,
            EventListener<TEvent> listener ) throws MessagingException {
        if ( parserCache.containsKey(listener.getEventType()) ) {
            TEvent eventMessage = (TEvent) parserCache.get(listener.getEventType());
            listener.onEvent(eventMessage);
        }
        else {

            MessageUnmarshaller<? extends EventMessage<@NonNull ? extends MessageSource>> unmarshaller = this.unmarshallerManager
                    .getUnmarshaller(listener.getEventType());
            EventMessage<@NonNull ? extends MessageSource> event = unmarshaller.unmarshall(msg, listener.getEventType().getClassLoader(), null);

            if ( event == null ) {
                throw new MessagingException("Event is NULL"); //$NON-NLS-1$
            }

            listener.onEvent((TEvent) event);
            parserCache.put(listener.getEventType(), event);

        }
    }

}
