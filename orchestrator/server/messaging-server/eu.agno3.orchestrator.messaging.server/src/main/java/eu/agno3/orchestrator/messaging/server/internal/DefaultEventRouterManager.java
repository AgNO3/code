/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2013 by mbechler
 */
package eu.agno3.orchestrator.messaging.server.internal;


import javax.jms.JMSException;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.routing.EventRouter;
import eu.agno3.runtime.messaging.routing.EventRouterManager;
import eu.agno3.runtime.util.osgi.AbstractClassInheritanceServiceResolver;


/**
 * @author mbechler
 * 
 */
@Component ( service = EventRouterManager.class )
public class DefaultEventRouterManager extends AbstractClassInheritanceServiceResolver<EventRouter, EventScope> implements EventRouterManager {

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
        return "scopeClass"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.util.osgi.AbstractClassInheritanceServiceResolver#getObjectClass()
     */
    @Override
    protected Class<EventScope> getObjectClass () {
        return EventScope.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.util.osgi.AbstractClassInheritanceServiceResolver#getServiceClass()
     */
    @Override
    protected Class<EventRouter> getServiceClass () {
        return EventRouter.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws MessagingException
     * 
     * @see eu.agno3.runtime.messaging.routing.EventRouterManager#getRouterFor(eu.agno3.runtime.messaging.addressing.EventScope)
     */
    @Override
    public EventRouter getRouterFor ( EventScope scope ) throws MessagingException {

        EventRouter r;
        try {
            r = this.getServiceFor(scope.getClass());
        }
        catch ( Exception e ) {
            throw new MessagingException(e);
        }

        if ( r == null ) {
            throw new MessagingException("Unable to find any EventRouter for this event"); //$NON-NLS-1$
        }

        return r;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.routing.EventRouterManager#close()
     */
    @Override
    public void close () throws JMSException {}
}
