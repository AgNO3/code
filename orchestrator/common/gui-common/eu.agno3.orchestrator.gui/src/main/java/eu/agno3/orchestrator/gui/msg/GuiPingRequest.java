/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.06.2014 by mbechler
 */
package eu.agno3.orchestrator.gui.msg;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.gui.msg.addressing.GuiMessageSource;
import eu.agno3.orchestrator.server.component.msg.AbstractComponentPingMessage;
import eu.agno3.runtime.messaging.addressing.JMSQueueMessageTarget;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.MessageTarget;
import eu.agno3.runtime.messaging.msg.Message;


/**
 * @author mbechler
 * 
 */
public class GuiPingRequest extends AbstractComponentPingMessage<@NonNull GuiMessageSource> {

    /**
     * 
     */
    public GuiPingRequest () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public GuiPingRequest ( @NonNull GuiMessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public GuiPingRequest ( @NonNull GuiMessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public GuiPingRequest ( @NonNull GuiMessageSource origin ) {
        super(origin);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getTarget()
     */
    @Override
    public MessageTarget getTarget () {
        return new JMSQueueMessageTarget("guis-ping"); //$NON-NLS-1$
    }

}
