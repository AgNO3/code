/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.msg;


import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.scopes.ServersEventScope;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractAgentEvent extends XmlMarshallableMessage<@NonNull AgentMessageSource> {

    /**
     * 
     */
    public AbstractAgentEvent () {}


    /**
     * @param origin
     * @param replyTo
     */
    public AbstractAgentEvent ( @NonNull AgentMessageSource origin, Message<@NonNull MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public AbstractAgentEvent ( @NonNull AgentMessageSource origin ) {
        super(origin);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.xml.XmlMarshallableMessage#getOrigin()
     */
    @Override
    public @NonNull AgentMessageSource getOrigin () {
        return super.getOrigin();
    }


    /**
     * 
     * @return the event scopes
     */
    public Set<EventScope> getScopes () {
        Set<EventScope> scopes = new HashSet<>();
        scopes.add(new ServersEventScope());
        return scopes;
    }

}
