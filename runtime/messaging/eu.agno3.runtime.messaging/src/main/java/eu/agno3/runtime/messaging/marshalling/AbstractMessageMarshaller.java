/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.06.2014 by mbechler
 */
package eu.agno3.runtime.messaging.marshalling;


import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSourceRegistry;
import eu.agno3.runtime.messaging.msg.MessageProperties;
import eu.agno3.runtime.messaging.msg.impl.AbstractMessage;


/**
 * @author mbechler
 * 
 * @param <T>
 */
public abstract class AbstractMessageMarshaller <T extends eu.agno3.runtime.messaging.msg.Message<@NonNull ? extends MessageSource>>
        implements MessageMarshaller<T>, MessageUnmarshaller<T> {

    private MessageSourceRegistry msgSourceRegistry;


    @Reference
    protected synchronized void setMessageSourceRegistry ( MessageSourceRegistry reg ) {
        this.msgSourceRegistry = reg;
    }


    protected synchronized void unsetMessageSourceRegistry ( MessageSourceRegistry reg ) {
        if ( this.msgSourceRegistry == reg ) {
            this.msgSourceRegistry = null;
        }
    }


    protected static Map<String, Object> extractProperties ( Message msg ) throws MarshallingException {
        Map<String, Object> properties = new HashMap<>();
        try {
            Enumeration<String> propertyNames = msg.getPropertyNames();
            while ( propertyNames != null && propertyNames.hasMoreElements() ) {
                String property = propertyNames.nextElement();
                properties.put(property, msg.getObjectProperty(property));
            }
        }
        catch ( JMSException e ) {
            throw new MarshallingException("Failed to extract message properties:", e); //$NON-NLS-1$
        }

        return properties;
    }


    protected String getMessageType ( Map<String, Object> props ) throws MarshallingException {
        Object msgType = props.get(MessageProperties.TYPE);

        if ( ! ( msgType instanceof String ) ) {
            throw new MarshallingException("msgType is not set"); //$NON-NLS-1$
        }

        return (String) msgType;
    }


    /**
     * @param msg
     * @return
     * @throws JMSException
     * @throws MarshallingException
     */
    protected int getTTL ( Message msg ) throws MarshallingException {
        try {
            return msg.getIntProperty(MessageProperties.TTL);
        }
        catch ( JMSException e ) {
            throw new MarshallingException("ttl is not set", e); //$NON-NLS-1$
        }
    }


    /**
     * @param msg
     * @return
     * @throws JMSException
     * @throws MarshallingException
     */
    protected @NonNull MessageSource getMessageSource ( Message msg ) throws MarshallingException {
        try {
            String msgSource = msg.getStringProperty(MessageProperties.SOURCE);
            MessageSource source = this.msgSourceRegistry.getMessageSource(msgSource);

            if ( source == null ) {
                throw new MarshallingException("Failed to determine message source for specification " + msgSource); //$NON-NLS-1$
            }

            return source;
        }
        catch ( JMSException e ) {
            throw new MarshallingException("Failed to get message source", e); //$NON-NLS-1$
        }
    }


    /**
     * @param msg
     * @param o
     * @throws MarshallingException
     */
    @SuppressWarnings ( "unchecked" )
    protected void restoreProperties ( Message msg, Object o ) throws MarshallingException {
        if ( o instanceof AbstractMessage ) {
            AbstractMessage<@NonNull MessageSource> abstractMessage = (AbstractMessage<@NonNull MessageSource>) o;
            abstractMessage.setTtl(this.getTTL(msg));
            abstractMessage.setOrigin(this.getMessageSource(msg));

            if ( msg instanceof org.apache.activemq.command.Message ) {
                String userId = ( (org.apache.activemq.command.Message) msg ).getUserID();
                abstractMessage.setSenderUser(userId);
            }

        }
        else {
            throw new MarshallingException("Cannot restore properties: Not a AbstractMessage"); //$NON-NLS-1$
        }
    }


    protected void saveProperties ( eu.agno3.runtime.messaging.msg.Message<@NonNull ? extends MessageSource> evt, final Message m )
            throws JMSException {
        m.setIntProperty(MessageProperties.TTL, evt.getTTL());
        String msgType = evt.getClass().getName();
        m.setStringProperty(MessageProperties.TYPE, msgType);
        m.setStringProperty(MessageProperties.SOURCE, evt.getOrigin().encode());
    }


    /**
     * 
     */
    public AbstractMessageMarshaller () {
        super();
    }

}