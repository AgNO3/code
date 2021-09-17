/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.msg;


import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageTarget;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.MessageTarget;
import eu.agno3.runtime.messaging.msg.ErrorResponseMessage;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.msg.RequestMessage;
import eu.agno3.runtime.messaging.msg.ResponseMessage;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 * @param <T>
 * @param <TReply>
 * @param <TError>
 * 
 */
public abstract class AbstractAgentRequestMessage <@NonNull T extends MessageSource, TReply extends ResponseMessage<@NonNull ? extends MessageSource>, TError extends ErrorResponseMessage<@NonNull ? extends MessageSource>>
        extends XmlMarshallableMessage<T> implements RequestMessage<T, TReply, TError> {

    private static final long DEFAULT_AGENT_REQUEST_TIMEOUT = 1000;

    private UUID targetId;


    /**
     * 
     */
    public AbstractAgentRequestMessage () {}


    /**
     * @param targetId
     * @param origin
     * @param replyTo
     */
    public AbstractAgentRequestMessage ( @NonNull UUID targetId, T origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
        this.targetId = targetId;
    }


    /**
     * @param targetId
     * @param origin
     */
    public AbstractAgentRequestMessage ( @NonNull UUID targetId, T origin ) {
        super(origin);
        this.targetId = targetId;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getReplyTimeout()
     */
    @Override
    public long getReplyTimeout () {
        return DEFAULT_AGENT_REQUEST_TIMEOUT;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getTarget()
     */
    @Override
    public MessageTarget getTarget () {
        UUID agentId = this.targetId;
        if ( agentId == null ) {
            throw new IllegalStateException();
        }
        return new AgentMessageTarget(agentId);
    }
}
