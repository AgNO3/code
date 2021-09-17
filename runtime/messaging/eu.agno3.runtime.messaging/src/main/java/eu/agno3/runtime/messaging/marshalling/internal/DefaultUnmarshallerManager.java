/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.marshalling.internal;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.marshalling.MarshallingException;
import eu.agno3.runtime.messaging.marshalling.MessageUnmarshaller;
import eu.agno3.runtime.messaging.marshalling.UnmarshallerManager;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.util.osgi.AbstractClassInheritanceServiceResolver;


/**
 * @author mbechler
 * 
 */
@Component ( service = UnmarshallerManager.class )
@SuppressWarnings ( "rawtypes" )
public class DefaultUnmarshallerManager extends AbstractClassInheritanceServiceResolver<MessageUnmarshaller, Message> implements UnmarshallerManager {

    @Activate
    protected void activate ( ComponentContext context ) {
        this.setContext(context.getBundleContext());
    }


    /**
     * {@inheritDoc}
     * 
     * @throws MarshallingException
     * 
     * @see eu.agno3.runtime.messaging.marshalling.MarshallerManager#getMarshaller(java.lang.Class)
     */
    @Override
    public @NonNull <TMsg extends Message<@NonNull ? extends MessageSource>> MessageUnmarshaller<TMsg> getUnmarshaller ( Class<TMsg> msgType )
            throws MarshallingException {

        MessageUnmarshaller<TMsg> unmarshaller;
        try {
            unmarshaller = this.getServiceFor(msgType);
        }
        catch ( Exception e ) {
            throw new MarshallingException(e);
        }

        if ( unmarshaller == null ) {
            throw new MarshallingException("Failed to locate marshaller for " + msgType.getName()); //$NON-NLS-1$
        }

        return unmarshaller;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.internal.AbstractClassInheritanceServiceResolver#getClassProperty()
     */
    @Override
    protected String getClassProperty () {
        return "msgType"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.internal.AbstractClassInheritanceServiceResolver#getObjectClass()
     */
    @Override
    protected Class<Message> getObjectClass () {
        return Message.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.internal.AbstractClassInheritanceServiceResolver#getServiceClass()
     */
    @Override
    protected Class<MessageUnmarshaller> getServiceClass () {
        return MessageUnmarshaller.class;
    }

}
