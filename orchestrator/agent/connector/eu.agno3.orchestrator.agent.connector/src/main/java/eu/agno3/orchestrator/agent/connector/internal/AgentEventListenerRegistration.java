/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.08.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.connector.internal;


import javax.jms.JMSException;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.config.AgentConfig;
import eu.agno3.runtime.messaging.listener.AbstractEventListenerRegistration;
import eu.agno3.runtime.messaging.listener.DestinationStrategy;
import eu.agno3.runtime.messaging.listener.EventListener;
import eu.agno3.runtime.messaging.listener.FixedTopicStrategy;
import eu.agno3.runtime.messaging.listener.MessageListenerFactory;
import eu.agno3.runtime.messaging.marshalling.UnmarshallerManager;


/**
 * @author mbechler
 * 
 */
@SuppressWarnings ( "rawtypes" )
@Component ( immediate = true )
public class AgentEventListenerRegistration extends AbstractEventListenerRegistration<@NonNull EventListener> {

    private AgentConfig agentConfig;


    @Override
    @Activate
    protected synchronized void activate ( ComponentContext context ) {
        super.activate(context);
    }


    @Override
    @Deactivate
    protected synchronized void deactivate ( ComponentContext context ) throws JMSException {
        super.deactivate(context);
    }


    @Override
    @Reference
    protected synchronized void setMessageListenerFactory ( MessageListenerFactory msf ) {
        super.setMessageListenerFactory(msf);
    }


    @Override
    protected synchronized void unsetMessageListenerFactory ( MessageListenerFactory msf ) {
        super.unsetMessageListenerFactory(msf);
    }


    @Override
    @Reference
    protected synchronized void setUnmarshallerManager ( UnmarshallerManager um ) {
        super.setUnmarshallerManager(um);
    }


    @Override
    protected synchronized void unsetUnmarshallerManager ( UnmarshallerManager um ) {
        super.unsetUnmarshallerManager(um);
    }


    @Reference
    protected synchronized void setAgentConfig ( AgentConfig config ) {
        this.agentConfig = config;
    }


    protected synchronized void unsetAgentConfig ( AgentConfig config ) {
        if ( this.agentConfig == config ) {
            this.agentConfig = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.AbstractEventListenerRegistration#getListenerClass()
     */
    @SuppressWarnings ( "null" )
    @Override
    protected Class<@NonNull EventListener> getListenerClass () {
        return EventListener.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.AbstractEventListenerRegistration#getListenerDestinationStragegy(eu.agno3.runtime.messaging.listener.EventListener)
     */
    @Override
    protected DestinationStrategy getListenerDestinationStragegy ( @NonNull EventListener listener ) {
        return new FixedTopicStrategy(this.agentConfig.getEventTopic());
    }

}
