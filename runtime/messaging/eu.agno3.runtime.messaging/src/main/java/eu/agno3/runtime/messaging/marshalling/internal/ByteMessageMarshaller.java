/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.06.2014 by mbechler
 */
package eu.agno3.runtime.messaging.marshalling.internal;


import javax.jms.BytesMessage;
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
import eu.agno3.runtime.messaging.msg.impl.ByteMessage;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    MessageMarshaller.class, MessageUnmarshaller.class
}, property = {
    "msgType=eu.agno3.runtime.messaging.msg.impl.ByteMessage"
} )
public class ByteMessageMarshaller extends AbstractRawMessageMarshaller<ByteMessage<@NonNull ? extends MessageSource>> {

    private static final String BODY_LENGTH_LIMIT = "Body length exceeds maximum size "; //$NON-NLS-1$
    private static final long MAX_BODY_LENGTH = 512 * 1024;


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
    protected <TMsg extends ByteMessage<@NonNull ? extends MessageSource>> Message createMessage ( Session s, TMsg msg ) throws JMSException {
        BytesMessage m = s.createBytesMessage();
        byte[] bytes = msg.getBytes();
        if ( bytes.length >= MAX_BODY_LENGTH ) {
            throw new JMSException(BODY_LENGTH_LIMIT + MAX_BODY_LENGTH);
        }
        m.writeBytes(bytes);
        return m;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.marshalling.AbstractRawMessageMarshaller#setBody(eu.agno3.runtime.messaging.msg.impl.AbstractRawMessage,
     *      javax.jms.Message)
     */
    @Override
    protected void setBody ( ByteMessage<@NonNull ? extends MessageSource> deserialized, Message msg ) throws JMSException {
        BytesMessage m = (BytesMessage) msg;

        m.reset();

        long bodyLength = m.getBodyLength();
        if ( bodyLength >= MAX_BODY_LENGTH ) {
            throw new JMSException(BODY_LENGTH_LIMIT + MAX_BODY_LENGTH);
        }

        byte[] body = new byte[(int) m.getBodyLength()];
        m.readBytes(body);
        deserialized.setBytes(body);
    }

}
