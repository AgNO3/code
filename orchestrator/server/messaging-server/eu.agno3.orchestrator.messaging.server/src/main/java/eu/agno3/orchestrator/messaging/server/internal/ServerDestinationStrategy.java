/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.09.2015 by mbechler
 */
package eu.agno3.orchestrator.messaging.server.internal;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.activemq.command.ActiveMQTopic;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.ListeningEventScopeResolver;
import eu.agno3.runtime.messaging.addressing.scopes.ServerEventScope;
import eu.agno3.runtime.messaging.listener.BaseListener;
import eu.agno3.runtime.messaging.listener.CustomDestination;
import eu.agno3.runtime.messaging.listener.DestinationStrategy;


/**
 * @author mbechler
 *
 */
public class ServerDestinationStrategy implements DestinationStrategy {

    private @NonNull UUID serverId;


    /**
     * @param serverId
     */
    public ServerDestinationStrategy ( @NonNull UUID serverId ) {
        this.serverId = serverId;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.DestinationStrategy#getDestination(eu.agno3.runtime.messaging.listener.BaseListener,
     *      javax.jms.Session)
     */
    @Override
    public Destination getDestination ( BaseListener listener, Session s ) throws JMSException {
        Destination destination = null;
        if ( listener instanceof CustomDestination ) {
            CustomDestination customDest = (CustomDestination) listener;
            destination = customDest.createCustomDestination(s);
        }
        else {
            destination = getDefaultDestination();
        }
        return destination;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.DestinationStrategy#getDestinationId(eu.agno3.runtime.messaging.listener.BaseListener)
     */
    @Override
    public String getDestinationId ( BaseListener listener ) {
        if ( listener instanceof CustomDestination ) {
            CustomDestination customDest = (CustomDestination) listener;
            return customDest.createCustomDestinationId();
        }

        return this.getDefaultDestinationId();
    }


    /**
     * @return
     */
    private String getDefaultDestinationId () {
        ListeningEventScopeResolver resolver = new ListeningEventScopeResolver();
        ServerEventScope scope = new ServerEventScope(this.serverId);
        List<String> topics = new ArrayList<>(resolver.getListeningTopics(scope));
        Collections.sort(topics);
        String topicsStr = StringUtils.join(topics, ","); //$NON-NLS-1$
        return "topics://" + topicsStr; //$NON-NLS-1$ 
    }


    /**
     * @return
     */
    private Destination getDefaultDestination () {
        ListeningEventScopeResolver resolver = new ListeningEventScopeResolver();
        ServerEventScope scope = new ServerEventScope(this.serverId);
        List<String> topics = new ArrayList<>(resolver.getListeningTopics(scope));
        Collections.sort(topics);
        return new ActiveMQTopic(StringUtils.join(topics, ",")); //$NON-NLS-1$
    }
}
