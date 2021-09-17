/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.base.component;


import java.util.UUID;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.server.component.ComponentConfig;
import eu.agno3.orchestrator.server.component.ComponentLifecycleListener;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.client.MessagingClient;
import eu.agno3.runtime.messaging.msg.EventMessage;


/**
 * @author mbechler
 * 
 * @param <T>
 */
public abstract class AbstractStateNotifier <T extends ComponentConfig> implements ComponentLifecycleListener<T> {

    private static final Logger log = Logger.getLogger(AbstractStateNotifier.class);

    private MessagingClient<ServerMessageSource> messagingClient;


    /**
     * 
     */
    public AbstractStateNotifier () {
        super();
    }


    @Reference
    protected synchronized void setMessagingClient ( MessagingClient<ServerMessageSource> client ) {
        this.messagingClient = client;
    }


    protected synchronized void unsetMessagingClient ( MessagingClient<ServerMessageSource> client ) {
        if ( this.messagingClient == client ) {
            this.messagingClient = null;
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.component.ComponentLifecycleListener#connecting(eu.agno3.orchestrator.server.component.ComponentConfig)
     */
    @Override
    public void connecting ( T c ) {}


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.component.ComponentLifecycleListener#connected(eu.agno3.orchestrator.server.component.ComponentConfig)
     */
    @Override
    public void connected ( T c ) {

        try {
            this.messagingClient.publishEvent(this.makeUpEvent(c.getId(), this.messagingClient.getMessageSource()));
        }
        catch (
            MessagingException |
            InterruptedException e ) {
            log.warn("Failed to publish AgentUpEvent:", e); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.component.ComponentLifecycleListener#disconnecting(eu.agno3.orchestrator.server.component.ComponentConfig)
     */
    @Override
    public void disconnecting ( T c ) {}


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.component.ComponentLifecycleListener#failed(eu.agno3.orchestrator.server.component.ComponentConfig)
     */
    @Override
    public void failed ( T c ) {
        try {
            this.messagingClient.publishEvent(this.makeIllegalConnStateEvent(c.getId(), this.messagingClient.getMessageSource()));
        }
        catch (
            MessagingException |
            InterruptedException e ) {
            log.warn("Failed to notify component of connection failure:", e); //$NON-NLS-1$
        }
    }


    /**
     * @param id
     * @param messageSource
     * @return
     */
    protected abstract @NonNull EventMessage<@NonNull ? extends MessageSource> makeUpEvent ( UUID id, @NonNull ServerMessageSource messageSource );


    /**
     * 
     * @param id
     * @param messageSource
     * @return
     */
    protected abstract @NonNull EventMessage<@NonNull ? extends MessageSource> makeIllegalConnStateEvent ( UUID id,
            @NonNull ServerMessageSource messageSource );

}