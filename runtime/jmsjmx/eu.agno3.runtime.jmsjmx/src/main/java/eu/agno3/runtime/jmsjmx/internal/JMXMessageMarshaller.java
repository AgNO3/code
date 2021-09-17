/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.jmsjmx.internal;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.xml.stream.FactoryConfigurationError;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.jmsjmx.AbstractJMXMessage;
import eu.agno3.runtime.jmsjmx.AbstractJMXRequest;
import eu.agno3.runtime.jmsjmx.JMXErrorResponse;
import eu.agno3.runtime.jmsjmx.JMXResponse;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSourceRegistry;
import eu.agno3.runtime.messaging.marshalling.AbstractMessageMarshaller;
import eu.agno3.runtime.messaging.marshalling.MarshallingException;
import eu.agno3.runtime.messaging.marshalling.MessageMarshaller;
import eu.agno3.runtime.messaging.marshalling.MessageUnmarshaller;
import eu.agno3.runtime.messaging.msg.MessageProperties;
import eu.agno3.runtime.util.serialization.FilteredObjectInputStream;
import eu.agno3.runtime.xml.XmlParserFactory;
import eu.agno3.runtime.xml.binding.XmlMarshallingService;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    MessageMarshaller.class, MessageUnmarshaller.class
}, property = {
    "msgType=eu.agno3.runtime.jmsjmx.AbstractJMXMessage"
} )
public class JMXMessageMarshaller extends AbstractMessageMarshaller<AbstractJMXMessage<@NonNull ? extends MessageSource>> {

    private static final Logger log = Logger.getLogger(JMXMessageMarshaller.class);

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
    @Override
    public AbstractJMXMessage<@NonNull ? extends MessageSource> unmarshall ( Message msg, ClassLoader cl,
            eu.agno3.runtime.messaging.msg.Message<@NonNull MessageSource> req ) throws MarshallingException {

        if ( ! ( msg instanceof BytesMessage ) ) {
            throw new MarshallingException("Expected BytesMessage, but got " + msg.getClass().getName()); //$NON-NLS-1$
        }

        final BytesMessage m = (BytesMessage) msg;

        // override classloader for parsing responses as the response type is generic
        ClassLoader useCl = req != null ? req.getClass().getClassLoader() : cl;
        Class<AbstractJMXMessage<@NonNull ? extends MessageSource>> messageTypeClass = getMessageType(useCl, m);
        AbstractJMXMessage<@NonNull MessageSource> o = unmarshallContents(m, messageTypeClass, useCl);

        if ( o == null ) {
            throw new MarshallingException("Recovered object is NULL"); //$NON-NLS-1$
        }

        this.restoreProperties(msg, o);
        return o;
    }


