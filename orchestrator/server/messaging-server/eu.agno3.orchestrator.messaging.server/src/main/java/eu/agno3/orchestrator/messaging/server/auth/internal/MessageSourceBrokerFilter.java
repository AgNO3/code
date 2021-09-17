/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.12.2014 by mbechler
 */
package eu.agno3.orchestrator.messaging.server.auth.internal;


import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.ProducerBrokerExchange;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.Message;
import org.apache.activemq.security.SecurityContext;
import org.apache.log4j.Logger;

import eu.agno3.orchestrator.server.component.auth.ComponentPrincipal;
import eu.agno3.orchestrator.server.component.auth.ComponentSecurityContext;
import eu.agno3.orchestrator.server.component.auth.SystemSecurityContext;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSourceRegistry;
import eu.agno3.runtime.messaging.broker.auth.BrokerDestinationAccessDeniedException;
import eu.agno3.runtime.messaging.broker.auth.DestinationAccess;
import eu.agno3.runtime.messaging.msg.MessageProperties;


/**
 * @author mbechler
 *
 */
public class MessageSourceBrokerFilter extends BrokerFilter {

    private static final Logger log = Logger.getLogger(MessageSourceBrokerFilter.class);
    private MessageSourceRegistry msgSourceRegistry;


    /**
     * @param next
     * @param msgSourceRegistry
     */
    public MessageSourceBrokerFilter ( Broker next, MessageSourceRegistry msgSourceRegistry ) {
        super(next);
        this.msgSourceRegistry = msgSourceRegistry;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws Exception
     * @see org.apache.activemq.broker.BrokerFilter#send(org.apache.activemq.broker.ProducerBrokerExchange,
     *      org.apache.activemq.command.Message)
     */
    @Override
    public void send ( ProducerBrokerExchange exch, Message msg ) throws Exception {
        SecurityContext secContext = exch.getConnectionContext().getSecurityContext();
        ActiveMQDestination dest = msg.getDestination();
        DestinationAccess perm = DestinationAccess.PRODUCE;
        Object msgSource = msg.getProperty(MessageProperties.SOURCE);

        if ( isValidDLQRequest(secContext, dest) ) {
            // allow DLQ access by broker
        }
        else if ( msgSource instanceof String ) {
            this.validateMessageSource(secContext, (String) msgSource, dest, perm);
        }
        else if ( msgSource != null ) {
            throw new BrokerDestinationAccessDeniedException(secContext, dest, perm);
        }

        super.send(exch, msg);
    }


    private static boolean isValidDLQRequest ( SecurityContext secContext, ActiveMQDestination dest ) {
        return dest.getPhysicalName().startsWith("ActiveMQ.DLQ") //$NON-NLS-1$
                && ( ! ( secContext instanceof ComponentSecurityContext ) && "ActiveMQBroker".equals(secContext.getUserName()) ); //$NON-NLS-1$
    }


    /**
     * @param secContext
     * @param msgSource
     * @param perm
     * @param dest
     */
    private void validateMessageSource ( SecurityContext secContext, String msgSource, ActiveMQDestination dest, DestinationAccess perm ) {

        if ( secContext instanceof SystemSecurityContext ) {
            return;
        }

        if ( ! ( secContext instanceof ComponentSecurityContext ) ) {
            throw new IllegalArgumentException("Context is not a ComponentSecurityContext"); //$NON-NLS-1$
        }

        ComponentSecurityContext ctx = (ComponentSecurityContext) secContext;
        MessageSource messageSource = this.msgSourceRegistry.getMessageSource(msgSource);
        if ( messageSource == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Failed to get message source from message " + msgSource); //$NON-NLS-1$
            }
            throw new BrokerDestinationAccessDeniedException(secContext, dest, perm);
        }
        ComponentPrincipal princFromMessage = PrincipalFactory.fromMessageSource(messageSource);

        if ( !ctx.getComponentPrincipal().equals(princFromMessage) ) {
            log.warn(String.format(
                "Mismatch between message source %s and authenticated sender %s", //$NON-NLS-1$
                princFromMessage,
                ctx.getComponentPrincipal()));
            throw new BrokerDestinationAccessDeniedException(secContext, dest, perm);
        }

    }
}
