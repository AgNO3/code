/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.base.component;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.server.component.ComponentConfig;
import eu.agno3.orchestrator.server.component.ComponentLifecycleListener;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.events.EventTypeRegistry;
import eu.agno3.runtime.messaging.listener.MessageListenerFactory;
import eu.agno3.runtime.messaging.marshalling.UnmarshallerManager;
import eu.agno3.runtime.messaging.routing.EventRouterManager;


/**
 * @author mbechler
 * 
 * @param <T>
 */
public abstract class AbstractEventPumpManager <T extends ComponentConfig> implements ComponentLifecycleListener<T> {

    private static final Logger log = Logger.getLogger(AbstractEventPumpManager.class);

    private Map<UUID, ComponentEventPump<T>> eventPumps = new HashMap<>();

    private ConnectionFactory connFactory;
    private Connection connection;
    private EventTypeRegistry eventTypeRegistry;
    private EventRouterManager eventRouterManager;
    private UnmarshallerManager unmarshallerManager;
    private MessageListenerFactory messageListenerFactory;


    /**
     * 
     */
    public AbstractEventPumpManager () {
        super();
    }


    @Activate
    protected void activate ( ComponentContext context ) throws JMSException {
        this.connection = this.connFactory.createConnection();
        this.connection.start();
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) throws JMSException {
        for ( ComponentEventPump<T> pump : this.eventPumps.values() ) {
            pump.close();
        }

        this.connection.close();
    }


    @Reference
    protected synchronized void setConnectionFactory ( ConnectionFactory cf ) {
        this.connFactory = cf;
    }


    protected synchronized void unsetConnectionFactory ( ConnectionFactory cf ) {
        if ( this.connFactory == cf ) {
            this.connFactory = null;
        }
    }


    @Reference
    protected synchronized void setEventTypeRegistry ( EventTypeRegistry etr ) {
        this.eventTypeRegistry = etr;
    }


    protected synchronized void unsetEventTypeRegistry ( EventTypeRegistry etr ) {
        if ( this.eventTypeRegistry == etr ) {
            this.eventTypeRegistry = null;
        }
    }


    @Reference
    protected synchronized void setEventRouterManager ( EventRouterManager erm ) {
        this.eventRouterManager = erm;
    }


    protected synchronized void unsetEventRouterManager ( EventRouterManager erm ) {
        if ( this.eventRouterManager == erm ) {
            this.eventRouterManager = null;
        }
    }


    @Reference
    protected synchronized void setUnmarshallerManager ( UnmarshallerManager um ) {
        this.unmarshallerManager = um;
    }


    protected synchronized void unsetUnmarshallerManager ( UnmarshallerManager um ) {
        if ( this.unmarshallerManager == um ) {
            this.unmarshallerManager = null;
        }
    }


    @Reference
    protected synchronized void setMessageListenerFactory ( MessageListenerFactory mlf ) {
        this.messageListenerFactory = mlf;
    }


    protected synchronized void unsetMessageListenerFactory ( MessageListenerFactory mlf ) {
        if ( this.messageListenerFactory == mlf ) {
            this.messageListenerFactory = null;
        }
    }


    protected abstract EventScope getListeningScope ( T c );


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.component.ComponentLifecycleListener#connecting(eu.agno3.orchestrator.server.component.ComponentConfig)
     */
    @Override
    public synchronized void connecting ( T c ) {
        try {
            ComponentEventPump<T> pump = new ComponentEventPump<>(
                c,
                this.getListeningScope(c),
                this.eventTypeRegistry,
                this.unmarshallerManager,
                this.eventRouterManager,
                this.messageListenerFactory);
            pump.start(this.connection);
            this.eventPumps.put(c.getId(), pump);

            if ( log.isDebugEnabled() ) {
                log.debug("Set up event pump for component " + c.getId()); //$NON-NLS-1$
            }
        }
        catch (
            JMSException |
            MessagingException e ) {
            log.error("Failed to create event pump for component " + c.getId(), e); //$NON-NLS-1$
        }
    }


    @Override
    public void connected ( T c ) {}


    @Override
    public void disconnecting ( T c ) {
        this.removeComponent(c);
    }


    /**
     * @param c
     */
    protected void removeComponent ( T c ) {
        ComponentEventPump<T> pump = this.eventPumps.remove(c.getId());
        if ( pump != null ) {
            try {
                pump.close();
            }
            catch ( JMSException e ) {
                log.warn("Failed to cleanly close message pump:", e); //$NON-NLS-1$
            }
        }
    }


    @Override
    public void failed ( T c ) {
        this.removeComponent(c);
    }

}