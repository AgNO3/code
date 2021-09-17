/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.broker.auth.impl;


import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.ProducerBrokerExchange;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.command.DestinationInfo;
import org.apache.activemq.command.Message;
import org.apache.activemq.command.ProducerInfo;
import org.apache.activemq.security.SecurityContext;

import eu.agno3.runtime.messaging.broker.auth.BrokerDestinationAccessDeniedException;
import eu.agno3.runtime.messaging.broker.auth.DestinationAccess;


/**
 * @author mbechler
 * 
 */
public class DynamicAuthorizationBroker extends BrokerFilter {

    private DynamicAccessDecisionManager accessManager;


    /**
     * @param next
     * @param accessManager
     */
    public DynamicAuthorizationBroker ( Broker next, DynamicAccessDecisionManager accessManager ) {
        super(next);
        this.accessManager = accessManager;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws Exception
     * 
     * @see org.apache.activemq.security.AuthorizationBroker#addDestination(org.apache.activemq.broker.ConnectionContext,
     *      org.apache.activemq.command.ActiveMQDestination, boolean)
     */
    @Override
    public Destination addDestination ( ConnectionContext context, ActiveMQDestination dest, boolean timeout ) throws Exception {
        SecurityContext secContext = context.getSecurityContext();

        if ( secContext == null ) {
            secContext = createAnonymousContext();
        }

        if ( secContext.isBrokerContext() ) {
            return super.addDestination(context, dest, timeout);
        }

        DestinationAccess perm = DestinationAccess.CREATE;
        // check permissions
        if ( !this.accessManager.decide(context, secContext, dest, perm) ) {
            throw new BrokerDestinationAccessDeniedException(secContext, dest, perm);
        }

        return super.addDestination(context, dest, timeout);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.activemq.broker.BrokerFilter#addDestinationInfo(org.apache.activemq.broker.ConnectionContext,
     *      org.apache.activemq.command.DestinationInfo)
     */
    @Override
    public void addDestinationInfo ( ConnectionContext context, DestinationInfo info ) throws Exception {
        SecurityContext secContext = context.getSecurityContext();

        if ( secContext == null ) {
            secContext = createAnonymousContext();
        }

        if ( secContext.isBrokerContext() ) {
            super.addDestinationInfo(context, info);
            return;
        }

        DestinationAccess perm = DestinationAccess.CREATE;
        // check permissions
        if ( !this.accessManager.decide(context, secContext, info.getDestination(), perm) ) {
            throw new BrokerDestinationAccessDeniedException(secContext, info.getDestination(), perm);
        }

        super.addDestinationInfo(context, info);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.activemq.security.AuthorizationBroker#removeDestination(org.apache.activemq.broker.ConnectionContext,
     *      org.apache.activemq.command.ActiveMQDestination, long)
     */
    @Override
    public void removeDestination ( ConnectionContext context, ActiveMQDestination dest, long timeout ) throws Exception {
        SecurityContext secContext = context.getSecurityContext();

        if ( secContext == null ) {
            secContext = createAnonymousContext();
        }

        if ( secContext.isBrokerContext() ) {
            secContext.getAuthorizedWriteDests().remove(dest);
            super.removeDestination(context, dest, timeout);
            return;
        }

        DestinationAccess perm = DestinationAccess.DELETE;

        // check permissions
        if ( !this.accessManager.decide(context, secContext, dest, perm) ) {
            throw new BrokerDestinationAccessDeniedException(secContext, dest, perm);
        }

        secContext.getAuthorizedWriteDests().remove(dest);
        super.removeDestination(context, dest, timeout);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.activemq.broker.BrokerFilter#removeDestinationInfo(org.apache.activemq.broker.ConnectionContext,
     *      org.apache.activemq.command.DestinationInfo)
     */
    @Override
    public void removeDestinationInfo ( ConnectionContext context, DestinationInfo info ) throws Exception {
        SecurityContext secContext = context.getSecurityContext();

        if ( secContext == null ) {
            secContext = createAnonymousContext();
        }

        if ( secContext.isBrokerContext() ) {
            secContext.getAuthorizedWriteDests().remove(info.getDestination());
            super.removeDestinationInfo(context, info);
            return;
        }

        DestinationAccess perm = DestinationAccess.DELETE;

        // check permissions
        if ( !this.accessManager.decide(context, secContext, info.getDestination(), perm) ) {
            throw new BrokerDestinationAccessDeniedException(secContext, info.getDestination(), perm);
        }

        secContext.getAuthorizedWriteDests().remove(info.getDestination());
        super.removeDestinationInfo(context, info);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.activemq.broker.BrokerFilter#addConsumer(org.apache.activemq.broker.ConnectionContext,
     *      org.apache.activemq.command.ConsumerInfo)
     */
    @Override
    public Subscription addConsumer ( ConnectionContext context, ConsumerInfo consumerInfo ) throws Exception {
        SecurityContext secContext = context.getSecurityContext();

        if ( secContext == null ) {
            secContext = createAnonymousContext();
        }

        if ( secContext.isBrokerContext() ) {
            return super.addConsumer(context, consumerInfo);
        }

        ActiveMQDestination dest = consumerInfo.getDestination();

        DestinationAccess perm = DestinationAccess.CONSUME;
        // check permissions
        if ( !this.accessManager.decide(context, secContext, dest, perm) ) {
            throw new BrokerDestinationAccessDeniedException(secContext, dest, perm);
        }

        return super.addConsumer(context, consumerInfo);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.activemq.security.AuthorizationBroker#addProducer(org.apache.activemq.broker.ConnectionContext,
     *      org.apache.activemq.command.ProducerInfo)
     */
    @Override
    public void addProducer ( ConnectionContext context, ProducerInfo producerInfo ) throws Exception {
        SecurityContext secContext = context.getSecurityContext();

        if ( secContext == null ) {
            secContext = createAnonymousContext();
        }

        if ( secContext.isBrokerContext() ) {
            super.addProducer(context, producerInfo);
            return;
        }

        ActiveMQDestination dest = producerInfo.getDestination();

        if ( dest == null ) {
            throw new IllegalArgumentException("Destination must not be NULL"); //$NON-NLS-1$
        }

        if ( secContext.getAuthorizedWriteDests().containsKey(dest) ) {
            super.addProducer(context, producerInfo);
            return;
        }

        DestinationAccess perm = DestinationAccess.PRODUCE;
        // check permissions
        if ( !this.accessManager.decide(context, secContext, dest, perm) ) {
            throw new BrokerDestinationAccessDeniedException(secContext, dest, perm);
        }

        secContext.getAuthorizedWriteDests().putIfAbsent(dest, dest);
        super.addProducer(context, producerInfo);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.activemq.security.AuthorizationBroker#send(org.apache.activemq.broker.ProducerBrokerExchange,
     *      org.apache.activemq.command.Message)
     */
    @Override
    public void send ( ProducerBrokerExchange exch, Message msg ) throws Exception {
        SecurityContext secContext = exch.getConnectionContext().getSecurityContext();

        if ( secContext == null ) {
            secContext = createAnonymousContext();
        }

        if ( secContext.isBrokerContext() ) {
            super.send(exch, msg);
            return;
        }

        ActiveMQDestination dest = msg.getDestination();

        if ( secContext.getAuthorizedWriteDests().containsKey(dest) ) {
            super.send(exch, msg);
            return;
        }

        DestinationAccess perm = DestinationAccess.PRODUCE;
        // check permissions
        if ( !this.accessManager.decide(exch.getConnectionContext(), secContext, dest, perm) ) {
            throw new BrokerDestinationAccessDeniedException(secContext, dest, perm);
        }

        secContext.getAuthorizedWriteDests().putIfAbsent(dest, dest);
        super.send(exch, msg);
    }


    private static SecurityContext createAnonymousContext () {
        return new SecurityContext("anonymous") { //$NON-NLS-1$

            @Override
            public Set<Principal> getPrincipals () {
                return new HashSet<>();
            }
        };
    }

}
