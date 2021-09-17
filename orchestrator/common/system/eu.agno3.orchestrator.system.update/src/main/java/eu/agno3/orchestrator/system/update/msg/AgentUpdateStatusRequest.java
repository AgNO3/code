/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.12.2015 by mbechler
 */
package eu.agno3.orchestrator.system.update.msg;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageTarget;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.addressing.MessageTarget;
import eu.agno3.runtime.messaging.msg.RequestMessage;
import eu.agno3.runtime.messaging.xml.DefaultXmlErrorResponseMessage;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 *
 */
public class AgentUpdateStatusRequest extends XmlMarshallableMessage<@NonNull ServerMessageSource>
        implements RequestMessage<@NonNull ServerMessageSource, AgentUpdateStatusResponse, DefaultXmlErrorResponseMessage> {

    private AgentMessageTarget target;


    /**
     * 
     */
    public AgentUpdateStatusRequest () {}


    /**
     * @param tgt
     * 
     */
    public AgentUpdateStatusRequest ( AgentMessageTarget tgt ) {
        this.target = tgt;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getResponseType()
     */
    @Override
    public Class<AgentUpdateStatusResponse> getResponseType () {
        return AgentUpdateStatusResponse.class;
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
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getReplyTimeout()
     */
    @Override
    public long getReplyTimeout () {
        return 500;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getTarget()
     */
    @Override
    public MessageTarget getTarget () {
        return this.target;
    }

}
