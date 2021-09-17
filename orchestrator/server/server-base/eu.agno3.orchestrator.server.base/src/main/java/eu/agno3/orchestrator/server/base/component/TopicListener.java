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
import javax.jms.Topic;

import org.apache.log4j.Logger;

import eu.agno3.orchestrator.server.component.ComponentConfig;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.listener.MessageListener;
import eu.agno3.runtime.messaging.msg.MessageProperties;


class TopicListener <T extends ComponentConfig> implements MessageListener {

    private static final Logger log = Logger.getLogger(TopicListener.class);

    /**
     * 
     */
    private final ComponentEventPump<T> componentEventPump;

    private Topic topic;


    /**
     * @param agentEventPump
     * @param topic
     */
    public TopicListener ( ComponentEventPump<T> agentEventPump, Topic topic ) {
        this.componentEventPump = agentEventPump;
        this.topic = topic;
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
        return this.topic;
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

            String msgType = m.getStringProperty(MessageProperties.TYPE);

            // TODO: this catches advisory messages, nice one destination =
            // ActiveMQ.Advisory.Consumer.Topic.topic://events-global,
            // topic://events-agents,
            // topic://events-agent-e317cbd9-052a-4519-8b12-ecb152560ba9,
            // topic://events-backend,
            if ( msgType == null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("NULL message type: " + m); //$NON-NLS-1$
                }
                return;
            }

            if ( log.isTraceEnabled() ) {
                log.trace("Pumping outgoing event " + m); //$NON-NLS-1$
            }
            this.componentEventPump.pumpEventToComponent(m, s);
        }
        catch ( JMSException e ) {
            log.warn("Failed to pump outgoing event:", e); //$NON-NLS-1$
        }
    }

}