/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.09.2013 by mbechler
 */
package eu.agno3.orchestrator.server.connector.impl;


import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.log4j.Logger;

import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.routing.EventRouter;
import eu.agno3.runtime.messaging.routing.EventRouterManager;
import eu.agno3.runtime.messaging.routing.MessageRoutingException;


/**
 * @author mbechler
 * 
 */
public class SystemEventRouterManager implements EventRouterManager, EventRouter {

    private static final Logger log = Logger.getLogger(SystemEventRouterManager.class);

    private Map<Session, WeakReference<MessageProducer>> producerCache = Collections.synchronizedMap(new WeakHashMap<>());

    private String systemTopic;
    private Topic destination;


    /**
     * @param systemTopic
     */
    public SystemEventRouterManager ( String systemTopic ) {
        this.systemTopic = systemTopic;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.routing.EventRouterManager#getRouterFor(eu.agno3.runtime.messaging.addressing.EventScope)
     */
    @Override
    public EventRouter getRouterFor ( EventScope scope ) throws MessagingException {
        return this;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.routing.EventRouter#close()
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
            if ( log.isDebugEnabled() ) {
                log.debug("Publishing event using system routing on " + getDestination(s)); //$NON-NLS-1$
            }
            p.send(m, m.getJMSDeliveryMode(), m.getJMSPriority(), m.getJMSExpiration());
        }
        catch ( JMSException e ) {
            throw new MessageRoutingException("Failed to route message:", e); //$NON-NLS-1$
        }
    }


    /**
     * @param s
     * @param scope
     * @return
     * @throws JMSException
     */
    protected Topic getDestination ( Session s ) throws JMSException {
        if ( this.destination == null ) {
            this.destination = s.createTopic(this.systemTopic);
        }
        return this.destination;
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
            log.debug("Creating new producer, new session"); //$NON-NLS-1$
            prod = s.createProducer(getDestination(s));
            this.producerCache.put(s, new WeakReference<>(prod));
        }
        else {
            prod = ref.get();
            if ( prod == null ) {
                log.debug("Creating new producer"); //$NON-NLS-1$
                prod = s.createProducer(getDestination(s));
                this.producerCache.put(s, new WeakReference<>(prod));
            }
        }
        return prod;
    }

}
