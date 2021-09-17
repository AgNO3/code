/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.09.2013 by mbechler
 */
package eu.agno3.orchestrator.gui.config;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.gui.msg.addressing.GuiMessageSource;
import eu.agno3.orchestrator.server.component.msg.ComponentConfigRequest;
import eu.agno3.runtime.messaging.addressing.DefaultMessageTarget;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.MessageTarget;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.xml.DefaultXmlErrorResponseMessage;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 * 
 */
public class GuiConfigRequest extends XmlMarshallableMessage<@NonNull GuiMessageSource> implements
        ComponentConfigRequest<@NonNull GuiMessageSource, @NonNull GuiConfig, GuiConfigResponse, DefaultXmlErrorResponseMessage> {

    /**
     * 
     */
    private static final int GUI_CONFIG_REQUEST_TIMEOUT = 5000;


    /**
     * 
     */
    public GuiConfigRequest () {}


    /**
     * @param origin
     * @param replyTo
     */
    public GuiConfigRequest ( @NonNull GuiMessageSource origin, Message<@NonNull MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public GuiConfigRequest ( @NonNull GuiMessageSource origin ) {
        super(origin);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getResponseType()
     */
    @Override
    public Class<GuiConfigResponse> getResponseType () {
        return GuiConfigResponse.class;
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
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getTarget()
     */
    @Override
    public MessageTarget getTarget () {
        return new DefaultMessageTarget();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getReplyTimeout()
     */
    @Override
    public long getReplyTimeout () {
        return GUI_CONFIG_REQUEST_TIMEOUT;
    }
}
