/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.server.internal;


import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.config.AgentConfig;
import eu.agno3.orchestrator.agent.msg.addressing.AgentEventScope;
import eu.agno3.orchestrator.agent.server.AgentLifecycleListener;
import eu.agno3.orchestrator.server.base.component.AbstractEventPumpManager;
import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.events.EventTypeRegistry;
import eu.agno3.runtime.messaging.listener.MessageListenerFactory;
import eu.agno3.runtime.messaging.marshalling.UnmarshallerManager;
import eu.agno3.runtime.messaging.routing.EventRouterManager;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    AgentLifecycleListener.class
}, immediate = true )
public class AgentEventPumpManager extends AbstractEventPumpManager<AgentConfig> implements AgentLifecycleListener {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.base.component.AbstractEventPumpManager#activate(org.osgi.service.component.ComponentContext)
     */
    @Override
    @Activate
    protected void activate ( ComponentContext context ) throws JMSException {
        super.activate(context);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.base.internal.AbstractEventPumpManager#deactivate(org.osgi.service.component.ComponentContext)
     */
    @Override
    @Deactivate
    protected void deactivate ( ComponentContext context ) throws JMSException {
        super.deactivate(context);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.base.internal.AbstractEventPumpManager#setEventRouterManager(eu.agno3.orchestrator.messaging.routing.EventRouterManager)
     */
    @Reference
    @Override
    protected synchronized void setEventRouterManager ( EventRouterManager erm ) {
        super.setEventRouterManager(erm);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.base.internal.AbstractEventPumpManager#unsetEventRouterManager(eu.agno3.orchestrator.messaging.routing.EventRouterManager)
     */
    @Override
    protected synchronized void unsetEventRouterManager ( EventRouterManager erm ) {
        super.unsetEventRouterManager(erm);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.base.internal.AbstractEventPumpManager#setEventTypeRegistry(eu.agno3.orchestrator.messaging.events.EventTypeRegistry)
     */
    @Reference
    @Override
    protected synchronized void setEventTypeRegistry ( EventTypeRegistry etr ) {
        super.setEventTypeRegistry(etr);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.base.internal.AbstractEventPumpManager#unsetEventTypeRegistry(eu.agno3.orchestrator.messaging.events.EventTypeRegistry)
     */
    @Override
    protected synchronized void unsetEventTypeRegistry ( EventTypeRegistry etr ) {
        super.unsetEventTypeRegistry(etr);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.base.internal.AbstractEventPumpManager#setMessageListenerFactory(eu.agno3.orchestrator.messaging.listener.MessageListenerFactory)
     */
    @Reference
    @Override
    protected synchronized void setMessageListenerFactory ( MessageListenerFactory mlf ) {
        super.setMessageListenerFactory(mlf);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.base.internal.AbstractEventPumpManager#unsetMessageListenerFactory(eu.agno3.orchestrator.messaging.listener.MessageListenerFactory)
     */
    @Override
    protected synchronized void unsetMessageListenerFactory ( MessageListenerFactory mlf ) {
        super.unsetMessageListenerFactory(mlf);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.base.component.AbstractEventPumpManager#setConnectionFactory(javax.jms.ConnectionFactory)
     */
    @Override
    @Reference
    protected synchronized void setConnectionFactory ( ConnectionFactory cf ) {
        super.setConnectionFactory(cf);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.base.component.AbstractEventPumpManager#unsetConnectionFactory(javax.jms.ConnectionFactory)
     */
    @Override
    protected synchronized void unsetConnectionFactory ( ConnectionFactory cf ) {
        super.unsetConnectionFactory(cf);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.base.internal.AbstractEventPumpManager#setUnmarshallerManager(eu.agno3.orchestrator.messaging.marshalling.UnmarshallerManager)
     */
    @Reference
    @Override
    protected synchronized void setUnmarshallerManager ( UnmarshallerManager um ) {
        super.setUnmarshallerManager(um);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.base.internal.AbstractEventPumpManager#unsetUnmarshallerManager(eu.agno3.orchestrator.messaging.marshalling.UnmarshallerManager)
     */
    @Override
    protected synchronized void unsetUnmarshallerManager ( UnmarshallerManager um ) {
        super.unsetUnmarshallerManager(um);
    }


    /**
     * @param c
     * @return
     */
    @Override
    protected EventScope getListeningScope ( AgentConfig c ) {
        return new AgentEventScope(c.getId());
    }

}
