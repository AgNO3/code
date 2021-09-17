/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.events;


import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.agent.config.AgentConfig;
import eu.agno3.orchestrator.agent.msg.addressing.AgentEventScope;
import eu.agno3.orchestrator.server.component.msg.ComponentConfigUpdatedEvent;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.addressing.scopes.ServersEventScope;
import eu.agno3.runtime.messaging.msg.EventMessage;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 * 
 */
@Component ( service = EventMessage.class )
public class AgentConfigUpdatedEvent extends XmlMarshallableMessage<@NonNull ServerMessageSource> implements ComponentConfigUpdatedEvent<AgentConfig> {

    private AgentConfig config;


    /**
     * 
     */
    public AgentConfigUpdatedEvent () {
        super();
    }


    /**
     * @param config
     * @param origin
     */
    public AgentConfigUpdatedEvent ( AgentConfig config, @NonNull ServerMessageSource origin ) {
        super(origin);
        this.config = config;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.EventMessage#getScopes()
     */
    @Override
    public Set<EventScope> getScopes () {
        Set<EventScope> scopes = new HashSet<>();
        scopes.add(new AgentEventScope(this.config.getId()));
        scopes.add(new ServersEventScope());
        return scopes;
    }


    /**
     * @return the config
     */
    @Override
    public AgentConfig getConfig () {
        return this.config;
    }


    /**
     * @param config
     *            the config to set
     */
    public void setConfig ( AgentConfig config ) {
        this.config = config;
    }
}
