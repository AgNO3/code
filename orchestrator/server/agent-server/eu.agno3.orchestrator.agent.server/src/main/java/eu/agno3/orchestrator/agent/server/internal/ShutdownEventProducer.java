/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 29, 2017 by mbechler
 */
package eu.agno3.orchestrator.agent.server.internal;


import java.util.Arrays;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.msg.addressing.AgentsEventScope;
import eu.agno3.orchestrator.server.component.msg.ServerShutdownEvent;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.client.MessagingClient;
import eu.agno3.runtime.update.PlatformState;
import eu.agno3.runtime.update.PlatformStateListener;


/**
 * @author mbechler
 *
 */
@Component ( service = PlatformStateListener.class )
public class ShutdownEventProducer implements PlatformStateListener {

    private static final Logger log = Logger.getLogger(ShutdownEventProducer.class);
    private MessagingClient<ServerMessageSource> messageClient;


    @Reference
    protected synchronized void setMessagingClient ( MessagingClient<ServerMessageSource> client ) {
        this.messageClient = client;
    }


    protected synchronized void unsetMessagingClient ( MessagingClient<ServerMessageSource> client ) {
        if ( this.messageClient == client ) {
            this.messageClient = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.PlatformStateListener#stateChanged(eu.agno3.runtime.update.PlatformState)
     */
    @Override
    public void stateChanged ( PlatformState state ) {
        if ( state == PlatformState.STOPPING ) {
            log.debug("Sending shutdown event"); //$NON-NLS-1$
            ServerShutdownEvent ev = new ServerShutdownEvent(this.messageClient.getMessageSource());
            ev.setScopes(Arrays.asList(new AgentsEventScope()));
            try {
                this.messageClient.publishEvent(ev);
            }
            catch (
                MessagingException |
                InterruptedException e ) {
                log.warn("Failed to send shutdown event", e); //$NON-NLS-1$
            }
        }
    }

}
