/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.01.2014 by mbechler
 */
package eu.agno3.orchestrator.server.base.component;


import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.server.component.ComponentConfig;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.listener.MessageListener;


class IncomingListener <T extends ComponentConfig> implements MessageListener {

    private static final Logger log = Logger.getLogger(IncomingListener.class);

    /**
     * 
     */
    private final ComponentEventPump<T> componentEventPump;

    private String queue;


    /**
     * @param agentEventPump
     * @param queue
     * 
     */
    public IncomingListener ( ComponentEventPump<T> agentEventPump, String queue ) {
        this.componentEventPump = agentEventPump;
        this.queue = queue;
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
        try {
            return session.createQueue(this.queue);
        }
        catch ( JMSException e ) {
            throw new MessagingException("Could not create queue", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.MessageListener#onMessage(javax.jms.Message, javax.jms.Session)
     */
    @Override
    public void onMessage ( Message m, Session s ) {
        try {
            if ( log.isTraceEnabled() ) {
                log.trace("Pumping incoming event " + m); //$NON-NLS-1$
            }

            for ( EventScope scope : this.componentEventPump.getTargetScopes(m) ) {
                this.componentEventPump.routeEventToScope(m, scope);
            }
        }
        catch (
            JMSException |
            MessagingException e ) {
            log.warn("Failed to pump incoming event:", e); //$NON-NLS-1$
        }

    }

}