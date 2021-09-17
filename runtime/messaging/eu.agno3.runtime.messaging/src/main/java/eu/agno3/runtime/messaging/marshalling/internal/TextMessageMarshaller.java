/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.06.2014 by mbechler
 */
package eu.agno3.runtime.messaging.marshalling.internal;


import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSourceRegistry;
import eu.agno3.runtime.messaging.marshalling.AbstractRawMessageMarshaller;
import eu.agno3.runtime.messaging.marshalling.MessageMarshaller;
import eu.agno3.runtime.messaging.marshalling.MessageUnmarshaller;
import eu.agno3.runtime.messaging.msg.impl.TextMessage;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    MessageMarshaller.class, MessageUnmarshaller.class
}, property = {
    "msgType=eu.agno3.runtime.messaging.msg.impl.TextMessage"
} )
public class TextMessageMarshaller extends AbstractRawMessageMarshaller<TextMessage<@NonNull ? extends MessageSource>> {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.marshalling.AbstractRawMessageMarshaller#setMessageSourceRegistry(eu.agno3.runtime.messaging.addressing.MessageSourceRegistry)
     */
    @Reference
    @Override
    protected synchronized void setMessageSourceRegistry ( MessageSourceRegistry reg ) {
        super.setMessageSourceRegistry(reg);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.marshalling.AbstractRawMessageMarshaller#unsetMessageSourceRegistry(eu.agno3.runtime.messaging.addressing.MessageSourceRegistry)
     */
    @Override
    protected synchronized void unsetMessageSourceRegistry ( MessageSourceRegistry reg ) {
        super.unsetMessageSourceRegistry(reg);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.marshalling.AbstractRawMessageMarshaller#createMessage(javax.jms.Session,
     *      eu.agno3.runtime.messaging.msg.impl.AbstractRawMessage)
     */
    @Override
    protected <TMsg extends TextMessage<@NonNull ? extends MessageSource>> Message createMessage ( Session s, TMsg msg ) throws JMSException {
        String text = msg.getText();
        return s.createTextMessage(text);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws JMSException
     * 
     * @see eu.agno3.runtime.messaging.marshalling.AbstractRawMessageMarshaller#setBody(eu.agno3.runtime.messaging.msg.impl.AbstractRawMessage,
     *      javax.jms.Message)
     */
    @Override
    protected void setBody ( TextMessage<@NonNull ? extends MessageSource> deserialized, Message msg ) throws JMSException {
        deserialized.setText( ( (javax.jms.TextMessage) msg ).getText());
    }

}
