/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.server.internal;


import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.config.AgentConfig;
import eu.agno3.orchestrator.agent.events.AgentIllegalConnStateEvent;
import eu.agno3.orchestrator.agent.events.AgentUpEvent;
import eu.agno3.orchestrator.agent.server.AgentLifecycleListener;
import eu.agno3.orchestrator.server.base.component.AbstractStateNotifier;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.client.MessagingClient;


/**
 * @author mbechler
 * 
 */
@Component ( service = AgentLifecycleListener.class )
public class AgentStateNotifier extends AbstractStateNotifier<AgentConfig> implements AgentLifecycleListener {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.base.internal.AbstractStateNotifier#setMessagingClient(eu.agno3.orchestrator.messaging.client.MessagingClient)
     */
    @Reference
    @Override
    protected synchronized void setMessagingClient ( MessagingClient<ServerMessageSource> client ) {
        super.setMessagingClient(client);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.base.internal.AbstractStateNotifier#unsetMessagingClient(eu.agno3.orchestrator.messaging.client.MessagingClient)
     */
    @Override
    protected synchronized void unsetMessagingClient ( MessagingClient<ServerMessageSource> client ) {
        super.unsetMessagingClient(client);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.base.component.AbstractStateNotifier#connected(eu.agno3.orchestrator.server.component.ComponentConfig)
     */
    @Override
    public void connected ( AgentConfig c ) {
        super.connected(c);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.base.component.AbstractStateNotifier#disconnecting(eu.agno3.orchestrator.server.component.ComponentConfig)
     */
    @Override
    public void disconnecting ( AgentConfig c ) {
        super.disconnecting(c);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.base.component.AbstractStateNotifier#failed(eu.agno3.orchestrator.server.component.ComponentConfig)
     */
    @Override
    public void failed ( AgentConfig c ) {
        super.failed(c);
    }


    /**
     * @param id
     * @param messageSource
     * @return
     */
    @Override
    protected @NonNull AgentIllegalConnStateEvent makeIllegalConnStateEvent ( UUID id, @NonNull ServerMessageSource messageSource ) {
        return new AgentIllegalConnStateEvent(id, messageSource);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.base.internal.AbstractStateNotifier#makeUpEvent(java.util.UUID,
     *      eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource)
     */
    @Override
    protected @NonNull AgentUpEvent makeUpEvent ( UUID id, @NonNull ServerMessageSource messageSource ) {
        return new AgentUpEvent(id, messageSource);
    }

}
