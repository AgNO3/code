/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.10.2016 by mbechler
 */
package eu.agno3.orchestrator.config.model.msg;


import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.agent.msg.AbstractAgentRequestMessage;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.xml.DefaultXmlErrorResponseMessage;


/**
 * @author mbechler
 *
 */
public class AgentServicesRequest
        extends AbstractAgentRequestMessage<@NonNull ServerMessageSource, AgentServicesResponse, DefaultXmlErrorResponseMessage> {

    /**
     * 
     */
    public AgentServicesRequest () {
        super();
    }


    /**
     * @param targetId
     * @param origin
     * @param replyTo
     */
    public AgentServicesRequest ( @NonNull UUID targetId, @NonNull ServerMessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(targetId, origin, replyTo);
    }


    /**
     * @param targetId
     * @param origin
     */
    public AgentServicesRequest ( @NonNull UUID targetId, @NonNull ServerMessageSource origin ) {
        super(targetId, origin);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getResponseType()
     */
    @Override
    public Class<AgentServicesResponse> getResponseType () {
        return AgentServicesResponse.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getErrorResponseType()
     */
    @Override
    public Class<DefaultXmlErrorResponseMessage> getErrorResponseType () {
        return DefaultXmlErrorResponseMessage.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.msg.AbstractAgentRequestMessage#getReplyTimeout()
     */
    @Override
    public long getReplyTimeout () {
        return 5000;
    }
}
