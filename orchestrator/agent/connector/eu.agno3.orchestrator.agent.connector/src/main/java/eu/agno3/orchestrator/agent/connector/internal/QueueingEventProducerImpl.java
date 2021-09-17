/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.12.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.connector.internal;


import java.util.Queue;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.agent.connector.QueueingEventProducer;
import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.orchestrator.server.connector.ServerConnectorConfiguration;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.client.MessagingClient;
import eu.agno3.runtime.messaging.msg.EventMessage;


/**
 * @author mbechler
 *
 */
@Component ( service = QueueingEventProducer.class )
public class QueueingEventProducerImpl implements QueueingEventProducer {

    private static final Logger log = Logger.getLogger(QueueingEventProducer.class);

    private static final int QUEUE_SIZE = 512;

    private MessagingClient<AgentMessageSource> messageClient;

    private Queue<EventMessage<? extends @NonNull MessageSource>> queued = new CircularFifoQueue<>(QUEUE_SIZE);

    private ServerConnectorConfiguration connectorConfig;


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void setMessageClient ( MessagingClient<AgentMessageSource> mc ) {
        this.messageClient = mc;
        sendEvents();
    }


    protected synchronized void unsetMessageClient ( MessagingClient<AgentMessageSource> mc ) {
        if ( this.messageClient == mc ) {
            this.messageClient = null;
        }
    }


    @Reference
    protected synchronized void setServerConnectorConfig ( ServerConnectorConfiguration scc ) {
        this.connectorConfig = scc;
    }


    protected synchronized void unsetServerConnectorConfig ( ServerConnectorConfiguration scc ) {
        if ( this.connectorConfig == scc ) {
            this.connectorConfig = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.connector.QueueingEventProducer#getMessageSource()
     */
    @Override
    public @NonNull AgentMessageSource getMessageSource () {
        return new AgentMessageSource(this.connectorConfig.getComponentId());
    }


    @Override
    public void publish ( @NonNull EventMessage<? extends @NonNull MessageSource> ev ) {
        MessagingClient<AgentMessageSource> mc = this.messageClient;
        if ( mc != null ) {
            try {
                mc.publishEvent(ev);
            }
            catch (
                MessagingException |
                InterruptedException e ) {
                log.warn("Failed to publish event", e); //$NON-NLS-1$
                this.queued.add(ev);
            }
        }
        else {
            this.queued.add(ev);
        }
    }


    private void sendEvents () {
        MessagingClient<AgentMessageSource> mc = this.messageClient;
        if ( mc == null ) {
            return;
        }
        while ( !this.queued.isEmpty() ) {
            EventMessage<? extends @NonNull MessageSource> poll = this.queued.poll();

            if ( poll == null ) {
                continue;
            }

            try {
                mc.publishEvent(poll);
            }
            catch (
                MessagingException |
                InterruptedException e ) {
                log.warn("Failed to publish event from queue", e); //$NON-NLS-1$
                this.queued.add(poll);
                return;
            }
        }
    }

}
