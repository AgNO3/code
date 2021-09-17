/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.09.2013 by mbechler
 */
package eu.agno3.orchestrator.server.base.component;


import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.command.ActiveMQTopic;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.server.component.ComponentConfig;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.addressing.ListeningEventScopeResolver;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.events.EventTypeRegistry;
import eu.agno3.runtime.messaging.listener.MessageListenerContainer;
import eu.agno3.runtime.messaging.listener.MessageListenerFactory;
import eu.agno3.runtime.messaging.marshalling.MessageUnmarshaller;
import eu.agno3.runtime.messaging.marshalling.UnmarshallerManager;
import eu.agno3.runtime.messaging.msg.EventMessage;
import eu.agno3.runtime.messaging.msg.MessageProperties;
import eu.agno3.runtime.messaging.routing.EventRouterManager;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public class ComponentEventPump <T extends ComponentConfig> {

    private static final Logger log = Logger.getLogger(ComponentEventPump.class);

    private EventTypeRegistry eventTypeRegistry;
    private EventRouterManager eventRouterManager;
    private UnmarshallerManager unmarshallerManager;

    private MessageListenerFactory messageListenerFactory;
    private MessageProducer outgoingProducer;
    private MessageListenerContainer incomingListener;
    private MessageListenerContainer topicListener;
    private EventScope listeningScope;
    private T config;

    private Session session;

    private final AtomicInteger inflight = new AtomicInteger();


    /**
     * @param config
     * @param listeningScope
     * @param eventTypeRegistry
     * @param unmarshallerManager
     * @param eventRouterManager
     * @param mlf
     */
    public ComponentEventPump ( T config, EventScope listeningScope, EventTypeRegistry eventTypeRegistry, UnmarshallerManager unmarshallerManager,
            EventRouterManager eventRouterManager, MessageListenerFactory mlf ) {

        this.listeningScope = listeningScope;
        this.messageListenerFactory = mlf;
        this.eventRouterManager = eventRouterManager;
        this.eventTypeRegistry = eventTypeRegistry;
        this.unmarshallerManager = unmarshallerManager;
        this.config = config;
    }


    /**
     * 
     * @param c
     * @throws JMSException
     * @throws MessagingException
     */
    public final void start ( Connection c ) throws JMSException, MessagingException {
        if ( log.isDebugEnabled() ) {
            log.debug("Setting up event pump for agent " + this.config.getId()); //$NON-NLS-1$
        }
        // make sure we use the same session for consumers and producers
        this.session = c.createSession(true, Session.SESSION_TRANSACTED);
        this.incomingListener = setupIncomingListener();
        this.outgoingProducer = setupOutgoingProducer();
        this.topicListener = setupServerTopicListener();
        this.messageListenerFactory.startListener(this.incomingListener, this.session);
        this.messageListenerFactory.startListener(this.topicListener, this.session);
    }


    private Topic getListeningTopics () {
        ListeningEventScopeResolver scopeResolver = new ListeningEventScopeResolver();
        Set<String> listenTopics = scopeResolver.getListeningTopics(getEventScope());
        return new ActiveMQTopic(StringUtils.join(listenTopics, ",")); //$NON-NLS-1$
    }


    private MessageListenerContainer setupServerTopicListener () {
        Topic listenTopic = getListeningTopics();

        if ( log.isDebugEnabled() ) {
            log.debug("Listening on topics " + listenTopic); //$NON-NLS-1$
        }

        return this.messageListenerFactory.createMessageListener(new TopicListener<>(this, listenTopic));
    }


    private MessageProducer setupOutgoingProducer () throws JMSException {
        return this.session.createProducer(this.session.createTopic(this.config.getEventTopic()));
    }


    private MessageListenerContainer setupIncomingListener () {
        return this.messageListenerFactory.createMessageListener(new IncomingListener<>(this, this.config.getEventOutQueue()));
    }


    protected EventScope getEventScope () {
        return this.listeningScope;
    }


    /**
     * @throws JMSException
     * 
     */
    public void close () throws JMSException {
        long timeOut = System.currentTimeMillis() + 1000;
        while ( this.inflight.get() != 0 && System.currentTimeMillis() < timeOut ) {
            try {
                Thread.sleep(100);
            }
            catch ( InterruptedException e ) {
                throw new JMSException("Interrupted while waiting for inflight events"); //$NON-NLS-1$
            }
        }

        int remain = this.inflight.get();
        if ( remain != 0 ) {
            log.warn(String.format("Shutting down, but there are still %d inflight events", remain)); //$NON-NLS-1$
        }

        log.debug("Closing down event pump"); //$NON-NLS-1$
        this.messageListenerFactory.remove(this.incomingListener);
        this.messageListenerFactory.remove(this.topicListener);
        this.outgoingProducer.close();
        this.session.close();

    }


    /**
     * @param m
     * @return the target scope for this message
     * @throws MessagingException
     * @throws JMSException
     */
    Collection<EventScope> getTargetScopes ( Message m ) throws MessagingException, JMSException {
        String msgType = m.getStringProperty(MessageProperties.TYPE);
        Class<@NonNull ? extends EventMessage<@NonNull ? extends MessageSource>> typeClass = this.eventTypeRegistry.getEventType(msgType);
        @NonNull
        MessageUnmarshaller<@NonNull ? extends EventMessage<@NonNull ? extends MessageSource>> um = this.unmarshallerManager
                .getUnmarshaller(typeClass);
        EventMessage<@NonNull ? extends MessageSource> evt = um.unmarshall(m, typeClass.getClassLoader(), null);
        return evt.getScopes();
    }


    /**
     * @param m
     * @param scope
     * @throws MessagingException
     */
    synchronized void routeEventToScope ( Message m, EventScope scope ) throws MessagingException {
        this.inflight.incrementAndGet();
        try {
            if ( this.session != null ) {
                this.eventRouterManager.getRouterFor(scope).routeMessage(this.session, scope, m);
            }
        }
        finally {
            this.inflight.decrementAndGet();
        }
    }


    /**
     * @param m
     * @param s
     * @throws JMSException
     */
    public void pumpEventToComponent ( Message m, Session s ) throws JMSException {
        this.outgoingProducer.send(m, m.getJMSDeliveryMode(), m.getJMSPriority(), m.getJMSExpiration());
    }

}
