/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.09.2013 by mbechler
 */
package eu.agno3.runtime.messaging.listener;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.marshalling.UnmarshallerManager;
import eu.agno3.runtime.messaging.msg.EventMessage;


/**
 * @author mbechler
 * @param <T>
 * 
 */
@SuppressWarnings ( "rawtypes" )
public abstract class AbstractEventListenerRegistration <T extends EventListener<?>> implements ServiceTrackerCustomizer<T, Optional<@NonNull T>> {

    private static final Logger log = Logger.getLogger(AbstractEventListenerRegistration.class);

    private ServiceTracker<T, Optional<@NonNull T>> serviceTracker;

    private UnmarshallerManager unmarshallManager;
    private BundleContext bundleContext;
    private Map<String, DelegatingEventListener> destinationListeners = new HashMap<>();

    private MessageListenerFactory messageListenerFactory;


    /**
     * 
     */
    public AbstractEventListenerRegistration () {
        super();
    }


    protected abstract Class<T> getListenerClass ();


    @Activate
    protected synchronized void activate ( ComponentContext context ) {
        log.debug("Starting event listener registrations"); //$NON-NLS-1$
        this.bundleContext = context.getBundleContext();
        this.serviceTracker = new ServiceTracker<>(context.getBundleContext(), getListenerClass(), this);
        this.serviceTracker.open();

    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext context ) throws JMSException {
        log.debug("Stopping event listener registrations"); //$NON-NLS-1$
        this.serviceTracker.close();
        this.serviceTracker = null;

        for ( DelegatingEventListener l : this.destinationListeners.values() ) {
            l.close();
        }

        this.destinationListeners.clear();
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
    protected synchronized void setMessageListenerFactory ( MessageListenerFactory msf ) {
        this.messageListenerFactory = msf;
    }


    protected synchronized void unsetMessageListenerFactory ( MessageListenerFactory msf ) {
        if ( this.messageListenerFactory == msf ) {
            this.messageListenerFactory = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public Optional<@NonNull T> addingService ( ServiceReference<T> reference ) {

        T listener = this.bundleContext.getService(reference);

        if ( listener == null ) {
            return Optional.empty();
        }

        String eventTypeName = (String) reference.getProperty("eventType"); //$NON-NLS-1$

        if ( eventTypeName == null ) {
            log.warn(String.format("EventListener %s has no eventType set, ignoring", listener.getClass().getName())); //$NON-NLS-1$
            return Optional.empty();
        }

        Class<? extends EventMessage<@NonNull ? extends MessageSource>> eventType = listener.getEventType();

        if ( !eventType.getName().equals(eventTypeName) ) {
            log.warn("Property eventType does not match the event type specified by the listener: " + listener.getClass().getName()); //$NON-NLS-1$
        }

        try {

            setupEventListener(listener, eventTypeName);

        }
        catch ( Exception e ) {
            log.error("Failed to setup event listener:", e); //$NON-NLS-1$
            return Optional.empty();
        }

        return Optional.of(listener);
    }


    /**
     * @param listener
     * @param eventTypeName
     * @throws JMSException
     * @throws MessagingException
     */
    private void setupEventListener ( T listener, String eventTypeName ) throws JMSException, MessagingException {
        if ( listener == null ) {
            return;
        }

        DestinationStrategy dest = getListenerDestinationStragegy(listener);
        String destinationId = dest.getDestinationId(listener);
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Registering event listener %s for type %s on %s", listener.getClass().getName(), eventTypeName, destinationId)); //$NON-NLS-1$
        }

        synchronized ( this.destinationListeners ) {
            if ( !this.destinationListeners.containsKey(destinationId) ) {
                this.destinationListeners.put(destinationId, new DelegatingEventListener(
                    this.messageListenerFactory,
                    this.unmarshallManager,
                    this.messageListenerFactory.createDestination(dest, listener)));
            }

            this.destinationListeners.get(destinationId).addDelegate(listener);
        }
    }


    /**
     * @param listener
     * @return
     */
    protected abstract DestinationStrategy getListenerDestinationStragegy ( T listener );


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService ( ServiceReference<T> reference, Optional<@NonNull T> service ) {
        // no dynamic reconfiguration
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<T> reference, Optional<@NonNull T> service ) {

        if ( !service.isPresent() ) {
            return;
        }

        try {
            @NonNull
            T listener = service.get();
            closeListenerForDestination(listener, this.getListenerDestinationStragegy(listener).getDestinationId(listener));
        }
        catch ( JMSException e ) {
            log.error("Failed to tear down event listener:", e); //$NON-NLS-1$
        }
    }


    /**
     * @param service
     * @param destination
     * @throws JMSException
     */
    private void closeListenerForDestination ( T service, String destinationId ) throws JMSException {
        if ( service == null ) {
            return;
        }
        if ( log.isDebugEnabled() ) {
            log.debug("Trying to close event listener " + service.getClass().getName()); //$NON-NLS-1$
        }
        synchronized ( this.destinationListeners ) {
            if ( this.destinationListeners.containsKey(destinationId) ) {
                log.debug(String.format("Unregistering event listener %s for type %s on %s", //$NON-NLS-1$
                    service.getClass().getName(),
                    service.getEventType().getName(),
                    destinationId));
                this.destinationListeners.get(destinationId).removeDelegate(service);

                if ( this.destinationListeners.get(destinationId).getNumDelegates() == 0 ) {
                    DelegatingEventListener listener = this.destinationListeners.remove(destinationId);
                    listener.close();
                }
            }
            else {
                log.warn("No listener found for destination " + destinationId); //$NON-NLS-1$
                for ( String dest : this.destinationListeners.keySet() ) {
                    log.debug("Destination known " + dest); //$NON-NLS-1$
                }
            }

        }
    }

}