/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.09.2013 by mbechler
 */
package eu.agno3.orchestrator.messaging.server;


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

import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.routing.EventRouter;
import eu.agno3.runtime.messaging.routing.MessageRoutingException;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractServerEventRouter implements EventRouter {

    private static final Logger log = Logger.getLogger(AbstractServerEventRouter.class);

    private Map<Session, WeakReference<MessageProducer>> producerCache = Collections.synchronizedMap(new WeakHashMap<>());


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
            Topic destination = getDestination(s, scope);
            MessageProducer p = getProducer(s);
            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Routing message to " + scope.getEventTopic())); //$NON-NLS-1$
            }

            p.send(destination, m);
        }
        catch ( JMSException e ) {
            throw new MessageRoutingException("Failed to route message:", e); //$NON-NLS-1$
        }
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
            log.debug("Creating new producer, new session"); //$NON-NLS-1$
            prod = makeProducer(s);
        }
        else {
            prod = ref.get();
            if ( prod == null ) {
                log.debug("Creating new producer"); //$NON-NLS-1$
                prod = makeProducer(s);
            }
        }
        return prod;
    }


    /**
     * @param s
     * @return
     * @throws JMSException
     */
    protected MessageProducer makeProducer ( Session s ) throws JMSException {
        log.debug("Creating new producer"); //$NON-NLS-1$
        MessageProducer prod = s.createProducer(null);
        this.producerCache.put(s, new WeakReference<>(prod));
        return prod;
    }
}
