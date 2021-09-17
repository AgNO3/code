/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.12.2014 by mbechler
 */
package eu.agno3.orchestrator.gui.server.internal;


import javax.jms.JMSException;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.gui.config.GuiConfig;
import eu.agno3.orchestrator.gui.connector.GuiEventListener;
import eu.agno3.runtime.messaging.listener.AbstractEventListenerRegistration;
import eu.agno3.runtime.messaging.listener.DestinationStrategy;
import eu.agno3.runtime.messaging.listener.FixedTopicStrategy;
import eu.agno3.runtime.messaging.listener.MessageListenerFactory;
import eu.agno3.runtime.messaging.marshalling.UnmarshallerManager;


/**
 * @author mbechler
 *
 */
@Component ( immediate = true )
public class LocalGuiEventListenerRegistration extends AbstractEventListenerRegistration<GuiEventListener> {

    private GuiConfig guiConfig;


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
    protected synchronized void setGuiConfig ( LocalGuiConfig config ) {
        this.guiConfig = config;
    }


    protected synchronized void unsetGuiConfig ( LocalGuiConfig config ) {
        if ( this.guiConfig == config ) {
            this.guiConfig = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.AbstractEventListenerRegistration#getListenerClass()
     */
    @Override
    protected Class<GuiEventListener> getListenerClass () {
        return GuiEventListener.class;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.AbstractEventListenerRegistration#getListenerDestinationStragegy(eu.agno3.runtime.messaging.listener.EventListener)
     */
    @Override
    protected DestinationStrategy getListenerDestinationStragegy ( GuiEventListener listener ) {
        return new FixedTopicStrategy(this.guiConfig.getEventTopic());
    }

}
