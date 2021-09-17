/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.routing.internal;


import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.routing.EventRouter;
import eu.agno3.runtime.messaging.routing.MessageRoutingException;


/**
 * @author mbechler
 * 
 */
@Component ( service = EventRouter.class, property = {
    "scopeClass=eu.agno3.runtime.messaging.addressing.scopes.GlobalEventScope"
} )
public class GlobalScopeEventRouter implements EventRouter {

    private static final Logger log = Logger.getLogger(GlobalScopeEventRouter.class);

    private Map<Session, Map<String, WeakReference<MessageProducer>>> producerCache = Collections.synchronizedMap(new WeakHashMap<>());


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
            String eventType = m.getStringProperty(eu.agno3.runtime.messaging.msg.MessageProperties.TYPE);
            MessageProducer p = getProducer(s, eventType);
            p.send(m, m.getJMSDeliveryMode(), m.getJMSPriority(), m.getJMSExpiration());
        }
        catch ( JMSException e ) {
            throw new MessageRoutingException("Failed to route message:", e); //$NON-NLS-1$
        }
    }


    /**
     * @throws JMSException
     * 
     */
    @Override
    public void close () throws JMSException {
        for ( Map<String, WeakReference<MessageProducer>> refs : this.producerCache.values() ) {
            for ( WeakReference<MessageProducer> weakReference : refs.values() ) {
                MessageProducer messageProducer = weakReference.get();
                if ( messageProducer != null ) {
                    messageProducer.close();
                }
            }
            refs.clear();
        }
        this.producerCache.clear();
    }


    /**
     * @param s
     * @param scope
     * @return
     * @throws JMSException
     */
    protected Topic getDestination ( Session s, String eventType ) throws JMSException {
        return s.createTopic(eventType);
    }


    /**
     * @param s
     * @return
     * @throws JMSException
     */
    protected MessageProducer getProducer ( Session s, String eventType ) throws JMSException {
        Map<String, WeakReference<MessageProducer>> perType = this.producerCache.get(s);
        MessageProducer prod;
        if ( perType == null ) {
            perType = new HashMap<>();
            this.producerCache.put(s, perType);
            return createProducer(s, eventType, perType);
        }
        WeakReference<MessageProducer> ref = perType.get(eventType);
        if ( ref != null ) {
            prod = ref.get();
            if ( prod != null ) {
                return prod;
            }
        }
        return createProducer(s, eventType, perType);
    }


    /**
     * @param s
     * @param perType2
     * @return
     * @throws JMSException
     */
    protected MessageProducer createProducer ( Session s, String eventType, Map<String, WeakReference<MessageProducer>> pt ) throws JMSException {
        if ( log.isDebugEnabled() ) {
            log.debug("Creating new producer for " + eventType); //$NON-NLS-1$
        }
        MessageProducer prod = s.createProducer(getDestination(s, eventType));
        pt.put(eventType, new WeakReference<>(prod));
        return prod;
    }
}
