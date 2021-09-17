/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.xml.internal;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSourceRegistry;
import eu.agno3.runtime.messaging.marshalling.AbstractMessageMarshaller;
import eu.agno3.runtime.messaging.marshalling.MarshallingException;
import eu.agno3.runtime.messaging.marshalling.MessageMarshaller;
import eu.agno3.runtime.messaging.marshalling.MessageUnmarshaller;
import eu.agno3.runtime.messaging.msg.MessageProperties;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;
import eu.agno3.runtime.xml.XMLParserConfigurationException;
import eu.agno3.runtime.xml.XmlParserFactory;
import eu.agno3.runtime.xml.binding.XMLBindingException;
import eu.agno3.runtime.xml.binding.XmlMarshallingService;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    MessageMarshaller.class, MessageUnmarshaller.class
}, property = {
    "msgType=eu.agno3.runtime.messaging.xml.XmlMarshallableMessage"
} )
public class XmlMessageMarshaller extends AbstractMessageMarshaller<XmlMarshallableMessage<@NonNull ? extends MessageSource>> {

    /**
     * 
     */
    private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(XmlMessageMarshaller.class);

    private XmlMarshallingService marshallingService;
    private XmlParserFactory xmlParserFactory;


    @Reference
    protected synchronized void setXmlMarshallingService ( XmlMarshallingService s ) {
        this.marshallingService = s;
    }


    protected synchronized void unsetXmlMarshallingService ( XmlMarshallingService s ) {
        if ( this.marshallingService == s ) {
            this.marshallingService = null;
        }
    }


    @Reference
    protected synchronized void setXmlParserFactory ( XmlParserFactory f ) {
        this.xmlParserFactory = f;
    }


    protected synchronized void unsetXmlParserFactory ( XmlParserFactory f ) {
        if ( this.xmlParserFactory == f ) {
            this.xmlParserFactory = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.marshalling.AbstractMessageMarshaller#setMessageSourceRegistry(eu.agno3.runtime.messaging.addressing.MessageSourceRegistry)
     */
    @Reference
    @Override
    protected synchronized void setMessageSourceRegistry ( MessageSourceRegistry reg ) {
        super.setMessageSourceRegistry(reg);
    }


    @Override
    protected synchronized void unsetMessageSourceRegistry ( MessageSourceRegistry reg ) {
        super.unsetMessageSourceRegistry(reg);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.marshalling.MessageUnmarshaller#unmarshall(javax.jms.Message,
     *      java.lang.ClassLoader, eu.agno3.runtime.messaging.msg.Message)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public XmlMarshallableMessage<@NonNull ? extends MessageSource> unmarshall ( Message msg, ClassLoader cl,
            eu.agno3.runtime.messaging.msg.Message<@NonNull MessageSource> req ) throws MarshallingException {

        if ( ! ( msg instanceof BytesMessage ) ) {
            throw new MarshallingException("Expected BytesMessage, but got " + msg.getClass().getName()); //$NON-NLS-1$
        }

        final BytesMessage m = (BytesMessage) msg;
        Class<XmlMarshallableMessage<@NonNull ? extends MessageSource>> messageTypeClass = getMessageType(cl, m);
        Object o = unmarshallContents(m, messageTypeClass);

        if ( o == null ) {
            throw new MarshallingException("Recovered object is NULL"); //$NON-NLS-1$
        }

        if ( ! ( o instanceof XmlMarshallableMessage ) ) {
            throw new MarshallingException("Serialized object is not a XMLSerializeableMessage"); //$NON-NLS-1$
        }

        this.restoreProperties(msg, o);
        return (XmlMarshallableMessage<@NonNull ? extends MessageSource>) o;
    }


    /**
     * @param messageTypeClass
     * @return
     * @throws MarshallingException
     */
    private Object unmarshallContents ( BytesMessage m, Class<XmlMarshallableMessage<@NonNull ? extends MessageSource>> messageTypeClass )
            throws MarshallingException {
        Object o = null;
        try {
            ByteArrayOutputStream debugBuf = null;
            if ( log.isTraceEnabled() ) {
                debugBuf = new ByteArrayOutputStream();
            }

            try ( InputStream i = new DebuggingBytesMessageStreamReader(m, debugBuf) ) {
                XMLStreamReader reader = this.xmlParserFactory.createStreamReader(i);

                if ( debugBuf != null ) {
                    log.trace("Unmarshalling message:" + System.lineSeparator() + debugBuf.toString(UTF_8)); //$NON-NLS-1$
                }

                o = this.marshallingService.unmarshall(messageTypeClass, reader);
            }
        }
        catch (
            JMSException |
            XMLBindingException |
            XMLParserConfigurationException |
            IOException e ) {
            throw new MarshallingException("Failed to unmarshall message:", e); //$NON-NLS-1$
        }
        return o;
    }


    /**
     * @param cl
     * @param m
     * @return
     * @throws MarshallingException
     */
    @SuppressWarnings ( "unchecked" )
    private static Class<XmlMarshallableMessage<@NonNull ? extends MessageSource>> getMessageType ( ClassLoader cl, final BytesMessage m )
            throws MarshallingException {
        Class<XmlMarshallableMessage<@NonNull ? extends MessageSource>> messageTypeClass;
        try {
            String type = m.getStringProperty(MessageProperties.TYPE);
            messageTypeClass = (Class<XmlMarshallableMessage<@NonNull ? extends MessageSource>>) cl.loadClass(type);
        }
        catch (
            JMSException |
            ClassNotFoundException e ) {
            throw new MarshallingException("Failed to obtain message type:", e); //$NON-NLS-1$
        }
        return messageTypeClass;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @throws JMSException
     *
     * @see eu.agno3.runtime.messaging.marshalling.MessageMarshaller#marshall(javax.jms.Session,
     *      eu.agno3.runtime.messaging.msg.Message)
     */
    @Override
    public <TMsg extends XmlMarshallableMessage<@NonNull ? extends MessageSource>> Message marshall ( Session s, TMsg evt )
            throws MarshallingException, JMSException {

        try {
            final BytesMessage m = s.createBytesMessage();
            saveProperties(evt, m);
            try ( OutputStream o = new ByteMessageOutputStream(m) ) {
                XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(o);
                this.marshallingService.marshall(evt, writer);
            }

            return m;
        }
        catch ( JMSException e ) {
            throw e;
        }
        catch (
            XMLStreamException |
            FactoryConfigurationError |
            XMLBindingException |
            IOException e ) {
            throw new MarshallingException("Failed to marshall message:", e); //$NON-NLS-1$
        }
    }
}
