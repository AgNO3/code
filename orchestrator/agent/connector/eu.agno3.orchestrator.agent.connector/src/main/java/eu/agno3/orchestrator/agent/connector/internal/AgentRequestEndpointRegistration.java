/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.connector.internal;


import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.config.AgentConfig;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.listener.DefaultRequestEndpointRegistration;
import eu.agno3.runtime.messaging.listener.MessageListenerFactory;
import eu.agno3.runtime.messaging.marshalling.MarshallerManager;
import eu.agno3.runtime.messaging.marshalling.UnmarshallerManager;


/**
 * @author mbechler
 *
 */
@Component ( immediate = true )
public class AgentRequestEndpointRegistration extends DefaultRequestEndpointRegistration {

    private AgentConfig agentConfig;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.DefaultRequestEndpointRegistration#activate(org.osgi.service.component.ComponentContext)
     */
    @Override
    @Activate
    protected void activate ( ComponentContext context ) {
        super.activate(context);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.DefaultRequestEndpointRegistration#deactivate(org.osgi.service.component.ComponentContext)
     */
    @Override
    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        super.deactivate(context);
    }


    @Reference
    protected synchronized void setAgentConfig ( AgentConfig cfg ) {
        this.agentConfig = cfg;
    }


    protected synchronized void unsetAgentConfig ( AgentConfig cfg ) {
        if ( this.agentConfig == cfg ) {
            this.agentConfig = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.DefaultRequestEndpointRegistration#setMessageSource(eu.agno3.runtime.messaging.addressing.MessageSource)
     */
    @Override
    @Reference
    protected synchronized void setMessageSource ( @NonNull MessageSource ms ) {
        super.setMessageSource(ms);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.DefaultRequestEndpointRegistration#unsetMessageSource(eu.agno3.runtime.messaging.addressing.MessageSource)
     */
    @Override
    protected synchronized void unsetMessageSource ( @NonNull MessageSource ms ) {
        super.unsetMessageSource(ms);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.DefaultRequestEndpointRegistration#setMarshallerManager(eu.agno3.runtime.messaging.marshalling.MarshallerManager)
     */
    @Reference
    @Override
    protected synchronized void setMarshallerManager ( MarshallerManager mm ) {
        super.setMarshallerManager(mm);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.DefaultRequestEndpointRegistration#unsetMarshallerManager(eu.agno3.runtime.messaging.marshalling.MarshallerManager)
     */
    @Override
    protected synchronized void unsetMarshallerManager ( MarshallerManager mm ) {
        super.unsetMarshallerManager(mm);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.DefaultRequestEndpointRegistration#setMessageListenerFactory(eu.agno3.runtime.messaging.listener.MessageListenerFactory)
     */
    @Reference
    @Override
    protected synchronized void setMessageListenerFactory ( MessageListenerFactory mlf ) {
        super.setMessageListenerFactory(mlf);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.DefaultRequestEndpointRegistration#unsetMessageListenerFactory(eu.agno3.runtime.messaging.listener.MessageListenerFactory)
     */
    @Override
    protected synchronized void unsetMessageListenerFactory ( MessageListenerFactory mlf ) {
        super.unsetMessageListenerFactory(mlf);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.DefaultRequestEndpointRegistration#setUnmarshallerManager(eu.agno3.runtime.messaging.marshalling.UnmarshallerManager)
     */
    @Reference
    @Override
    protected synchronized void setUnmarshallerManager ( UnmarshallerManager um ) {
        super.setUnmarshallerManager(um);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.DefaultRequestEndpointRegistration#unsetUnmarshallerManager(eu.agno3.runtime.messaging.marshalling.UnmarshallerManager)
     */
    @Override
    protected synchronized void unsetUnmarshallerManager ( UnmarshallerManager um ) {
        super.unsetUnmarshallerManager(um);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.DefaultRequestEndpointRegistration#getDefaultQueuePrefix()
     */
    @Override
    protected String getDefaultQueuePrefix () {
        return this.agentConfig.getRequestQueuePrefix();
    }
}
