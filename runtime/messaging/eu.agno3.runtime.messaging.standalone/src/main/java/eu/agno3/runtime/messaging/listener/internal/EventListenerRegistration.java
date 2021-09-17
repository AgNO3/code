/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.listener.internal;


import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.listener.EventListener;
import eu.agno3.runtime.messaging.listener.EventListenerWrapper;
import eu.agno3.runtime.messaging.listener.MessageListenerContainer;
import eu.agno3.runtime.messaging.listener.MessageListenerFactory;
import eu.agno3.runtime.messaging.marshalling.UnmarshallerManager;
import eu.agno3.runtime.messaging.msg.EventMessage;


/**
 * @author mbechler
 * 
 */
@SuppressWarnings ( "rawtypes" )
@Component ( immediate = true )
public class EventListenerRegistration implements ServiceTrackerCustomizer<EventListener, MessageListenerContainer> {

    private static final Logger log = Logger.getLogger(EventListenerRegistration.class);

    private ServiceTracker<EventListener, MessageListenerContainer> serviceTracker;

    private UnmarshallerManager unmarshallManager;
    private MessageListenerFactory messageListenerFactory;

    private BundleContext bundleContext;


    @Activate
    protected void activate ( ComponentContext context ) {
        log.info("Starting event listener registrations"); //$NON-NLS-1$
        this.bundleContext = context.getBundleContext();
        this.serviceTracker = new ServiceTracker<>(context.getBundleContext(), EventListener.class, this);
        this.serviceTracker.open();
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        log.info("Stopping event listener registrations"); //$NON-NLS-1$
        this.serviceTracker.close();
        this.serviceTracker = null;
    }


    @Reference
    protected synchronized void setUnmarshallerManager ( UnmarshallerManager um ) {
        this.unmarshallManager = um;
    }


    protected synchronized void unsetUnmarshallerManager ( UnmarshallerManager um ) {
        if ( this.unmarshallManager == um ) {
            this.unmarshallManager = null;
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


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public MessageListenerContainer addingService ( ServiceReference<EventListener> reference ) {

        EventListener<@NonNull EventMessage<@NonNull ? extends MessageSource>> listener = this.bundleContext.getService(reference);
        String eventTypeName = (String) reference.getProperty("eventType"); //$NON-NLS-1$

        if ( eventTypeName == null ) {
            log.warn(String.format("EventListener %s has no eventType set, ignoring", listener.getClass().getName())); //$NON-NLS-1$
            return null;
        }

        Class<@NonNull ? extends EventMessage<@NonNull ? extends MessageSource>> eventType = listener.getEventType();

        if ( !eventType.getName().equals(eventTypeName) ) {
            log.warn("Property eventType does not match the event type specified by the listener: " + listener.getClass().getName()); //$NON-NLS-1$
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Registering event listener %s for type %s", listener.getClass().getName(), eventTypeName)); //$NON-NLS-1$
        }

        try {
            EventListenerWrapper elw = new EventListenerWrapper(listener, eventType, this.unmarshallManager);
            MessageListenerContainer mlc = this.messageListenerFactory.createMessageListener(elw);
            this.messageListenerFactory.startListener(mlc);
            return mlc;
        }
        catch ( Exception e ) {
            log.error("Failed to setup event listener:", e); //$NON-NLS-1$
            return null;
        }

    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService ( ServiceReference<EventListener> reference, MessageListenerContainer service ) {

    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<EventListener> reference, MessageListenerContainer service ) {
        if ( service != null ) {
            try {
                this.messageListenerFactory.remove(service);
            }
            catch ( JMSException e ) {
                log.error("Failed to tear down event listener:", e); //$NON-NLS-1$
            }
        }
    }
}
