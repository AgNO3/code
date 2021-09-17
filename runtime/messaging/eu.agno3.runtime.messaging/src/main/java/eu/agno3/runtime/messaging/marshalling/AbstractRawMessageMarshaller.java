/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.06.2014 by mbechler
 */
package eu.agno3.runtime.messaging.marshalling;


import java.util.Map;
import java.util.Map.Entry;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.impl.AbstractRawMessage;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public abstract class AbstractRawMessageMarshaller <T extends AbstractRawMessage<@NonNull ? extends MessageSource>>
        extends AbstractMessageMarshaller<@NonNull T> {

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
    public <TMsg extends T> Message marshall ( Session s, TMsg msg ) throws MarshallingException, JMSException {
        Message m = this.createMessage(s, msg);

        Map<String, Object> props = msg.getProperties();
        for ( Entry<String, Object> prop : props.entrySet() ) {
            m.setObjectProperty(prop.getKey(), prop.getValue());
        }

        this.saveProperties(msg, m);
        return m;
    }


    /**
     * @param s
     * @param msg
     * @throws JMSException
     */
    protected abstract <TMsg extends T> Message createMessage ( Session s, TMsg msg ) throws JMSException;


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.marshalling.MessageUnmarshaller#unmarshall(javax.jms.Message,
     *      java.lang.ClassLoader, eu.agno3.runtime.messaging.msg.Message)
     */
    @Override
    public @NonNull T unmarshall ( Message msg, ClassLoader targetClassLoader, eu.agno3.runtime.messaging.msg.Message<@NonNull MessageSource> req )
            throws MarshallingException {
        Map<String, Object> props = extractProperties(msg);
        String msgType = getMessageType(props);

        try {
            T deserialized = this.createObject(targetClassLoader, msgType);
            this.restoreProperties(msg, deserialized);
            deserialized.setProperties(props);
            this.setBody(deserialized, msg);
            return deserialized;
        }
        catch ( JMSException e ) {
            throw new MarshallingException("Failed to unmarshall message", e); //$NON-NLS-1$
        }

    }


    /**
     * @param deserialized
     * @param msg
     * @throws JMSException
     */
    protected abstract void setBody ( T deserialized, Message msg ) throws JMSException;


    /**
     * @param targetClassLoader
     * @param msgType
     * @return
     * @throws MarshallingException
     */
    @SuppressWarnings ( "unchecked" )
    private T createObject ( ClassLoader targetClassLoader, String msgType ) throws MarshallingException {

        try {
            Class<?> cl = targetClassLoader.loadClass(msgType);

            if ( !AbstractRawMessage.class.isAssignableFrom(cl) ) {
                throw new MarshallingException("Illegal message type, not a AbstractRawMessage"); //$NON-NLS-1$
            }
            return (T) cl.newInstance();
        }
        catch ( ClassNotFoundException e ) {
            throw new MarshallingException("Message type not found", e); //$NON-NLS-1$
        }
        catch (
            InstantiationException |
            IllegalAccessException |
            SecurityException e ) {
            throw new MarshallingException("Illegal message type, could not instantiate " + msgType, e); //$NON-NLS-1$
        }

    }

}
