/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.routing.internal;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.MessageTarget;
import eu.agno3.runtime.messaging.msg.ErrorResponseMessage;
import eu.agno3.runtime.messaging.msg.RequestMessage;
import eu.agno3.runtime.messaging.msg.ResponseMessage;
import eu.agno3.runtime.messaging.routing.DestinationResolverManager;
import eu.agno3.runtime.messaging.routing.MessageDestinationResolver;
import eu.agno3.runtime.util.osgi.AbstractClassInheritanceServiceResolver;


/**
 * @author mbechler
 * 
 */
@Component ( service = DestinationResolverManager.class )
public class DefaultDestinationResolverManager extends AbstractClassInheritanceServiceResolver<MessageDestinationResolver, MessageTarget>
        implements DestinationResolverManager {

    @Activate
    protected void activate ( ComponentContext ctx ) {
        this.setContext(ctx.getBundleContext());
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.util.osgi.AbstractClassInheritanceServiceResolver#getClassProperty()
     */
    @Override
    protected String getClassProperty () {
        return "targetClass"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.util.osgi.AbstractClassInheritanceServiceResolver#getObjectClass()
     */
    @Override
    protected Class<MessageTarget> getObjectClass () {
        return MessageTarget.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.util.osgi.AbstractClassInheritanceServiceResolver#getServiceClass()
     */
    @Override
    protected Class<MessageDestinationResolver> getServiceClass () {
        return MessageDestinationResolver.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws MessagingException
     * 
     * @see eu.agno3.runtime.messaging.routing.DestinationResolverManager#getResolverFor(eu.agno3.runtime.messaging.msg.RequestMessage)
     */
    @Override
    public MessageDestinationResolver getResolverFor (
            RequestMessage<@NonNull ? extends MessageSource, ResponseMessage<@NonNull ? extends MessageSource>, ErrorResponseMessage<@NonNull ? extends MessageSource>> msg )
                    throws MessagingException {
        Class<? extends MessageTarget> targetClass = msg.getTarget().getClass();

        MessageDestinationResolver r;
        try {
            r = this.getServiceFor(targetClass);
        }
        catch ( Exception e ) {
            throw new MessagingException(e);
        }

        if ( r == null ) {
            throw new MessagingException("Unable to find any MessageDestinationResolver for this message"); //$NON-NLS-1$
        }

        return r;
    }

}
