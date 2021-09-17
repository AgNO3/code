/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.events;


import java.util.HashSet;
import java.util.Set;

import javax.jms.DeliveryMode;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.addressing.scopes.ServersEventScope;
import eu.agno3.runtime.messaging.msg.EventMessage;
import eu.agno3.runtime.messaging.msg.impl.EmptyMessage;


/**
 * @author mbechler
 * 
 */
@Component ( service = EventMessage.class )
public class AgentConnectingEvent extends EmptyMessage<@NonNull AgentMessageSource> implements EventMessage<@NonNull AgentMessageSource> {

    /**
     * 
     */
    public AgentConnectingEvent () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public AgentConnectingEvent ( @NonNull AgentMessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param messageSource
     */
    public AgentConnectingEvent ( @NonNull AgentMessageSource messageSource ) {
        super(messageSource);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.EventMessage#getScopes()
     */
    @Override
    public Set<EventScope> getScopes () {
        Set<EventScope> scopes = new HashSet<>();
        scopes.add(new ServersEventScope());
        return scopes;
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
