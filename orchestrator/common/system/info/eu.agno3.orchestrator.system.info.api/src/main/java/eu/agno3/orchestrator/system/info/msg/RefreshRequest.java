/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.msg;


import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.agent.msg.AbstractAgentRequestMessage;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.xml.DefaultXmlErrorResponseMessage;


/**
 * @author mbechler
 * 
 */
public class RefreshRequest extends AbstractAgentRequestMessage<@NonNull MessageSource, RefreshResponse, DefaultXmlErrorResponseMessage> {

    /**
     * 
     */
    public RefreshRequest () {
        super();
    }


    /**
     * @param targetId
     * @param origin
     * @param replyTo
     */
    public RefreshRequest ( @NonNull UUID targetId, @NonNull MessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(targetId, origin, replyTo);
    }


    /**
     * @param targetId
     * @param origin
     */
    public RefreshRequest ( @NonNull UUID targetId, @NonNull MessageSource origin ) {
        super(targetId, origin);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getResponseType()
     */
    @Override
    public Class<RefreshResponse> getResponseType () {
        return RefreshResponse.class;
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
     * @see eu.agno3.runtime.messaging.msg.impl.AbstractMessage#hasResponse()
     */
    @Override
    public boolean hasResponse () {
        return false;
    }
}
