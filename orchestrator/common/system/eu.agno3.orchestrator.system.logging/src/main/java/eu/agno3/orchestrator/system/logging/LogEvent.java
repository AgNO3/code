/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.02.2016 by mbechler
 */
package eu.agno3.orchestrator.system.logging;


import java.util.Collection;
import java.util.Collections;

import javax.jms.DeliveryMode;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.scopes.ServersEventScope;
import eu.agno3.runtime.messaging.msg.EventMessage;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.msg.impl.ByteMessage;


/**
 * @author mbechlers
 *
 */
@Component ( service = EventMessage.class )
public class LogEvent extends ByteMessage<@NonNull AgentMessageSource> implements EventMessage<@NonNull AgentMessageSource> {

    private static final String COUNT = "eventCount"; //$NON-NLS-1$


    /**
     * 
     */
    public LogEvent () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public LogEvent ( @NonNull AgentMessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public LogEvent ( @NonNull AgentMessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public LogEvent ( @NonNull AgentMessageSource origin ) {
        super(origin);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.EventMessage#getScopes()
     */
    @Override
    public Collection<EventScope> getScopes () {
        return Collections.singleton(new ServersEventScope());
    }


    /**
     * @return the number of log events included in this message
     */
    public int getCount () {
        return (Integer) this.getProperties().getOrDefault(COUNT, 1);
    }


    /**
     * 
     * @param count
     */
    public void setCount ( int count ) {
        this.getProperties().put(COUNT, count);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.impl.AbstractMessage#getDeliveryTTL()
     */
    @Override
    public long getDeliveryTTL () {
        return 2000;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.impl.AbstractMessage#getDeliveryMode()
     */
    @Override
    public int getDeliveryMode () {
        return DeliveryMode.NON_PERSISTENT;
    }
}
