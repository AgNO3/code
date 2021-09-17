/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.09.2013 by mbechler
 */
package eu.agno3.runtime.messaging.client.internal;


import javax.jms.ConnectionFactory;
import javax.transaction.TransactionManager;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.client.MessagingClient;
import eu.agno3.runtime.messaging.client.MessagingClientFactory;
import eu.agno3.runtime.messaging.marshalling.MarshallerManager;
import eu.agno3.runtime.messaging.marshalling.UnmarshallerManager;
import eu.agno3.runtime.messaging.routing.DestinationResolverManager;
import eu.agno3.runtime.messaging.routing.EventRouterManager;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    MessagingClientFactory.class
} )
public class DefaultMessagingClientFactory implements MessagingClientFactory {

    private TransactionManager tm;
    private EventRouterManager eventRouterManager;
    private DestinationResolverManager destinationResolverManager;
    private MarshallerManager marshallerManager;
    private UnmarshallerManager unmarshallerManager;


    @Reference
    protected synchronized void setTransactionManager ( TransactionManager tx ) {
        this.tm = tx;
    }


    protected synchronized void unsetTransactionManager ( TransactionManager tx ) {
        if ( this.tm == tx ) {
            this.tm = null;
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
    protected synchronized void setDestinationResolverManager ( DestinationResolverManager drm ) {
        this.destinationResolverManager = drm;
    }


    protected synchronized void unsetDestinationResolverManager ( DestinationResolverManager drm ) {
        if ( this.destinationResolverManager == drm ) {
            this.destinationResolverManager = null;
        }
    }


    @Reference
    protected synchronized void setMarshallerManager ( MarshallerManager mm ) {
        this.marshallerManager = mm;
    }


    protected synchronized void unsetMarshallerManager ( MarshallerManager mm ) {
        if ( this.marshallerManager == mm ) {
            this.marshallerManager = null;
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


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.client.MessagingClientFactory#createClient(eu.agno3.runtime.messaging.addressing.MessageSource,
     *      javax.jms.ConnectionFactory)
     */
    @SuppressWarnings ( "unused" )
    @Override
    public <@NonNull T extends MessageSource> MessagingClient<T> createClient ( @NonNull T source, @NonNull ConnectionFactory cf ) {
        return new MessagingClientImpl<T>(
            source,
            cf,
            this.eventRouterManager,
            this.destinationResolverManager,
            this.marshallerManager,
            this.unmarshallerManager);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.client.MessagingClientFactory#createClient(eu.agno3.runtime.messaging.addressing.MessageSource,
     *      javax.jms.ConnectionFactory, eu.agno3.runtime.messaging.routing.EventRouterManager)
     */
    @SuppressWarnings ( "unused" )
    @Override
    public <@NonNull T extends MessageSource> MessagingClient<T> createClient ( @NonNull T source, @NonNull ConnectionFactory cf,
            @NonNull EventRouterManager erm ) {
        return new MessagingClientImpl<T>(source, cf, erm, this.destinationResolverManager, this.marshallerManager, this.unmarshallerManager);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.client.MessagingClientFactory#createTransactedClient(eu.agno3.runtime.messaging.addressing.MessageSource,
     *      javax.jms.ConnectionFactory)
     */
    @SuppressWarnings ( "unused" )
    @Override
    public <@NonNull T extends MessageSource> MessagingClient<T> createTransactedClient ( @NonNull T source, @NonNull ConnectionFactory cf ) {
        return new TransactedMessagingClientImpl<T>(
            source,
            cf,
            this.tm,
            this.eventRouterManager,
            this.destinationResolverManager,
            this.marshallerManager,
            this.unmarshallerManager);
    }

}
