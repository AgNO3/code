/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.listener;


import java.util.Optional;

import javax.jms.JMSException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.marshalling.MarshallerManager;
import eu.agno3.runtime.messaging.marshalling.UnmarshallerManager;
import eu.agno3.runtime.messaging.msg.ErrorResponseMessage;
import eu.agno3.runtime.messaging.msg.RequestMessage;
import eu.agno3.runtime.messaging.msg.ResponseMessage;


/**
 * @author mbechler
 * 
 */
@SuppressWarnings ( "rawtypes" )
@Component ( immediate = true, configurationPid = "msg.endpoint.default", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class DefaultRequestEndpointRegistration implements ServiceTrackerCustomizer<RequestEndpoint, MessageListenerContainer> {

    private static final Logger log = Logger.getLogger(DefaultRequestEndpointRegistration.class);

    private ServiceTracker<RequestEndpoint, MessageListenerContainer> serviceTracker;

    private UnmarshallerManager unmarshallManager;
    private MarshallerManager marshallManager;
    private MessageListenerFactory messageListenerFactory;

    private BundleContext bundleContext;

    private Optional<@NonNull MessageSource> msgSource = Optional.empty();


    @Activate
    protected void activate ( ComponentContext context ) {
        log.debug("Starting request endpoint registrations"); //$NON-NLS-1$
        this.bundleContext = context.getBundleContext();
        this.serviceTracker = new ServiceTracker<>(context.getBundleContext(), RequestEndpoint.class, this);
        this.serviceTracker.open();
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        log.debug("Stopping request endpoint registrations"); //$NON-NLS-1$
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
    protected synchronized void setMarshallerManager ( MarshallerManager mm ) {
        this.marshallManager = mm;
    }


    protected synchronized void unsetMarshallerManager ( MarshallerManager mm ) {
        if ( this.marshallManager == mm ) {
            this.marshallManager = null;
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


    @Reference
    protected synchronized void setMessageSource ( @NonNull MessageSource ms ) {
        this.msgSource = Optional.of(ms);
    }


    protected synchronized void unsetMessageSource ( @NonNull MessageSource ms ) {
        if ( this.msgSource.isPresent() && this.msgSource.get() == ms ) {
            this.msgSource = Optional.empty();
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public MessageListenerContainer addingService ( ServiceReference<RequestEndpoint> reference ) {

        RequestEndpoint<RequestMessage<@NonNull ? extends MessageSource, ResponseMessage<@NonNull ? extends MessageSource>, ErrorResponseMessage<@NonNull ? extends MessageSource>>, ResponseMessage<@NonNull ? extends MessageSource>, ErrorResponseMessage<@NonNull ? extends MessageSource>> endpoint = this.bundleContext
                .getService(reference);

        if ( endpoint == null ) {
            return null;
        }

        String messageTypeName = (String) reference.getProperty("msgType"); //$NON-NLS-1$

        if ( messageTypeName == null ) {
            log.warn(String.format("RequestEndpoint %s has no msgType set, ignoring", endpoint.getClass().getName())); //$NON-NLS-1$
            return null;
        }

        @SuppressWarnings ( "null" )
        Class<@NonNull ? extends RequestMessage<@NonNull ? extends MessageSource, ResponseMessage<@NonNull ? extends MessageSource>, ErrorResponseMessage<@NonNull ? extends MessageSource>>> msgType = endpoint
                .getMessageType();

        if ( !msgType.getName().equals(messageTypeName) ) {
            log.warn("Property msgType does not match the message type specified by the endpoint: " + endpoint.getClass().getName()); //$NON-NLS-1$
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Registering request endpoint %s for type %s", endpoint.getClass().getName(), messageTypeName)); //$NON-NLS-1$
        }

        try {
            return setupEndpoint(endpoint, msgType);

        }
        catch ( Exception e ) {
            log.error("Failed to setup request endpoint:", e); //$NON-NLS-1$
            return null;
        }

    }


    /**
     * @param endpoint
     * @param msgType
     * @return
     * @throws JMSException
     * @throws MessagingException
     */
    private MessageListenerContainer setupEndpoint (
            RequestEndpoint<RequestMessage<@NonNull ? extends MessageSource, ResponseMessage<@NonNull ? extends MessageSource>, ErrorResponseMessage<@NonNull ? extends MessageSource>>, ResponseMessage<@NonNull ? extends MessageSource>, ErrorResponseMessage<@NonNull ? extends MessageSource>> endpoint,
            @NonNull Class<@NonNull ? extends RequestMessage<@NonNull ? extends MessageSource, ResponseMessage<@NonNull ? extends MessageSource>, ErrorResponseMessage<@NonNull ? extends MessageSource>>> msgType )
            throws JMSException, MessagingException {
        RequestEndpointWrapper rew = new RequestEndpointWrapper(
            msgType,
            endpoint,
            this.marshallManager,
            this.unmarshallManager,
            this.getDefaultQueuePrefix(),
            this.msgSource.get());
        MessageListenerContainer container = this.messageListenerFactory.createMessageListener(rew);
        this.messageListenerFactory.startListener(container);
        return container;
    }


    /**
     * @return
     */
    protected String getDefaultQueuePrefix () {
        return StringUtils.EMPTY;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void modifiedService ( ServiceReference<RequestEndpoint> reference, MessageListenerContainer service ) {
        // unused
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
     *      java.lang.Object)
     */
    @Override
    public void removedService ( ServiceReference<RequestEndpoint> reference, MessageListenerContainer service ) {
        if ( service != null ) {
            try {
                this.messageListenerFactory.remove(service);
            }
            catch ( JMSException e ) {
                log.error("Failed to tear down request endpoint:", e); //$NON-NLS-1$
            }
        }
    }
}