    /**
     * @param messageTypeClass
     * @param cl
     * @return
     * @throws MarshallingException
     */
    private AbstractJMXMessage<@NonNull MessageSource> unmarshallContents ( BytesMessage m,
            Class<AbstractJMXMessage<@NonNull ? extends MessageSource>> messageTypeClass, ClassLoader cl ) throws MarshallingException {
        AbstractJMXMessage<@NonNull MessageSource> o = null;

        byte[] data;
        try {
            data = new byte[(int) m.getBodyLength()];
            m.readBytes(data);
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("received %d bytes", data.length)); //$NON-NLS-1$
            }
        }
        catch ( JMSException e ) {
            throw new MarshallingException("Failed to get message body", e); //$NON-NLS-1$
        }

        try ( ByteArrayInputStream is = new ByteArrayInputStream(data);
              ObjectInputStream ois = new FilteredObjectInputStream(is, cl) ) {

            if ( AbstractJMXRequest.class.isAssignableFrom(messageTypeClass) ) {
                o = unmarshallRequest(m, ois, messageTypeClass, cl);
            }
            else if ( JMXResponse.class.equals(messageTypeClass) ) {
                o = unmarshallResponse(m, ois, cl);
            }
            else if ( JMXErrorResponse.class.isAssignableFrom(messageTypeClass) ) {
                o = unmarshallError(m, ois, cl);
            }
            else {
                throw new MarshallingException("Unexpected message type " + messageTypeClass.getName()); //$NON-NLS-1$
            }
        }
        catch ( IOException e ) {
            throw new MarshallingException("Failed to unmarshall message", e); //$NON-NLS-1$
        }

        return o;
    }


    /**
     * @param m
     * @param messageTypeClass
     * @param o
     * @return
     * @throws MarshallingException
     */
    @SuppressWarnings ( "unchecked" )
    private AbstractJMXMessage<@NonNull MessageSource> unmarshallRequest ( BytesMessage m, ObjectInputStream is,
            Class<AbstractJMXMessage<@NonNull ? extends MessageSource>> messageTypeClass, ClassLoader cl ) throws MarshallingException {
        try {
            AbstractJMXRequest<@NonNull ? extends MessageSource, ? extends JMXErrorResponse> req = (AbstractJMXRequest<@NonNull ? extends MessageSource, ? extends JMXErrorResponse>) messageTypeClass
                    .newInstance();

            int type = m.getIntProperty("jmx_type"); //$NON-NLS-1$
            if ( type == AbstractJMXRequest.INVOKE ) {
                req.setName(m.getStringProperty("jmx_name")); //$NON-NLS-1$
                req.setSignature(StringUtils.split(m.getStringProperty("jmx_signature"), '|')); //$NON-NLS-1$
                req.setParams(JMXMarshaller.unmarshallObjects(is, req.getSignature(), this.marshallingService, cl));
            }
            else if ( type == AbstractJMXRequest.GET_ATTR ) {
                req.setName(m.getStringProperty("jmx_name")); //$NON-NLS-1$
            }
            else if ( type == AbstractJMXRequest.SET_ATTR ) {
                req.setName(m.getStringProperty("jmx_name")); //$NON-NLS-1$
                String objType = m.getStringProperty("jmx_signature"); //$NON-NLS-1$
                req.setSignature(new String[] {
                    objType
                });
                req.setParams(new Object[] {
                    JMXMarshaller.unmarshallObject(is, objType, this.marshallingService, cl)
                });
            }
            else {
                throw new MarshallingException("Unknown message type " + type); //$NON-NLS-1$
            }

            req.setType(type);
            req.restoreExtraProperties(m);

            return (AbstractJMXMessage<@NonNull MessageSource>) req;
        }
        catch (
            JMSException |
            InstantiationException |
            IllegalAccessException |
            ClassNotFoundException |
            IOException e ) {
            throw new MarshallingException("Failed to restore properties", e); //$NON-NLS-1$
        }
    }


    /**
     * @param m
     * @param cl
     * @return
     * @throws MarshallingExcep1tion
     * @throws JMSException
     */
    private AbstractJMXMessage<@NonNull MessageSource> unmarshallResponse ( BytesMessage m, ObjectInputStream is, ClassLoader cl )
            throws MarshallingException {
        JMXResponse resp = new JMXResponse();
        String signature;
        try {
            signature = m.getStringProperty("jmx_signature"); //$NON-NLS-1$

            if ( !StringUtils.isBlank(signature) ) {
                resp.setResponseObject(JMXMarshaller.unmarshallObject(is, signature, this.marshallingService, cl));
            }

            return resp;
        }
        catch (
            JMSException |
            ClassNotFoundException |
            IOException e ) {
            throw new MarshallingException("Failed to read response", e); //$NON-NLS-1$
        }

    }


    /**
     * @param m
     * @param is
     * @param cl
     * @return
     * @throws MarshallingException
     */
    private static AbstractJMXMessage<@NonNull MessageSource> unmarshallError ( BytesMessage m, ObjectInputStream is, ClassLoader cl )
            throws MarshallingException {
        JMXErrorResponse resp = new JMXErrorResponse();
        try {
            Object readObject = is.readObject();
            if ( ! ( readObject instanceof Throwable ) ) {
                throw new MarshallingException("JMXErrorResponse does not contain a throwable"); //$NON-NLS-1$
            }
            resp.setThrowable((Throwable) readObject);

            return resp;
        }
        catch (
            IOException |
            ClassNotFoundException e ) {
            throw new MarshallingException("Failed to read error message", e); //$NON-NLS-1$
        }
    }


    /**
     * @param cl
     * @param m
     * @return
     * @throws MarshallingException
     */
    @SuppressWarnings ( "unchecked" )
    private static Class<AbstractJMXMessage<@NonNull ? extends MessageSource>> getMessageType ( ClassLoader cl, final BytesMessage m )
            throws MarshallingException {
        Class<AbstractJMXMessage<@NonNull ? extends MessageSource>> messageTypeClass;
        try {
            String type = m.getStringProperty(MessageProperties.TYPE);
            messageTypeClass = (Class<AbstractJMXMessage<@NonNull ? extends MessageSource>>) cl.loadClass(type);
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
    public <TMsg extends AbstractJMXMessage<@NonNull ? extends MessageSource>> Message marshall ( Session s, TMsg msg )
            throws MarshallingException, JMSException {
        try {
            final BytesMessage m = s.createBytesMessage();
            try ( ByteMessageOutputStream bos = new ByteMessageOutputStream(m, log.isTraceEnabled());
                  ObjectOutputStream oos = new ObjectOutputStream(bos) ) {
                if ( msg instanceof AbstractJMXRequest ) {
                    AbstractJMXRequest<@NonNull ?, ?> req = (AbstractJMXRequest<@NonNull ?, ?>) msg;

                    m.setIntProperty("jmx_type", req.getType()); //$NON-NLS-1$
                    if ( req.getType() == AbstractJMXRequest.INVOKE ) {
                        m.setStringProperty("jmx_name", req.getName()); //$NON-NLS-1$
                        m.setStringProperty("jmx_signature", StringUtils.join(req.getSignature(), '|')); //$NON-NLS-1$
                        JMXMarshaller.marshallObjects(oos, this.marshallingService, req.getParams());
                    }
                    else if ( req.getType() == AbstractJMXRequest.GET_ATTR ) {
                        m.setStringProperty("jmx_name", req.getName()); //$NON-NLS-1$
                    }
                    else if ( req.getType() == AbstractJMXRequest.SET_ATTR ) {
                        m.setStringProperty("jmx_name", req.getName()); //$NON-NLS-1$
                        if ( req.getParams() == null || req.getParams().length != 1 ) {
                            throw new MarshallingException("Not a valid set request"); //$NON-NLS-1$
                        }
                        m.setStringProperty("jmx_signature", JMXMarshaller.marshallObject(oos, this.marshallingService, req.getParams()[ 0 ])); //$NON-NLS-1$
                    }
                    else {
                        throw new MarshallingException("Unknown message type"); //$NON-NLS-1$
                    }

                    req.saveExtraProperties(m);
                }
                else if ( msg instanceof JMXResponse ) {
                    Object responseObject = ( (JMXResponse) msg ).getResponseObject();
                    if ( responseObject != null ) {
                        m.setStringProperty("jmx_signature", JMXMarshaller.marshallObject(oos, this.marshallingService, responseObject)); //$NON-NLS-1$
                    }
                }
                else if ( msg instanceof JMXErrorResponse ) {
                    oos.writeObject( ( (JMXErrorResponse) msg ).getThrowable());
                }

                oos.close();
            }

            saveProperties(msg, m);
            return m;
        }
        catch (
            FactoryConfigurationError |
            IOException e ) {
            throw new MarshallingException("Failed to marshall message:", e); //$NON-NLS-1$
        }
    }
}
