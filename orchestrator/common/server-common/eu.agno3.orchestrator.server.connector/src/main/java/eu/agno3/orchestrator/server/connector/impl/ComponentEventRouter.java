/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.09.2013 by mbechler
 */
package eu.agno3.orchestrator.server.connector.impl;


import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.server.component.ComponentConfig;
import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.routing.EventRouter;
import eu.agno3.runtime.messaging.routing.MessageRoutingException;


/**
 * @author mbechler
 * 
 */
public class ComponentEventRouter implements EventRouter {

    private static final Logger log = Logger.getLogger(ComponentEventRouter.class);

    private Map<Session, WeakReference<MessageProducer>> producerCache = Collections.synchronizedMap(new WeakHashMap<>());

    private String outQueueName;
    private Queue destination;


    /**
     * @param agentConfig
     */
    public ComponentEventRouter ( ComponentConfig agentConfig ) {
        this.outQueueName = agentConfig.getEventOutQueue();
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.routing.EventRouter#routeMessage(javax.jms.Session,
     *      eu.agno3.runtime.messaging.addressing.EventScope, javax.jms.Message)
     */
    @Override
    public void routeMessage ( Session s, EventScope scope, Message m ) throws MessageRoutingException {
        try {
            MessageProducer p = getProducer(s);
            p.send(m, m.getJMSDeliveryMode(), m.getJMSPriority(), m.getJMSExpiration());
        }
        catch ( JMSException e ) {
            throw new MessageRoutingException("Failed to route message:", e); //$NON-NLS-1$
        }
    }


    /**
     * @param s
     * @return
     * @throws JMSException
     */
    protected Queue getDestination ( Session s ) throws JMSException {
        if ( this.destination == null ) {
            this.destination = s.createQueue(this.outQueueName);
        }
        return this.destination;
    }


    /**
     * @param s
     * @param scope
     * @return
     * @throws JMSException
     */
    protected Topic getDestination ( Session s, EventScope scope ) throws JMSException {
        return s.createTopic(scope.getEventTopic());
    }


    /**
     * @param s
     * @return
     * @throws JMSException
     */
    protected MessageProducer getProducer ( Session s ) throws JMSException {
        WeakReference<MessageProducer> ref = this.producerCache.get(s);
        MessageProducer prod;
        if ( ref == null ) {
            log.trace("Creating new producer, new session"); //$NON-NLS-1$
            prod = s.createProducer(getDestination(s));
            this.producerCache.put(s, new WeakReference<>(prod));
        }
        else {
            prod = ref.get();
            if ( prod == null ) {
                log.trace("Creating new producer"); //$NON-NLS-1$
                prod = s.createProducer(getDestination(s));
                this.producerCache.put(s, new WeakReference<>(prod));
            }
        }
        return prod;
    }


    /**
     * @throws JMSException
     * 
     */
    @Override
    public void close () throws JMSException {
        for ( WeakReference<MessageProducer> weakReference : this.producerCache.values() ) {
            MessageProducer messageProducer = weakReference.get();
            if ( messageProducer != null ) {
                messageProducer.close();
            }
        }
        this.producerCache.clear();
    }

}
