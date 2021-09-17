/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.events;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.jms.DeliveryMode;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.orchestrator.agent.msg.addressing.AgentsEventScope;
import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.addressing.scopes.ServersEventScope;
import eu.agno3.runtime.messaging.msg.EventMessage;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 * 
 */
@Component ( service = EventMessage.class )
public class AgentDownEvent extends XmlMarshallableMessage<@NonNull AgentMessageSource> implements EventMessage<@NonNull AgentMessageSource> {

    private UUID agentId;


    /**
     * 
     */
    public AgentDownEvent () {
        super();
    }


    /**
     * @param agentId
     * @param origin
     */
    public AgentDownEvent ( UUID agentId, @NonNull AgentMessageSource origin ) {
        super(origin);
        this.agentId = agentId;
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
        scopes.add(new AgentsEventScope());
        scopes.add(new ServersEventScope());
        return scopes;
    }


    /**
     * @return the agentId
     */
    public UUID getAgentId () {
        return this.agentId;
    }


    /**
     * @param agentId
     *            the agentId to set
     */
    public void setAgentId ( UUID agentId ) {
        this.agentId = agentId;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.impl.AbstractMessage#getDeliveryTTL()
     */
    @Override
    public long getDeliveryTTL () {
        return 1000;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.impl.AbstractMessage#getDeliveryMode()
     */
    @Override
    public int getDeliveryMode () {
        return DeliveryMode.NON_PERSISTENT;
    }
}
